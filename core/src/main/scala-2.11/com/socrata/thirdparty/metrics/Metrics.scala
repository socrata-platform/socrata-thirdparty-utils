package com.socrata.thirdparty.metrics

import com.codahale.metrics.MetricRegistry
import nl.grons.metrics4.scala.InstrumentedBuilder

/*
 * Provides a [[MetricRegistry]].
 */
object Metrics {
  lazy val metricsRegistry = new MetricRegistry

  val defaultOptions = MetricsOptions()
}

/**
 * Mix this into your classes etc. to get easy access to metrics.  Example:
 * {{{
 *   class RowDAO(db: Database) extends Metrics {
 *     val dbTimer = metrics.timer("db-access-latency")
 *     dbTimer.time {
         db.runQuery(....)
 *     }
 *   }
 * }}}
 * See https://github.com/erikvanoosten/metrics-scala/blob/master/docs/Manual.md for
 * more usage info, such as how to override metrics name, etc.
 */
trait Metrics extends InstrumentedBuilder {
  val metricRegistry = Metrics.metricsRegistry
}
