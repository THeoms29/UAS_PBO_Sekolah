package NilaiSiswa;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import shared.Koneksi;

public class WaliKelasModel {
    private final Connection conn;
    private long lastUpdateTimestamp = 0; // Melacak waktu pembaruan terakhir

    public WaliKelasModel() {
        this.conn = Koneksi.getConnection();
        if (conn == null) {
            throw new RuntimeException("Gagal terhubung ke database!");
        }
    }

    public List<Map<String, Object>> getDaftarKelas() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT id, nama_kelas FROM kelas";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> kelas = new HashMap<>();
                kelas.put("id", rs.getInt("id"));
                kelas.put("nama_kelas", rs.getString("nama_kelas"));
                list.add(kelas);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Gagal mengambil daftar kelas: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    public int getKelasIdByNama(String namaKelas) {
        String sql = "SELECT id FROM kelas WHERE nama_kelas = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, namaKelas);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Gagal menemukan ID kelas: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return -1;
    }

    public List<Map<String, Object>> getRekapNilaiDanAbsensi(int kelasId, String semester, int mapelId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sqlSiswa = "SELECT id, nama, nis FROM siswa WHERE kelas_id = ?";
        try (PreparedStatement psSiswa = conn.prepareStatement(sqlSiswa)) {
            psSiswa.setInt(1, kelasId);
            ResultSet rsSiswa = psSiswa.executeQuery();

            while (rsSiswa.next()) {
                Map<String, Object> siswa = new HashMap<>();
                siswa.put("id", rsSiswa.getInt("id"));
                siswa.put("nama", rsSiswa.getString("nama"));
                siswa.put("nis", rsSiswa.getString("nis"));

                int siswaId = rsSiswa.getInt("id");
                String sqlNilai = """
                    SELECT n.nilai_uh, n.nilai_uts, n.nilai_uas, n.nilai_akhir
                    FROM nilai n
                    WHERE n.siswa_id = ? AND n.semester = ? AND n.mapel_id = ?
                    ORDER BY n.updated_at DESC
                    LIMIT 1
                    """;
                try (PreparedStatement psNilai = conn.prepareStatement(sqlNilai)) {
                    psNilai.setInt(1, siswaId);
                    psNilai.setString(2, semester);
                    psNilai.setInt(3, mapelId);
                    try (ResultSet rsNilai = psNilai.executeQuery()) {
                        if (rsNilai.next()) {
                            siswa.put("nilai_uh", rsNilai.getObject("nilai_uh") != null ? rsNilai.getInt("nilai_uh") : 0);
                            siswa.put("nilai_uts", rsNilai.getObject("nilai_uts") != null ? rsNilai.getInt("nilai_uts") : 0);
                            siswa.put("nilai_uas", rsNilai.getObject("nilai_uas") != null ? rsNilai.getInt("nilai_uas") : 0);
                            siswa.put("nilai_akhir", rsNilai.getObject("nilai_akhir") != null ? rsNilai.getDouble("nilai_akhir") : 0.0);
                        } else {
                            siswa.put("nilai_uh", 0);
                            siswa.put("nilai_uts", 0);
                            siswa.put("nilai_uas", 0);
                            siswa.put("nilai_akhir", 0.0);
                        }
                    }
                }

                String sqlAbsensi = """
                    SELECT 
                        COUNT(*) AS total_absensi,
                        SUM(CASE WHEN status = 'Hadir' THEN 1 ELSE 0 END) AS hadir,
                        SUM(CASE WHEN status = 'Izin' THEN 1 ELSE 0 END) AS izin,
                        SUM(CASE WHEN status = 'Sakit' THEN 1 ELSE 0 END) AS sakit,
                        SUM(CASE WHEN status = 'Alpha' THEN 1 ELSE 0 END) AS alfa
                    FROM absensi 
                    WHERE siswa_id = ?
                    """;
                try (PreparedStatement psAbsensi = conn.prepareStatement(sqlAbsensi)) {
                    psAbsensi.setInt(1, siswaId);
                    try (ResultSet rsAbsensi = psAbsensi.executeQuery()) {
                        if (rsAbsensi.next()) {
                            siswa.put("total_absensi", rsAbsensi.getInt("total_absensi"));
                            siswa.put("hadir", rsAbsensi.getInt("hadir"));
                            siswa.put("izin", rsAbsensi.getInt("izin"));
                            siswa.put("sakit", rsAbsensi.getInt("sakit"));
                            siswa.put("alfa", rsAbsensi.getInt("alfa"));
                        }
                    }
                }

                list.add(siswa);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Gagal mengambil data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    public boolean checkForUpdates(int kelasId, String semester, int mapelId) {
        String sql = """
            SELECT MAX(last_updated) AS last_update
            FROM (
                SELECT MAX(updated_at) AS last_updated FROM nilai 
                WHERE siswa_id IN (SELECT id FROM siswa WHERE kelas_id = ?) AND semester = ? AND mapel_id = ?
                UNION ALL
                SELECT MAX(updated_at) AS last_updated FROM absensi 
                WHERE siswa_id IN (SELECT id FROM siswa WHERE kelas_id = ?)
            ) AS combined
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, kelasId);
            ps.setString(2, semester);
            ps.setInt(3, mapelId);
            ps.setInt(4, kelasId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Timestamp lastUpdate = rs.getTimestamp("last_update");
                if (lastUpdate != null && lastUpdate.getTime() > lastUpdateTimestamp) {
                    lastUpdateTimestamp = lastUpdate.getTime();
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Gagal memeriksa pembaruan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}