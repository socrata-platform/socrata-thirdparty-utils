package com.socrata.thirdparty.typesafeconfig

import scala.collection.JavaConverters._

import com.typesafe.config.ConfigFactory
import org.scalatest.FunSuite
import org.scalatest.MustMatchers

import java.util.Properties

class PropertizerTest extends FunSuite with MustMatchers {
  val log4jConfig = ConfigFactory.parseString(
    """log4j {
      |  rootLogger = [ INFO, console ]
      |  appender.console.class = org.apache.log4j.ConsoleAppender
      |  appender.console.props {
      |    layout.class = org.apache.log4j.PatternLayout
      |    layout.props {
      |      ConversionPattern = "[%t] %d %c %m%n"
      |    }
      |  }
      |}""".stripMargin)

  val log4jProps = new Properties
  log4jProps.put("log4j.rootLogger","INFO,console")
  log4jProps.put("log4j.appender.console","org.apache.log4j.ConsoleAppender")
  log4jProps.put("log4j.appender.console.layout","org.apache.log4j.PatternLayout")
  log4jProps.put("log4j.appender.console.layout.ConversionPattern","[%t] %d %c %m%n")

  test("Without being given a root") {
    val props = Propertizer("", log4jConfig)
    props must equal (log4jProps)
  }

  test("With a new root") {
    val props = Propertizer("foo", log4jConfig)
    val targetProps = new Properties
    for((k,v) <- log4jProps.asScala) {
      targetProps.put("foo." + k, v)
    }
    props must equal (targetProps)
  }
}
