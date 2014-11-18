package com.socrata.thirdparty.geojson

import com.rojoma.json.ast._
import com.rojoma.json.codec.JsonCodec
import com.rojoma.json.util._
import com.vividsolutions.jts.geom._

/**
 * JsonCodecs for various jts.geom._ objects for GeoJSON.
 * All of the Codecs except for GeometryCodec decodes the "coordinates" of a Geometry.
 * GeometryCodec decodes the type and coordinates together to result in proper Geometry sub-type.
 * Has NO dependencies on GeoTools!!
 *
 * See JtsCodecsTest for examples.
 *
 * BTW, aren't type classes awesome?  :)
 */
object JtsCodecs {
  private val factory = new GeometryFactory

  implicit object CoordinateCodec extends JsonCodec[Coordinate] {
    def encode(pt: Coordinate): JValue =
      if (pt.z.isNaN) {
        JArray(Seq(JNumber(pt.x), JNumber(pt.y)))
      } else {
        JArray(Seq(JNumber(pt.x), JNumber(pt.y), JNumber(pt.z)))
      }

    def decode(json: JValue): Option[Coordinate] = json match {
      case JArray(Seq(JNumber(x), JNumber(y))) =>
        Some(new Coordinate(x.toDouble, y.toDouble))
      case JArray(Seq(JNumber(x), JNumber(y), JNumber(z))) =>
        Some(new Coordinate(x.toDouble, y.toDouble, z.toDouble))
      case other =>
        None
    }
  }

  implicit object PointCodec extends JsonCodec[Point] {
    def encode(pt: Point): JValue = JsonCodec[Coordinate].encode(pt.getCoordinate)
    def decode(json: JValue): Option[Point] =
      CoordinateCodec.decode(json).map { coord => factory.createPoint(coord) }
  }

  implicit object LineStringCodec extends JsonCodec[LineString] {
    def encode(line: LineString): JValue = JsonCodec[Array[Coordinate]].encode(line.getCoordinates)
    def decode(json: JValue): Option[LineString] =
      JsonCodec[Array[Coordinate]].decode(json).map { coords => factory.createLineString(coords) }
  }

  implicit object PolygonCodec extends JsonCodec[Polygon] {
    def encode(polygon: Polygon): JValue = {
      val exteriorRing = polygon.getExteriorRing.getCoordinates
      val interiorRings = (0 to polygon.getNumInteriorRing - 1).map { idx =>
        polygon.getInteriorRingN(idx).getCoordinates
      }
      JsonCodec[List[Array[Coordinate]]].encode(List(exteriorRing) ++ interiorRings)
    }

    def decode(json: JValue): Option[Polygon] = {
      for {
        // Splits the first ring of a polygon from subsequent rings, which are holes.
        ring :: holes <- JsonCodec[List[Array[Coordinate]]].decode(json)
      } yield {
        factory.createPolygon(
          factory.createLinearRing(ring),
          holes.map(hole => factory.createLinearRing(hole)).toArray
        )
      }
    }
  }

  implicit object MultiPolygonCodec extends JsonCodec[MultiPolygon] {
    def encode(mp: MultiPolygon): JValue =
      JsonCodec[Array[Polygon]].encode((0 to mp.getNumGeometries - 1).map { idx =>
        mp.getGeometryN(idx) match { case p: Polygon => p }
      }.toArray)

    def decode(json: JValue): Option[MultiPolygon] =
      JsonCodec[Array[Polygon]].decode(json).map { polygons => factory.createMultiPolygon(polygons) }
  }

  implicit object MultiLineStringCodec extends JsonCodec[MultiLineString] {
    def encode(mls: MultiLineString): JValue =
      JsonCodec[Array[LineString]].encode((0 to mls.getNumGeometries - 1).map { idx =>
        mls.getGeometryN(idx) match { case ls: LineString => ls }
      }.toArray)

    def decode(json: JValue): Option[MultiLineString] =
      JsonCodec[Array[LineString]].decode(json).map { lines => factory.createMultiLineString(lines) }
  }

  implicit val geoCodec = SimpleHierarchyCodecBuilder[Geometry](TagAndValue("type", "coordinates")).
                             branch[Point]("Point").
                             branch[LineString]("LineString").
                             branch[Polygon]("Polygon").
                             branch[MultiPolygon]("MultiPolygon").
                             branch[MultiLineString]("MultiLineString").
                             build
}