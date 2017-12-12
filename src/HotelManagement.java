import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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


}

class Staff {
    private String name, gender, address, phone;

    public Staff(String name, String gender, String address, String phone) {
        this.name = name;
        this.gender = gender;
        this.address = address;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    static boolean addStaff(Connector connector, String name, String gender, String phone, String address) {
        String query = String.format("INSERT INTO staff VALUES('%s', '%s', '%s', '%s')", name, gender, phone, address);
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    static boolean addCustomer(Connector connector, String name, String gender, String address, String phone) {
        String query = String.format("INSERT INTO customer VALUES('%s', '%s', '%s', '%s')", name, gender, phone, address);
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

    HotelManagement() throws SQLException {
        this("localhost", "database", "q1w2e3r4");
    }
    HotelManagement(String addr) throws SQLException {
        this(addr, "database", "q1w2e3r4");
    }
    HotelManagement(String addr, String id, String pw) throws SQLException {
        boolean isCustomerLoaded = false;
        boolean isStaffLoaded = false;
        boolean isRoomLoaded = false;
        boolean isReservationsLoaded = false;

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
                default:
                    break;
            }
        }

        if(!(isCustomerLoaded && isStaffLoaded && isRoomLoaded && isReservationsLoaded)) {
            System.out.println("DB is not initialized properly! Initializing...");
            if (!isCustomerLoaded)
                Initializer.loadCustomer(connector);
            if (!isRoomLoaded)
                Initializer.loadRoom(connector);
            if (!isStaffLoaded)
                Initializer.loadStaff(connector);
            if (!isReservationsLoaded)
                Initializer.loadReservations(connector);
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
            return !(connector.getQueryResult(query).length == 1);
        } catch (SQLException e) {
            return false;
        }
    }

    boolean customerExists(String name) throws SQLException {
        return check("SELECT name FROM customer WHERE name = '" + name + "'");
    }

    String makeReservation(String name, String date, int nights, int roomNo) throws SQLException {
        try {
            if(!customerExists(name))
                return "E:NO_SUCH_CUSTOMER";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Calendar startDate = new GregorianCalendar();
            startDate.setTime(sdf.parse(date));

            Calendar endDate = new GregorianCalendar();
            endDate.setTime(sdf.parse(date));
            endDate.add(Calendar.DATE, nights);
            String startDateString = "TO_DATE('" + date + "', 'YYYYMMDD')";

            String[] checks = new String[] {
                    String.format("SELECT * FROM reservations " +
                            "WHERE (room_no = %s OR customer = '%s') AND " +
                            "((start_date > %s AND (start_date - %s < %s)) OR " +
                            "(start_date < %s AND (%s - start_date > %s)) OR " +
                            "(start_date = %s))", roomNo, name, startDateString, startDateString, nights, startDateString, startDateString, nights, startDateString)
            };
            Arrays.stream(checks).forEach(System.out::println);

            if(Arrays.stream(checks).anyMatch(this::check))
                return "E:CHECK_FAIL";

            String[] staffs = Arrays.stream(connector.getQueryResult("SELECT name FROM staff")).skip(1).map(t -> t[0]).toArray(String[]::new);
            String staff = staffs[(int)(Math.random() * staffs.length)];

            connector.executeQuery(String.format("INSERT INTO reservations VALUES('%s', TO_DATE('%s', 'YYYYMMDD'), %s, %s, '%s')", name, date, nights, roomNo, staff));

            return "I:DONE";
        } catch (ParseException e) {
            e.printStackTrace();
            return "E:PARSE_EXCEPTION";
        }

    }

    // TODO: 투숙 정보 없이 이름만 가지고 예약 취소 가능케 하기 - 1인 1예약만 가능하니까
    String cancelReservation(String name, String date, int nights, int roomNo) throws SQLException {
        String query = String.format("SELECT * FROM reservations WHERE customer='%s' AND start_date=TO_DATE('%s', 'YYYYMMDD') AND NIGHTS=%s AND room_no=%s", name, date, nights, roomNo);
        String[][] result = connector.getQueryResult(query);
        if(result.length == 1)
            return "E:NO_SUCH_RESERVATION";
        connector.executeQuery(String.format("DELETE FROM reservations WHERE customer='%s' AND start_date=TO_DATE('%s', 'YYYYMMDD') AND NIGHTS=%s AND room_no=%s", name, date, nights, roomNo));
        return "I:DONE";
    }

    boolean[] getReservations() throws SQLException {
        return getReservations(new Date(System.currentTimeMillis()));
    }

    boolean[] getReservations(Date date) throws SQLException {
        boolean[] rooms = new boolean[20];

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String toDate = "to_date('" + sdf.format(date) + "', 'YYYYMMDD')";
        String query = String.format("SELECT room_no FROM reservations WHERE start_date <= %s AND (start_date + nights) >= %s", toDate, toDate);

        String[][] result = connector.getQueryResult(query);
        if(result.length != 1)
            Arrays.stream(result).skip(1).forEach(row -> rooms[Integer.parseInt(row[0]) - ((Integer.parseInt(row[0]) < 200) ? 101 : 191)] = true);
        return rooms;
    }

    String queryCustomer(String name) throws SQLException {
        String infoQuery = String.format("SELECT * FROM CUSTOMER WHERE name = '%s'", name);
        String[][] result = connector.getQueryResult(infoQuery);

        if(result.length == 1) return "E:NO_SUCH_CUSTOMER";
        String[] row = result[1];

        String nightsQuery = String.format("SELECT to_char(start_date, 'YYYYMMDD') , nights, staff FROM reservations WHERE customer = '%s'", name);
        String[][] nightsResult = connector.getQueryResult(nightsQuery);
        HashMap<String, Integer> staffs = new HashMap<>();
        int nights = 0;
        String recentStart = "00000000";

        if(nightsResult.length != 1) {
            nightsResult = Arrays.copyOfRange(nightsResult, 1, nightsResult.length);
            for(String[] nightsRow: nightsResult) {
                nights += Integer.parseInt(nightsRow[1]);
                addItem(staffs, nightsRow[2]);
                if(Integer.parseInt(recentStart) < Integer.parseInt(nightsRow[0]))
                    recentStart = nightsRow[0];
            }
        }

        if(recentStart.equals("00000000"))
            recentStart = "없음";

        Map.Entry<String, Integer> staffInfo = new BasicEntry<>("" ,0);
        if(!staffs.isEmpty()) staffInfo = staffs.entrySet().stream().max((p1, p2) -> p2.getValue().compareTo(p1.getValue())).get();


        String desc = "고객명: %s\n성별: %s\n주소: %s\n연락처: %s\n총 투숙기간: %s\n최근 투숙일:%s\n 객실전담직원(최다): %s\t\t(%s회)";
        desc = String.format(desc, row[0], row[1], row[2], row[3], nights, recentStart, staffInfo.getKey(), staffInfo.getValue());

        return desc;
    }

    String queryRoom(int roomNo) throws SQLException {
        String infoQuery = String.format("SELECT * FROM room WHERE room_no = '%s'", roomNo);
        String[][] result = connector.getQueryResult(infoQuery);

        if(result.length == 1) return "E:NO_SUCH_ROOM";
        String[] row = result[1];

        String nightsQuery = String.format("SELECT to_char(start_date, 'YYYYMMDD') , nights, staff, customer FROM reservations WHERE room_no = %s", roomNo);
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
                    addItem(staffs, nightsRow[2]);
                    addItem(customers, nightsRow[3]);
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

        Map.Entry<String, Integer> staffInfo = new BasicEntry<>("", 0);
        if(!staffs.isEmpty()) staffs.entrySet().stream().max((p1, p2) -> p2.getValue().compareTo(p1.getValue())).get();
        Map.Entry<String, Integer> customerInfo = new BasicEntry<>("", 0);
        if(!customers.isEmpty()) customers.entrySet().stream().max((p1, p2) -> p2.getValue().compareTo(p1.getValue())).get();

        String desc = "객실번호: %s\n수용인원: %s\n타입: %s\n상태: %s\n투숙고객(최다): %s\t\t(%s회)\n객실전담직원(최다): %s\t\t(%s회)";
        desc = String.format(desc, roomNo, row[1], row[2], isVacant ? "비어있음" : "투숙중", customerInfo.getKey(), customerInfo.getValue(), staffInfo.getKey(), staffInfo.getValue());
        return desc;
    }

    boolean queryRoomStatus(int roomNo) throws SQLException {
        String infoQuery = String.format("SELECT * FROM room WHERE room_no = '%s'", roomNo);
        String[][] result = connector.getQueryResult(infoQuery);

        if(result.length == 1) throw new SQLException("No such room");
        String[] row = result[1];

        String nightsQuery = String.format("SELECT to_char(start_date, 'YYYYMMDD') , nights, staff, customer FROM reservations WHERE room_no = %s", roomNo);
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

    String queryStaff(String name) throws SQLException {
        String infoQuery = String.format("SELECT * FROM staff WHERE name = '%s'", name);
        String[][] result = connector.getQueryResult(infoQuery);

        if(result.length == 1) return "E:NO_SUCH_STAFF";
        String[] row = result[1];

        String nightsQuery = String.format("SELECT customer, room_no FROM reservations WHERE staff = '%s'", name);
        String[][] nightsResult = connector.getQueryResult(nightsQuery);

        HashMap<String, Integer> customers = new HashMap<>();
        HashMap<String, Integer> rooms = new HashMap<>();

        if(nightsResult.length != 1) {
            for(String[] nightsRow: nightsResult) {
                addItem(customers, nightsRow[0]);
                addItem(rooms, nightsRow[1]);
            }
        }

        Map.Entry<String, Integer> customerInfo = new BasicEntry<>("", 0);
        if(!customers.isEmpty()) customers.entrySet().stream().max((p1, p2) -> p2.getValue().compareTo(p1.getValue())).get();
        Map.Entry<String, Integer> roomInfo = new BasicEntry<>("", 0);
        if(!rooms.isEmpty()) rooms.entrySet().stream().max((p1, p2) -> p2.getValue().compareTo(p1.getValue())).get();

        String desc = "직원명: %s\n성별: %s\n주소: %s\n연락처: %s\n접대 고객(최다): %s\n관리 객실(최다): %s\n";
        desc = String.format(desc, row[0], row[1], row[2], row[3], customerInfo.getKey(), customerInfo.getValue(), roomInfo.getKey(), roomInfo.getValue());

        return desc;
    }
}

