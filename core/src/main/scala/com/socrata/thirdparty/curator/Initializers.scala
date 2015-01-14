package com.socrata.thirdparty.curator

import com.rojoma.simplearm.{Managed, SimpleArm}
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry
import org.apache.curator.x.discovery._

/**
 * Top level function for managed resource use of CuratorFramework.  Starts the framework and closes it
 * after the closure is done.
 * {{{
 *   for { curator <- CuratorFromConfig(curatorConfig) } { .... }
 * }}}
 */
object CuratorFromConfig {
  def apply(config: CuratorConfig): Managed[CuratorFramework] = new SimpleArm[CuratorFramework] {
    def flatMap[A](f: CuratorFramework => A): A = {
       val curator = unmanaged(config)
       curator.start()
       try f(curator) finally curator.close()
    }
  }

  /**
   * Function for creating a curator framework without starting it or closing it.
   * User will have to be responsible for calling `start()` and `close()`.
   *
   * @param config a CuratorConfig from which to create the [[CuratorFramework]]
   */
  def unmanaged(config: CuratorConfig): CuratorFramework = {
    CuratorFrameworkFactory.builder.
      connectString(config.ensemble).
      sessionTimeoutMs(config.sessionTimeout.toMillis.toInt).
      connectionTimeoutMs(config.connectTimeout.toMillis.toInt).
      retryPolicy(new retry.BoundedExponentialBackoffRetry(config.baseRetryWait.toMillis.toInt,
                                                           config.maxRetryWait.toMillis.toInt,
                                                           config.maxRetries)).
      namespace(config.namespace).
      build()
  }
}

/**
 * Top level function for managed resource use of ServiceDiscovery.  Starts the framework and closes it
 * after the closure is done.
 * {{{
 *   for { curator <- DiscoveryFromConfig(classOf[AuxiliaryData], curator, discoveryConfig) } { .... }
 * }}}
 */
object DiscoveryFromConfig {
  def apply[T](payloadClass: Class[T], curator: CuratorFramework, config: DiscoveryConfig):
      Managed[ServiceDiscovery[T]] = new SimpleArm[ServiceDiscovery[T]] {
    def flatMap[A](f: ServiceDiscovery[T] => A): A = {
      val discovery = unmanaged(payloadClass, curator, config)
      discovery.start()
      try f(discovery) finally discovery.close()
    }
  }

  /**
   * Function for creating a ServiceDiscovery without starting it or closing it.
   * User will have to be responsible for calling `start()` and `close()`.
   */
  def unmanaged[T](payloadClass: Class[T], curator: CuratorFramework, config: DiscoveryConfig): ServiceDiscovery[T] = {
    ServiceDiscoveryBuilder.builder(payloadClass).
      client(curator).
      basePath(config.serviceBasePath).
      build()
  }
}

object ServiceProviderFromConfig {
  def apply[T](discovery: ServiceDiscovery[T], serviceName: String): Managed[ServiceProvider[T]] =
    new SimpleArm[ServiceProvider[T]] {
      def flatMap[A](f: ServiceProvider[T] => A): A = {
        val sp = discovery.serviceProviderBuilder().
          providerStrategy(new strategies.RoundRobinStrategy).
          serviceName(serviceName).
          build()
        try {
          sp.start()
          f(sp)
        } finally {
          sp.close()
        }
      }
    }
}
