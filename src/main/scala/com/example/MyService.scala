package com.example

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import main.scala.hbase.ReadFromHbase

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

  val myRoute =
    path("") {
      get {
        val test = new ReadFromHbase
        val test2 = test.readTimeFilterComments("comments1h", "king", 12000, 0)
        
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            <html>
              <body>
                <h1>Time waster time saver</h1>
        	  	{test2.foreach(f=>f.foreach(f=>"<h3>"+f+"</h3>"))}
              </body>
            </html>
          }
        }
      }
    }
}