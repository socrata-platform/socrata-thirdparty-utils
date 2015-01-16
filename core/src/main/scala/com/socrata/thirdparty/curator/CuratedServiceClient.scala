package com.socrata.thirdparty.curator

import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success, Try}

import com.rojoma.json.v3.ast.{JString, JValue}
import org.slf4j.LoggerFactory

import com.socrata.http.client.{RequestBuilder, Response, SimpleHttpRequest}
import com.socrata.http.server.HttpResponse
import com.socrata.thirdparty.curator.ServerProvider.{Complete, Retry}

/**
  * Manages connections and requests to the provided service.
  * @param provider Service discovery object.
  * @param config The configuration for this client.
  */
case class CuratedServiceClient(provider: ServerProvider,
                                config: CuratedClientConfig) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val connectTimeout = config.connectTimeout
  private val maxRetries = config.maxRetries

  /**
    * Sends a get request to the provided service.
    * @return HTTP response code and body
    */
  def execute[T](request: RequestBuilder => SimpleHttpRequest,
                 callback: Response => T): T = {
    val requestWithTimeout = { base: RequestBuilder =>
      val req = base.connectTimeoutMS match {
        case Some(timeout) => base
        case None => base.connectTimeoutMS(connectTimeout)
      }

      request(req)
    }

    provider.withRetries(maxRetries,
                         request,
                         ServerProvider.RetryOnAllExceptionsDuringInitialRequest) {
      case Some(response) =>
        Complete(callback(response))
      case None =>
        throw ServiceDiscoveryException(s"Failed to discover service: ${config.serviceName}")
    }
  }
}
