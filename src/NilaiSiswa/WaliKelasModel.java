package NilaiSiswa;

import java.sql.*;
import java.util.*;
import javax.swing.JOptionPane;
import shared.Koneksi;

public class WaliKelasModel {
    private final Connection conn;

    public WaliKelasModel() {
        this.conn = Koneksi.getConnection();
    }

    // Mendapatkan daftar kelas yang ada dalam database
    public List<Map<String, Object>> getDaftarKelas() {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            String sql = "SELECT id, nama_kelas FROM kelas ORDER BY nama_kelas";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("nama_kelas", rs.getString("nama_kelas"));
                list.add(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error getDaftarKelas: " + e.getMessage());
        }
        return list;
    }

   public List<Map<String, Object>> getRekapNilaiDanAbsensi(int kelasId, String semester) {
    List<Map<String, Object>> list = new ArrayList<>();
    try {
        String sql = """
            SELECT s.id AS siswa_id, s.nama AS nama_siswa, s.nis,
                   COALESCE(n.nilai_akhir, 0) AS nilai_akhir,
                   COUNT(a.id) AS total_absensi,
                   SUM(CASE WHEN a.status = 'hadir' THEN 1 ELSE 0 END) AS hadir,
                   SUM(CASE WHEN a.status = 'izin' THEN 1 ELSE 0 END) AS izin,
                   SUM(CASE WHEN a.status = 'sakit' THEN 1 ELSE 0 END) AS sakit,
                   SUM(CASE WHEN a.status = 'alfa' THEN 1 ELSE 0 END) AS alfa
            FROM siswa s
            LEFT JOIN nilai n ON s.id = n.siswa_id AND n.semester = ?
            LEFT JOIN absensi a ON s.id = a.siswa_id  -- hilangkan AND a.semester = ?
            WHERE s.kelas_id = ?
            GROUP BY s.id, s.nama, s.nis, n.nilai_akhir
            ORDER BY s.nama
        """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, semester);  // untuk nilai
        // ps.setString(2, semester); // jangan set ini lagi, karena a.semester dihapus
        ps.setInt(2, kelasId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            row.put("siswa_id", rs.getInt("siswa_id"));
            row.put("nama_siswa", rs.getString("nama_siswa"));
            row.put("nis", rs.getString("nis"));
            row.put("nilai_akhir", rs.getDouble("nilai_akhir"));
            row.put("total_absensi", rs.getInt("total_absensi"));
            row.put("hadir", rs.getInt("hadir"));
            row.put("izin", rs.getInt("izin"));
            row.put("sakit", rs.getInt("sakit"));
            row.put("alfa", rs.getInt("alfa"));
            list.add(row);
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Error getRekapNilaiDanAbsensi: " + e.getMessage());
        e.printStackTrace();
    }
    return list;
}

    // Mendapatkan nama kelas berdasarkan ID
    public String getNamaKelas(int kelasId) {
        try {
            String sql = "SELECT nama_kelas FROM kelas WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, kelasId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("nama_kelas");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error getNamaKelas: " + e.getMessage());
        }
        return "";
    }
    public int getKelasIdByNama(String namaKelas) {
    try {
        String sql = "SELECT id FROM kelas WHERE nama_kelas = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, namaKelas);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt("id");
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Error getKelasIdByNama: " + e.getMessage());
    }
    return -1; // Jika tidak ditemukan
}
}
