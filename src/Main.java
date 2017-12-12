import java.sql.SQLException;
public class Main {
    public static void main(String[] args) {
        HotelManagement management;
        try {
            management = new HotelManagement("r3mark.xyz", "database", "nhn3012");

            HotelGUI gui = new HotelGUI(management);
            gui.setVisible(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

