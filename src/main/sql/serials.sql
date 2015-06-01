create or replace view serials as
with indexed_samples as 
(select 
ROW_NUMBER() OVER (PARTITION BY DEVICEIDENTIT_ID ORDER BY SOURCE_TIME) as rn, 
DEVICEIDENTIT_SAMPLE.*
from DEVICEIDENTIT_SAMPLE)
select 
deviceidentit.unique_device_identifier,
before.source_time as start_time,
after.source_time as end_time,
before.serial_number
from
indexed_samples before
inner join deviceidentit on before.deviceidentit_id = deviceidentit.deviceidentit_id
left outer join indexed_samples after on before.deviceidentit_id = after.deviceidentit_id and before.rn + 1 = after.rn
;


