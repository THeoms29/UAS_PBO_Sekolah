package manajemenuser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import shared.Koneksi;

public class UserManajemenModel {
    private final Connection conn;

    public UserManajemenModel() {
        this.conn = Koneksi.getConnection();
    }

    private boolean isUserLinkedToJadwal(int userId) {
        String sql = "SELECT COUNT(*) FROM jadwal WHERE guru_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal memeriksa keterkaitan user dengan jadwal: " + e.getMessage());
        }
        return false;
    }

    public List<Object[]> getAllUsers() {
        List<Object[]> users = new ArrayList<>();
        String sql = "SELECT id, nama, username, role FROM users ORDER BY nama ASC";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nama"),
                    rs.getString("username"),
                    rs.getString("role")
                });
            }
        } catch (SQLException e) {
            System.err.println("Gagal mengambil data user: " + e.getMessage());
        }
        return users;
    }

    public boolean addUser(String nama, String username, String password, String role) {
        String sql = "INSERT INTO users (nama, username, password, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nama);
            stmt.setString(2, username);
            stmt.setString(3, password);
            stmt.setString(4, role);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal menambah user: " + e.getMessage());
            return false;
        }
    }

    public boolean updateUser(int id, String nama, String username, String password, String role) {
        // Cek apakah password diisi atau tidak
        boolean changePassword = password != null && !password.isEmpty();
        
        StringBuilder sqlBuilder = new StringBuilder("UPDATE users SET nama = ?, username = ?, role = ?");
        if (changePassword) {
            sqlBuilder.append(", password = ?");
        }
        sqlBuilder.append(" WHERE id = ?");

        try (PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {
            stmt.setString(1, nama);
            stmt.setString(2, username);
            stmt.setString(3, role);
            if (changePassword) {
                stmt.setString(4, password);
                stmt.setInt(5, id);
            } else {
                stmt.setInt(4, id);
            }
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal mengupdate user: " + e.getMessage());
            return false;
        }
    }

    public String deleteUser(int id) {
        // Validasi apakah user adalah seorang guru yang masih punya jadwal
        if (isUserLinkedToJadwal(id)) {
            return "Gagal: Guru ini masih memiliki jadwal mengajar. Hapus jadwalnya terlebih dahulu.";
        }
        
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                return null; // sukses
            } else {
                return "Gagal menghapus: User dengan ID tersebut tidak ditemukan.";
            }
        } catch (SQLException e) {
            System.err.println("Gagal menghapus user: " + e.getMessage());
            return "Terjadi kesalahan pada database saat mencoba menghapus.";
        }
    }
}