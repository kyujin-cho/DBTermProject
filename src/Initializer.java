import java.sql.SQLException;

class Initializer {
    static void loadCustomer(Connector conn) throws SQLException {
        conn.executeQuery("CREATE TABLE customer (\n" +
                "\tname\tvarchar(20),\n" +
                "\tgender\tvarchar(10),\n" +
                "\taddress\tvarchar(20),\n" +
                "\tphone\tvarchar(20),\n" +
                "\tPRIMARY KEY (name)\n" +
                ")");
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
                "\tname\tvarchar(20),\n" +
                "\tgender\tvarchar(10),\n" +
                "\taddress\tvarchar(20),\n" +
                "\tphone\tvarchar(20),\n" +
                "\tPRIMARY KEY (name)\n" +
                ")");
    }

    static void loadReservations(Connector conn) throws SQLException {
        conn.executeQuery("CREATE TABLE reservations(\n" +
                "\tcustomer\tvarchar(20),\n" +
                "\tstart_date\tdate,\n" +
                "\tnights\t\tnumber(10),\n" +
                "\troom_no\t\tnumber(4),\n" +
                "\tstaff\t\tvarchar(20)," +
                "\tPRIMARY KEY (customer, start_date)\n," +
                "\tFOREIGN KEY (customer) REFERENCES customer,\n" +
                "\tFOREIGN KEY (staff) REFERENCES staff" +
                ")");

    }
}

