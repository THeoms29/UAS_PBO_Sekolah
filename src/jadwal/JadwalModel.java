package jadwal;

import java.sql.*;
import java.util.*;

import shared.Koneksi;

public class JadwalModel {
    private Connection conn;

    public JadwalModel() {
        this.conn = Koneksi.getConnection();
    }

    public List<String[]> getAllKelas() throws SQLException {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT id, nama_kelas FROM kelas";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new String[]{rs.getString("id"), rs.getString("nama_kelas")});
            }
        }
        return list;
    }

    public List<String[]> getAllMapel() throws SQLException {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT id, nama_mapel FROM mapel";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new String[]{rs.getString("id"), rs.getString("nama_mapel")});
            }
        }
        return list;
    }

    public List<String[]> getAllGuru() throws SQLException {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT id, nama FROM users WHERE role = 'guru'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new String[]{rs.getString("id"), rs.getString("nama")});
            }
        }
        return list;
    }
    
    public void tambahMapel(String namaMapel) {
    try {
        String sql = "INSERT INTO mapel (nama_mapel) VALUES (?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, namaMapel);
        stmt.executeUpdate();
    } catch (SQLException e) {
        System.out.println("Gagal menambah mapel: " + e.getMessage());
    }
    }

    public boolean isBentrok(String hari, int jamKe, int kelasId, int guruId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM jadwal WHERE hari = ? AND jam_ke = ? AND (kelas_id = ? OR guru_id = ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hari);
            ps.setInt(2, jamKe);
            ps.setInt(3, kelasId);
            ps.setInt(4, guruId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public void insertJadwal(String hari, int jamKe, int kelasId, int mapelId, int guruId) throws SQLException {
        String sql = "INSERT INTO jadwal (hari, jam_ke, kelas_id, mapel_id, guru_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hari);
            ps.setInt(2, jamKe);
            ps.setInt(3, kelasId);
            ps.setInt(4, mapelId);
            ps.setInt(5, guruId);
            ps.executeUpdate();
        }
    }

    public List<String[]> getJadwalByKelas(int kelasId) throws SQLException {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT hari, jam_ke, m.nama_mapel, u.nama FROM jadwal j JOIN mapel m ON j.mapel_id = m.id JOIN users u ON j.guru_id = u.id WHERE j.kelas_id = ? ORDER BY hari, jam_ke";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, kelasId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{rs.getString("hari"), rs.getString("jam_ke"), rs.getString("nama_mapel"), rs.getString("nama")});
            }
        }
        return list;
    }

    public List<String[]> getJadwalByGuru(int guruId) throws SQLException {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT hari, jam_ke, k.nama_kelas, m.nama_mapel FROM jadwal j JOIN kelas k ON j.kelas_id = k.id JOIN mapel m ON j.mapel_id = m.id WHERE j.guru_id = ? ORDER BY hari, jam_ke";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, guruId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{rs.getString("hari"), rs.getString("jam_ke"), rs.getString("nama_kelas"), rs.getString("nama_mapel")});
            }
        }
        return list;
    }

    // Helper methods untuk import CSV
    public int getKelasIdByName(String namaKelas) throws SQLException {
        String sql = "SELECT id FROM kelas WHERE nama_kelas = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, namaKelas);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1; // Tidak ditemukan
    }

    public int getMapelIdByName(String namaMapel) throws SQLException {
        String sql = "SELECT id FROM mapel WHERE nama_mapel = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, namaMapel);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1; // Tidak ditemukan
    }

    public int getGuruIdByName(String namaGuru) throws SQLException {
        String sql = "SELECT id FROM users WHERE nama = ? AND role = 'guru'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, namaGuru);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1; // Tidak ditemukan
    }
}