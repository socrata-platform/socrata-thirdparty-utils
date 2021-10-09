package com.socrata.thirdparty.typesafeconfig

import java.util.Properties

import scala.jdk.CollectionConverters._

import com.typesafe.config.{ConfigValueType, Config}


/**
 * Converts a Config object into a Properties object suitable for consumption
 * by c3p0.  In particular, converts nested structures such as an "extensions"
 * hash into an actual java.util.Map.  This violates the String => String
 * contract of java.util.Properties, but is what c3p0 needs so that is what we will
 * give it.
 */
object C3P0Propertizer extends ((String, Config) => Properties) {

  def apply(root: String, config: Config): Properties = {
    val props = new Properties()
    val es = config.root().entrySet().asScala

    es.foreach { entry =>
      val newKey = if (root == "") entry.getKey else root + "." + entry.getKey
      entry.getValue.valueType match {
        case ConfigValueType.OBJECT | ConfigValueType.LIST => props.put(newKey, entry.getValue.unwrapped())
        case ConfigValueType.NULL =>  // noop
        case _ => props.setProperty(newKey, config.getString(entry.getKey))
      }
    }

    props
  }
}
