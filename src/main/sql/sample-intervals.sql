-- Name this useful subquery that provides a sequential numbering of the samples
-- for each numeric ordered by SOURCE_TIME
WITH SAMPLES AS (
SELECT NUMERIC_ID, ROW_NUMBER() OVER (PARTITION BY NUMERIC_ID ORDER BY SOURCE_TIME) AS RN, NUMERIC_SAMPLE_ID, SOURCE_TIME
FROM (SELECT MIN(NUMERIC_SAMPLE_ID) AS NUMERIC_SAMPLE_ID, NUMERIC_ID, SOURCE_TIME FROM NUMERIC_SAMPLE
      GROUP BY NUMERIC_ID, SOURCE_TIME) -- Unique samples (sadly currently creating duplicates)
)
-- For each numeric instance find the overall avg/min/max interval between samples
SELECT 
 UNIQUE_DEVICE_IDENTIFIER,
 METRIC_ID,
 INSTANCE_ID,
 AVG(interval) as avg_interval,
 MIN(interval) as min_interval,
 MAX(interval) as max_interval,
 COUNT(interval) as cnt_interval
 FROM
 (
 -- For each sample->sample interval computes the difference in SOURCE_TIME
 -- as milliseconds
 SELECT 
  UNIQUE_DEVICE_IDENTIFIER,
  METRIC_ID,
  INSTANCE_ID,
  EXTRACT( day from DIFF )*24*60*60*1000 +
  EXTRACT( hour from DIFF )*60*60*1000 +
  EXTRACT( minute from DIFF )*60*1000 +
  ROUND(EXTRACT( second from DIFF )*1000) interval
  FROM 
  (
   -- This inside query joins the previous sample with the next sample
   -- for convenience we also compute a difference in SOURCE_TIME
   SELECT 
    NUMERIC.UNIQUE_DEVICE_IDENTIFIER, 
    NUMERIC.METRIC_ID, 
    NUMERIC.INSTANCE_ID,
    BEFORE.SOURCE_TIME AS START_TIME,
    AFTER.SOURCE_TIME AS END_TIME,
    AFTER.SOURCE_TIME - BEFORE.SOURCE_TIME AS DIFF
    FROM SAMPLES BEFORE
    INNER JOIN NUMERIC ON BEFORE.NUMERIC_ID = NUMERIC.NUMERIC_ID
    INNER JOIN SAMPLES AFTER ON BEFORE.NUMERIC_ID = AFTER.NUMERIC_ID AND BEFORE.RN = (AFTER.RN - 1)
  )
 )
 -- This is a crude way to eliminate long gaps between runs of the data collector
 -- service.
 WHERE interval < 5000
 GROUP BY
  UNIQUE_DEVICE_IDENTIFIER,
  METRIC_ID,
  INSTANCE_ID
 ORDER BY 
  UNIQUE_DEVICE_IDENTIFIER,
  METRIC_ID,
  INSTANCE_ID;
 
 
