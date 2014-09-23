package com.socrata.thirdparty.metrics

import com.typesafe.config.{Config, ConfigFactory}
import scala.collection.JavaConverters._

/**
 * Options for configuring metrics and reporters.
 * Easiest way to get started is using a Typesafe config:
 * {{{
 *   com.socrata {
 *     metrics {
 *       prefix = "com.socrata.soda.server"
 *       log-metrics = true
 *       reporting-interval = 60 s
 *       graphite-enable = true
 *       graphite-host = "my.graphite.host"
 *       graphite-port = 2003
 *     }
 *   }
 * }}}
 *
 * Then:
 * {{{
 *   val metricsOpts = MetricsOptions(ConfigFactory.load().getConfig("com.socrata.metrics"))
 * }}}
 */
case class MetricsOptions(// Should be a prefix string unique to each service
                          prefix: String = MetricsOptions.defPrefix,
                          logMetrics: Boolean = MetricsOptions.defLogMetrics,
                          // How often metrics are logged / reported to statsd etc.
                          reportingIntervalSecs: Int = MetricsOptions.defReportingIntervalSecs)

object MetricsOptions {
  val defPrefix = "com.socrata.some.application"
  val defLogMetrics = true
  val defReportingIntervalSecs = 60

  val defaultConfig = ConfigFactory.parseMap(Map(
                        "prefix" -> defPrefix,
                        "log-metrics" -> defLogMetrics,
                        "reporting-interval" -> (defReportingIntervalSecs + " s")
                      ).asJava)

  def apply(config: Config): MetricsOptions = {
    val configWithDefaults = config.withFallback(defaultConfig)
    val reportingIntervalSecs = configWithDefaults.getMilliseconds("reporting-interval") / 1000
    new MetricsOptions(configWithDefaults.getString("prefix"),
                       configWithDefaults.getBoolean("log-metrics"),
                       reportingIntervalSecs.toInt)
  }
}
