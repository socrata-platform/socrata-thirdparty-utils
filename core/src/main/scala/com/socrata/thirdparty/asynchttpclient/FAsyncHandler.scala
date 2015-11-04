package com.socrata.thirdparty.asynchttpclient

import com.ning.http.client.AsyncHandler.STATE
import com.ning.http.client._
import com.socrata.thirdparty.asynchttpclient.FAsyncHandler.FBodyHandler
import org.slf4j.{Logger, LoggerFactory}

trait BodyHandler[T] {
  def bodyPart(bodyPart: HttpResponseBodyPart): Either[T, BodyHandler[T]]

  /** Called when there is no more input. */
  def done(): T

  /** Called when the body has been created but completion was signalled
    * due to an exception */
  def abort(cause: Throwable): Unit = ()
}

final class FAsyncHandler[T] private (log: Logger, newBodyHandler: FBodyHandler[T]) extends AsyncHandler[T] {
  private var status: Option[HttpResponseStatus] = None
  private var headersReceived = false
  private var abortingDueTo: Option[Throwable] = None
  private var finished = false
  private var result: T = _
  private var bodyHandler: Option[BodyHandler[T]] = None

  private def setResult(r: T): Unit = {
    result = r
    finished = true
  }

  def onThrowable(t: Throwable): Unit = {
    abortingDueTo = Option(t)
  }

  def onStatusReceived(responseStatus: HttpResponseStatus): STATE = {
    assert(Option(status).isEmpty, "onStatusReceived called twice?")
    status = Option(responseStatus)
    log.trace("Received response code {}: {}", responseStatus.getStatusCode, responseStatus.getStatusText)
    STATE.CONTINUE
  }

  def onHeadersReceived(headers: HttpResponseHeaders): STATE = {
    status.map { s =>
      if (!headers.isTraillingHeadersReceived) {
        assert(!headersReceived, "onHeadersReceived called twice?")
        headersReceived = true
        log.trace("Received headers")
        newBodyHandler(s, headers.getHeaders) match {
          case Right(handler) =>
            log.trace("Proceeding to read the response body (if there is one)")
            bodyHandler = Option(handler)
            STATE.CONTINUE
          case Left(r) =>
            log.trace("Body handler constructor told us to abort instead of continuing on to the body")
            setResult(r)
            STATE.ABORT
        }
      } else {
        // trailing headers? meh whatever
        STATE.CONTINUE
      }
    }.getOrElse(throw new AssertionError("status not received before headers?"))
  }

  def onBodyPartReceived(bodyPart: HttpResponseBodyPart): STATE = {
    status.map { s =>
      if (!headersReceived) {
        log.trace("Received no headers; using an empty map")
        headersReceived = true
        newBodyHandler(s, new FluentCaseInsensitiveStringsMap) match {
          case Right(handler) =>
            bodyHandler = Option(handler)
          case Left(r) =>
            log.trace("Body handler constructor told us to abort after receiving no headers")
            setResult(r)
            STATE.ABORT
        }
      }
      bodyHandler.map { _.bodyPart(bodyPart) match {
        case Right(bh) =>
          bodyHandler = Option(bh)
          STATE.CONTINUE
        case Left(r) =>
          setResult(r)
          STATE.ABORT
      }}.getOrElse(throw new NoSuchElementException("bodyHandler"))
    }.getOrElse(throw new AssertionError("status not received before body?"))
  }

  def onCompleted(): T = {
    abortingDueTo.map { t =>
      bodyHandler.foreach(_.abort(t))
      None.asInstanceOf[T]
    }.getOrElse {
      status.map { s =>
        if (!finished) {
          log.trace("End of body received")
          setResult(
            bodyHandler.map(_.done()).getOrElse {
              log.trace("Never received headers; creating a new body handler now.")
              newBodyHandler(s, new FluentCaseInsensitiveStringsMap) match {
                case Right(handler) =>
                  bodyHandler = Option(handler)
                  log.trace("Telling new body handler that the end has arrived")
                  handler.done()
                case Left(r) =>
                  log.trace("Body handler didn't like getting no headers")
                  r
              }
            }
          )
        }
        log.trace("Finished processing response; completing the future")
        result
      }.getOrElse(throw new AssertionError("Completed without ever getting a status message?"))
    }
  }
}

object FAsyncHandler {
  private val asyncHandlerLog = LoggerFactory.getLogger(classOf[FAsyncHandler[_]])

  type FBodyHandler[T] = (HttpResponseStatus, FluentCaseInsensitiveStringsMap) => Either[T, BodyHandler[T]]

  def apply[T](log: Logger = asyncHandlerLog)(f: FBodyHandler[T]): FAsyncHandler[T] =
    new FAsyncHandler(log, f)
}
