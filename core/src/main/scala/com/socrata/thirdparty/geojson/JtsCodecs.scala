package com.socrata.thirdparty.geojson

import com.rojoma.json.v3.ast._
import com.rojoma.json.v3.codec.DecodeError.{InvalidValue, InvalidType}
import com.rojoma.json.v3.codec._
import com.rojoma.json.v3.util._
import com.vividsolutions.jts.geom._
import scala.language.postfixOps

import com.socrata.thirdparty.EitherCompat._

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
      JArray(Seq(pt.x, pt.y, pt.z).filterNot(_.isNaN).map(i => JNumber(i)))

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
      CoordinateCodec.decode(json).rightProjection.map(factory.createPoint)
  }


  implicit object LineStringCodec extends JsonEncode[LineString] with JsonDecode[LineString] {
    def encode(line: LineString): JValue = JsonEncode[Array[Coordinate]].encode(line.getCoordinates)

    def decode(json: JValue): JsonDecode.DecodeResult[LineString] =
      JsonDecode[Array[Coordinate]].decode(json).rightProjection.map(factory.createLineString)
  }

  implicit object PolygonCodec extends JsonEncode[Polygon] with JsonDecode[Polygon] {
    def encode(polygon: Polygon): JValue = {
      val rings = new Array[Array[Coordinate]](polygon.getNumInteriorRing + 1)
      rings(0) = polygon.getExteriorRing.getCoordinates

      var ringNo = 0
      while(ringNo < polygon.getNumInteriorRing) {
        rings(ringNo + 1) = polygon.getInteriorRingN(ringNo).getCoordinates
        ringNo += 1
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

  implicit class GeometryCollectionUtils(mp: GeometryCollection) {
    def map[B](f: Geometry => B): Seq[B] =
      (0 until mp.getNumGeometries).map { idx => f(mp.getGeometryN(idx)) }
  }

  implicit object MultiPolygonCodec extends JsonEncode[MultiPolygon] with JsonDecode[MultiPolygon] {
    def encode(mp: MultiPolygon): JValue =
      JsonEncode.toJValue(mp.map(_.asInstanceOf[Polygon]))

    def decode(json: JValue): JsonDecode.DecodeResult[MultiPolygon] =
      JsonDecode[Array[Polygon]].decode(json).rightProjection.map(factory.createMultiPolygon)
  }

  implicit object MultiLineStringCodec extends JsonEncode[MultiLineString] with JsonDecode[MultiLineString] {
   def encode(mls: MultiLineString): JValue =
     JsonEncode.toJValue(mls.map(_.asInstanceOf[LineString]))

    def decode(json: JValue): JsonDecode.DecodeResult[MultiLineString] =
      JsonDecode[Array[LineString]].decode(json).rightProjection.map(factory.createMultiLineString)
  }

  implicit object MultiPointCodec extends JsonEncode[MultiPoint] with JsonDecode[MultiPoint] {
    def encode(mp: MultiPoint): JValue =
      JsonEncode.toJValue(mp.map(_.asInstanceOf[Point]))

    def decode(json: JValue): JsonDecode.DecodeResult[MultiPoint] =
      JsonDecode[Array[Point]].decode(json).rightProjection.map(factory.createMultiPoint)
  }

  implicit val geoCodec = SimpleHierarchyCodecBuilder[Geometry](TagAndValue("type", "coordinates")).
                            branch[Point]("Point").
                            branch[MultiPoint]("MultiPoint").
                            branch[LineString]("LineString").
                            branch[MultiLineString]("MultiLineString").
                            branch[Polygon]("Polygon").
                            branch[MultiPolygon]("MultiPolygon").
                            build
}
