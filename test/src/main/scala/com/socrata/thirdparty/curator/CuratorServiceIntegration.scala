package com.socrata.thirdparty.curator

import com.socrata.http.client.{NoopLivenessChecker, HttpClientHttpClient}
import com.socrata.http.common.AuxiliaryData
import com.typesafe.config.ConfigFactory
import java.util.concurrent.Executors
import org.apache.curator.test.TestingServer
import scala.util.Try

/**
 *  A trait that provides an in-process ZK and curator-based service discovery
 *  to make integration/functional testing easier.  For example, you can "register"
 *  a mock HTTP service so that the service under test can talk to the other service
 *  using the standard Curator-based Socrata HttpClient library.
 *  {{{
 *    class SomeServiceTest extends FunSpec with CuratorServiceIntegration {
 *      override def configPrefix = "com.socrata.my.service"
 *      val broker = new CuratorBroker(discovery, "localhost", "myFooService", None))
 *      val cookie = broker.register(servicePort)
 *
 *      override def beforeAll() {
 *        startServices()
 *        cookie
 *      }
 *
 *      override def afterAll() {
 *        broker.deregister(cookie)
 *        stopServices()
 *      }
 *    }
 *  }}}
 *
 * It starts the in-process ZK at a random port so it won't conflict with any ZKs already running on your
 * dev machine, which probably runs at port 2181.
 *
 * NOTE: You will need to pull in the curator-test jar
 */
trait CuratorServiceIntegration {
  import collection.JavaConverters._

  // You will want to override def configPrefix
  def configPrefix = "com.socrata"
  def curatorConfigPrefix = configPrefix + ".curator"
  def discoveryConfigPrefix = configPrefix + ".service-advertisement"

  lazy val zk = new TestingServer
  lazy val cfgOverride = Map(curatorConfigPrefix + ".ensemble" -> Seq(s"localhost:${zk.getPort}").asJava,
                             discoveryConfigPrefix + ".address" -> "localhost").asJava
  lazy val config = ConfigFactory.parseMap(cfgOverride).withFallback(getFallback)
  lazy val curatorConfig = new CuratorConfig(config, curatorConfigPrefix)
  lazy val discoveryConfig = new DiscoveryConfig(config, discoveryConfigPrefix)

  lazy val curator = CuratorFromConfig.unmanaged(curatorConfig)
  lazy val discovery = DiscoveryFromConfig.unmanaged(classOf[AuxiliaryData], curator, discoveryConfig)

  // HttpClientHttpClient constructor has changed, as such  .Options object needs to be passed.
  private lazy val httpOptions: HttpClientHttpClient.Options = HttpClientHttpClient.defaultOptions
  httpOptions.withUserAgent("test")
  httpOptions.withLivenessChecker(NoopLivenessChecker)
  lazy val httpClient = new HttpClientHttpClient( Executors.newCachedThreadPool(), httpOptions)

  protected def getFallback = ConfigFactory.load()

  def startServices() {
    curator.start
    discovery.start
  }

  def stopServices() {
    httpClient.close
    Try(discovery.close)
    curator.close
    zk.close          // shut down ZK and delete temp dirs
  }
}
