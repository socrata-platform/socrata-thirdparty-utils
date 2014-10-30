package com.socrata.thirdparty.c3p0

import java.sql.Connection

import com.mchange.v2.c3p0.AbstractConnectionCustomizer
import com.mchange.v2.log.{MLevel, MLog, MLogger}
import com.rojoma.simplearm.util.using

/**
 * Implementation of c3p0's AbstractConnectionCustomizer to let you execute arbitrary sql statements
 * on any of the defined events.  The statements to execute are configured with key/value pairs in
 * the c3p0 extensions configuration, named after each of the methods.  eg. "onAcquire".
 */
class ConnectionCustomizer extends AbstractConnectionCustomizer {
  private val logger: MLogger = MLog.getLogger(classOf[ConnectionCustomizer])

  private def getConfig(parentDataSourceIdentityToken: String, configName: String): Option[String] = {
    return Option(extensionsForToken(parentDataSourceIdentityToken).get(configName).asInstanceOf[String])
  }

  private def onEvent(c: Connection, parentDataSourceIdentityToken: String, event: String) {
    val sql = getConfig(parentDataSourceIdentityToken, event)
    sql.foreach { s =>
      using(c.createStatement()) { stmt =>
        val result = stmt.executeUpdate(s)
        if (logger.isLoggable(MLevel.FINEST)) logger.log(MLevel.FINEST, s"Executed $event statement $s on connection $c returned $result")
      }
    }
  }

  override def onAcquire(c: Connection, parentDataSourceIdentityToken: String) {
    onEvent(c, parentDataSourceIdentityToken, "onAcquire")
  }

  override def onDestroy(c: Connection, parentDataSourceIdentityToken: String) {
    onEvent(c, parentDataSourceIdentityToken, "onDestroy")
  }

  override def onCheckOut(c: Connection, parentDataSourceIdentityToken: String) {
    onEvent(c, parentDataSourceIdentityToken, "onCheckOut")
  }

  override def onCheckIn(c: Connection, parentDataSourceIdentityToken: String) {
    onEvent(c, parentDataSourceIdentityToken, "onCheckIn")
  }

}
