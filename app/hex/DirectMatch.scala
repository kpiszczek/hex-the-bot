package hex

trait DirectMatch {
  def response(source: Map[String, String], in: String): Option[String] =
    source.get(in)
}
