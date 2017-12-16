import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class BasicEntry<K, V> implements Map.Entry<K, V> {
    K key;
    V value;

    public BasicEntry(K key) {
        this.key = key;
    }

    public BasicEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K setKey(K key) { this.key = key; return key; }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        this.value = value;
        return this.value;
    }
}

class Room {
    enum RoomType {
        Standard, Deluxe, Suite, Superior;
    }
    private int roomNo;
    private int maxPeople;
    private RoomType roomType;

    public Room(int roomNo, int maxPeople, RoomType roomType) {
        this.roomNo = roomNo;
        this.maxPeople = maxPeople;
        this.roomType = roomType;
    }

    public int getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(int roomNo) {
        this.roomNo = roomNo;
    }

    public int getMaxPeople() {
        return maxPeople;
    }

    public void setMaxPeople(int maxPeople) {
        this.maxPeople = maxPeople;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    @Override
    public String toString() {
        return "방 번호: " + roomNo + ", 수용인원: " + maxPeople + ", 방 유형: " + roomType.toString();
    }

    static boolean addRoom(Connector connector, int roomNo, int maxPeople, String roomType) {
        String query = String.format("INSERT INTO room VALUES(%s, %s, '%s')", roomNo, maxPeople, roomType);
        try {
            return connector.executeQuery(query);
        } catch (SQLException e) {
            return false;
        }

    }

}

class Staff {
    private String name, gender, address, phone;

    public Staff(String name, String gender, String address, String phone) {
        this.name = name;
        this.gender = gender;
        this.address = address;
        this.phone = phone;
    }

    static boolean addStaff(Connector connector, String name, String gender, String phone, String address) {
        String query = String.format("INSERT INTO staff " +
                "SELECT increments, '%s', '%s', '%s', '%s' " +
                "FROM increments " +
                "WHERE table_name='staff' AND column_name='id'", name, gender, phone, address);
        try {
            return connector.executeQuery(query);
        } catch (SQLException e) {
            return false;
        }
    }
}

class Customer {
    private String name, gender, address, phone;

    public Customer(String name, String gender, String address, String phone) {
        this.name = name;
        this.gender = gender;
        this.address = address;
        this.phone = phone;
    }

    static boolean addCustomer(Connector connector, String name, String gender, String address, String phone) {
        String query = String.format(
                "INSERT INTO customer " +
                        "SELECT increments, '%s', '%s', '%s', '%s' " +
                        "FROM increments " +
                        "WHERE table_name='customer' AND column_name='id'", name, gender, phone, address);
        try {
            return connector.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

class HotelManagement {
    Connector connector;


    private void addItem(HashMap<String, Integer> t, String u) {
        if (t.containsKey(u))
            t.replace(u, t.get(u) + 1);
        else
            t.put(u, 1);
    }

    HotelManagement() throws Exception {
        this("localhost", "database", "q1w2e3r4");
    }
    HotelManagement(String addr) throws Exception {
        this(addr, "database", "q1w2e3r4");
    }
    HotelManagement(String addr, String id, String pw) throws Exception {
        boolean isCustomerLoaded = false;
        boolean isStaffLoaded = false;
        boolean isRoomLoaded = false;
        boolean isReservationsLoaded = false;
        boolean isIncrementsLoaded = false;
        int triggerCounts = 0;

        connector = new Connector(addr, id, pw);

        String[][] tables = connector.getQueryResult("SELECT table_name FROM user_tables");
        for (String[] name:
                tables) {
            System.out.println(name[0]);
            switch (name[0].toLowerCase()) {
                case "customer":
                    isCustomerLoaded = true;
                    break;
                case "staff":
                    isStaffLoaded = true;
                    break;
                case "room":
                    isRoomLoaded = true;
                    break;
                case "reservations":
                    isReservationsLoaded = true;
                    break;
                case "increments":
                    isIncrementsLoaded = true;
                    break;
                default:
                    break;
            }
        }

        String[][] triggers = connector.getQueryResult("SELECT trigger_name FROM user_triggers");
        for(int i = 1; i < triggers.length; i++) {
            String triggerName = triggers[i][0].toLowerCase();
            System.out.println(triggerName);
            if (triggerName.equals("addindexcustomer") || triggerName.equals("addindexstaff") || triggerName.equals("checkintegrity")) triggerCounts++;
            if(triggerCounts == 3) break;
        }
        if(!(isCustomerLoaded && isStaffLoaded && isRoomLoaded && isReservationsLoaded)) {
            System.out.println("DB is not initialized properly! Initializing...");
            if (!isIncrementsLoaded)
                Initializer.loadIncrements(connector);
            if (!isCustomerLoaded)
                Initializer.loadCustomer(connector);
            if (!isRoomLoaded)
                Initializer.loadRoom(connector);
            if (!isStaffLoaded)
                Initializer.loadStaff(connector);
            if (!isReservationsLoaded)
                Initializer.loadReservations(connector);
        }
        if(triggerCounts < 3) {
            System.out.println("Trigger not loaded properly! Initializing...");
            Initializer.setTrigger(connector);
        }
    }

    ArrayList<Room> getRooms() throws SQLException {
        String[][] result = connector.getQueryResult("SELECT * FROM room");
        ArrayList<Room> rooms = new ArrayList<>();
        for(int i = 1; i < result.length; i++) {
            Room r = new Room(Integer.valueOf(result[i][0]), Integer.valueOf(result[i][1]), null);
            try {
                r.setRoomType(Room.RoomType.valueOf(result[i][2].substring(0, 1).toUpperCase() + result[i][2].substring(1)));
            } catch (IllegalArgumentException e) {
                return null;
            }
            rooms.add(r);
        }
        return rooms;
    }

    boolean check(String query) {
        try {
            return (connector.getQueryResult(query).length != 1);
        } catch (SQLException e) {
            return false;
        }
    }

    boolean customerExists(String name) throws SQLException {
        return check("SELECT name FROM customer WHERE name = '" + name + "'");
    }

    String makeReservation(String customerId, String date, int nights, int roomNo) throws SQLException {
        String[] staffs = Arrays.stream(connector.getQueryResult("SELECT id FROM staff")).skip(1).map(t -> t[0]).toArray(String[]::new);
        if(staffs.length == 1)
            return "E:NO_STAFF";
        System.out.println(Arrays.toString(staffs));
        int index =(int) Math.floor(Math.random() * staffs.length);
        System.out.println(index);
        String staff = staffs[index];

        connector.executeQuery(String.format("INSERT INTO reservations VALUES('%s', TO_DATE('%s', 'YYYYMMDD'), %s, %s, '%s')", customerId, date, nights, roomNo, staff));

        return "I:DONE";
    }

    // TODO: 투숙 정보 없이 이름만 가지고 예약 취소 가능케 하기 - 1인 1예약만 가능하니까
    int cancelReservation(String roomNo, String date) throws SQLException {
        return connector.executeUpdate(String.format("DELETE FROM reservations WHERE customer_id='%s' AND start_date=TO_DATE('%s', 'YYYYMMDD')", roomNo, date));

    }


    boolean[] getReservations() throws SQLException {
        return getReservations(new Date(System.currentTimeMillis()));
    }

    boolean[] getReservations(Date date) throws SQLException {
        boolean[] rooms = new boolean[20];

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String toDate = "to_date('" + sdf.format(date) + "', 'YYYYMMDD')";
        String query = String.format("SELECT room_no FROM reservations WHERE start_date <= %s AND (start_date + nights) > %s", toDate, toDate);

        String[][] result = connector.getQueryResult(query);
        if(result.length != 1)
            Arrays.stream(result).skip(1).forEach(row -> rooms[Integer.parseInt(row[0]) - ((Integer.parseInt(row[0]) < 200) ? 101 : 191)] = true);
        return rooms;
    }
    String[][] getCustomersByName(String name) throws SQLException {
        String query = String.format("SELECT * FROM customer WHERE name = '%s'", name);
        return connector.getQueryResult(query);
    }


    String[][] getStaffsByName(String name) throws SQLException {
        String query = String.format("SELECT * FROM staff WHERE name = '%s'", name);
        return connector.getQueryResult(query);
    }


    String queryCustomerById(String id) throws SQLException {
        String infoQuery = String.format("SELECT name, gender, address, phone, id FROM CUSTOMER WHERE id = '%s'", id);
        String[][] result = connector.getQueryResult(infoQuery);
        if(result.length == 1)
            return "E:NO_SUCH_CUSTOMER";
        String[] row = result[1];

        String nightsQuery = String.format("SELECT to_char(start_date, 'YYYYMMDD') , nights, staff.name, staff.id FROM reservations, staff WHERE reservations.staff_id = staff.id AND  customer_id = '%s'", id);
        String[][] nightsResult = connector.getQueryResult(nightsQuery);
        HashMap<String, Integer> staffs = new HashMap<>();
        int nights = 0;
        String recentStart = "00000000";

        if(nightsResult.length != 1) {
            nightsResult = Arrays.copyOfRange(nightsResult, 1, nightsResult.length);
            for(String[] nightsRow: nightsResult) {
                nights += Integer.parseInt(nightsRow[1]);
                addItem(staffs, nightsRow[3]);
                System.out.println(nightsRow[3]);
                if(Integer.parseInt(recentStart) < Integer.parseInt(nightsRow[0]))
                    recentStart = nightsRow[0];
            }
        }

        if(recentStart.equals("00000000"))
            recentStart = "없음";

        BasicEntry<String, Integer> staffInfo = new BasicEntry<>("" ,0);
        if(!staffs.isEmpty()) {
            Map.Entry<String, Integer> t = staffs.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue)).get();
            staffInfo.setKey(connector.getQueryResult("SELECT name FROM staff WHERE id=" + t.getKey())[1][0]);
            staffInfo.setValue(t.getValue());
        }


        String desc = "고객명: %s\n성별: %s\n주소: %s\n연락처: %s\n총 투숙기간: %s\n최근 투숙일:%s\n 객실전담직원(최다): %s\t\t(%s회)";
        desc = String.format(desc, row[0], row[1], row[2], row[3], nights, recentStart, staffInfo.getKey(), staffInfo.getValue());

        return desc;
    }

    String queryRoom(int roomNo) throws SQLException {
        String infoQuery = String.format("SELECT * FROM room WHERE room_no = '%s'", roomNo);
        String[][] result = connector.getQueryResult(infoQuery);

        if(result.length == 1) return "E:NO_SUCH_ROOM";
        String[] row = result[1];

        String nightsQuery = String.format("SELECT to_char(start_date, 'YYYYMMDD') , nights, staff.name, customer.name, staff_id, customer_id FROM reservations, customer, staff WHERE reservations.customer_id = customer.id AND staff.id = reservations.staff_id AND room_no = %s", roomNo);
        String[][] nightsResult = connector.getQueryResult(nightsQuery);
        boolean isVacant = true;
        HashMap<String, Integer> customers = new HashMap<>();
        HashMap<String, Integer> staffs = new HashMap<>();


        Date today = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        if(nightsResult.length != 1) {
            try {
                nightsResult = Arrays.copyOfRange(nightsResult, 1, nightsResult.length);
                for (String[] nightsRow : nightsResult) {
                    System.out.println(Arrays.toString(nightsRow));
                    addItem(staffs, nightsRow[4]);
                    addItem(customers, nightsRow[5]);
                    Calendar d = new GregorianCalendar();
                    d.setTime(sdf.parse(nightsRow[0]));
                    if (isVacant && Integer.parseInt(nightsRow[0]) <= Integer.parseInt(sdf.format(today))) {
                        d.add(Calendar.DATE, Integer.parseInt(nightsRow[1]));
                        if(Integer.parseInt(nightsRow[0]) >= Integer.parseInt(sdf.format(today)))
                            isVacant = false;
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        System.out.println(staffs.isEmpty());
        System.out.println(customers.isEmpty());
        BasicEntry<String, Integer> staffInfo = new BasicEntry<>("", 0);
        if(!staffs.isEmpty()) {
            Map.Entry<String, Integer> t = staffs.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue)).get();
            staffInfo.setKey(connector.getQueryResult("SELECT name FROM staff WHERE id=" + t.getKey())[1][0]);
            staffInfo.setValue(t.getValue());
        }
        BasicEntry<String, Integer> customerInfo = new BasicEntry<>("", 0);
        if(!customers.isEmpty()) {
            Map.Entry<String, Integer> t2 = customers.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue)).get();
            customerInfo.setKey(connector.getQueryResult("SELECT name FROM customer WHERE id=" + t2.getKey())[1][0]);
            customerInfo.setValue(t2.getValue());
        }

        String desc = "객실번호: %s\n수용인원: %s\n타입: %s\n상태: %s\n투숙고객(최다): %s\t\t(%s회)\n객실전담직원(최다): %s\t\t(%s회)";
        desc = String.format(desc, roomNo, row[1], row[2], isVacant ? "비어있음" : "투숙중", customerInfo.getKey(), customerInfo.getValue(), staffInfo.getKey(), staffInfo.getValue());
        return desc;
    }

    boolean queryRoomStatus(int roomNo) throws SQLException {
        String infoQuery = String.format("SELECT * FROM room WHERE room_no = '%s'", roomNo);
        String[][] result = connector.getQueryResult(infoQuery);

        if(result.length == 1) throw new SQLException("No such room");

        String nightsQuery = String.format("SELECT to_char(start_date, 'YYYYMMDD'), nights, staff, customer.name customer FROM reservations, customer WHERE reservations.customer_id = customer.id AND  room_no = %s", roomNo);
        String[][] nightsResult = connector.getQueryResult(nightsQuery);
        boolean isVacant = true;

        Calendar today = new GregorianCalendar();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        if(nightsResult.length != 1) {
            try {
                for (String[] nightsRow : nightsResult) {
                    Calendar d = new GregorianCalendar();
                    d.setTime(sdf.parse(nightsRow[0]));
                    if (isVacant && Integer.parseInt(sdf.format(d)) <= Integer.parseInt(sdf.format(today))) {
                        d.add(Calendar.DATE, Integer.parseInt(nightsRow[1]));
                        if(Integer.parseInt(sdf.format(d)) >= Integer.parseInt(sdf.format(today)))
                            isVacant = false;
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return isVacant;
    }

    String queryStaffById(String id) throws SQLException {
        String infoQuery = String.format("SELECT id, name, gender, address, phone FROM staff WHERE id = '%s'", id);
        String[][] result = connector.getQueryResult(infoQuery);

        if(result.length == 1) return "E:NO_SUCH_STAFF";
        String[] row = result[1];

        String nightsQuery = String.format("SELECT customer.id, room_no FROM reservations, customer, staff WHERE reservations.customer_id = customer.id AND reservations.staff_id = staff.id AND staff_id = '%s'", id);
        String[][] nightsResult = connector.getQueryResult(nightsQuery);
        HashMap<String, Integer> customers = new HashMap<>();
        HashMap<String, Integer> rooms = new HashMap<>();

        if(nightsResult.length != 1) {
            for(String[] nightsRow: Arrays.copyOfRange(nightsResult, 1, nightsResult.length)) {
                System.out.println(Arrays.toString(nightsRow));
                addItem(customers, nightsRow[0]);
                addItem(rooms, nightsRow[1]);
            }
        }
        Map.Entry<String, Integer> roomInfo = new BasicEntry<>("", 0);
        if(!rooms.isEmpty()) roomInfo =  rooms.entrySet().stream().max((p1, p2) -> p2.getValue().compareTo(p1.getValue())).get();

        BasicEntry<String, Integer> customerInfo = new BasicEntry<>("", 0);
        if(!customers.isEmpty()) {
            Map.Entry<String, Integer> t = customers.entrySet().stream().max((p1, p2) -> p2.getValue().compareTo(p1.getValue())).get();
            customerInfo.setKey(connector.getQueryResult("SELECT name FROM customer WHERE id=" + t.getKey())[1][0]);
            customerInfo.setValue(t.getValue());
        }
        String desc = "직원명: %s\n성별: %s\n주소: %s\n연락처: %s\n접대 고객(최다): %s\n관리 객실(최다): %s\n";
        desc = String.format(desc, row[1], row[2], row[3], row[4], customerInfo.getKey(), customerInfo.getValue(), roomInfo.getKey(), roomInfo.getValue());

        return desc;
    }
}

