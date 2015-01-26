package com.example

import scala.collection.mutable.ArrayBuffer

object GetCommentsTopic {
  

	def getCommentsHTML(value:ArrayBuffer[List[Comment]]):String = {
		value
			.reduce((t,i)=> t:::i)
			.sortBy(- _.like_count)
			.map(t=>(t.created_time+" "+t.from+"("+t.like_count+"): "+t.message+" "+t.url))
			.mkString("<hr>")
	}
  
}