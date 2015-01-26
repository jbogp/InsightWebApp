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
import org.apache.hadoop.fs.viewfs.Constants
import org.apache.hadoop.hbase.HBaseConfiguration
import scala.concurrent._
import ExecutionContext.Implicits.global
import org.apache.hadoop.hbase.CellUtil


class ReadFromHbase {
  
  	/*Creating configuration and connecting*/
	val config = HBaseConfiguration.create()
    config.clear();
    config.set("hbase.zookeeper.quorum", "ip-172-31-11-73.us-west-1.compute.internal");
    config.set("hbase.zookeeper.property.clientPort","2181");
    config.set("hbase.master", "ip-172-31-11-73.us-west-1.compute.internal:60000");
	val admin = new HBaseAdmin(config)
	implicit val formats = Serialization.formats(NoTypeHints)
	
	/*Generic Hbase reader to fetch all the rows of a table beetween 2 times and create objects out of that*/
	def readTimeFilterGeneric[T](table:String,minutesBackMax:Int,minutesBackMin:Int,handleRow:Result=>T,column:String):ArrayBuffer[T] = {
		/*Fetch the table*/
		val conn = admin.getConnection()
		val httable = conn.getTable(table)
		
		val offsetMax:Long = minutesBackMax*60000L
		val offsetMin:Long = minutesBackMin*60000L
		
		

		val theScan = new Scan().addColumn("infos".getBytes(),column.getBytes()).setTimeRange(Calendar.getInstance().getTimeInMillis()-offsetMax, Calendar.getInstance().getTimeInMillis()-offsetMin);
		
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
	
	
	
	def readFutureTrendsComments(table:String,column:String):Future[ArrayBuffer[String]] =  Future{
		/*function to handle meta link results*/
		def handleRow(next:Result):String = {
			val jsonString = {
			  val col = next.getColumnLatestCell("infos".getBytes(), column.getBytes())
			  val value = CellUtil.cloneValue(col)
			  if(value.length != 0)
				  new String(value)
			  else
				  "empty"
			}
			
			jsonString
		}
		/*Calling the database*/
		readTimeFilterGeneric[String](table, 20, 0, handleRow,column)
	}
 
	
	def readFutureTimeFilterComments(table:String,column:String,minutesBackMax:Int,minutesBackMin:Int):Future[ArrayBuffer[List[Comment]]] = Future {
		/*function to handle meta link results*/
		def handleRow(next:Result):List[Comment] = {
			val jsonString = {
			  val col = next.getColumnLatestCell("infos".getBytes(), column.getBytes())
			  val value = CellUtil.cloneValue(col)
			  if(value.length != 0)
				  new String(value)
			  else
				  "empty"
			}
			
			val json = parse(jsonString)
			json.extract[List[Comment]]
		}
		/*Calling the database*/
		readTimeFilterGeneric[List[Comment]](table, minutesBackMax, minutesBackMin, handleRow,column)
	}

}