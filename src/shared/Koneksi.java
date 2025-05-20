package shared;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Koneksi {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/sistem_sekolah";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private static Connection conn;

    public static Connection getConnection() {
        if (conn == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("Koneksi database berhasil.");
            } catch (ClassNotFoundException | SQLException e) {
                System.err.println("Koneksi database gagal: " + e.getMessage());
            }
        }
        return conn;
    }

    // Fungsi main untuk tes koneksi
    public static void main(String[] args) {
        Connection test = getConnection();
        if (test != null) {
            System.out.println("Tes koneksi sukses.");
        } else {
            System.out.println("Tes koneksi gagal.");
        }
    }
}
