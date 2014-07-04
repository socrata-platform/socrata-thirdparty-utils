package com.socrata.thirdparty.geojson

import com.rojoma.json.ast._
import com.rojoma.json.io.JsonReader
import com.vividsolutions.jts.geom._
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers


class JtsCodecsTest extends FunSpec with ShouldMatchers {
  import JtsCodecs._

  val pointCoords = JArray(Seq(JNumber(6.0), JNumber(1.2)))
  val point2Coords = JArray(Seq(JNumber(3.4), JNumber(-2.7)))
  val lineCoords = JArray(Seq(pointCoords, point2Coords))

  describe("GeometryCodec") {
    it("should convert geometry JSON of type Point correctly") {
      val body = """{
                    |  "type": "Point",
                    |  "coordinates": [6.0, 1.2]
                    |}""".stripMargin
      val pt = geoCodec.decode(JsonReader.fromString(body)).asInstanceOf[Option[Point]]
      (pt.get.getX, pt.get.getY) should equal (6.0, 1.2)
    }

    it("should not convert non-GeoJSON or unsupported types") {
      val body = JObject(Map("type" -> JString("foo"), "coordinates" -> pointCoords))
      geoCodec.decode(body) should equal (None)
    }
  }

  describe("coordinates") {
    it("should convert Points correctly") {
      val pt = PointCodec.decode(pointCoords)
      (pt.get.getX, pt.get.getY) should equal (6.0, 1.2)
    }

    it("should not convert non-Points") {
      PointCodec.decode(JArray(Seq(JNumber(-1)))) should equal (None)
      PointCodec.decode(lineCoords) should equal (None)
    }
  }
}