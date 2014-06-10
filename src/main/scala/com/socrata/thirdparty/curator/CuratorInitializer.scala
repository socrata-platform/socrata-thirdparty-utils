package com.socrata.thirdparty.curator

import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder

/**
 * Common methods for ZK Curator initialization
 */
trait CuratorInitializer {
  // The [[CuratorConfig]] for populating the curator framework
  val curatorConfig: CuratorConfig

  // The payload class for Curator service storage
  val payloadClass: Class[_]

  lazy val curator = CuratorFrameworkFactory.builder.
    connectString(curatorConfig.ensemble).
    sessionTimeoutMs(curatorConfig.sessionTimeout.toMillis.toInt).
    connectionTimeoutMs(curatorConfig.connectTimeout.toMillis.toInt).
    retryPolicy(new retry.BoundedExponentialBackoffRetry(curatorConfig.baseRetryWait.toMillis.toInt,
                                                         curatorConfig.maxRetryWait.toMillis.toInt,
                                                         curatorConfig.maxRetries)).
    namespace(curatorConfig.namespace).
    build()

  lazy val discovery = ServiceDiscoveryBuilder.builder(payloadClass).
    client(curator).
    basePath(curatorConfig.serviceBasePath).
    build()
}