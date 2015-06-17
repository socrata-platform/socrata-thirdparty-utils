package com.socrata.thirdparty.scalatra

import org.scalatra.ScalatraServlet
import org.slf4j.LoggerFactory

trait RequestLogging extends ScalatraServlet {
  val logger = LoggerFactory.getLogger(getClass)
  before() {
    logger.info(request.getMethod + " - " + request.getRequestURI + " ? " + request.getQueryString)
  }

  after() {
    logger.info("Status - " + response.getStatus)
  }
}
