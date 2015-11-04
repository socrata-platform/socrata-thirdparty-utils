package com.socrata.thirdparty.metrics

import com.codahale.metrics.jetty9.InstrumentedHandler
import org.eclipse.jetty.server.Handler

/**
 * Integrates Dropwizard Metrics into Socrata HTTP by providing an easy way
 * to instrument all Jetty requests.
 */
object SocrataHttpSupport {
  /**
   * Function to wrap an existing Jetty handler with an InstrumentedHandler.
   * See https://dropwizard.github.io/metrics/3.1.0/manual/jetty/.
   * Can be used with socrata-http; see [[AbstractSocrataServerJetty.Options]].
   * Set extraHandlers = List(SocrataHttpSupport.getHandler(options)).
   *
   * @param options a MetricsOptions for setting the metrics prefix
   * @param underlying base Handler.  This is usually not specified directly; rather
   *        getHandler is usually passed as a partially applied function.
   */
  def getHandler(options: MetricsOptions)(underlying: Handler): InstrumentedHandler = {
    val handler = new InstrumentedHandler(Metrics.metricsRegistry, options.prefix)
    handler.setHandler(underlying)
    handler
  }
}
