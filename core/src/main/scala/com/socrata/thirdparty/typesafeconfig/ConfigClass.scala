package com.socrata.thirdparty.typesafeconfig

import com.typesafe.config.{Config, ConfigException, ConfigUtil}
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

/** A helper for making typed configurations. */
abstract class ConfigClass(config: Config, root: String) {
  protected def path(key: String*) = root + "." + ConfigUtil.joinPath(key : _*)

  // we'll add more of these as we need them
  def getInt(key: String): Int = config.getInt(path(key))
  def getLong(key: String): Long = config.getLong(path(key))
  def getString(key: String): String = config.getString(path(key))
  def getStringList(key: String): Seq[String] = config.getStringList(path(key)).asScala.toSeq
  def getDuration(key: String): FiniteDuration = config.getMilliseconds(path(key)).longValue.millis
  def getBytes(key: String): Long = config.getBytes(path(key)).longValue
  def getBoolean(key: String): Boolean = config.getBoolean(path(key))
  def getConfig[T](key: String, decoder: (Config, String) => T): T = decoder(config, path(key))
  def getObjectOf[T](key: String, decoder: (Config, String) => T): Map[String, T] =
    config.getObject(path(key)).keySet.asScala.toSeq.map { k =>
      k -> decoder(config, path(key, k))
    }.toMap
  def getRawConfig(key: String): Config = config.getConfig(path(key))
  def optionally[T](e: => T): Option[T] = try {
    Some(e)
  } catch {
    case _: ConfigException.Missing => None
  }
}
