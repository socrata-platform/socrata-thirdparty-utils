package com.socrata.thirdparty.typesafeconfig

import scala.collection.JavaConverters._

import com.typesafe.config.ConfigFactory
import org.scalatest.FunSuite
import org.scalatest.MustMatchers

import java.util.Properties

class C3P0PropertizerTest extends FunSuite with MustMatchers {
  val c3p0Config = ConfigFactory.parseString(
    """c3p0 = {
        maxPoolSize = 20
        testConnectionOnCheckin = true
        connectionCustomizerClassName=com.mchange.v2.c3p0.example.InitSqlConnectionCustomizer
        extensions {
          initSql="SET work_mem = '768MB'"
          monkey="Marmoset"
        }
      }""")

  val c3p0Props = new Properties
  c3p0Props.setProperty("maxPoolSize", "20")
  c3p0Props.setProperty("testConnectionOnCheckin", "true")
  c3p0Props.setProperty("connectionCustomizerClassName", "com.mchange.v2.c3p0.example.InitSqlConnectionCustomizer")

  val extensionsHash = new java.util.HashMap[String, String]()
  extensionsHash.put("initSql", "SET work_mem = '768MB'")
  extensionsHash.put("monkey", "Marmoset")

  c3p0Props.put("extensions", extensionsHash)

  test("Without being given a root") {
    val props = C3P0Propertizer("", c3p0Config.getConfig("c3p0"))
    props must equal (c3p0Props)
  }

  test("With a new root") {
    val props = C3P0Propertizer("foo", c3p0Config.getConfig("c3p0"))
    val targetProps = new Properties
    for {(k,v) <- c3p0Props.asInstanceOf[java.util.Map[String,Object]].asScala} {
      targetProps.put("foo." + k, v)
    }
    props must equal (targetProps)
  }
}
