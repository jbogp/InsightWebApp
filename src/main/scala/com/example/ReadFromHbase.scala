package main.scala.hbase

import org.apache.hadoop.hbase.client.HTable
import org.apache.hadoop.hbase.client.HBaseAdmin
import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.util.Bytes
import java.util.Calendar
import org.apache.hadoop.hbase.client.Scan
import scala.collection.mutable.ArrayBuffer
import net.liftweb.json.DefaultFormats
import net.liftweb.json._
import org.apache.hadoop.hbase.client.Result


class ReadFromHbase {
  
  	/*Creating configuration and connecting*/
	val conf = new Configuration()
	val admin = new HBaseAdmin(conf)
	implicit val formats = Serialization.formats(NoTypeHints)
	
	/*Generic Hbase reader to fetch all the rows of a table beetween 2 times and create objects out of that*/
	def readTimeFilterGeneric[T](table:String,minutesBackMax:Int,minutesBackMin:Int,handleRow:Result=>T):ArrayBuffer[T] = {
		/*Fetch the table*/
		val httable = new HTable(conf, table)
		val offsetMax:Long = minutesBackMax*60000L
		val offsetMin:Long = minutesBackMin*60000L
		val theScan = new Scan().setTimeRange(Calendar.getInstance().getTimeInMillis()-offsetMax, Calendar.getInstance().getTimeInMillis()-offsetMin);
		
		/*Adding timestamp filter*/
		val res = httable.getScanner(theScan)

		val iterator = res.iterator()
		val ret = new ArrayBuffer[T]
		while(iterator.hasNext()) {
			val next = iterator.next()
			ret.append(handleRow(next))		
		}
		ret		
	}
	
	case class Comment(created_time:String,from:String,like_count:Int,message:String)
 
	
	def readTimeFilterComments(table:String,column:String,minutesBackMax:Int,minutesBackMin:Int):ArrayBuffer[List[Comment]] =  {
		/*function to handle meta link results*/
		def handleRow(next:Result):List[Comment] = {
			val jsonString = {
			  val col = next.getColumn("infos".getBytes(), column.getBytes())
			  if(col.isEmpty())
			    """[{"created_time":"never","from":"noone","like_count":0,"message":"nothing"}]"""
			  else{
			     new String(col.get(0).getValue())
			  }
			}
			
			val json = parse(jsonString)
			json.extract[List[Comment]]
		}
		/*Calling the database*/
		readTimeFilterGeneric[List[Comment]](table, minutesBackMax, minutesBackMin, handleRow)
	}

}