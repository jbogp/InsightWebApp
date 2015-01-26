package com.example

import scala.collection.mutable.ArrayBuffer

object GetCommentsTopic {
  
  	def getCommentsUlrs(value:ArrayBuffer[ListOfComments]):String = {
		value.map(_.url)
			.mkString("<hr>")
	}

	def getCommentsHTML(value:ArrayBuffer[ListOfComments]):String = {
		value.map(_.comments)
			.reduce((t,i)=> t:::i)
			.sortBy(- _.like_count)
			.map(t=>(t.created_time+" "+t.from+"("+t.like_count+"): "+t.message))
			.mkString("<hr>")
	}
  
}