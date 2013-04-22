package com.socrata.thirdparty.asynchttpclient

import com.ning.http.client._
import org.slf4j.{LoggerFactory, Logger}

trait BodyHandler[T] {
  def bodyPart(bodyPart: HttpResponseBodyPart): Either[T, BodyHandler[T]]

  /** Called when there is no more input. */
  def done(): T

  /** Called when the body has been created but completion was signalled
    * due to an exception */
  def abort(cause: Throwable) {}
}

final class FAsyncHandler[T] private (log: Logger, newBodyHandler: (HttpResponseStatus, FluentCaseInsensitiveStringsMap) => Either[T, BodyHandler[T]]) extends AsyncHandler[T] {
  import AsyncHandler.STATE

  private var status: HttpResponseStatus = null
  private var headersReceived = false
  private var abortingDueTo: Throwable = null
  private var finished = false
  private var result: T = _
  private var bodyHandler: BodyHandler[T] = null

  private def setResult(r: T) {
    result = r
    finished = true
  }

  def onThrowable(t: Throwable) {
    abortingDueTo = t
  }

  def onStatusReceived(responseStatus: HttpResponseStatus): STATE = {
    assert(status == null, "onStatusReceived called twice?")
    status = responseStatus
    log.trace("Received response code {}: {}", responseStatus.getStatusCode, responseStatus.getStatusText)
    STATE.CONTINUE
  }

  def onHeadersReceived(headers: HttpResponseHeaders): STATE = {
    assert(status != null, "status not received before headers?")
    if(!headers.isTraillingHeadersReceived) {
      assert(!headersReceived, "onHeadersReceived called twice?")
      headersReceived = true
      log.trace("Received headers")
      newBodyHandler(status, headers.getHeaders) match {
        case Right(handler) =>
          log.trace("Proceeding to read the response body (if there is one)")
          bodyHandler = handler
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
  }

  def onBodyPartReceived(bodyPart: HttpResponseBodyPart): STATE = {
    assert(status != null, "status not received before body?")
    if(!headersReceived) {
      log.trace("Received no headers; using an empty map")
      headersReceived = true
      newBodyHandler(status, new FluentCaseInsensitiveStringsMap) match {
        case Right(handler) =>
          bodyHandler = handler
        case Left(r) =>
          log.trace("Body handler constructor told us to abort after receiving no headers")
          setResult(r)
          STATE.ABORT
      }
    }
    bodyHandler.bodyPart(bodyPart) match {
      case Right(bh) =>
        bodyHandler = bh
        STATE.CONTINUE
      case Left(r) =>
        setResult(r)
        STATE.ABORT
    }
  }

  def onCompleted(): T = {
    if(abortingDueTo != null) {
      if(bodyHandler != null) bodyHandler.abort(abortingDueTo)
      return null.asInstanceOf[T]
    }

    assert(status != null, "Completed without ever getting a status message?")
    if(!finished) {
      log.trace("End of body received")
      if(bodyHandler == null) {
        log.trace("Never received headers; creating a new body handler now.")
        newBodyHandler(status, new FluentCaseInsensitiveStringsMap) match {
          case Right(handler) =>
            bodyHandler = handler
            log.trace("Telling new body handler that the end has arrived")
            setResult(bodyHandler.done())
          case Left(r) =>
            log.trace("Body handler didn't like getting no headers")
            setResult(r)
        }
      } else {
        setResult(bodyHandler.done())
      }
    }
    log.trace("Finished processing response; completing the future")
    result
  }
}

object FAsyncHandler {
  private val asyncHandlerLog = LoggerFactory.getLogger(classOf[FAsyncHandler[_]])

  def apply[T](log: Logger = asyncHandlerLog)(f: (HttpResponseStatus, FluentCaseInsensitiveStringsMap) => Either[T, BodyHandler[T]]) =
    new FAsyncHandler(log, f)
}
