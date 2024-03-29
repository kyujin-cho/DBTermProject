import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

class RoomStatus extends JPanel {
    boolean isReserved = false;

    RoomStatus() {
        super();
        this.setBackground(Color.WHITE);
    }

    void setReserved(boolean isReserved) {
        this.isReserved = isReserved;
        this.setBackground(isReserved ? Color.YELLOW : Color.WHITE);
    }
}

class HotelGUI extends JFrame implements ActionListener{
    JPanel mainPanel;
    HotelManagement management;
    JLabel titleLabel;
    JPanel reservationPanel, statusPanel, queryPanel;
    JTabbedPane queryTabs;
    JPanel[] tabPanels = new JPanel[3];
    JTextField reservationNameField, reservationCheckInField;
    JComboBox<String> reservationDatesComboBox, reservationRoomsComboBox;
    JButton reservationConfirmButton, reservationCancelButton;
    JTextField queryCustomerNameField, queryStaffNameField;
    JButton addCustomerButton, queryCustomerButton, addStaffButton, queryStaffButton;
    JComboBox<String> queryRoomNoComboBox;
    RoomStatus[][] rooms = new RoomStatus[4][5];
    JTextArea customerQueryResultArea, roomQueryResultArea, staffQueryResultArea;
    JMenuBar menuBar;
    JMenu file;
    JMenuItem open;

    static void showMessageDialog(String msg) {
        JOptionPane.showMessageDialog(null, msg);
    }

    HotelGUI() {
        JPanel panel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(300, 100);
            }
        };
        panel.setLayout(null);

        JLabel addrLabel = new JLabel("Server Address: ");
        JLabel idLabel = new JLabel("ID: ");
        JLabel pwLabel = new JLabel("PW: ");

        JTextField server = new JTextField();
        JTextField id = new JTextField(20);
        JPasswordField pass = new JPasswordField(20);

        addrLabel.setBounds(10, 10, 120, 20);
        idLabel.setBounds(10, 40, 120, 20);
        pwLabel.setBounds(10, 70, 120, 20);
        server.setBounds(120, 10, 160, 20);
        id.setBounds(120, 40, 160, 20);
        pass.setBounds(120, 70, 160, 20);

        panel.add(addrLabel);
        panel.add(server);
        panel.add(idLabel);
        panel.add(id);
        panel.add(pwLabel);
        panel.add(pass);

        String[] options = new String[]{"OK", "Cancel"};
        int option = JOptionPane.showOptionDialog(null, panel, "Enter Credentials",
                JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[1]);
        System.out.println(option);
        if(option == 0) {
            String addr = server.getText();
            String username = id.getText();
            String password = new String(pass.getPassword());
            try {
                management = new HotelManagement(addr, username, password);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                if(e.getMessage().contains("The Network Adapter could not establish the connection"))
                    showMessageDialog("서버에 연결할 수 없습니다. 네트워크 연결 및 서버 주소를 제대로 입력했는 지 확인해 주세요.");
                else if(e.getMessage().contains("invalid username/password; logon denied"))
                    showMessageDialog("올바르지 않은 ID/비밀번호 입니다.");
                else
                    showMessageDialog(e.getMessage());
                System.exit(1);
            }
        } else {
            System.exit(0);
        }
        UIManager.put("swing.boldMetal", Boolean.FALSE);

        setTitle("호텔 예약 관리 프로그램");
        setBounds(0, 0, 700, 640);
        mainPanel = new JPanel();
        mainPanel.setLayout(null);
        setResizable(false);

        makeComponent();
        getContentPane().add(mainPanel, BorderLayout.CENTER);
    }

    void makeComponent() {
        titleLabel = new JLabel("호텔 관리 시스템", SwingConstants.CENTER);
        titleLabel.setFont(new Font("나눔고딕", 0, 30));
        titleLabel.setBounds(120, 20, 460, 50);
        titleLabel.setBorder(new LineBorder(Color.BLACK, 2, true));
        titleLabel.setVisible(true);
        mainPanel.add(titleLabel);

        menuBar = new JMenuBar();
        file = new JMenu("File");

        open = new JMenuItem("Open");

        file.add(open);
        open.addActionListener(this);
        menuBar.add(file);
        setJMenuBar(menuBar);

        reservationPanel = new JPanel();
        reservationPanel.setLayout(null);
        reservationPanel.setBounds(30, 100, 310, 220);
        reservationPanel.setVisible(true);
        reservationPanel.setBorder(BorderFactory.createTitledBorder("투숙예약"));

        reservationNameField = new JTextField();
        reservationCheckInField = new JTextField();

        JLabel[] reservationLabels = {new JLabel("고객명"), new JLabel("체크인(YYYYMMDD)"), new JLabel("박"), new JLabel("객실")};
        for(int i = 0; i < 4; i++) {
            reservationLabels[i].setBounds(10, 35 + 30 * i, 130, 20);
            reservationPanel.add(reservationLabels[i]);
        }

        reservationNameField = new JTextField();
        reservationCheckInField = new JTextField();
        reservationNameField.setBounds(150, 35, 140, 20);
        reservationCheckInField.setBounds(150, 65, 140, 20);

        reservationPanel.add(reservationNameField);
        reservationPanel.add(reservationCheckInField);

        reservationDatesComboBox = new JComboBox<>();
        reservationRoomsComboBox = new JComboBox<>();

        IntStream.rangeClosed(1, 10).forEach(i -> reservationDatesComboBox.addItem(Integer.toString(i)));
        IntStream.rangeClosed(101, 110).forEach(i -> reservationRoomsComboBox.addItem(Integer.toString(i)));
        IntStream.rangeClosed(201, 210).forEach(i -> reservationRoomsComboBox.addItem(Integer.toString(i)));

        reservationDatesComboBox.setBounds(150, 95, 140, 20);
        reservationRoomsComboBox.setBounds(150, 125, 140, 20);

        reservationPanel.add(reservationDatesComboBox);
        reservationPanel.add(reservationRoomsComboBox);

        reservationConfirmButton = new JButton("예약 등록/변경");
        reservationCancelButton = new JButton("예약 취소");

        reservationConfirmButton.setBounds(20, 165, 130, 30);
        reservationCancelButton.setBounds(160, 165, 130, 30);

        reservationPanel.add(reservationConfirmButton);
        reservationPanel.add(reservationCancelButton);

        String date = new SimpleDateFormat("yyyy/MM/dd").format(new Date(System.currentTimeMillis()));

        statusPanel = new JPanel();
        statusPanel.setLayout(null);
        statusPanel.setBounds(360, 100, 310, 220);
        statusPanel.setVisible(true);
        statusPanel.setBorder(BorderFactory.createTitledBorder("객실 예약 현황 (" + date + ")"));

        for(int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                rooms[i][j] = new RoomStatus();
                rooms[i][j].setLayout(null);
                rooms[i][j].setBounds(10 + j * 58, 18 + i * (48), 55, 45);
                rooms[i][j].setBorder(new LineBorder(Color.BLACK, 1, true));
                JLabel tmp = new JLabel(Integer.toString((i < 2 ? 100 : 190) + (5 * i + j + 1)), SwingConstants.CENTER);
                tmp.setFont(new Font("나눔고딕", 0, 15));
                tmp.setBounds(5, 5, 45, 35);
                rooms[i][j].add(tmp);
                statusPanel.add(rooms[i][j]);
            }
        }


        mainPanel.add(reservationPanel);
        mainPanel.add(statusPanel);

        queryPanel = new JPanel();
        queryPanel.setLayout(null);
        queryPanel.setBounds(30, 360, 640, 220);
        queryPanel.setVisible(true);
        queryPanel.setBorder(BorderFactory.createTitledBorder("등록/조회"));

        queryTabs = new JTabbedPane();
        queryTabs.setBounds(20, 20, 600, 180);
        tabPanels[0] = new JPanel();
        tabPanels[0].setLayout(null);
        JLabel nameLabel = new JLabel("고객명");
        nameLabel.setBounds(15, 30, 60, 20);
        queryCustomerNameField = new JTextField();
        queryCustomerNameField.setBounds(80, 30, 90, 20);
        addCustomerButton = new JButton("회원가입");
        addCustomerButton.setBounds(10, 70, 95, 20);
        queryCustomerButton = new JButton("조회");
        queryCustomerButton.setBounds(120, 70, 70, 20);
        customerQueryResultArea = new JTextArea();
        customerQueryResultArea.setEditable(false);
        customerQueryResultArea.setBounds(200, 10, 360, 115);

        tabPanels[0].add(nameLabel);
        tabPanels[0].add(queryCustomerNameField);
        tabPanels[0].add(addCustomerButton);
        tabPanels[0].add(queryCustomerButton);
        tabPanels[0].add(customerQueryResultArea);

        queryTabs.addTab("고객", tabPanels[0]);

        tabPanels[1] = new JPanel();
        queryTabs.addTab("객실", tabPanels[1]);
        tabPanels[1].setLayout(null);
        nameLabel = new JLabel("객실");
        nameLabel.setBounds(15, 30, 60, 20);

        queryRoomNoComboBox = new JComboBox<>();
        IntStream.rangeClosed(101, 110).forEach(i -> queryRoomNoComboBox.addItem(Integer.toString(i)));
        IntStream.rangeClosed(201, 210).forEach(i -> queryRoomNoComboBox.addItem(Integer.toString(i)));
        queryRoomNoComboBox.setBounds(80, 30, 90, 20);
        roomQueryResultArea = new JTextArea();
        roomQueryResultArea.setEditable(false);
        roomQueryResultArea.setBounds(200, 10, 360, 115);


        tabPanels[1].add(nameLabel);
        tabPanels[1].add(queryRoomNoComboBox);
        tabPanels[1].add(roomQueryResultArea);

        tabPanels[2] = new JPanel();
        queryTabs.addTab("직원", tabPanels[2]);

        tabPanels[2].setLayout(null);
        nameLabel = new JLabel("직원명");
        nameLabel.setBounds(15, 30, 60, 20);
        queryStaffNameField = new JTextField();
        queryStaffNameField.setBounds(80, 30, 90, 20);
        addStaffButton = new JButton("직원등록");
        addStaffButton.setBounds(10, 70, 95, 20);
        queryStaffButton = new JButton("조회");
        queryStaffButton.setBounds(120, 70, 70, 20);
        staffQueryResultArea = new JTextArea();
        staffQueryResultArea.setEditable(false);
        staffQueryResultArea.setBounds(200, 10, 360, 115);

        tabPanels[2].add(nameLabel);
        tabPanels[2].add(queryStaffNameField);
        tabPanels[2].add(addStaffButton);
        tabPanels[2].add(queryStaffButton);
        tabPanels[2].add(staffQueryResultArea);

        queryPanel.add(queryTabs);
        mainPanel.add(queryPanel);
        
        reservationConfirmButton.addActionListener(this);
        reservationCancelButton.addActionListener(this);
        addCustomerButton.addActionListener(this); 
        queryCustomerButton.addActionListener(this);
        addStaffButton.addActionListener(this);
        queryStaffButton.addActionListener(this);
        queryRoomNoComboBox.addActionListener(this);

        updateReservationStatus();
        try {
            String result = management.queryRoom(101);
            if (result.substring(0, 2).equals("E:")) {
                switch (result.substring(2)) {
                    case "NO_SUCH_ROOM":
                        roomQueryResultArea.setText("그런 방이 없습니다.");
                        break;
                    default:
                        roomQueryResultArea.setText(result.substring(2));
                        break;
                }
            } else {
                roomQueryResultArea.setText(result);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    int getIndex(String[][] customers) {
        int selectedIndex;
        if(customers.length > 2) {
            System.out.println("동명이인");
            String[] items = Arrays.stream(customers)
                    .skip(1)
                    .map(i -> String.format("이름: %s, 성별: %s, 지역: %s, 전화번호: %s", i[1], i[2], i[3], i[4]))
                    .toArray(String[]::new);
            String s = (String)JOptionPane.showInputDialog(
                    this,
                    "동명이인이 존재합니다.\n찾으려고 하는 사람을 선택하세요.",
                    "동명이인 선택",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    items,
                    items[0]);
            if(s != null)
                selectedIndex = Arrays.asList(items).indexOf(s) + 1;
            else
                selectedIndex = -2;
        }
        else if(customers.length == 1) {
            selectedIndex = -1;
        } else {
            selectedIndex = 1;
        }
        return selectedIndex;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == reservationConfirmButton) {
            String customerName = reservationNameField.getText();
            String checkInDate = reservationCheckInField.getText();
            int nights = reservationDatesComboBox.getSelectedIndex() + 1;
            int roomNo = Integer.parseInt(reservationRoomsComboBox.getSelectedItem().toString());

            if(!Pattern.matches("^[0-9]{4}[01][0-9][0-3][0-9]$", checkInDate)) {
                if(Pattern.matches("^[0-9]{4}/[01][0-9]/[0-3][0-9]$", checkInDate)) {
                    checkInDate = checkInDate.replaceAll("/", "");
                } else {
                    showMessageDialog("올바른 날짜 형식이 아닙니다");
                    return;
                }
            }
            try {
                String[][] customers = management.getCustomersByName(customerName);
                Arrays.asList(customers).forEach(s -> System.out.println(Arrays.toString(s)));
                int selectedIndex = getIndex(customers);
                if(selectedIndex == -1)
                    showMessageDialog("존재하지 않는 고객입니다");
                else if(selectedIndex == -2) return;
                String result = management.makeReservation(customers[selectedIndex][Arrays.asList(customers[0]).indexOf("ID")], checkInDate, nights, roomNo);
                if(result.substring(0, 2).equals("E:")) {
                    switch (result.substring(2)) {
                        case "NO_SUCH_CUSTOMER":
                            showMessageDialog("존재하지 않는 고객입니다");
                            break;
                        case "ALREADY_RESERVED":
                            showMessageDialog("이미 예약되었습니다.");
                            break;
                        case "NO_STAFF":
                            showMessageDialog("예약을 담당할 직원이 없습니다.");
                        default:
                            showMessageDialog(result.substring(2));
                            break;
                    }
                } else {
                    showMessageDialog("예약되었습니다");
                    updateReservationStatus();
                }


            } catch (java.sql.SQLException e1) {
                System.out.println(e1.getMessage());
                if(e1.getMessage().contains("ORA-04098"))
                    showMessageDialog("무결성 검증에 실패했습니다");
                else if(e1.getMessage().contains("INTEGRITY_VIOLATE"))
                    showMessageDialog("이미 예약된 방입니다");
                else if(e1.getMessage().contains("RESERVATION_EDIT")) {
                    showMessageDialog("이미 예약이 있으므로 예약이 수정되었습니다.");
                    updateReservationStatus();
                }
                else
                    e1.printStackTrace();

            }
        } else if(e.getSource() == reservationCancelButton) {
            String customerName = reservationNameField.getText();
            String checkInDate = reservationCheckInField.getText();
            try {
                String[][] customers = management.getCustomersByName(customerName);
                Arrays.asList(customers).forEach(s -> System.out.println(Arrays.toString(s)));
                int selectedIndex = getIndex(customers);
                if(selectedIndex == -1)
                    showMessageDialog("존재하지 않는 고객입니다");
                else if(selectedIndex == -2) return;
                int result = management.cancelReservation(customers[selectedIndex][Arrays.asList(customers[0]).indexOf("ID")], checkInDate);
                if(result == 0) {
                     showMessageDialog("존재하지 않는 예약입니다");
                } else {
                    showMessageDialog("취소되었습니다");
                    updateReservationStatus();
                }

            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } else if(e.getSource() == addCustomerButton) {
            new CustomerRegistrationDialog(this, "고객 등록", management.connector);
        } else if(e.getSource() == queryCustomerButton) {
            try {
                String[][] customers = management.getCustomersByName(queryCustomerNameField.getText());
                int selectedIndex = getIndex(customers);
                if(selectedIndex == -1) {
                    customerQueryResultArea.setText("그런 고객이 없습니다");
                    return;
                }
                else if(selectedIndex == -2) return;
                String result = management.queryCustomerById(customers[selectedIndex][Arrays.asList(customers[0]).indexOf("ID")]);
                if(result.substring(0, 2).equals("E:")) {
                    switch (result.substring(2)) {
                        case "NO_SUCH_CUSTOMER":
                            customerQueryResultArea.setText("그런 고객이 없습니다.");
                            break;
                        default:
                            customerQueryResultArea.setText(result.substring(2));
                            break;
                    }
                } else {
                    customerQueryResultArea.setText(result);
                }
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } else if(e.getSource() == addStaffButton) {
            new StaffRegistrationDialog(this, "직원 등록", management.connector);
        } else if(e.getSource() == queryStaffButton) {
            try {
                String[][] staffs = management.getStaffsByName(queryStaffNameField.getText());
                int selectedIndex = getIndex(staffs);
                if(selectedIndex == -1) {
                    customerQueryResultArea.setText("그런 직원이 없습니다");
                    return;
                }
                String result = management.queryStaffById(staffs[selectedIndex][Arrays.asList(staffs[0]).indexOf("ID")]);
                System.out.println(result);
                if(result.substring(0, 2).equals("E:")) {
                    switch (result.substring(2)) {
                        case "NO_SUCH_STAFF":
                            staffQueryResultArea.setText("그런 직원이 없습니다.");
                            break;
                        default:
                            staffQueryResultArea.setText(result.substring(2));
                            break;
                    }
                } else {
                    staffQueryResultArea.setText(result);
                }
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } else if(e.getSource() == queryRoomNoComboBox) {
            try {
                String result = management.queryRoom(Integer.parseInt(queryRoomNoComboBox.getSelectedItem().toString()));
                System.out.println(result);
                if(result.substring(0, 2).equals("E:")) {
                    switch (result.substring(2)) {
                        case "NO_SUCH_ROOM":
                            roomQueryResultArea.setText("그런 방이 없습니다.");
                            break;
                        default:
                            roomQueryResultArea.setText(result.substring(2));
                            break;
                    }
                } else {
                    roomQueryResultArea.setText(result);
                }
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } else if(e.getSource() == open) {
            FileDialog dialog = new FileDialog(this, "Open", FileDialog.LOAD);
            dialog.setVisible(true);
            if(dialog.getFile() == null || dialog.getFile().equals(""))
                return;
            try {
                BufferedReader fr = new BufferedReader(new FileReader(dialog.getDirectory() + "/" + dialog.getFile()));
                String line = fr.readLine();
                int customerNo = Integer.parseInt(line);
                ArrayList<String> customers = new ArrayList<>();
                IntStream.range(0, customerNo).forEach(i -> {
                    try {
                        customers.add(fr.readLine());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                });
                int staffNo = Integer.parseInt(fr.readLine());
                ArrayList<String> staffs = new ArrayList<>();
                IntStream.range(0, staffNo).forEach(i -> {
                    try {
                        staffs.add(fr.readLine());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                });
                int roomNo = Integer.parseInt(fr.readLine());
                ArrayList<String> rooms = new ArrayList<>();
                IntStream.range(0, roomNo).forEach(i -> {
                    try {
                        rooms.add(fr.readLine());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                });

                customers.forEach(s -> {
                    String[] customerInfo = s.split("\t");
                    Customer.addCustomer(management.connector, customerInfo[0], customerInfo[1], customerInfo[2], customerInfo[3]);
                });
                staffs.forEach(s -> {
                    String[] customerInfo = s.split("\t");
                    Staff.addStaff(management.connector, customerInfo[0], customerInfo[1], customerInfo[2], customerInfo[3]);
                });
                rooms.forEach(s -> {
                    String[] customerInfo = s.split("\t");
                    Room.addRoom(management.connector, Integer.parseInt(customerInfo[0]), Integer.parseInt(customerInfo[1]), customerInfo[2]);
                });
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    void updateReservationStatus() {
        try {
            boolean[] result = management.getReservations();
            IntStream.range(0, 4).forEach(i ->
                IntStream.range(0, 5).forEach(j ->
                    rooms[i][j].setReserved(result[i * 5 + j])
                )
            );
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }
}

abstract class RegistrationDialog extends JDialog implements ActionListener {

    JLabel nameLabel, genderLabel, addressLabel, contactLabel;
    JTextField nameField, contactField;
    JComboBox<String> genderComboBox, addressComboBox;
    JButton registrationButton, cancelButton;


    RegistrationDialog(JFrame frame, String title) {
        super(frame, title);
        setLayout(null);
        UIManager.put("swing.boldMetal", Boolean.FALSE);

        setTitle("호텔 예약 관리 프로그램");
        setSize(380, 280);
        setLayout(null);

        nameLabel = new JLabel();
        genderLabel = new JLabel("성별");
        addressLabel = new JLabel("주소");
        contactLabel = new JLabel("연락처");
        nameLabel.setBounds(30, 40, 150, 20);
        genderLabel.setBounds(30, 70, 150, 20);
        addressLabel.setBounds(30, 100, 150, 20);
        contactLabel.setBounds(30, 130, 150, 20);
        add(nameLabel);
        add(genderLabel);
        add(addressLabel);
        add(contactLabel);


        nameField = new JTextField();
        genderComboBox = new JComboBox<>();
        addressComboBox = new JComboBox<>();
        contactField = new JTextField();
        nameField.setBounds(200, 40, 150, 20);
        genderComboBox.setBounds(200, 70, 150, 20);
        addressComboBox.setBounds(200, 100, 150, 20);
        contactField.setBounds(200, 130, 150, 20);
        add(nameField);
        add(genderComboBox);
        add(addressComboBox);
        add(contactField);

        genderComboBox.addItem("남");
        genderComboBox.addItem("여");

        addressComboBox.addItem("서울");
        addressComboBox.addItem("경기");
        addressComboBox.addItem("강원");
        addressComboBox.addItem("충북");
        addressComboBox.addItem("대전");
        addressComboBox.addItem("충남");
        addressComboBox.addItem("경북");
        addressComboBox.addItem("경남");
        addressComboBox.addItem("전북");
        addressComboBox.addItem("전남");
        addressComboBox.addItem("제주");

        registrationButton = new JButton();
        cancelButton = new JButton("취소");
        registrationButton.setBounds(30, 180, 150, 20);
        cancelButton.setBounds(200, 180, 150, 20);

        add(registrationButton);
        add(cancelButton);

        registrationButton.addActionListener(this);
        cancelButton.addActionListener(this);

        setText();

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == registrationButton) {
            makeRegistration();
        } else if(e.getSource() == cancelButton) {
            dispose();
        }
    }

    abstract void setText();
    abstract void makeRegistration();
}

class CustomerRegistrationDialog extends RegistrationDialog {
    Connector connector;
    CustomerRegistrationDialog(JFrame frame, String title, Connector connector) {
        super(frame, title);
        this.connector = connector;
    }

    @Override
    void setText() {
        nameLabel.setText("고객명");
        registrationButton.setText("가입요청");
    }

    @Override
    void makeRegistration() {
        String name = nameField.getText();
        String gender = genderComboBox.getSelectedItem().toString();
        String address = addressComboBox.getSelectedItem().toString();
        String contact = contactField.getText();


        if(Customer.addCustomer(connector, name, gender, contact, address)) {
            JOptionPane.showMessageDialog(null, "등록되었습니다.");
            dispose();
        } else {
            JOptionPane.showMessageDialog(null, "등록에 실패하였습니다.");
        }    }
}

class StaffRegistrationDialog extends RegistrationDialog {
    Connector connector;
    StaffRegistrationDialog(JFrame frame, String title, Connector connector) {
        super(frame, title);
        this.connector = connector;
    }

    @Override
    void setText() {
        nameLabel.setText("직원명");
        registrationButton.setText("직원등록");
    }

    @Override
    void makeRegistration() {
        String name = nameField.getText();
        String gender = genderComboBox.getSelectedItem().toString();
        String address = addressComboBox.getSelectedItem().toString();
        String contact = contactField.getText();

        if(Staff.addStaff(connector, name, gender, address, contact)) {
            JOptionPane.showMessageDialog(null, "등록되었습니다.");
            dispose();
        } else {
            JOptionPane.showMessageDialog(null, "등록에 실패하였습니다.");
        }

    }
}
