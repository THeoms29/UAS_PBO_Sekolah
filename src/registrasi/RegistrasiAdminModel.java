package registrasi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import shared.Koneksi;

public class RegistrasiAdminModel {
    private final Connection conn;

    public RegistrasiAdminModel() {
        this.conn = Koneksi.getConnection();
        if (this.conn == null) {
            // Menangani kasus jika koneksi gagal didapatkan
            throw new RuntimeException("Gagal mendapatkan koneksi ke database.");
        }
    }

    public boolean createAdmin(String nama, String username, String password) {
        // Peran 'kepala_sekolah' di-hardcode karena ini adalah pembuatan user pertama
        String sql = "INSERT INTO users (nama, username, password, role) VALUES (?, ?, ?, 'kepala_sekolah')";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nama);
            stmt.setString(2, username);
            stmt.setString(3, password); // Ingat catatan keamanan: di dunia nyata, ini harus di-hash
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            // Tampilkan error jika username sudah ada (Unique Key constraint) atau error lainnya
            System.err.println("Gagal membuat user admin: " + e.getMessage());
            return false;
        }
    }
}