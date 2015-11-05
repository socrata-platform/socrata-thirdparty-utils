package com.socrata.thirdparty.geojson

import com.rojoma.json.v3.ast._
import com.rojoma.json.v3.io.JsonReader
import com.vividsolutions.jts.geom._
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence
import org.scalacheck.{Gen, Arbitrary}
import org.scalatest.{Matchers, FunSpec}


trait GeoTest {
  val factory = new GeometryFactory
  def coord(x: Double, y: Double): Coordinate = new Coordinate(x, y)
  def mkCoord(xy: (Double, Double)): Coordinate = (coord _).tupled(xy)
  def linestring(coords: (Double, Double)*): LineString = factory.createLineString(coords.map(mkCoord).toArray)
  def ring(coords: Seq[(Double, Double)]): LinearRing = factory.createLinearRing(coords.map(mkCoord).toArray)
  def polygon(coords: (Double, Double)*): Polygon = factory.createPolygon(ring(coords), Array.empty)
  def polygon(outer: Seq[(Double, Double)], inner: Seq[Seq[(Double, Double)]] = Seq.empty): Polygon =
    factory.createPolygon(ring(outer), inner.map(ring).toArray)


  // generate arbitrary coordinate
  implicit val arbCoordinate = Arbitrary[Coordinate]{
    for {
    // limits are +/- 180, avoid having to deal with adding constraints on tests.
      lon <- Gen.choose(-170d, 170d)
      // limits are +/- 90, avoid having to deal with adding constraints on tests.
      lat <- Gen.choose(-80d, 80d)
    } yield coord(lon, lat)
  }

  // Set up for arbitrary point, to be used to test round trip encode -> decode
  implicit val arbPoint = Arbitrary[Point] {
    for {
      c <- Arbitrary.arbitrary[Coordinate]
    } yield new Point(new CoordinateArraySequence(Array(c)), factory)
  }

  implicit val arbPoly = Arbitrary[Polygon] {
    def mkRing(lon: Double, lat: Double, size: Double): LinearRing = {
      ring(Seq((lon, lat), (lon + size, lat), (lon + size, lat + size), (lon, lat + size), (lon, lat)))
    }

    for {
      c <- Arbitrary.arbitrary[Coordinate]

      shell = mkRing(c.x, c.y, 1)
      holes = Array(mkRing(c.x + 0.1, c.y + 0.1, 0.1), mkRing(c.x + .3, c.y + .3, 0.2))
    } yield new Polygon(shell, holes, factory)
  }


  implicit val arbMultiLineStr = Arbitrary[MultiLineString] {
    for{
      c1 <- Arbitrary.arbitrary[Coordinate]
      c2 <- Arbitrary.arbitrary[Coordinate]
      c3 <- Arbitrary.arbitrary[Coordinate]
      c4 <- Arbitrary.arbitrary[Coordinate]

      ls1 = linestring((c1.x, c1.y), (c2.x, c2.y))
      ls2 = linestring((c3.x, c3.y), (c4.x, c4.y))
    } yield factory.createMultiLineString(Array(ls1,ls2))
  }

  implicit val arbMultiPoly = Arbitrary[MultiPolygon] {
    for{
      p <- Arbitrary.arbitrary[Polygon]
      p1 <- Arbitrary.arbitrary[Polygon]
      p2 <- Arbitrary.arbitrary[Polygon]
      p3 <- Arbitrary.arbitrary[Polygon]

    } yield factory.createMultiPolygon(Array(p, p1, p2, p3))
  }

  implicit val arbMultiPoint = Arbitrary[MultiPoint] {
    for{
      p0 <- Arbitrary.arbitrary[Coordinate]
      p1 <- Arbitrary.arbitrary[Coordinate]
      p2 <- Arbitrary.arbitrary[Coordinate]
    } yield factory.createMultiPoint(Array(p0, p1, p2))
  }



}

class GeoJsonTest extends FunSpec with Matchers with GeoTest {
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
      GeoJson.codec.decode(JsonReader.fromString(feat)) should equal (Right(featureJson))
    }
  }

  describe("FeatureCollectionJson") {
    it("should decode a FeatureCollection JSON") {
      val body = """{"type":"FeatureCollection",
                    |"crs": {"type": "name", "properties": {"foo": "gooblygoo"}},
                    |"features": [""".stripMargin + feat + "]}"
      GeoJson.codec.decode(JsonReader.fromString(body)) should equal (Right(
        FeatureCollectionJson(features = Seq(featureJson),
                              crs = Some(CRS("name", Map("foo" -> JString("gooblygoo")))) )))
    }
  }
}
