package com.example

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import main.scala.hbase.ReadFromHbase
import spray.httpx.marshalling.Marshaller




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
    path("") {
      get {
        val test = new ReadFromHbase
        val test2 = test.readTimeFilterComments("comments1h", "king", 12000, 0)
        
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete(test2.reduce((t,i)=> t:::i).map(t=>(t.message)).filter(p=>p == "nothing").mkString("</br>"))
        }
      }
    }
}