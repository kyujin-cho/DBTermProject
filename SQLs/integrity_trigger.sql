CREATE OR REPLACE TRIGGER checkIntegrity
BEFORE INSERT
  ON reservations
FOR EACH ROW
  DECLARE
    v_count NUMBER(20);
    v2_count NUMBER(20);
    v3_count NUMBER(20);
    PRAGMA AUTONOMOUS_TRANSACTION;
  BEGIN
    SELECT count(*) INTO v_count FROM reservations
    WHERE (room_no = :new.room_no) AND (
      (start_date > :new.start_date AND (:new.start_date + :new.nights) > start_date) OR
      (start_date = :new.start_date) OR
      (start_date < :new.start_date AND (start_date + nights) > :new.start_date)
    );
    SELECT count(*) INTO v2_count FROM reservations
    WHERE
      (customer_id = :new.customer_id) AND(
        (start_date > :new.start_date AND (:new.start_date + :new.nights) > start_date) OR
        (start_date = :new.start_date) OR
        (start_date < :new.start_date AND (start_date + nights) > :new.start_date)
      )
    ;
    dbms_output.put_line(:new.start_date);
    dbms_output.put_line(v_count);
    dbms_output.put_line(v2_count);
    IF v2_count >= 1 THEN
      SELECT count(*) INTO v3_count FROM reservations
      WHERE (room_no = :new.room_no AND customer_id != :new.customer_id) AND (
        (start_date > :new.start_date AND (:new.start_date + :new.nights) > start_date) OR
        (start_date = :new.start_date) OR
        (start_date < :new.start_date AND (start_date + nights) > :new.start_date)
      );
      dbms_output.put_line(v3_count);
      IF v3_count >= 1 THEN
        RAISE_APPLICATION_ERROR(-20000, 'INTEGRITY_VIOLATE');
        rollback;
      ELSE
        UPDATE reservations SET
          start_date = :new.start_date,
          nights = :new.nights,
          room_no = :new.room_no
        WHERE
          ((customer_id = :new.customer_id) AND(
            (start_date > :new.start_date AND (:new.start_date + :new.nights) > start_date) OR
            (start_date = :new.start_date) OR
            (start_date > :new.start_date AND (:new.start_date + :new.nights) > start_date)
          ));
        commit;
        RAISE_APPLICATION_ERROR(-20001, 'RESERVATION_EDIT');
      END IF;
    ELSIF v_count >= 1 THEN
      RAISE_APPLICATION_ERROR(-20000, 'INTEGRITY_VIOLATE');
      rollback;
    END IF;
  END;