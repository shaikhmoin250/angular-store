# Spark Course Activity Pipeline

This directory contains a PySpark job for processing user interaction logs on an online learning platform.

## Overview

The `course_activity_job.py` script reads raw JSON logs from `/mnt/events/user_logs.json`, filters relevant events, aggregates the total time users spend per course each day, and writes the result to `/mnt/analytics/course_activity` in Parquet format.

A schema is provided for expected fields. The job uses the `mergeSchema` option in permissive mode so additional unexpected fields in the JSON files are handled without errors.

## Running the Job

```bash
spark-submit course_activity_job.py
```
