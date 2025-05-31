package shared;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Koneksi {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/sistem_sekolah";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    // ✅ FIXED: Hapus static connection, buat connection baru setiap kali
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Create new connection every time
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            
            // Verify connection is valid
            if (conn != null && !conn.isClosed()) {
                System.out.println("Koneksi database berhasil dibuat.");
                return conn;
            } else {
                System.err.println("Koneksi database null atau sudah ditutup.");
                return null;
            }
            
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver tidak ditemukan: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Koneksi database gagal: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error tidak dikenal saat koneksi database: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    // ✅ NEW: Method untuk test koneksi dengan auto-close
    public static boolean testConnection() {
        Connection testConn = null;
        try {
            testConn = getConnection();
            if (testConn != null && !testConn.isClosed()) {
                System.out.println("Test koneksi database berhasil.");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error saat test koneksi: " + e.getMessage());
        } finally {
            // Close test connection
            if (testConn != null) {
                try {
                    testConn.close();
                } catch (SQLException e) {
                    System.err.println("Error menutup test connection: " + e.getMessage());
                }
            }
        }
        
        System.err.println("Test koneksi database gagal.");
        return false;
    }

    // ✅ NEW: Method untuk mendapatkan info database
    public static void printDatabaseInfo() {
        Connection conn = null;
        try {
            conn = getConnection();
            if (conn != null) {
                System.out.println("Database URL: " + DB_URL);
                System.out.println("Database User: " + DB_USER);
                System.out.println("Connection Valid: " + conn.isValid(5));
                System.out.println("Connection Closed: " + conn.isClosed());
                System.out.println("Auto Commit: " + conn.getAutoCommit());
            }
        } catch (SQLException e) {
            System.err.println("Error getting database info: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    // Fungsi main untuk tes koneksi
    public static void main(String[] args) {
        System.out.println("=== Testing Database Connection ===");
        
        // Test basic connection
        if (testConnection()) {
            System.out.println("✅ Koneksi database berhasil!");
        } else {
            System.out.println("❌ Koneksi database gagal!");
        }
        
        // Print database info
        System.out.println("\n=== Database Information ===");
        printDatabaseInfo();
        
        // Test multiple connections
        System.out.println("\n=== Testing Multiple Connections ===");
        for (int i = 1; i <= 3; i++) {
            Connection conn = getConnection();
            if (conn != null) {
                System.out.println("Connection " + i + ": SUCCESS");
                try {
                    conn.close();
                    System.out.println("Connection " + i + ": CLOSED");
                } catch (SQLException e) {
                    System.err.println("Error closing connection " + i + ": " + e.getMessage());
                }
            } else {
                System.out.println("Connection " + i + ": FAILED");
            }
        }
    }
}