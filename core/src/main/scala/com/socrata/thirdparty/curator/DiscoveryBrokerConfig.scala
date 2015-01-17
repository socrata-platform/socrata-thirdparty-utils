package com.socrata.thirdparty.curator

import com.typesafe.config.Config

class DiscoveryBrokerConfig(config: Config, curatorRoot: String, discoveryRoot: String) {
  def this(config: Config, root: String) {
    this(config, root, root)
  }

  /** Zookeeper configuration. */
  lazy val curator = new CuratorConfig(config, curatorRoot)

  /** Zookeeper configuration. */
  lazy val discovery = new DiscoveryConfig(config, discoveryRoot)
}
