package NilaiSiswa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.DefaultTableModel;

public class NilaiModel {
    private Connection conn;

    public NilaiModel(Connection conn) {
        this.conn = conn;
    }

    public boolean simpanNilai(int siswaId, int mapelId, int nilaiUH, int nilaiUTS, int nilaiUAS, String semester) throws SQLException {
        double nilaiAkhir = (nilaiUH + nilaiUTS + nilaiUAS) / 3.0;
        String checkSql = "SELECT COUNT(*) FROM nilai WHERE siswa_id = ? AND mapel_id = ? AND semester = ?";
        try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setInt(1, siswaId);
            checkPs.setInt(2, mapelId);
            checkPs.setString(3, semester);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    String updateSql = """
                        UPDATE nilai 
                        SET nilai_uh = ?, nilai_uts = ?, nilai_uas = ?, nilai_akhir = ?
                        WHERE siswa_id = ? AND mapel_id = ? AND semester = ?
                    """;
                    try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                        ps.setInt(1, nilaiUH);
                        ps.setInt(2, nilaiUTS);
                        ps.setInt(3, nilaiUAS);
                        ps.setDouble(4, nilaiAkhir);
                        ps.setInt(5, siswaId);
                        ps.setInt(6, mapelId);
                        ps.setString(7, semester);
                        int affectedRows = ps.executeUpdate();
                        return affectedRows > 0;
                    }
                } else {
                    String insertSql = """
                        INSERT INTO nilai (siswa_id, mapel_id, semester, nilai_uh, nilai_uts, nilai_uas, nilai_akhir)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                    """;
                    try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
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
            }
        }
    }

    public boolean simpanNilai(List<Map<String, Object>> dataNilai, int mapelId) throws SQLException {
        boolean allSuccess = true;
        for (Map<String, Object> nilai : dataNilai) {
            int siswaId = (int) nilai.get("siswa_id");
            int nilaiUH = (int) nilai.get("nilai_uh");
            int nilaiUTS = (int) nilai.get("nilai_uts");
            int nilaiUAS = (int) nilai.get("nilai_uas");
            String semester = (String) nilai.get("semester");
            if (!simpanNilai(siswaId, mapelId, nilaiUH, nilaiUTS, nilaiUAS, semester)) {
                allSuccess = false;
            }
        }
        return allSuccess;
    }

    public void loadNilai(String kelas, String semester, int mapelId, DefaultTableModel tableModel) throws SQLException {
        String sql = """
            SELECT s.siswa_id, s.nama, s.nis, n.nilai_uh, n.nilai_uts, n.nilai_uas, n.nilai_akhir
            FROM siswa s
            LEFT JOIN nilai n ON s.siswa_id = n.siswa_id AND n.semester = ? AND n.mapel_id = ?
            WHERE s.kelas_id = (SELECT kelas_id FROM kelas WHERE nama_kelas = ?)
            GROUP BY s.siswa_id, s.nama, s.nis, n.nilai_uh, n.nilai_uts, n.nilai_uas, n.nilai_akhir
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, semester);
            ps.setInt(2, mapelId);
            ps.setString(3, kelas);
            try (ResultSet rs = ps.executeQuery()) {
                tableModel.setRowCount(0);
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getInt("siswa_id"),
                            rs.getString("nama"),
                            rs.getString("nis"),
                            rs.getObject("nilai_uh") != null ? rs.getInt("nilai_uh") : 0,
                            rs.getObject("nilai_uts") != null ? rs.getInt("nilai_uts") : 0,
                            rs.getObject("nilai_uas") != null ? rs.getInt("nilai_uas") : 0,
                            rs.getObject("nilai_akhir") != null ? rs.getDouble("nilai_akhir") : 0.0
                    });
                }
            }
        }
    }

    public List<Map<String, Object>> getDataKelas() throws SQLException {
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
        }
        return list;
    }

    public List<Map<String, Object>> getDataSiswaKelas(int kelasId) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT id, nama, nis FROM siswa WHERE kelas_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, kelasId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> siswa = new HashMap<>();
                    siswa.put("siswa_id", rs.getInt("id"));
                    siswa.put("nama", rs.getString("nama"));
                    siswa.put("nis", rs.getString("nis"));
                    list.add(siswa);
                }
            }
        }
        return list;
    }

public List<Map<String, Object>> getNilaiByKelasDanMapel(int kelasId, int mapelId, String semester) throws SQLException {
    List<Map<String, Object>> list = new ArrayList<>();
    String sql = """
        SELECT s.id, s.nama, s.nis, n.nilai_uh, n.nilai_uts, n.nilai_uas, n.nilai_akhir
        FROM siswa s
        LEFT JOIN nilai n ON s.id = n.siswa_id AND n.mapel_id = ? AND n.semester = ?
        WHERE s.kelas_id = ?
        """;
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, mapelId);
        ps.setString(2, semester);
        ps.setInt(3, kelasId);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> nilai = new HashMap<>();
                nilai.put("siswa_id", rs.getInt("id"));
                nilai.put("nama", rs.getString("nama"));
                nilai.put("nis", rs.getString("nis"));
                nilai.put("nilai_uh", rs.getObject("nilai_uh") != null ? rs.getInt("nilai_uh") : 0);
                nilai.put("nilai_uts", rs.getObject("nilai_uts") != null ? rs.getInt("nilai_uts") : 0);
                nilai.put("nilai_uas", rs.getObject("nilai_uas") != null ? rs.getInt("nilai_uas") : 0);
                nilai.put("nilai_akhir", rs.getObject("nilai_akhir") != null ? rs.getDouble("nilai_akhir") : 0.0);
                list.add(nilai);
            }
        }
    }
    return list;
}

    public String getNamaGuru(int guruId) throws SQLException {
        String sql = "SELECT nama FROM users WHERE id = ? AND role = 'guru'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, guruId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nama");
                }
            }
        }
        return "Unknown";
    }

    public String getNamaMapel(int mapelId) throws SQLException {
        String sql = "SELECT nama_mapel FROM mapel WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, mapelId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nama_mapel");
                }
            }
        }
        return "Unknown";
    }

    public void startTransaction() throws SQLException {
        if (conn != null && conn.getAutoCommit()) {
            conn.setAutoCommit(false);
        }
    }

    public void commitTransaction() throws SQLException {
        if (conn != null && !conn.getAutoCommit()) {
            conn.commit();
            conn.setAutoCommit(true);
        }
    }

    public void rollbackTransaction() throws SQLException {
        if (conn != null && !conn.getAutoCommit()) {
            conn.rollback();
            conn.setAutoCommit(true);
        }
    }
}