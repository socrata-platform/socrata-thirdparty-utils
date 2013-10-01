package com.socrata.thirdparty.typesafeconfig

import com.typesafe.config.{Config, ConfigException}
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.collection.JavaConverters._

/** A helper for making typed configurations. */
abstract class ConfigClass(config: Config, root: String) {
  protected def path(key: String) = root + "." + key

  // we'll add more of these as we need them
  def getInt(key: String): Int = config.getInt(path(key))
  def getString(key: String): String = config.getString(path(key))
  def getStringList(key: String): Seq[String] = config.getStringList(path(key)).asScala
  def getDuration(key: String): FiniteDuration = config.getMilliseconds(path(key)).longValue.millis
  def getConfig[T](key: String, decoder: (Config, String) => T): T = decoder(config, path(key))
  def getRawConfig(key: String): Config = config.getConfig(path(key))
  def optionally[T](e: => T): Option[T] = try {
    Some(e)
  } catch {
    case _: ConfigException.Missing => None
  }
}
