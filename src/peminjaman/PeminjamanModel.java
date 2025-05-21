package peminjaman;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import shared.Koneksi;

public class PeminjamanModel {
    private Connection conn;

    public PeminjamanModel() {
        conn = Koneksi.getConnection();
    }

    public ArrayList<String[]> getDaftarSiswa() {
        ArrayList<String[]> list = new ArrayList<>();
        String sql = "SELECT id, nama FROM siswa ORDER BY nama ASC";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("id")),
                    rs.getString("nama")
                });
            }
        } catch (SQLException e) {
            System.err.println("Gagal mengambil daftar siswa: " + e.getMessage());
        }

        return list;
    }

    public ArrayList<String[]> getDaftarBuku() {
        ArrayList<String[]> list = new ArrayList<>();
        String sql = "SELECT id, judul FROM buku ORDER BY judul ASC";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("id")),
                    rs.getString("judul")
                });
            }
        } catch (SQLException e) {
            System.err.println("Gagal mengambil daftar buku: " + e.getMessage());
        }

        return list;
    }

    public ArrayList<String[]> getRiwayatPeminjamanByBulan(int bulan, int tahun) {
        ArrayList<String[]> list = new ArrayList<>();
        String sql = """
            SELECT p.id, s.nama AS siswa, b.judul AS buku,
                   p.tanggal_pinjam, p.tanggal_kembali,
                   p.tanggal_dikembalikan, p.denda
            FROM peminjaman p
            JOIN siswa s ON p.siswa_id = s.id
            JOIN buku b ON p.buku_id = b.id
            WHERE MONTH(p.tanggal_pinjam) = ? AND YEAR(p.tanggal_pinjam) = ?
            ORDER BY p.tanggal_pinjam DESC
        """;
    
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bulan);
            stmt.setInt(2, tahun);
    
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[]{
                        String.valueOf(rs.getInt("id")),
                        rs.getString("siswa"),
                        rs.getString("buku"),
                        String.valueOf(rs.getDate("tanggal_pinjam")),
                        String.valueOf(rs.getDate("tanggal_kembali")),
                        rs.getDate("tanggal_dikembalikan") != null
                            ? rs.getDate("tanggal_dikembalikan").toString() : "-",
                        String.valueOf(rs.getInt("denda"))
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal mengambil riwayat peminjaman berdasarkan bulan: " + e.getMessage());
        }
    
        return list;
    }

    public boolean tambahPeminjaman(int siswaId, int bukuId, Date tglPinjam, Date tglKembali) {
        String sql = "INSERT INTO peminjaman (siswa_id, buku_id, tanggal_pinjam, tanggal_kembali) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, siswaId);
            stmt.setInt(2, bukuId);
            stmt.setDate(3, tglPinjam);
            stmt.setDate(4, tglKembali);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal menambahkan peminjaman: " + e.getMessage());
            return false;
        }
    }

    public boolean kembalikanBuku(int peminjamanId, Date tglDikembalikan, int denda) {
        String sql = "UPDATE peminjaman SET tanggal_dikembalikan = ?, denda = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, tglDikembalikan);
            stmt.setInt(2, denda);
            stmt.setInt(3, peminjamanId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal mengembalikan buku: " + e.getMessage());
            return false;
        }
    }

    public boolean exportToCSV(ArrayList<String[]> data, String filePath) {
    try (FileWriter writer = new FileWriter(filePath)) {
        // Tulis header
        writer.append("ID,Siswa,Buku,Tanggal Pinjam,Tanggal Kembali,Tanggal Dikembalikan,Denda\n");
        
        // Tulis data
        for (String[] row : data) {
            for (int i = 0; i < row.length; i++) {
                // Handle koma dalam data
                String cell = row[i].replace("\"", "\"\""); // Escape quote dengan double quote
                if (cell.contains(",") || cell.contains("\"") || cell.contains("\n")) {
                    cell = "\"" + cell + "\"";
                }
                writer.append(cell);
                if (i < row.length - 1) {
                    writer.append(",");
                }
            }
            writer.append("\n");
        }
        writer.flush();
        return true;
    } catch (IOException e) {
        System.err.println("Gagal mengekspor data ke CSV: " + e.getMessage());
        return false;
    }
}

    public ArrayList<String[]> getRiwayatPeminjaman() {
        ArrayList<String[]> list = new ArrayList<>();
        String sql = """
            SELECT p.id, s.nama AS siswa, b.judul AS buku,
                   p.tanggal_pinjam, p.tanggal_kembali,
                   p.tanggal_dikembalikan, p.denda
            FROM peminjaman p
            JOIN siswa s ON p.siswa_id = s.id
            JOIN buku b ON p.buku_id = b.id
            ORDER BY p.tanggal_pinjam DESC
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("id")),
                    rs.getString("siswa"),
                    rs.getString("buku"),
                    String.valueOf(rs.getDate("tanggal_pinjam")),
                    String.valueOf(rs.getDate("tanggal_kembali")),
                    rs.getDate("tanggal_dikembalikan") != null
                        ? rs.getDate("tanggal_dikembalikan").toString() : "-",
                    String.valueOf(rs.getInt("denda"))
                });
            }
        } catch (SQLException e) {
            System.err.println("Gagal mengambil riwayat peminjaman: " + e.getMessage());
        }

        return list;
    }

    // 🔍 Metode baru: cari berdasarkan keyword (siswa atau buku)
    public ArrayList<String[]> getRiwayatPeminjaman(String keyword) {
        ArrayList<String[]> list = new ArrayList<>();
        String sql = """
            SELECT p.id, s.nama AS siswa, b.judul AS buku,
                   p.tanggal_pinjam, p.tanggal_kembali,
                   p.tanggal_dikembalikan, p.denda
            FROM peminjaman p
            JOIN siswa s ON p.siswa_id = s.id
            JOIN buku b ON p.buku_id = b.id
            WHERE s.nama LIKE ? OR b.judul LIKE ?
            ORDER BY p.tanggal_pinjam DESC
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            stmt.setString(1, kw);
            stmt.setString(2, kw);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[]{
                        String.valueOf(rs.getInt("id")),
                        rs.getString("siswa"),
                        rs.getString("buku"),
                        String.valueOf(rs.getDate("tanggal_pinjam")),
                        String.valueOf(rs.getDate("tanggal_kembali")),
                        rs.getDate("tanggal_dikembalikan") != null
                            ? rs.getDate("tanggal_dikembalikan").toString() : "-",
                        String.valueOf(rs.getInt("denda"))
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal mencari riwayat peminjaman: " + e.getMessage());
        }

        return list;
    }

    public int hitungDenda(Date tanggalKembali, Date tanggalDikembalikan) {
        long selisih = tanggalDikembalikan.getTime() - tanggalKembali.getTime();
        long hariTerlambat = selisih / (1000 * 60 * 60 * 24);

        return (hariTerlambat > 0) ? (int) hariTerlambat * 1000 : 0; // misal denda 1000 per hari
    }
}
