package com.socrata.csv

import scala.io.Codec

import java.io._

import au.com.bytecode.opencsv._

class CSVIterator private (reader: CSVReader, @deprecated("Do not use") val stream: FileInputStream) extends Iterator[IndexedSeq[String]] with Closeable {
  // this constructor exists to support the deprecated ctor below; when
  // that goes away, so can this.
  private def this(inputStream: FileInputStream,
                   codec: Codec, separator: Char, quote: Char, escape: Char, skipLines: Int, strictQuotes: Boolean, ignoreLeadingWhitespace: Boolean) =
    this(new CSVReader(new InputStreamReader(inputStream, codec.charSet),
      separator, quote, escape, skipLines, strictQuotes, ignoreLeadingWhitespace), inputStream)

  @deprecated("Use CSVIterator.fromFile instead")
  def this(filename: File,
           codec: Codec = Codec.UTF8,
           separator: Char = CSVParser.DEFAULT_SEPARATOR,
           quote: Char = CSVParser.DEFAULT_QUOTE_CHARACTER,
           escape: Char = CSVParser.DEFAULT_ESCAPE_CHARACTER,
           skipLines: Int = CSVReader.DEFAULT_SKIP_LINES,
           strictQuotes: Boolean = CSVParser.DEFAULT_STRICT_QUOTES,
           ignoreLeadingWhitespace: Boolean = CSVParser.DEFAULT_IGNORE_LEADING_WHITESPACE) =
    this(new FileInputStream(filename), codec.charSet,
      separator,
      quote,
      escape,
      skipLines,
      strictQuotes,
      ignoreLeadingWhitespace)

  val it = locally {
    def loop(): Stream[IndexedSeq[String]] = {
      reader.readNext() match {
        case null => Stream.empty
        case row => row #:: loop()
      }
    }
    loop().iterator
  }

  def hasNext = it.hasNext
  def next() = it.next()
  override def toStream = it.toStream
  override def toSeq = it.toSeq

  def close() {
    reader.close()
  }
}

object CSVIterator {
  def fromFile(filename: File,
               codec: Codec = Codec.UTF8,
               separator: Char = CSVParser.DEFAULT_SEPARATOR,
               quote: Char = CSVParser.DEFAULT_QUOTE_CHARACTER,
               escape: Char = CSVParser.DEFAULT_ESCAPE_CHARACTER,
               skipLines: Int = CSVReader.DEFAULT_SKIP_LINES,
               strictQuotes: Boolean = CSVParser.DEFAULT_STRICT_QUOTES,
               ignoreLeadingWhitespace: Boolean = CSVParser.DEFAULT_IGNORE_LEADING_WHITESPACE) = {
    val inputStream = new FileInputStream(filename)
    try {
      new CSVIterator(new CSVReader(new InputStreamReader(inputStream, codec.charSet),
        separator,
        quote,
        escape,
        skipLines,
        strictQuotes,
        ignoreLeadingWhitespace),
        null)
    } catch {
      case e: Throwable =>
        inputStream.close()
        throw e
    }
  }

  def fromInputStream(inputStream: InputStream,
                      codec: Codec = Codec.UTF8,
                      separator: Char = CSVParser.DEFAULT_SEPARATOR,
                      quote: Char = CSVParser.DEFAULT_QUOTE_CHARACTER,
                      escape: Char = CSVParser.DEFAULT_ESCAPE_CHARACTER,
                      skipLines: Int = CSVReader.DEFAULT_SKIP_LINES,
                      strictQuotes: Boolean = CSVParser.DEFAULT_STRICT_QUOTES,
                      ignoreLeadingWhitespace: Boolean = CSVParser.DEFAULT_IGNORE_LEADING_WHITESPACE) = {
    new CSVIterator(new CSVReader(new InputStreamReader(inputStream, codec.charSet),
      separator,
      quote,
      escape,
      skipLines,
      strictQuotes,
      ignoreLeadingWhitespace),
    null)
  }

  def fromReader(reader: Reader,
                 separator: Char = CSVParser.DEFAULT_SEPARATOR,
                 quote: Char = CSVParser.DEFAULT_QUOTE_CHARACTER,
                 escape: Char = CSVParser.DEFAULT_ESCAPE_CHARACTER,
                 skipLines: Int = CSVReader.DEFAULT_SKIP_LINES,
                 strictQuotes: Boolean = CSVParser.DEFAULT_STRICT_QUOTES,
                 ignoreLeadingWhitespace: Boolean = CSVParser.DEFAULT_IGNORE_LEADING_WHITESPACE) = {
    new CSVIterator(new CSVReader(reader,
        separator,
        quote,
        escape,
        skipLines,
        strictQuotes,
        ignoreLeadingWhitespace),
      null)
  }
}
