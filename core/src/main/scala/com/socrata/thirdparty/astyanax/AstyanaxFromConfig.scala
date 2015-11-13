package com.socrata.thirdparty.astyanax

import com.netflix.astyanax._
import com.netflix.astyanax.model.ConsistencyLevel
import com.rojoma.simplearm.v2._
import com.socrata.thirdparty.typesafeconfig.CassandraConfig

object AstyanaxFromConfig {
  private implicit def astyanaxResource[T] = new Resource[AstyanaxContext[T]] {
    def close(k: AstyanaxContext[T]) = k.shutdown()
  }

  def apply(config: CassandraConfig) = managed {
    unmanaged(config)
  }.and(_.start())

  def unmanaged(config: CassandraConfig) =
    new AstyanaxContext.Builder().
      forCluster(config.cluster).
      forKeyspace(config.keyspace).
      withAstyanaxConfiguration(new impl.AstyanaxConfigurationImpl().
      setDiscoveryType(connectionpool.NodeDiscoveryType.RING_DESCRIBE).
      setDefaultReadConsistencyLevel(ConsistencyLevel.CL_QUORUM).
      setDefaultWriteConsistencyLevel(ConsistencyLevel.CL_QUORUM)
      ).
      withConnectionPoolConfiguration(new connectionpool.impl.ConnectionPoolConfigurationImpl(config.connectionPool.name).
      setPort(config.connectionPool.port).
      setMaxConnsPerHost(config.connectionPool.maxConnectionsPerHost).
      setSeeds(config.connectionPool.seeds).
      setConnectTimeout(config.connectionPool.connectTimeout.toMillis.toInt)
      ).
      withConnectionPoolMonitor(new connectionpool.impl.CountingConnectionPoolMonitor()).
      buildKeyspace(thrift.ThriftFamilyFactory.getInstance())
}
