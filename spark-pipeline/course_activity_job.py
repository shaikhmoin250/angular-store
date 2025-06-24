from pyspark.sql import SparkSession
from pyspark.sql.functions import col, to_date, sum as _sum
from pyspark.sql.types import StructType, StructField, StringType, TimestampType, IntegerType


def main():
    spark = SparkSession.builder.appName("CourseActivityPipeline").getOrCreate()

    schema = StructType([
        StructField("user_id", StringType(), True),
        StructField("event_type", StringType(), True),
        StructField("timestamp", TimestampType(), True),
        StructField("course_id", StringType(), True),
        StructField("duration_minutes", IntegerType(), True),
    ])

    # mergeSchema allows new fields in the JSON without failing the load
    raw_df = (
        spark.read
        .schema(schema)
        .option("mode", "PERMISSIVE")
        .option("mergeSchema", "true")
        .json("/mnt/events/user_logs.json")
    )

    filtered_df = (
        raw_df.filter(col("event_type").isin("lesson_completed", "quiz_attempted"))
        .filter(col("user_id").isNotNull())
        .filter(col("course_id").isNotNull())
        .withColumn("activity_date", to_date(col("timestamp")))
    )

    result_df = (
        filtered_df.groupBy("course_id", "activity_date")
        .agg(_sum("duration_minutes").alias("total_duration_minutes"))
    )

    result_df.write.mode("overwrite").parquet("/mnt/analytics/course_activity")

    spark.stop()


if __name__ == "__main__":
    main()
