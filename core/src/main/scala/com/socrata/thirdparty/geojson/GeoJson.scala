package com.socrata.thirdparty.geojson

import com.rojoma.json.v3.ast._
import com.rojoma.json.v3.codec._
import com.rojoma.json.v3.util._
import com.vividsolutions.jts.geom._

sealed trait GeoJsonBase

// The possible subtypes of GeoJSON objects
// See http://geojson.org/geojson-spec.html
case class FeatureJson(properties: Map[String, JValue],
                       geometry: Geometry,
                       crs: Option[CRS] = None) extends GeoJsonBase
case class FeatureCollectionJson(features: Seq[FeatureJson],
                                 crs: Option[CRS] = None) extends GeoJsonBase

// Not considered a GeoJSON object, but still.
// Two possible values for crsType:  name, link
case class CRS(@JsonKey("type") crsType: String, properties: Map[String, JValue])

/**
  * The normal entry point for GeoJSON object decoding.  All GeoJSON objects are subtypes of GeoJsonBase.
  * {{{
  *   val geoJson = GeoJson.codec.decode(JsonReader.fromString(jsonString))
  *   geoJson match {
  *     case Some(FeatureCollectionJson(features, _)) => features.foreach(println)
  *   }
  * }}}
  *
  * NOTE: Can't think of a easy and nonclumsy way to add the other types here.
  *
  * TODO(velvia): represent CRS as a real case class
  */
object GeoJson {
  import JtsCodecs._

  implicit val crsJsonCodec = AutomaticJsonCodecBuilder[CRS]

  implicit val codec = locally {
    implicit val featureJsonCodec = AutomaticJsonCodecBuilder[FeatureJson]
    implicit val featureCollectionJsonCodec = AutomaticJsonCodecBuilder[FeatureCollectionJson]

    SimpleHierarchyCodecBuilder[GeoJsonBase](InternalTag("type")).
      branch[FeatureJson]("Feature").
      branch[FeatureCollectionJson]("FeatureCollection").
      build
  }
}

abstract class GeoJsonException(msg: String) extends Exception(msg)
case class DeserializationException(msg: String) extends GeoJsonException(msg)
