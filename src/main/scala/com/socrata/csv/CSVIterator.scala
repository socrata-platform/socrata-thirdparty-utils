package com.socrata.csv

import scala.io.Codec

import java.io._

import au.com.bytecode.opencsv._

class CSVIterator(
  filename: File,
  codec: Codec = Codec.UTF8,
  separator: Char = CSVParser.DEFAULT_SEPARATOR,
  quote: Char = CSVParser.DEFAULT_QUOTE_CHARACTER,
  escape: Char = CSVParser.DEFAULT_ESCAPE_CHARACTER,
  skipLines: Int = CSVReader.DEFAULT_SKIP_LINES,
  strictQuotes: Boolean = CSVParser.DEFAULT_STRICT_QUOTES,
  ignoreLeadingWhitespace: Boolean = CSVParser.DEFAULT_IGNORE_LEADING_WHITESPACE
) extends Iterator[IndexedSeq[String]] with Closeable {
  lazy val it = locally {
    val r = new CSVReader(
      new InputStreamReader(stream, codec.charSet),
      separator,
      quote,
      escape,
      skipLines,
      strictQuotes,
      ignoreLeadingWhitespace
    )
    def loop(): Stream[IndexedSeq[String]] = {
      r.readNext() match {
        case null => Stream.empty
        case row => row #:: loop()
      }
    }
    loop().iterator
  }

  val stream = new FileInputStream(filename)

  def hasNext = it.hasNext
  def next() = it.next()
  override def toStream = it.toStream
  override def toSeq = it.toSeq

  def close() {
    stream.close()
  }
}
