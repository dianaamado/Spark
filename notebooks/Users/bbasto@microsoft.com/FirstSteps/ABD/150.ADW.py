# Databricks notebook source
spark.conf.set(
   "spark.sql.parquet.writeLegacyFormat",
   "true")

spark.conf.set(
  "fs.azure.account.key.bvbdemodb001.blob.core.windows.net",
  "T+D3g5yZZpUO6/pH4+2Dw49ju6ZwxJOpxh9X5U/96IqpgKzTTzq7saehWRvJ9XcLIYg3B/qJT2kBJTBlRidt+Q==")

# Get some data from a SQL DW table.
df = spark.read \
  .format("com.databricks.spark.sqldw")\
  .option("forward_spark_azure_storage_credentials", "true")\
  .option("url", "jdbc:sqlserver://adwfordatabricksbvb01.database.windows.net:1433;database=adwfordatabricksbvb01;user=testDataBricks@adwfordatabricksbvb01;password=Qwerty123456")\
  .option("tempDir", "wasbs://ct1@bvbdemodb001.blob.core.windows.net/tempDirs")\
  .option("dbTable", "dbo.DimProduct")\
  .load()

# COMMAND ----------

# MAGIC %sql
# MAGIC Select * from  jdbcTable2

# COMMAND ----------

df = sqlContext.range(5).toDF("value")

df.write \
  .format("com.databricks.spark.sqldw") \
  .option("url", "jdbc:sqlserver://adwfordatabricksbvb01.database.windows.net:1433;database=adwfordatabricksbvb01;user=testDataBricks@adwfordatabricksbvb01;password=Qwerty123456") \
  .option("forward_spark_azure_storage_credentials", "true") \
  .option("dbTable", "dbo.fromDB") \
  .option("tempDir", "wasbs://ct1@bvbdemodb001.blob.core.windows.net/tempDirs")\
  .mode("overwrite")\
  .save()

# COMMAND ----------
