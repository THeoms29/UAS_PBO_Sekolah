package jadwal;

import shared.Koneksi;
import java.sql.*;
import java.util.ArrayList;

public class JadwalModel {

    public ArrayList<String> getComboData(String table, String column) {
        ArrayList<String> list = new ArrayList<>();
        String sql = "SELECT id, " + column + " FROM " + table;

        try (Connection conn = Koneksi.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(rs.getInt("id") + " - " + rs.getString(column));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean isBentrok(int kelasId, String hari, int jamKe) {
        try (Connection conn = Koneksi.getConnection()) {
            String sql = "SELECT * FROM jadwal WHERE kelas_id=? AND hari=? AND jam_ke=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, kelasId);
            ps.setString(2, hari);
            ps.setInt(3, jamKe);
            ResultSet rs = ps.executeQuery();
            return rs.next(); // true jika sudah ada (bentrok)
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public boolean simpanJadwal(int kelasId, int mapelId, int guruId, String hari, int jamKe) {
        String sql = "INSERT INTO jadwal (kelas_id, mapel_id, guru_id, hari, jam_ke) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, kelasId);
            ps.setInt(2, mapelId);
            ps.setInt(3, guruId);
            ps.setString(4, hari);
            ps.setInt(5, jamKe);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<String[]> getJadwalByKelas(int kelasId) {
        ArrayList<String[]> list = new ArrayList<>();

        String sql = """
            SELECT j.hari, j.jam_ke, m.nama_mapel, u.nama
            FROM jadwal j
            JOIN mapel m ON j.mapel_id = m.id
            JOIN users u ON j.guru_id = u.id
            WHERE j.kelas_id = ?
            ORDER BY FIELD(j.hari,'Senin','Selasa','Rabu','Kamis','Jumat','Sabtu'), j.jam_ke
        """;

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, kelasId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("hari"),
                    String.valueOf(rs.getInt("jam_ke")),
                    rs.getString("nama_mapel"),
                    rs.getString("nama")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
