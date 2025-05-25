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

    public List<Map<String, Object>> getNilaiByKelasDanMapel(int kelasId, int mapelId, String semester) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            String sql = """
                SELECT s.id AS siswa_id, s.nama, s.nis, n.nilai_uh, n.nilai_uts, n.nilai_uas, n.nilai_akhir
                FROM siswa s
                LEFT JOIN nilai n ON s.id = n.siswa_id AND n.mapel_id = ? AND n.semester = ?
                WHERE s.kelas_id = ?
                ORDER BY s.nama
            """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, mapelId);
            ps.setString(2, semester);
            ps.setInt(3, kelasId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("siswa_id", rs.getInt("siswa_id"));
                row.put("nama", rs.getString("nama"));
                row.put("nis", rs.getString("nis"));
                row.put("nilai_uh", rs.getObject("nilai_uh"));    // Bisa null
                row.put("nilai_uts", rs.getObject("nilai_uts"));
                row.put("nilai_uas", rs.getObject("nilai_uas"));
                row.put("nilai_akhir", rs.getObject("nilai_akhir"));
                list.add(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error getNilaiByKelasDanMapel: " + e.getMessage());
        }
        return list;
    }
   
    public void simpanNilai(int siswaId, int mapelId, int nilaiUH, int nilaiUTS, int nilaiUAS, String semester) {
        double nilaiAkhir = (nilaiUH + nilaiUTS + nilaiUAS) / 3.0;

        try {
            String cekSql = "SELECT id FROM nilai WHERE siswa_id = ? AND mapel_id = ? AND semester = ?";
            PreparedStatement cekStmt = conn.prepareStatement(cekSql);
            cekStmt.setInt(1, siswaId);
            cekStmt.setInt(2, mapelId);
            cekStmt.setString(3, semester);
            ResultSet rs = cekStmt.executeQuery();

            if (rs.next()) {
                String updateSql = """
                    UPDATE nilai SET nilai_uh = ?, nilai_uts = ?, nilai_uas = ?, nilai_akhir = ?
                    WHERE siswa_id = ? AND mapel_id = ? AND semester = ?
                """;
                PreparedStatement ps = conn.prepareStatement(updateSql);
                ps.setInt(1, nilaiUH);
                ps.setInt(2, nilaiUTS);
                ps.setInt(3, nilaiUAS);
                ps.setDouble(4, nilaiAkhir);
                ps.setInt(5, siswaId);
                ps.setInt(6, mapelId);
                ps.setString(7, semester);
                ps.executeUpdate();
            } else {
                String insertSql = """
                    INSERT INTO nilai (siswa_id, mapel_id, semester, nilai_uh, nilai_uts, nilai_uas, nilai_akhir)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
                PreparedStatement ps = conn.prepareStatement(insertSql);
                ps.setInt(1, siswaId);
                ps.setInt(2, mapelId);
                ps.setString(3, semester);
                ps.setInt(4, nilaiUH);
                ps.setInt(5, nilaiUTS);
                ps.setInt(6, nilaiUAS);
                ps.setDouble(7, nilaiAkhir);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error simpanNilai: " + e.getMessage());
        }
    }

    public Map<String, String> getNamaGuruDanMapel(int guruId, int mapelId) {
        Map<String, String> result = new HashMap<>();
        try {
            String sql = """
                SELECT u.nama AS nama_guru, m.nama_mapel
                FROM users u
                JOIN jadwal j ON u.id = j.guru_id
                JOIN mapel m ON m.id = j.mapel_id
                WHERE u.id = ? AND m.id = ?
                LIMIT 1
            """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, guruId);
            ps.setInt(2, mapelId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result.put("nama_guru", rs.getString("nama_guru"));
                result.put("nama_mapel", rs.getString("nama_mapel"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error getNamaGuruDanMapel: " + e.getMessage());
        }
        return result;
    }

    public List<Map<String, Object>> getDataSiswaKelas(int kelasId) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            String sql = "SELECT id AS siswa_id, nama, nis FROM siswa WHERE kelas_id = ? ORDER BY nama";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, kelasId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("siswa_id", rs.getInt("siswa_id"));
                row.put("nama", rs.getString("nama"));
                row.put("nis", rs.getString("nis"));
                list.add(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error getDataSiswaKelas: " + e.getMessage());
        }
        return list;
    }
}
