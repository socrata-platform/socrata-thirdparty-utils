package com.socrata.thirdparty.geojson

import com.rojoma.json.ast._
import com.rojoma.json.io.JsonReader
import com.vividsolutions.jts.geom._
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers


class JtsCodecsTest extends FunSpec with ShouldMatchers with GeoTest {
  import JtsCodecs._

  val pointCoords = JArray(Seq(JNumber(6.0), JNumber(1.2)))
  val point2Coords = JArray(Seq(JNumber(3.4), JNumber(-2.7)))
  val lineCoords = JArray(Seq(pointCoords, point2Coords))

  def decodeString(str: String) = geoCodec.decode(JsonReader.fromString(str))

  describe("GeometryCodec") {
    it("should convert geometry JSON of type Point correctly") {
      val body = """{
                    |  "type": "Point",
                    |  "coordinates": [6.0, 1.2]
                    |}""".stripMargin
      val pt = decodeString(body).asInstanceOf[Option[Point]]
      (pt.get.getX, pt.get.getY) should equal (6.0, 1.2)
    }

    it("should convert geometry JSON of type Polygon") {
      val body = """{
                    |  "type": "Polygon",
                    |  "coordinates": [[[0.0, 0.0], [0.0, 1.0], [1.0, 1.0], [0.0, 0.0]]]
                    |}""".stripMargin
      decodeString(body) should equal (Some(polygon((0.0, 0.0), (0.0, 1.0), (1.0, 1.0), (0.0, 0.0))))
    }

    it("should convert geometry JSON of MultiLineString") {
      val body = """{
                    |  "type": "MultiLineString",
                    |  "coordinates": [[[0.0, 0.0], [0.0, 1.0]], [[1.0, 0.0], [1.0, 1.0]]]
                    |}""".stripMargin
      val mls = factory.createMultiLineString(Array(
                  linestring((0.0, 0.0), (0.0, 1.0)), linestring((1.0, 0.0), (1.0, 1.0))
                ))
      decodeString(body) should equal (Some(mls))
    }

    it("should convert geometry JSON of type MultiPolygon") {
      val body = """{
          |  "type": "MultiPolygon",
          |  "coordinates": [[[[0.0, 0.0], [0.0, 1.0], [1.0, 1.0], [0.0, 0.0]]], [[[1.0, 1.0], [1.0, 2.0], [2.0, 2.0], [1.0, 1.0]]]]
          |}""".stripMargin
      val mp = factory.createMultiPolygon(Array(
                 polygon((0.0, 0.0), (0.0, 1.0), (1.0, 1.0), (0.0, 0.0)),
                 polygon((1.0, 1.0), (1.0, 2.0), (2.0, 2.0), (1.0, 1.0))
               ))
      decodeString(body) should equal (Some(mp))
    }

    it("should not convert non-GeoJSON or unsupported types") {
      val body = JObject(Map("type" -> JString("foo"), "coordinates" -> pointCoords))
      geoCodec.decode(body) should equal (None)

      val body2 = JArray(Seq(JString("totally not"), JNumber(5.6)))
      geoCodec.decode(body2) should equal (None)
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