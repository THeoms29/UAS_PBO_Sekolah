package NilaiSiswa;

import java.sql.*;
import java.util.*;
import javax.swing.JOptionPane;
import shared.Koneksi;

public class NilaiModel {
    private final Connection conn;

    public NilaiModel() {
        this.conn = Koneksi.getConnection();
    }

    // ==================== METODE UTILITAS ====================

    public boolean cekKoneksiDatabase() {
        try {
            return conn != null && !conn.isClosed() && conn.isValid(2);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, 
                "Error cek koneksi database: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // ==================== MANAJEMEN TRANSAKSI ====================

    public void startTransaction() throws SQLException {
        if (conn != null) {
            conn.setAutoCommit(false);
        }
    }

    public void commitTransaction() throws SQLException {
        if (conn != null) {
            conn.commit();
            conn.setAutoCommit(true);
        }
    }

    public void rollbackTransaction() {
        if (conn != null) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, 
                    "Error rollback transaction: " + e.getMessage(), 
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ==================== METODE DATA GURU DAN MAPEL ====================

    public String getNamaGuru(int guruId) throws SQLException {
        String sql = "SELECT nama FROM users WHERE id = ? AND role = 'guru'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, guruId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("nama");
            }
            throw new SQLException("Guru dengan ID " + guruId + " tidak ditemukan");
        }
    }

    public String getNamaMapel(int mapelId) throws SQLException {
        String sql = "SELECT nama_mapel FROM mapel WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, mapelId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("nama_mapel");
            }
            throw new SQLException("Mapel dengan ID " + mapelId + " tidak ditemukan");
        }
    }

    // ==================== METODE DATA KELAS ====================

    public List<Map<String, Object>> getDataKelas() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT id, nama_kelas FROM kelas ORDER BY nama_kelas";
        
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("nama_kelas", rs.getString("nama_kelas"));
                list.add(row);
            }
        }
        return list;
    }

    // ==================== METODE DATA SISWA ====================

    public List<Map<String, Object>> getDataSiswaKelas(int kelasId) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT id AS siswa_id, nama, nis FROM siswa WHERE kelas_id = ? ORDER BY nama";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, kelasId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("siswa_id", rs.getInt("siswa_id"));
                    row.put("nama", rs.getString("nama"));
                    row.put("nis", rs.getString("nis"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    // ==================== METODE NILAI ====================

    public List<Map<String, Object>> getNilaiByKelasDanMapel(int kelasId, int mapelId, String semester) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = """
            SELECT s.id AS siswa_id, s.nama, s.nis, 
                   n.nilai_uh, n.nilai_uts, n.nilai_uas, n.nilai_akhir
            FROM siswa s
            LEFT JOIN nilai n ON s.id = n.siswa_id AND n.mapel_id = ? AND n.semester = ?
            WHERE s.kelas_id = ?
            ORDER BY s.nama
        """;
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, mapelId);
            ps.setString(2, semester);
            ps.setInt(3, kelasId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("siswa_id", rs.getInt("siswa_id"));
                    row.put("nama", rs.getString("nama"));
                    row.put("nis", rs.getString("nis"));
                    row.put("nilai_uh", rs.getObject("nilai_uh"));
                    row.put("nilai_uts", rs.getObject("nilai_uts"));
                    row.put("nilai_uas", rs.getObject("nilai_uas"));
                    row.put("nilai_akhir", rs.getObject("nilai_akhir"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    public boolean simpanNilai(int siswaId, int mapelId, int nilaiUH, int nilaiUTS, int nilaiUAS, String semester) throws SQLException {
        double nilaiAkhir = (nilaiUH + nilaiUTS + nilaiUAS) / 3.0;
        
        String sql = """
            INSERT INTO nilai 
            (siswa_id, mapel_id, semester, nilai_uh, nilai_uts, nilai_uas, nilai_akhir)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
            nilai_uh = VALUES(nilai_uh),
            nilai_uts = VALUES(nilai_uts),
            nilai_uas = VALUES(nilai_uas),
            nilai_akhir = VALUES(nilai_akhir)
        """;
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, siswaId);
            ps.setInt(2, mapelId);
            ps.setString(3, semester);
            ps.setInt(4, nilaiUH);
            ps.setInt(5, nilaiUTS);
            ps.setInt(6, nilaiUAS);
            ps.setDouble(7, nilaiAkhir);
            
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        }
    }

    // ==================== METODE TAMBAHAN ====================

    public boolean hapusNilai(int siswaId, int mapelId, String semester) throws SQLException {
        String sql = "DELETE FROM nilai WHERE siswa_id = ? AND mapel_id = ? AND semester = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, siswaId);
            ps.setInt(2, mapelId);
            ps.setString(3, semester);
            
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        }
    }

    public void closeConnection() {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, 
                    "Error menutup koneksi: " + e.getMessage(), 
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}