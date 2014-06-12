package com.socrata.thirdparty.curator

import java.io.Closeable
import org.apache.curator.x.discovery.{strategies => providerStrategies, ServiceDiscovery}

/**
 * Manages connections and requests to a service that's registered via Curator/ZK
 * @param discovery Service discovery object for querying Zookeeper
 * @param serviceName Service name as registered in Zookeeper
 */
abstract class CuratorServiceBase[T](discovery: ServiceDiscovery[T], serviceName: String) extends Closeable {
  /**
   * Zookeeper service provider
   */
  val provider = discovery.serviceProviderBuilder().
    providerStrategy(new providerStrategies.RoundRobinStrategy).
    serviceName(serviceName).
    build()

  /**
   * Starts the Zookeeper service provider
   */
  def start() {
    provider.start()
  }

  /**
   * Closes the Zookeeper service provider
   */
  def close() {
    provider.close()
  }
}