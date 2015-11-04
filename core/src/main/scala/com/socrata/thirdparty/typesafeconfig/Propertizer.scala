package com.socrata.thirdparty.typesafeconfig

import java.util

import com.typesafe.config.{Config, ConfigValue, ConfigValueType}

import scala.collection.JavaConverters._

/** Converts a Config object into a Properties object.  This is
  * designed to be used with Log4j's `PropertyConfigurator`.
  * Since log4j frequently requires a property that both has
  * a value and "subkeys", a simple convention is used here:
  * if an object contains exactly two keys, one of which is
  * "name" (or "class", since these things frequently represent
  * an object creation) and "props", the "name" (or "class") and
  * "props" subkeys are removed.
  *
  * For example:
  * {{{
  * log4j {
  *   rootLogger = [ INFO, console ]
  *   appender.console.class = org.apache.log4j.ConsoleAppender
  *   appender.console.props {
  *     layout.class = org.apache.log4j.PatternLayout
  *     layout.props {
  *       ConversionPattern = "[%t] %d %c %m%n"
  *     }
  *   }
  * }
  * }}}
  * would become:
  * {{{
  * log4j.rootLogger=INFO,console
  * log4j.appender.console=org.apache.log4j.ConsoleAppender
  * log4j.appender.console.layout=org.apache.log4j.PatternLayout
  * log4j.appender.console.layout.ConversionPattern=[%t] %d %c %m%n
  * }}}
  */
object Propertizer extends ((String, Config) => util.Properties) {
  private val NameKey = "name"
  private val ClassKey = "class"
  private val PropsKey = "props"

  // scalastyle:ignore cyclomatic.complexity
  private def subtree(props: util.Properties, root: String, obj: Config): Unit = {
    def nestedRoot(s: String) = if(root == "") s else root + "." + s

    val isPair = {
      val keys = obj.root.entrySet.asScala.collect {
        case kv: util.Map.Entry[String,ConfigValue] if kv.getValue.valueType != ConfigValueType.NULL => kv.getKey
      }
      keys.size == 2 && (keys.contains(NameKey) || keys.contains(ClassKey)) && keys.contains(PropsKey)
    }

    val remainder =
      if (isPair) {
        val rootProp = if (obj.root.containsKey(NameKey)) NameKey else ClassKey
        props.put(root, obj.getString(rootProp))
        obj.getConfig(PropsKey)
      } else {
        obj
      }

    remainder.root.asScala.mapValues(_.valueType) foreach {
      case (_, ConfigValueType.NULL) =>
        // pass
      case (k, ConfigValueType.OBJECT) =>
        subtree(props, nestedRoot(k), remainder.getConfig(k))
      case (k, ConfigValueType.LIST) =>
        props.put(nestedRoot(k), remainder.getStringList(k).asScala.mkString(","))
      case (k, _) =>
        props.put(nestedRoot(k), remainder.getString(k))
    }
  }

  def apply(root: String, config: Config): util.Properties = {
    val props = new java.util.Properties
    subtree(props, root, config)
    props
  }
}
