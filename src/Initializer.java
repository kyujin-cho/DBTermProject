import java.sql.SQLException;

class Initializer {
    static void loadCustomer(Connector conn) throws SQLException {
        conn.executeQuery("CREATE TABLE customer (\n" +
                "\tid\t\tnumber(20),\n" +
                "\tname\tvarchar(20),\n" +
                "\tgender\tvarchar(10),\n" +
                "\taddress\tvarchar(20),\n" +
                "\tphone\tvarchar(20),\n" +
                "\tPRIMARY KEY (id)\n" +
                ")");
        conn.executeQuery("INSERT INTO increments(table_name, column_name)\n" +
                "\tVALUES('customer', 'id')");
    }

    static void loadRoom(Connector conn) throws SQLException {
        conn.executeQuery("CREATE TABLE room (\n" +
                "\troom_no\t\tnumber(4),\n" +
                "\tmax_people\tnumber(3),\n" +
                "\troom_type\tvarchar(10),\n" +
                "\tPRIMARY KEY (room_no)\n" +
                ")");
    }

    static void loadStaff(Connector conn) throws SQLException {
        conn.executeQuery("CREATE TABLE staff(\n" +
                "\tid\t\tnumber(20),\n" +
                "\tname\tvarchar(20),\n" +
                "\tgender\tvarchar(10),\n" +
                "\taddress\tvarchar(20),\n" +
                "\tphone\tvarchar(20),\n" +
                "\tPRIMARY KEY (id)\n" +
                ")");
        conn.executeQuery("INSERT INTO increments(table_name, column_name)\n" +
                "\tVALUES('staff', 'id')");
    }

    static void loadReservations(Connector conn) throws SQLException {
        conn.executeQuery("CREATE TABLE reservations(\n" +
                "\tcustomer_id\t\tnumber(20),\n" +
                "\tstart_date\tdate,\n" +
                "\tnights\t\tnumber(10),\n" +
                "\troom_no\t\tnumber(4),\n" +
                "\tstaff_id\t\tnumber(20),\n" +
                "\tPRIMARY KEY (room_no, start_date)\n," +
                "\tFOREIGN KEY (customer_id) REFERENCES customer(id),\n" +
                "\tFOREIGN KEY (staff_id) REFERENCES staff(id)\n" +
                ")");

    }

    static void loadIncrements(Connector conn) throws SQLException {
        conn.executeQuery("CREATE TABLE increments(\n" +
                "\ttable_name\tvarchar(20),\n" +
                "\tcolumn_name\tvarchar(20),\n" +
                "\tincrements\tnumber(20) DEFAULT 1,\n" +
                "\tPRIMARY KEY (table_name, column_name)\n" +
                ")"
        );
    }

    static void setTrigger(Connector conn) throws SQLException {
        conn.executeQuery("CREATE OR REPLACE TRIGGER addIndexCustomer\n" +
                "\tAFTER INSERT ON customer\n" +
                "\tREFERENCING new AS newRow \n" +
                "BEGIN\n" +
                "\tUPDATE increments \n" +
                "\tSET increments= increments + 1\n" +
                "\tWHERE table_name='customer' AND column_name='id';\n" +
                "END;\n" +
                "\n");
        conn.executeQuery("CREATE OR REPLACE TRIGGER addIndexStaff\n" +
                "\tAFTER INSERT ON staff\n" +
                "\tREFERENCING NEW AS newRow \n" +
                "BEGIN\n" +
                "\tUPDATE increments \n" +
                "\tSET increments= increments + 1\n" +
                "\tWHERE table_name='staff' AND column_name='id';\n" +
                "END;\n" +
                "\n");

        conn.executeQuery("CREATE OR REPLACE TRIGGER checkIntegrity\n" +
                "\tBEFORE INSERT \n" +
                "\tON reservations\n" +
                "\tFOR EACH ROW\n" +
                "DECLARE\n" +
                "\tv_count NUMBER(20);\n" +
                "\tv2_count NUMBER(20);\n" +
                "\tv3_count NUMBER(20);\n" +
                "\tPRAGMA AUTONOMOUS_TRANSACTION;\n" +
                "BEGIN\n" +
                "\tSELECT count(*) INTO v_count FROM reservations \n" +
                "\tWHERE (room_no = new.room_no) AND (\n" +
                "\t\t(start_date > new.start_date AND (new.start_date + new.nights) > start_date) OR\n" +
                "\t\t(start_date = new.start_date) OR\n" +
                "\t\t(start_date < new.start_date AND (start_date + nights) > new.start_date)\n" +
                "\t);\n" +
                "\tSELECT count(*) INTO v2_count FROM reservations \n" +
                "\tWHERE\n" +
                "\t\t(customer_id = new.customer_id) AND(\n" +
                "\t\t\t(start_date > new.start_date AND (new.start_date + new.nights) > start_date) OR\n" +
                "\t\t\t(start_date = new.start_date) OR\n" +
                "\t\t\t(start_date < new.start_date AND (start_date + nights) > new.start_date)\n" +
                "\t\t)\n" +
                "\t;\n" +
                "\tdbms_output.put_line(new.start_date);\n" +
                "\tdbms_output.put_line(v_count);\n" +
                "\tdbms_output.put_line(v2_count);\n" +
                "\tIF v2_count >= 1 THEN\n" +
                "\t\tSELECT count(*) INTO v3_count FROM reservations\n" +
                "\t\t\tWHERE (room_no = new.room_no AND customer_id != new.customer_id) AND (\n" +
                "\t\t\t\t(start_date > new.start_date AND (new.start_date + new.nights) > start_date) OR\n" +
                "\t\t\t\t(start_date = new.start_date) OR\n" +
                "\t\t\t\t(start_date < new.start_date AND (start_date + nights) > new.start_date)\n" +
                "\t\t\t);\n" +
                "\t\t\tdbms_output.put_line(v3_count);\n" +
                "\t\tIF v3_count >= 1 THEN\n" +
                "\t\t\tRAISE_APPLICATION_ERROR(-20000, 'INTEGRITY_VIOLATE');\n" +
                "\t\t\trollback;\n" +
                "\t\tELSE\n" +
                "\t\t\tUPDATE reservations SET \n" +
                "\t\t\t\tstart_date = new.start_date, \n" +
                "\t\t\t\tnights = new.nights, \n" +
                "\t\t\t\troom_no = new.room_no \n" +
                "\t\t\tWHERE \n" +
                "\t\t\t\t ((customer_id = new.customer_id) AND(\n" +
                "\t\t\t\t\t(start_date > new.start_date AND (new.start_date + new.nights) > start_date) OR\n" +
                "\t\t\t\t\t(start_date = new.start_date) OR\n" +
                "\t\t\t\t\t(start_date > new.start_date AND (new.start_date + new.nights) > start_date)\n" +
                "\t\t\t));\n" +
                "\t\t\tcommit;\n" +
                "\t\t\tRAISE_APPLICATION_ERROR(-20001, 'RESERVATION_EDIT');\n" +
                "\t\tEND IF;\n" +
                "\tELSIF v_count >= 1 THEN\n" +
                "\t\tRAISE_APPLICATION_ERROR(-20000, 'INTEGRITY_VIOLATE');\n" +
                "\t\trollback;\t\t\n" +
                "\tEND IF;\n" +
                "END");
    }
}

