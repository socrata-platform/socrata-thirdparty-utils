package com.socrata.thirdparty.geojson

import com.rojoma.json.v3.ast._
import com.rojoma.json.v3.codec.DecodeError.{InvalidValue, InvalidType}
import com.rojoma.json.v3.codec._
import com.rojoma.json.v3.util._
import com.vividsolutions.jts.geom._
import scalaxy.loops._
import scala.language.postfixOps
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

  implicit object CoordinateCodec extends JsonEncode[Coordinate] with JsonDecode[Coordinate] {
    def encode(pt: Coordinate): JValue =
      if (pt.z.isNaN)
        JArray(Seq(JNumber(pt.x), JNumber(pt.y)))
      else
        JArray(Seq(JNumber(pt.x), JNumber(pt.y), JNumber(pt.z)))



    def decode(json: JValue): JsonDecode.DecodeResult[Coordinate] =
      json match {
        case JArray(Seq(x: JNumber, y: JNumber)) => Right(new Coordinate(x.toDouble, y.toDouble))
        case JArray(Seq(x: JNumber, y:JNumber, z:JNumber)) => Right(new Coordinate(x.toDouble, y.toDouble, z.toDouble))
        case _ => Left(InvalidValue(json))
      }
  }

  implicit object PointCodec extends JsonEncode[Point] with JsonDecode[Point] {
    def encode(pt: Point): JValue = JsonEncode[Coordinate].encode(pt.getCoordinate)

    def decode(json: JValue): JsonDecode.DecodeResult[Point] =
      CoordinateCodec.decode(json).right.map(factory.createPoint)
  }

  implicit object LineStringCodec extends JsonEncode[LineString] with JsonDecode[LineString] {
    def encode(line: LineString): JValue = JsonEncode[Array[Coordinate]].encode(line.getCoordinates)

    def decode(json: JValue): JsonDecode.DecodeResult[LineString] =
      JsonDecode[Array[Coordinate]].decode(json).right.map(factory.createLineString)
  }

  implicit object PolygonCodec extends JsonEncode[Polygon] with JsonDecode[Polygon] {
    def encode(polygon: Polygon): JValue = {
      val rings = new Array[Array[Coordinate]](polygon.getNumInteriorRing + 1)
      rings(0) = polygon.getExteriorRing.getCoordinates
      for { ringNo <- 0 until polygon.getNumInteriorRing optimized } {
        rings(ringNo + 1) = polygon.getInteriorRingN(ringNo).getCoordinates
      }

      JsonEncode[Array[Array[Coordinate]]].encode(rings)
    }

    def decode(json: JValue): JsonDecode.DecodeResult[Polygon] = {
      JsonDecode[List[Array[Coordinate]]].decode(json) match {
        case Right(ring :: holes) =>
          Right(
            factory.createPolygon(
              factory.createLinearRing(ring),
              holes.map(hole => factory.createLinearRing(hole)).toArray)
          )
        case Right(Nil) => Left(InvalidValue(json))
        case Left(x) => Left(x)
      }
    }
  }

  implicit object MultiPolygonCodec extends JsonEncode[MultiPolygon] with JsonDecode[MultiPolygon] {
    def encode(mp: MultiPolygon): JValue =
      JsonEncode[Array[Polygon]].encode((0 until mp.getNumGeometries).map { idx =>
        mp.getGeometryN(idx) match { case p: Polygon => p }
      }.toArray)

    def decode(json: JValue): JsonDecode.DecodeResult[MultiPolygon] =
      JsonDecode[Array[Polygon]].decode(json).right.map(factory.createMultiPolygon)
  }

  implicit object MultiLineStringCodec extends JsonEncode[MultiLineString] with JsonDecode[MultiLineString] {
    def encode(mls: MultiLineString): JValue =
      JsonEncode[Array[LineString]].encode((0 until mls.getNumGeometries).map { idx =>
        mls.getGeometryN(idx) match { case ls: LineString => ls }
      }.toArray)

    def decode(json: JValue): JsonDecode.DecodeResult[MultiLineString] =
      JsonDecode[Array[LineString]].decode(json).right.map(factory.createMultiLineString)
  }

  implicit val geoCodec = SimpleHierarchyCodecBuilder[Geometry](TagAndValue("type", "coordinates")).
                             branch[Point]("Point").
                             branch[LineString]("LineString").
                             branch[Polygon]("Polygon").
                             branch[MultiPolygon]("MultiPolygon").
                             branch[MultiLineString]("MultiLineString").
                             build




}