package tasks

import scala.io.Source
import scala.language.reflectiveCalls
import actors._

object Tasks {
	import play.api.Play.current
	import play.api.libs.concurrent.Akka

	val system = Akka.system

	def using[A <: { def close(): Unit }, B](resource: A)(f: A => B): B = 
    try 
    	f(resource)
    finally 
      resource.close()

	def loadErrorMessages(fileName: String): Unit = 
		using(Source.fromFile(fileName, enc="UTF-8")) { source =>
			val store = system.actorSelection("/user/error-message-store")
			source.getLines.foreach(line =>
				line.trim.split("->") match {
          case Array(prob, error) => store ! AddErrorMessage(prob.toDouble, error.trim)
          case Array(text) => store ! AddErrorMessage(1.0, text.trim)
        }
			)
    }
}