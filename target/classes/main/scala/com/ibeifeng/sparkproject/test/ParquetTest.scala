package com.ibeifeng.sparkproject.test

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext

object ParquetTest {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName("aa")
    val sc = new SparkContext(conf)
     val sqlContext = new SQLContext(sc);
      val productInfo = sqlContext.read.format("json").load("D:\\\\beifeng\\\\data\\\\product_info")
  val user = sqlContext.read.parquet("F:\\devTool\\spark-1.6.0-bin-hadoop2.6\\spark-1.6.0-bin-hadoop2.6\\examples\\src\\main\\resources\\users.parquet")
  user.registerTempTable("user")
 val df =  sqlContext.sql("select * from user")
  df.collect().foreach { println }
      
//      Parquet form:
//message spark_schema {
//  required binary name (UTF8);
//  optional binary favorite_color (UTF8);
//  required group favorite_numbers (LIST) {
//    repeated int32 array;
//  }
//}
//Catalyst form:
//StructType(StructField(name,StringType,false), StructField(favorite_color,StringType,true), StructField(favorite_numbers,ArrayType(IntegerType,false),false))
 
//  [Alyssa,null,WrappedArray(3, 9, 15, 20)]
//[Ben,red,WrappedArray()]
  }
}