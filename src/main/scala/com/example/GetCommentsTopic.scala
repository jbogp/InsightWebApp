package com.example

import scala.collection.mutable.ArrayBuffer

object GetCommentsTopic {
  

	def getCommentsHTML(value:ArrayBuffer[ListOfComments]):String = {
		val allComments = new ArrayBuffer[CommentExtended]
		val intermediate = value.foreach(list =>{
			list.comments.map(comments => {
				allComments.append(new CommentExtended(
				    comments.created_time,
				    comments.from,
				    comments.like_count,
				    comments.message,
				    list.url))
			})
		})
		allComments
			.sortBy(- _.like_count)
			.map(t=>(t.created_time+" "+t.from+"("+t.like_count+"): "+t.message+" "+t.url))
			.mkString("<hr>")
	}
  
}