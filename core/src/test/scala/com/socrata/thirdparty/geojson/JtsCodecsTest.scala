package com.socrata.thirdparty.geojson

import com.rojoma.json.v3.ast._
import com.rojoma.json.v3.codec.{DecodeError, JsonDecode}
import com.rojoma.json.v3.io.JsonReader
import com.socrata.thirdparty.geojson.JtsCodecs._
import com.socrata.thirdparty.EitherCompat._
import com.vividsolutions.jts.geom._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.{FunSpec, Matchers}

class JtsCodecsTest extends FunSpec with Matchers with ScalaCheckPropertyChecks with GeoTest {
  val pointCoords = JArray(Seq(JNumber(6.0), JNumber(1.2)))
  val point2Coords = JArray(Seq(JNumber(3.4), JNumber(-2.7)))
  val lineCoords = JArray(Seq(pointCoords, point2Coords))

  def decodeString(str: String): JsonDecode.DecodeResult[Geometry] = geoCodec.decode(JsonReader.fromString(str))
  def encode(geom: Geometry): JValue = geoCodec.encode(geom)

  describe("GeometryCodec") {
    it("should convert geometry JSON of type Point correctly") {
      val body = """{
                    |  "type": "Point",
                    |  "coordinates": [6.0, 1.2]
                    |}""".stripMargin
      val pt = decodeString(body).asInstanceOf[JsonDecode.DecodeResult[Point]].rightProjection.getOrThrow
      (pt.getX, pt.getY) should equal ((6.0, 1.2))

      // uses implicit arbitrary.
      forAll { (point: Point) =>
        geoCodec.decode(encode(point)) should equal (Right(point))
      }
    }

    it("should convert geometry JSON of type Polygon - exterior and interior ring(s)") {
      val body = """{
                    |  "type": "Polygon",
                    |  "coordinates": [[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]],
                    |                  [[100.2, 0.2], [100.8, 0.2], [100.8, 0.8], [100.2, 0.8], [100.2, 0.2]]]
                    |}""".stripMargin
      val p = decodeString(body).asInstanceOf[JsonDecode.DecodeResult[Polygon]].rightProjection.getOrThrow
      p should equal (polygon(Seq((100.0, 0.0), (101.0, 0.0), (101.0, 1.0), (100.0, 1.0), (100.0, 0.0)),
                                  Seq(Seq((100.2, 0.2), (100.8, 0.2), (100.8, 0.8), (100.2, 0.8), (100.2, 0.2)))))

      forAll{(poly: Polygon) =>
        geoCodec.decode(encode(poly)) should equal (Right(poly))
      }


    }

    it("should convert geometry JSON of type Polygon - exterior ring only") {
      val body = """{
                   |  "type": "Polygon",
                   |  "coordinates": [[[0.0, 0.0], [0.0, 1.0], [1.0, 1.0], [0.0, 0.0]]]
                   |}""".stripMargin
      val p = decodeString(body).asInstanceOf[JsonDecode.DecodeResult[Polygon]].rightProjection.getOrThrow
      p should equal (polygon((0.0, 0.0), (0.0, 1.0), (1.0, 1.0), (0.0, 0.0)))

      forAll { (c: Coordinate, c1: Coordinate, c2: Coordinate, c3: Coordinate) =>
        val extPolygon = polygon((c.x, c.y), (c1.x, c1.y), (c2.x, c2.y), (c.x, c.y))

        geoCodec.decode(encode(extPolygon)) should equal (Right(extPolygon))
      }
    }

    it("should convert empty geometry JSON of type Polygon to a Left") {
      val body = """{
                   |  "type": "Polygon",
                   |  "coordinates": []
                   |}""".stripMargin
      val decoded = decodeString(body)
      decoded should be (Symbol("left"))
      decoded.left.getOrThrow should be (a[DecodeError.InvalidValue])
    }

    it("should convert geometry JSON of MultiLineString") {
      val body = """{
                    |  "type": "MultiLineString",
                    |  "coordinates": [[[0.0, 0.0], [0.0, 1.0]], [[1.0, 0.0], [1.0, 1.0]]]
                    |}""".stripMargin
      val mls = factory.createMultiLineString(Array(
                  linestring((0.0, 0.0), (0.0, 1.0)), linestring((1.0, 0.0), (1.0, 1.0))
                ))
      decodeString(body).rightProjection.getOrThrow should equal (mls)

      forAll{(ml: MultiLineString) =>
        geoCodec.decode(encode(ml)) should equal (Right(ml))
      }
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
      decodeString(body) should be (Right(mp))

      forAll{(mp: MultiPolygon) =>
        geoCodec.decode(encode(mp)) should equal (Right(mp))
      }
    }


    it("should convert geometry JSON of type MultiPoint") {
      val body = """{ "type": "MultiPoint",
          |"coordinates": [ [0, 0], [10.1, 10.1] ]
          |}""".stripMargin
        val mps = factory.createMultiPoint(Array(coord(0.0, 0.0), coord(10.10, 10.10)))
      decodeString(body) should be (Right(mps))


      geoCodec.decode(encode(mps)) should equal (Right(mps))
      forAll{(mps: MultiPoint) =>
        geoCodec.decode(encode(mps)) should equal(Right(mps))
      }
    }

    it("should not convert non-GeoJSON or unsupported types") {
      val body = JObject(Map("type" -> JString("foo"), "coordinates" -> pointCoords))
      geoCodec.decode(body) should be (Symbol("left"))

      val body2 = JArray(Seq(JString("totally not"), JNumber(5.6)))
      geoCodec.decode(body2) should be (Symbol("left"))
    }
  }

  describe("coordinates") {
    it("should convert Points correctly") {
      val pt = PointCodec.decode(pointCoords).rightProjection.getOrThrow
      (pt.getX, pt.getY) should equal ((6.0, 1.2))
    }

    it("should not convert non-Points") {
      PointCodec.decode(JArray(Seq(JNumber(-1)))) should be (Symbol("left"))
      PointCodec.decode(lineCoords) should be (Symbol("left"))
    }
  }
}
