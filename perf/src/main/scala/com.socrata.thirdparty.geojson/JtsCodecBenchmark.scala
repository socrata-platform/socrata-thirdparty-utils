package com.socrata.thirdparty.geojson

import java.util.concurrent.TimeUnit

import com.rojoma.json.v3.ast._
import com.rojoma.json.v3.codec.JsonDecode
import com.rojoma.json.v3.io.JsonReader
import com.socrata.thirdparty.geojson.JtsCodecs._
import com.vividsolutions.jts.geom._
import org.openjdk.jmh.annotations.{Benchmark, BenchmarkMode, Mode, OutputTimeUnit, Scope, State}

/**
 * Measures basic read benchmark with no NAs for an IntColumn.
 * Just raw read speed basically.
 *
 * For a description of the JMH measurement modes, see
 * https://github.com/ktoso/sbt-jmh/blob/master/src/sbt-test/sbt-jmh/jmh-run ...
 *   /src/main/scala/org/openjdk/jmh/samples/JMHSample_02_BenchmarkModes.scala
 */
// scalastyle:off magic.number
@State(Scope.Thread)
class JtsCodecBenchmark {
  def decodeString(str: String): JsonDecode.DecodeResult[Geometry] = geoCodec.decode(JsonReader.fromString(str))
  def encode(geom: Geometry): JValue = geoCodec.encode(geom)

  val body = """{
                |  "type": "Polygon",
                |  "coordinates": [[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]],
                |                  [[100.2, 0.2], [100.8, 0.2], [100.8, 0.8], [100.2, 0.8], [100.2, 0.2]]]
                |}""".stripMargin
  val p = decodeString(body).asInstanceOf[JsonDecode.DecodeResult[Polygon]].right.get

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def encodeGeoms(): Unit = {
    @annotation.tailrec def innerEncoder(iters: Int): Unit = {
      if (iters > 0) {
        encode(p)
        innerEncoder(iters - 1)
      }
    }
    innerEncoder(10000)
  }
}
