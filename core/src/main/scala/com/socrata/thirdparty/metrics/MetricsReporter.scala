package com.socrata.thirdparty.metrics

import com.codahale.metrics.graphite.{Graphite, GraphiteReporter}
import com.codahale.metrics.{MetricRegistry, Slf4jReporter, JmxReporter}
import com.rojoma.simplearm.{Managed, SimpleArm}
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory

/**
 * A centralized place for starting a set of CodaHale Metrics reporters.
 *
 * @param options a MetricsOptions case class; which reporters to enable
 * @param registry allows to specify a different registry if needed
 */
class MetricsReporter(options: MetricsOptions, registry: MetricRegistry = Metrics.metricsRegistry) {
  val logger = LoggerFactory.getLogger(classOf[MetricsReporter])

  lazy val slf4jReporter = Slf4jReporter.forRegistry(registry)
                                        .outputTo(logger)
                                        .convertRatesTo(TimeUnit.SECONDS)
                                        .convertDurationsTo(TimeUnit.MILLISECONDS)
                                        .build()

  lazy val jmxReporter = JmxReporter.forRegistry(registry).build()

  lazy val graphite = new Graphite(new InetSocketAddress(options.graphiteHost, options.graphitePort))

  lazy val graphiteReporter = GraphiteReporter.forRegistry(registry)
                                              .convertRatesTo(TimeUnit.SECONDS)
                                              .convertDurationsTo(TimeUnit.MILLISECONDS)
                                              .build(graphite)

  if (options.logMetrics) {
    logger.info("Starting metrics logging...")
    slf4jReporter.start(options.reportingIntervalSecs, TimeUnit.SECONDS)
  }

  if (options.enableJmx) {
    logger.info("Starting metrics JMX reporter...")
    jmxReporter.start()
  }

  if (options.enableGraphite) {
    logger.info("Starting metrics Graphite reporter to {}:{}", options.graphiteHost, options.graphitePort)
    graphiteReporter.start(options.reportingIntervalSecs, TimeUnit.SECONDS)
  }

  def stop() {
    logger.info("Shutting down reporters...")
    if (options.logMetrics) slf4jReporter.stop()
    if (options.enableJmx) jmxReporter.stop()
    if (options.enableGraphite) graphiteReporter.stop()
  }
}

/**
 * Offers a managed API for reporter lifecycle management
 */
object MetricsReporter {
  def managed(options: MetricsOptions,
              registry: MetricRegistry = Metrics.metricsRegistry): Managed[MetricsReporter] =
    new SimpleArm[MetricsReporter] {
      def flatMap[A](f: MetricsReporter => A): A = {
        val reporter = new MetricsReporter(options, registry)
        try f(reporter) finally reporter.stop()
      }
    }
}