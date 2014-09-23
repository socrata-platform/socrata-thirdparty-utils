package com.socrata.thirdparty.metrics

import com.codahale.metrics.Slf4jReporter
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory

/**
 * A centralized place for starting a set of CodaHale Metrics reporters.
 */
class MetricsReporter(options: MetricsOptions) {
  val logger = LoggerFactory.getLogger(classOf[MetricsReporter])

  lazy val reporter = Slf4jReporter.forRegistry(Metrics.metricsRegistry)
                                   .outputTo(logger)
                                   .convertRatesTo(TimeUnit.SECONDS)
                                   .convertDurationsTo(TimeUnit.MILLISECONDS)
                                   .build()

  if (options.logMetrics) {
    logger.info("Starting metrics logging...")
    reporter.start(options.reportingIntervalSecs, TimeUnit.SECONDS)
  }
}