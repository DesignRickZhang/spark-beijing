package com.ibeifeng.sparkproject.test

import org.apache.spark.streaming.StreamingContext
import org.apache.spark.SparkConf
import org.apache.spark.streaming.Seconds
import org.apache.spark.SparkContext
import com.ibeifeng.sparkproject.SortKey

object KryoTest {
    def main(args: Array[String]): Unit = {
          val conf = new SparkConf().setAppName("KafkaWordCount").setMaster("local")
//        		  .registerKryoClasses() 
    val sc = new SparkContext(conf)
      
    }

}