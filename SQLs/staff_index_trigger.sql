CREATE OR REPLACE TRIGGER ADDINDEXSTAFF
AFTER INSERT ON CUSTOMER
REFERENCING new AS newRow
  BEGIN
    UPDATE INCREMENTS
    SET INCREMENTS=INCREMENTS+1
    WHERE
      TABLE_NAME='staff' AND COLUMN_NAME='id'
    ;
  END;