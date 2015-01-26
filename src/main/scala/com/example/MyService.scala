package com.example

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import spray.httpx.marshalling.Marshaller
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.Success
import scala.util.Failure




// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

	// the HttpService trait defines only one abstract member, which
	// connects the services environment to the enclosing actor or test
	def actorRefFactory = context

	// this actor only runs our route, but you could add
	// other things here, like request stream processing
	// or timeout handling
	def receive = runRoute(myRoute)
}


// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService {
  
  

	def stringMarshaller(charset: HttpCharset, more: HttpCharset*): Marshaller[String] =
			stringMarshaller(ContentType(`text/plain`, charset), more map (ContentType(`text/plain`, _)): _*)

		//# string-marshaller
		// prefer UTF-8 encoding, but also render with other encodings if the client requests them
		implicit val StringMarshaller = stringMarshaller(ContentTypes.`text/plain(UTF-8)`, ContentTypes.`text/plain`)

		def stringMarshaller(contentType: ContentType, more: ContentType*): Marshaller[String] =
		Marshaller.of[String](contentType +: more: _*) { (value, contentType, ctx) ⇒
		ctx.marshalTo(HttpEntity(contentType, value))
	}
	val test = new ReadFromHbase


	val myRoute =
		path("comments"){
			parameters('req) { (req) =>
			    onComplete(test.readFutureTimeFilterComments("commentsalltime", req, 6000, 0)) {
			    	      case Success(value) => respondWithMediaType(`text/html`) {
			    	        complete{
			    	        	GetCommentsTopic.getCommentsHTML(value)
			    	        }
			    	      }
			    	      case Failure(ex)    => {
			    	        ex.printStackTrace()
			    	        complete(s"An error occurred: ${ex.getStackTrace()}")
			    	      }
			    }
			}
		}~
		path("topics"){
			parameters('req) { (req) =>
			    onComplete(test.readFutureTrendsComments(req,"val")) {
			    	      case Success(value) => respondWithMediaType(`text/html`) {
			    	        complete{
			    	        	value.map(topic=>{
			    	        		"<a href='/comments?req="+topic+"'>"+topic+"</a>"
			    	        	}).mkString("<hr>")
			    	        	
			    	        }
			    	      }
			    	      case Failure(ex)    => {
			    	        ex.printStackTrace()
			    	        complete(s"An error occurred: ${ex.getStackTrace()}")
			    	      }
			    }
			}
		}

}