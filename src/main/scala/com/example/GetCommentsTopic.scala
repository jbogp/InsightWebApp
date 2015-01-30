package com.example

import scala.collection.mutable.ArrayBuffer
import net.liftweb.json.DefaultFormats
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}
import java.sql.DriverManager
import java.sql.Connection
import java.sql.ResultSet


object GetCommentsTopic {
 
  
	implicit val formats = Serialization.formats(NoTypeHints)
	
	def getTopicsJson(queue:String):String ={
	  
		/*Getting the topics*/
		val topics:ResultSet = queue match {
		  	case x if(x == "topics1h" || x == "topics12h")  => {
				MySQLConnector.connection.createStatement()
					.executeQuery("SELECT topic FROM "+queue+" ORDER BY id DESC LIMIT 10")
		  	}
		  	case _  => {
				MySQLConnector.connection.createStatement()
					.executeQuery("SELECT topic FROM topicsalltime ORDER BY id DESC LIMIT 100")
		  	}
		}
		
		val ret = new ArrayBuffer[String]
		
		while(topics.next()) {
			ret.append(topics.getString("topic"))
		}
		
		write(ret.toList.reverse)
			
	}
	
  	def getCommentsJson(value:ArrayBuffer[List[Comment]]):String = {
		val json = value
			.reduce((t,i)=> t:::i)
			.sortBy(- _.like_count)
			
		write(json)		
	}
  	
   	def getTweetsJson(value:ArrayBuffer[Tweet]):String = {
		val json = value
			.sortBy(- _.createdAt)
		write(json)		
	}
	

	def getCommentsHTML(value:ArrayBuffer[List[Comment]]):String = {
		value
			.reduce((t,i)=> t:::i)
			.sortBy(- _.like_count)
			.map(t=>t.url match {
				case Some(_) => t.created_time+" "+t.from+"("+t.like_count+"): "+t.message+" <a href='"+t.url.get+"'>link</a>"
				case _ => t.created_time+" "+t.from+"("+t.like_count+"): "+t.message
			})
			.mkString("<hr>")
	}
  
}