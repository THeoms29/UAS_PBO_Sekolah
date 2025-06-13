package login;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import shared.Koneksi;

public class LoginModel {
    private static final Logger LOGGER = Logger.getLogger(LoginModel.class.getName());
    private final Connection conn;

    public LoginModel() {
        conn = Koneksi.getConnection();
    }

    // Method untuk validasi login
    public User validateLogin(String username, String password) {
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            LOGGER.warning("Username atau password kosong");
            return null;
        }

        String sql = "SELECT id, nama, username, role FROM users WHERE username = ? AND password = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username.trim());
            stmt.setString(2, password); // Dalam implementasi nyata, gunakan hash password
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("nama"),
                        rs.getString("username"),
                        rs.getString("role")
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gagal melakukan validasi login", e);
        }
        
        return null;
    }

    public int getUserCount() {
        String sql = "SELECT COUNT(*) FROM users";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gagal menghitung jumlah user", e);
        }
        return -1; // Mengindikasikan error
    }

    // untuk mengecek koneksi database
    public boolean isConnectionValid() {
        try {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking connection", e);
            return false;
        }
    }

    // Inner class untuk data user
    public static class User {
        private final int id;
        private final String nama;
        private final String username;
        private final String role;

        public User(int id, String nama, String username, String role) {
            this.id = id;
            this.nama = nama;
            this.username = username;
            this.role = role;
        }

        public int getId() { return id; }
        public String getNama() { return nama; }
        public String getUsername() { return username; }
        public String getRole() { return role; }

        @Override
        public String toString() {
            return "User{id=" + id + ", nama='" + nama + "', role='" + role + "'}";
        }
    }
}