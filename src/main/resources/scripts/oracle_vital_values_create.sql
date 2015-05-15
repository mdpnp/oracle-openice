CREATE TABLE IF NOT EXISTS "OPENICE"."VITAL_VALUES_ORA"
(
   ID_VITAL_VALUES decimal(10,0),
   DEVICE_ID varchar2(255),
   METRIC_ID varchar2(255),
   INSTANCE_ID decimal(10,0),
   TIME_TICK timestamp,
   VITAL_VALUE decimal(10,0)
);

ALTER TABLE VITAL_VALUES_ORA ADD (
  CONSTRAINT vital_values_ora_pk PRIMARY KEY (ID_VITAL_VALUES));

CREATE SEQUENCE vital_values_ora_seq;

/***/

CREATE OR REPLACE TRIGGER OPENICE.VITAL_VALUES_ORA_bi
BEFORE INSERT ON VITAL_VALUES_ORA 
FOR EACH ROW

BEGIN
  SELECT vital_values_ora_seq.NEXTVAL
  INTO   :new.ID_VITAL_VALUES
  FROM   dual;
END;
/