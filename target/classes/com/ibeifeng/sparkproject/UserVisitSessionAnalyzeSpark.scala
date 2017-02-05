package com.ibeifeng.sparkproject

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext

object UserVisitSessionAnalyzeSpark {
  def main(args: Array[String]): Unit = {
    
    
    val conf  = new SparkConf().setAppName("CusterAccu").setMaster("local")
        val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc);
      val productInfo = sqlContext.read.format("json").load("D:\\beifeng\\data\\product_info")
      val user_info = sqlContext.read.format("json").load("D:\\beifeng\\data\\user_info")
      val user_visit_action = sqlContext.read.format("json").load("D:\\beifeng\\data\\user_visit_action")
//      productInfo.take(1).foreach { println }
      
      println("user---")
    user_visit_action.select("", "")
      val sessionToRow =  user_visit_action.map { x => ( x.getString(x.fieldIndex("session_id")),x) }
    val userInfoDf = user_info.map{x=> (x.getLong(x.fieldIndex("user_id")),x)}
    
    
  }
}