package absensi;

import java.sql.*;
import java.util.ArrayList;
import shared.Koneksi;

public class AbsensiModel {
    private Connection conn;

    public AbsensiModel() {
        conn = Koneksi.getConnection();
        if (conn == null) {
            System.err.println("Gagal mendapatkan koneksi ke database.");
        }
    }

    public ArrayList<String> getDaftarKelas() {
        ArrayList<String> kelasList = new ArrayList<>();
        String sql = "SELECT nama_kelas FROM kelas ORDER BY nama_kelas ASC";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                kelasList.add(rs.getString("nama_kelas"));
            }
        } catch (SQLException e) {
            System.err.println("Gagal mengambil data kelas: " + e.getMessage());
        }

        return kelasList;
    }

    public ArrayList<String[]> getDaftarSiswaByKelas(String namaKelas) {
        ArrayList<String[]> siswaList = new ArrayList<>();
        String sql = "SELECT siswa.id, siswa.nama, siswa.nis " +
                     "FROM siswa " +
                     "JOIN kelas ON siswa.kelas_id = kelas.id " +
                     "WHERE kelas.nama_kelas = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, namaKelas);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String[] siswa = new String[3];
                    siswa[0] = String.valueOf(rs.getInt("id"));
                    siswa[1] = rs.getString("nama");
                    siswa[2] = rs.getString("nis");
                    siswaList.add(siswa);
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal mengambil data siswa: " + e.getMessage());
        }

        return siswaList;
    }

    public boolean simpanAbsensi(int siswaId, Date tanggal, String status) {
        String sql = "INSERT INTO absensi (siswa_id, tanggal, status) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, siswaId);
            stmt.setDate(2, tanggal);
            stmt.setString(3, status);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal menyimpan absensi: " + e.getMessage());
            return false;
        }
    }

    public ArrayList<String[]> getRiwayatAbsensi(int siswaId) {
        ArrayList<String[]> data = new ArrayList<>();
        String sql = "SELECT tanggal, status FROM absensi WHERE siswa_id = ? ORDER BY tanggal DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, siswaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    data.add(new String[] {
                        rs.getDate("tanggal").toString(),
                        rs.getString("status")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal mengambil riwayat absensi: " + e.getMessage());
        }

        return data;
    }

    public ArrayList<String[]> getRekapBulanan(String namaKelas, int bulan, int tahun) {
        ArrayList<String[]> hasil = new ArrayList<>();
        String sql = """
            SELECT s.nama, s.nis,
                   SUM(CASE WHEN a.status = 'Hadir' THEN 1 ELSE 0 END) AS hadir,
                   SUM(CASE WHEN a.status = 'Izin' THEN 1 ELSE 0 END) AS izin,
                   SUM(CASE WHEN a.status = 'Sakit' THEN 1 ELSE 0 END) AS sakit,
                   SUM(CASE WHEN a.status = 'Alpha' THEN 1 ELSE 0 END) AS alpha
            FROM siswa s
            JOIN kelas k ON s.kelas_id = k.id
            LEFT JOIN absensi a ON a.siswa_id = s.id 
                AND MONTH(a.tanggal) = ? 
                AND YEAR(a.tanggal) = ?
            WHERE k.nama_kelas = ?
            GROUP BY s.id, s.nama, s.nis
            ORDER BY s.nama ASC
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bulan);
            stmt.setInt(2, tahun);
            stmt.setString(3, namaKelas);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    hasil.add(new String[] {
                        rs.getString("nama"),
                        rs.getString("nis"),
                        String.valueOf(rs.getInt("hadir")),
                        String.valueOf(rs.getInt("izin")),
                        String.valueOf(rs.getInt("sakit")),
                        String.valueOf(rs.getInt("alpha"))
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal mengambil rekap bulanan: " + e.getMessage());
        }

        return hasil;
    }
    public boolean tambahSiswa(String nama, String nis, String namaKelas) {
    String sql = """
        INSERT INTO siswa (nama, nis, kelas_id)
        SELECT ?, ?, id FROM kelas WHERE nama_kelas = ?
    """;

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, nama);
        stmt.setString(2, nis);
        stmt.setString(3, namaKelas);
        return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
        System.err.println("Gagal menambahkan siswa: " + e.getMessage());
        return false;
    }
    }

}
