package com.socrata.thirdparty.geojson

import com.rojoma.json.ast._
import com.rojoma.json.io.JsonReader
import com.vividsolutions.jts.geom._
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers


trait GeoTest {
  val factory = new GeometryFactory
  def coord(x: Double, y: Double) = new Coordinate(x, y)
  def mkCoord(xy: (Double, Double)) = (coord _).tupled(xy)
  def linestring(coords: (Double, Double)*) = factory.createLineString(coords.map(mkCoord).toArray)
  def polygon(coords: (Double, Double)*) = factory.createPolygon(
                                             factory.createLinearRing(coords.map(mkCoord).toArray),
                                             Array.empty)
}

class GeoJsonTest extends FunSpec with ShouldMatchers with GeoTest {
  val feat = """{
                |  "type": "Feature",
                |  "geometry": {
                |    "type": "LineString",
                |    "coordinates": [[1.0, 2.0], [1.0, 3.0]]
                |  },
                |  "properties": {"foo": 321}
                |}""".stripMargin

  val line = linestring((1.0, 2.0), (1.0, 3.0))
  val featureJson = FeatureJson(Map("foo" -> JNumber(321)), line)

  describe("FeatureJson") {
    it("should decode a Feature GeoJSON") {
      GeoJson.codec.decode(JsonReader.fromString(feat)) should equal (Some(featureJson))
    }
  }

  describe("FeatureCollectionJson") {
    it("should decode a FeatureCollection JSON") {
      val body = """{"type":"FeatureCollection",
                    |"crs": {"type": "name", "properties": {"foo": "gooblygoo"}},
                    |"features": [""".stripMargin + feat + "]}"
      GeoJson.codec.decode(JsonReader.fromString(body)) should equal (Some(
        FeatureCollectionJson(features = Seq(featureJson),
                              crs = Some(CRS("name", Map("foo" -> JString("gooblygoo")))) )))
    }
  }
}