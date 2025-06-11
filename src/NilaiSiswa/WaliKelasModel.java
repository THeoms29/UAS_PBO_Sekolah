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

    public List<Map<String, Object>> getRekapNilaiDanAbsensi(int kelasId, String semester) {
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
                    SELECT 
                        AVG(n.nilai_uh) as nilai_uh,
                        AVG(n.nilai_uts) as nilai_uts,
                        AVG(n.nilai_uas) as nilai_uas,
                        AVG(n.nilai_akhir) as nilai_akhir
                    FROM nilai n
                    JOIN mapel m ON n.mapel_id = m.id
                    WHERE n.siswa_id = ? AND n.semester = ?
                    """;
                PreparedStatement psNilai = conn.prepareStatement(sqlNilai);
                psNilai.setInt(1, siswaId);
                psNilai.setString(2, semester);
                ResultSet rsNilai = psNilai.executeQuery();

                double nilaiUH = 0.0, nilaiUTS = 0.0, nilaiUAS = 0.0, nilaiAkhir = 0.0;
                if (rsNilai.next()) {
                    nilaiUH = rsNilai.getDouble("nilai_uh");
                    nilaiUTS = rsNilai.getDouble("nilai_uts");
                    nilaiUAS = rsNilai.getDouble("nilai_uas");
                    nilaiAkhir = rsNilai.getDouble("nilai_akhir");
                }
                siswa.put("nilai_uh", nilaiUH);
                siswa.put("nilai_uts", nilaiUTS);
                siswa.put("nilai_uas", nilaiUAS);
                siswa.put("nilai_akhir", nilaiAkhir);

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
                PreparedStatement psAbsensi = conn.prepareStatement(sqlAbsensi);
                psAbsensi.setInt(1, siswaId);
                ResultSet rsAbsensi = psAbsensi.executeQuery();

                int totalAbsensi = 0, hadir = 0, izin = 0, sakit = 0, alfa = 0;
                if (rsAbsensi.next()) {
                    totalAbsensi = rsAbsensi.getInt("total_absensi");
                    hadir = rsAbsensi.getInt("hadir");
                    izin = rsAbsensi.getInt("izin");
                    sakit = rsAbsensi.getInt("sakit");
                    alfa = rsAbsensi.getInt("alfa");
                }
                siswa.put("total_absensi", totalAbsensi);
                siswa.put("hadir", hadir);
                siswa.put("izin", izin);
                siswa.put("sakit", sakit);
                siswa.put("alfa", alfa);

                list.add(siswa);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Gagal mengambil data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

public boolean checkForUpdates(int kelasId, String semester) {
    String sql = """
        SELECT MAX(last_updated) AS last_update
        FROM (
            SELECT MAX(updated_at) AS last_updated FROM nilai 
            WHERE siswa_id IN (SELECT id FROM siswa WHERE kelas_id = ?) AND semester = ?
            UNION ALL
            SELECT MAX(updated_at) AS last_updated FROM absensi 
            WHERE siswa_id IN (SELECT id FROM siswa WHERE kelas_id = ?)
        ) AS combined
    """;
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, kelasId);
        ps.setString(2, semester);
        ps.setInt(3, kelasId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Timestamp lastUpdate = rs.getTimestamp("last_update");
            long currentTimestamp = lastUpdate != null ? lastUpdate.getTime() : 0; // Gunakan 0 jika null
            if (currentTimestamp > lastUpdateTimestamp || lastUpdateTimestamp == 0) { // Perbarui jika timestamp baru lebih besar atau belum diinisialisasi
                lastUpdateTimestamp = currentTimestamp;
                return true;
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Gagal memeriksa pembaruan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
    return false;
}
}