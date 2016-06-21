package com.socrata.thirdparty.typesafeconfig

import com.typesafe.config.Config

class CassandraConfig(config: Config, root: String) extends ConfigClass(config, root) {
  val cluster = getString("cluster")
  val keyspace = getString("keyspace")
  val connectionPool = getConfig("connection-pool", new CassandraConnectionPoolConfig(_, _))
}

class CassandraConnectionPoolConfig(config: Config, root: String) extends ConfigClass(config, root) {
  val name = getString("name")
  val port = getInt("port")
  val maxConnectionsPerHost = getInt("max-connections-per-host")
  val seeds = getStringList("seeds").mkString(",")
  val connectTimeout = getDuration("connect-timeout")
  val datacenter = optionally[String]("datacenter")
}
