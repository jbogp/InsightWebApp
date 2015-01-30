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
import spray.http.HttpHeaders.RawHeader




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


	val myRoute =
	  	path(""){
			complete {
				<html>
					<body>
Nothing to see here
					</body>
				</html>
			}
		}~
		path("comments"){
			parameters('req) { (req) => respondWithHeader(RawHeader("Access-Control-Allow-Origin", "*")){
			    onComplete(ReadFromHbase.readFutureTimeFilterComments("commentsalltime", req, 600, 0)) {
			    	      case Success(value) => respondWithMediaType(`application/json`) {
								complete{
									GetCommentsTopic.getCommentsJson(value)
								}
			    	      }
			    	      case Failure(ex)    => respondWithMediaType(`application/json`){
			    	        complete("""{"error":"no comments on this topic"}""")
			    	      }
			    }
			}
			}
		}~
		path("tweets"){
			parameters('req) { (req) => respondWithHeader(RawHeader("Access-Control-Allow-Origin", "*")){
			    onComplete(ReadFromHbase.readFutureTimeFilterTweets("commentsalltime", "theTweets_"+req, 60, 0)) {
			    	      case Success(value) => respondWithMediaType(`application/json`) {
			    	        complete{
			    	        	GetCommentsTopic.getTweetsJson(value)
			    	        }
			    	      }
			    	      case Failure(ex)    => respondWithMediaType(`application/json`){
			    	    	  ex.printStackTrace()
			    	        complete("""{"error":"no comments on this topic"}""")
			    	      }
			    }
			}
			}
		}~
		path("topics"){
			parameters('req) { (req) =>respondWithHeader(RawHeader("Access-Control-Allow-Origin", "*")){
    	        complete{
    	        	GetCommentsTopic.getTopicsJson(req)
    	        }
			}}
		}

}