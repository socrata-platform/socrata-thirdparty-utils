package com.socrata.thirdparty.curator

import com.rojoma.json.ast.{JNull, JValue}
import com.rojoma.json.io.JValueEventIterator
import com.socrata.http.common.AuxiliaryData
import com.socrata.http.client.{Response, SimpleHttpRequest, RequestBuilder, HttpClient}
import org.apache.curator.x.discovery.ServiceDiscovery
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success, Try}

/**
 * Container for HttpCuratorService-related case classes and objects
 */
object HttpCuratorService {
  case class HttpCuratorServiceException(message: String) extends Exception(message)
  case class ServiceDiscoveryException(message: String) extends Exception(message)
}

/**
 * Provides a HTTP and JSON query interface for a curated service
 * @param httpClient HttpClient object used to make requests
 * @param discovery Service discovery object for querying Zookeeper
 * @param serviceName Service name as registered in Zookeeper
 * @param connectTimeout Timeout setting for connecting to the service
 */
abstract class HttpCuratorService(httpClient: HttpClient, discovery: ServiceDiscovery[AuxiliaryData], serviceName: String, connectTimeout: FiniteDuration) extends CuratorServiceBase(discovery, serviceName) {
  import HttpCuratorService._

  /**
   * Queries the service, validates that the expected response code was returned
   * and returns the JSON response
   * @param requestBuilder       HTTP request
   * @param payload              Request body as JSON
   * @param expectedResponseCode HTTP response code that indicates the expected response
   * @return                     Response body as JSON
   */
  protected def query(requestBuilder: RequestBuilder => RequestBuilder, payload: JValue, expectedResponseCode: Int): Try[JValue] =
    query { rb => requestBuilder(rb).json(JValueEventIterator(payload)) } { response =>
      val body = if (response.isJson) response.asJValue() else JNull

      response.resultCode match {
        case `expectedResponseCode` => Success(body)
        case _ => Failure(new HttpCuratorServiceException(s"Soda fountain response: ${response.resultCode} Payload: $body"))
      }
    }

  private def query[T](buildRequest: RequestBuilder => SimpleHttpRequest)(f: Response => T) = {
    requestBuilder match {
      case Some(rb) =>
        val request = buildRequest(rb)
        // TODO - What's the right way to log something when it's in a lib?
        for (response <- httpClient.execute(request)) yield {
          f(response)
        }
      case None => throw new ServiceDiscoveryException("Could not connect to Soda Fountain")
    }
  }

  private[this] val connectTimeoutMS = connectTimeout.toMillis.toInt
  if(connectTimeoutMS != connectTimeout.toMillis) {
    throw new IllegalArgumentException("Connect timeout out of range (milliseconds must fit in an int)")
  }

  private def requestBuilder: Option[RequestBuilder] = Option(provider.getInstance()).map { serv =>
    RequestBuilder(new java.net.URI(serv.buildUriSpec())).
      livenessCheckInfo(Option(serv.getPayload).flatMap(_.livenessCheckInfo)).
      connectTimeoutMS(connectTimeoutMS)
  }
}
