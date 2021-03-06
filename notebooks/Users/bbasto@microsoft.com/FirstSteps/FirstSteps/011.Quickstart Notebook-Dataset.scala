// Databricks notebook source
// range of 100 numbers to create a Dataset.
val range100 = spark.range(100)
range100.collect()

// COMMAND ----------

display(dbutils.fs.ls("/my-data"))
//dbutils.fs.rm("/my-data/people.json")
//dbutils.fs.rm("/my-data/iot_devices.json")
//dbutils.fs.rm("/my-data")

//dbutils.fs.help()

// COMMAND ----------

//display(dbutils.fs.ls("/my-data"))
//dbutils.fs.rm("/my-data/people.json")
//dbutils.fs.rm("/my-data")

//copiar ficheiros para /FileStore/tables

dbutils.fs.mkdirs("/my-data")
//copiar do import de tabelas
dbutils.fs.cp("dbfs:/FileStore/tables/iot_devices.json", "/my.data/iot_devices.json")
dbutils.fs.rm("dbfs:/FileStore/tables/iot_devices.json")
//copiar do import de tabelas
dbutils.fs.cp("dbfs:/FileStore/tables/people.json", "/my-data/people.json")
dbutils.fs.rm("dbfs:/FileStore/tables/people.json")

// COMMAND ----------

// read a JSON file from a location mounted on a DBFS mount point
// Note that we are using the new entry point in Spark 2.0 called spark
val jsonData = spark.read.json("/my-data/people.json")

// COMMAND ----------

//First, define a case class that represents our type-specific Scala JVM Object
case class Person (age: Long, name: String)

// Read the JSON file, convert the DataFrames into a type-specific JVM Scala object 
//Person. Note that at this stage Spark, upon reading JSON, created a generic
// DataFrame = Dataset[Rows]. By explicitly converting DataFrame into Dataset
// results in a type-specific rows or collection of objects of type Person
val ds = spark.read.json("/my-data/people.json").as[Person]

// COMMAND ----------

// define a case class that represents our Device data.
case class DeviceIoTData (
  battery_level: Long,
  c02_level: Long,
  cca2: String,
  cca3: String,
  cn: String,
  device_id: Long,
  device_name: String,
  humidity: Long,
  ip: String,
  latitude: Double,
  longitude: Double,
  scale: String,
  temp: Long,
  timestamp: Long
)

// fetch the JSON device information uploaded into the Filestore
val jsonFile = "/my-data/iot_devices.json"

// read the json file and create the dataset from the case class DeviceIoTData
// ds is now a collection of JVM Scala objects DeviceIoTData
val ds = spark.read.json(jsonFile).as[DeviceIoTData]

// COMMAND ----------

display(ds)

// COMMAND ----------

// registering your Dataset as a temporary table to which you can issue SQL queries
ds.createOrReplaceTempView("iot_device_data")

// COMMAND ----------

// MAGIC %sql select cca3,count(distinct device_id) as device_id from iot_device_data group by cca3 order by device_id desc limit 100

// COMMAND ----------

// filter out all devices whose temperature exceed 25 degrees and generate
// another Dataset with three fields that of interest and then display
// the mapped Dataset
val dsTemp = ds.filter(d => d.temp > 25).map(d => (d.temp, d.device_name, d.cca3))
display(dsTemp)

// COMMAND ----------

// Apply higher-level Dataset API methods such as groupBy() and avg().
// Filter temperatures > 25, along with their corresponding
// devices' humidity, compute averages, groupBy cca3 country codes,
// and display the results, using table and bar charts
val dsAvgTmp = ds.filter(d => {d.temp > 25}).map(d => (d.temp, d.humidity, d.cca3)).
groupBy($"_3").avg()

// display averages as a table, grouped by the country
display(dsAvgTmp)

// COMMAND ----------

// display the averages as bar graphs, grouped by the country
display(dsAvgTmp)

// COMMAND ----------

// Select individual fields using the Dataset method select()
// where battery_level is greater than 6. Note this high-level
// domain specific language API reads like a SQL query
display(ds.select($"battery_level", $"c02_level", $"device_name").where($"battery_level" > 6).sort($"c02_level"))

// COMMAND ----------

// MAGIC %sql select `State Code`, `2015 median sales price` from data_geo where `2015 median sales price` is not null ;

// COMMAND ----------

// MAGIC %python
// MAGIC # Use the Spark CSV datasource with options specifying:
// MAGIC # - First line of file is a header
// MAGIC # - Automatically infer the schema of the data
// MAGIC data = spark.read.format("csv") \
// MAGIC   .option("header", "true") \
// MAGIC   .option("inferSchema", "true") \
// MAGIC   .load("/databricks-datasets/samples/population-vs-price/data_geo.csv")
// MAGIC 
// MAGIC data.cache() # Cache data for faster reuse
// MAGIC data = data.dropna() # drop rows with missing values
// MAGIC 
// MAGIC data = data.withColumnRenamed("State Code","StateCode").withColumnRenamed("2015 median sales price","2015MediaSalesPrice")

// COMMAND ----------

// MAGIC %python
// MAGIC # Register table so it is accessible via SQL Context
// MAGIC # For Apache Spark = 2.0
// MAGIC data.createOrReplaceTempView("data_geo")

// COMMAND ----------

// MAGIC %python
// MAGIC data.take(10)

// COMMAND ----------

// MAGIC %python
// MAGIC display(data)

// COMMAND ----------

// MAGIC %sql select StateCode, 2015MediaSalesPrice from data_geo

// COMMAND ----------

// MAGIC %sql describe data_geo

// COMMAND ----------

