package com.ibeifeng.sparkproject.spark.page

import java.util.ArrayList
import java.util.Date
import scala.collection.JavaConverters._
import org.apache.spark.Accumulator
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.SQLContext
import com.ibeifeng.sparkproject.util.DateUtils
import com.ibeifeng.sparkproject.util.NumberUtils
object PageOneStepConvertRateSpark {
  def main(args: Array[String]): Unit = {
        val conf  = new SparkConf().setAppName("CusterAccu").setMaster("local")
        val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc);
      val productInfo = sqlContext.read.format("json").load("D:\\beifeng\\data\\product_info")
      val user_info = sqlContext.read.format("json").load("D:\\beifeng\\data\\user_info")
      val user_visit_action = sqlContext.read.format("json").load("D:\\beifeng\\data\\user_visit_action")
//      productInfo.take(1).foreach { println }
      
      println("user---")
      val sessionid2actionRDD =  user_visit_action.map { x => ( x.getString(x.fieldIndex("session_id")),x) }
    sessionid2actionRDD.cache()
      val sessionid2actionsRDD = sessionid2actionRDD.groupByKey()
      val startPageAccumulator = sc.accumulator(0L)
      val pageSplitRdd = generateAndMatchPageSplit(sc,sessionid2actionsRDD,"1_3_5",startPageAccumulator)
      val pageSplitPvMap =  pageSplitRdd.countByKey()
      val startPageCount =  startPageAccumulator.value
  computePageSplitConvertRate(pageSplitPvMap, startPageCount, "1_3")
      
  }
  def generateAndMatchPageSplit(sc:SparkContext,sessionid2actionsRDD: RDD[(String, Iterable[Row])],pageFlow:String,startPageAccumulator: Accumulator[Long])={
       val rdd = sessionid2actionsRDD.flatMap(tuple=>{
        val iter = tuple._2.iterator
        val targetPages = pageFlow.split(",")
        // 这里，我们拿到的session的访问行为，默认情况下是乱序的
            // 比如说，正常情况下，我们希望拿到的数据，是按照时间顺序排序的
            // 但是问题是，默认是不排序的
            // 所以，我们第一件事情，对session的访问行为数据按照时间进行排序
        val visitList = iter.toList.sortWith((row1:Row,row2:Row)=>{
           val actionTime1 = row1.getAs[String]("action_time")
           val actionTime2 = row2.getAs[String]("action_time")
           val date1 = DateUtils.parseTime(actionTime1);
           val date2 = DateUtils.parseTime(actionTime2);
              date1.getTime < date2.getTime
         }) 
         //page_id
         var lastPageId :Long= -1
           val list = new ArrayList[Tuple2[String,Long]]
         for(v<- visitList){
          val pageId = v.getAs[Long]("pageId")
        		  if(pageId==targetPages(0)){
                 startPageAccumulator.add(0)
              }
          if(lastPageId== -1){
            lastPageId = pageId 
            //这里要赋值 continue
          }
          val pageSplit = lastPageId +"_"+ pageId
        
          if(pageFlow.contains(pageSplit)){
            list.add((pageSplit,1))
          }
          lastPageId = pageId 
         }
        list.asScala.toIterator
       })
       rdd
  }
    def computePageSplitConvertRate(pageSplitPvMap: scala.collection.Map[String, Long],startPageCount:Long,pageFlow:String)={
        val pages =pageFlow.split(",")
        val convertRateMap = scala.collection.mutable.HashMap[String,Double]()
           for(i<- 1 to pages.length){
           val targetPageSplit =  pages(i-1) + "_"+pages(i)
           val targetPageSplitPv = pageSplitPvMap.get(targetPageSplit)
           var convertRate = 0.0
           var lastPageSplitPv = 0L
           if(i==1){
             convertRate = NumberUtils.formatDouble(targetPageSplitPv.getOrElse(0L) / startPageCount.toDouble,2)
           }else{
              convertRate = NumberUtils.formatDouble(targetPageSplitPv.getOrElse(0L) / lastPageSplitPv.toDouble,2)
           }
           convertRateMap.put(targetPageSplit, convertRate)
           lastPageSplitPv = targetPageSplitPv.get
        }
        convertRateMap
    }
}