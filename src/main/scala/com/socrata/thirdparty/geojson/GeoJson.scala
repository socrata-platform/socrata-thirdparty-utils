package com.socrata.thirdparty.geojson

import com.rojoma.json.ast._
import com.rojoma.json.codec.JsonCodec
import com.rojoma.json.util._
import com.vividsolutions.jts.geom._

sealed trait GeoJsonBase

// The possible subtypes of GeoJSON objects
case class FeatureJson(properties: Map[String, JValue], geometry: Geometry) extends GeoJsonBase
case class FeatureCollectionJson(features: Seq[FeatureJson], crs: Option[JObject]) extends GeoJsonBase

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

  implicit val featureJsonCodec = AutomaticJsonCodecBuilder[FeatureJson]
  implicit val featureCollectionJsonCodec = AutomaticJsonCodecBuilder[FeatureCollectionJson]

  val codec = SimpleHierarchyCodecBuilder[GeoJsonBase](InternalTag("type")).
                branch[FeatureJson]("Feature").
                branch[FeatureCollectionJson]("FeatureCollection").
                build
}

abstract class GeoJsonException(msg: String) extends Exception(msg)
case class DeserializationException(msg: String) extends GeoJsonException(msg)
