package com.socrata.thirdparty.curator

import com.socrata.thirdparty.typesafeconfig.ConfigClass
import com.typesafe.config.Config

/**
 * Contains curator-specific configuration values
 * @param config Configuration object
 * @param root Root of the curator configuration subset
 */
class CuratorConfig(config: Config, root: String) extends ConfigClass(config, root) {
  val ensemble        = getStringList("ensemble").mkString(",")
  val sessionTimeout  = getDuration("session-timeout")
  val connectTimeout  = getDuration("connect-timeout")
  val maxRetries      = getInt("max-retries")
  val baseRetryWait   = getDuration("base-retry-wait")
  val maxRetryWait    = getDuration("max-retry-wait")
  val namespace       = getString("namespace")
  val serviceBasePath = getString("service-base-path")
}
