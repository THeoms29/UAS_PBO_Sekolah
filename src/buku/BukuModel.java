package buku;

import java.sql.*;
import java.util.ArrayList;
import shared.Koneksi;

public class BukuModel {
    private Connection conn;

    public BukuModel() {
        conn = Koneksi.getConnection();
    }

    public ArrayList<String[]> getDaftarBuku() {
        ArrayList<String[]> list = new ArrayList<>();
        String sql = "SELECT id, judul, penulis, jumlah FROM buku ORDER BY judul ASC";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("id")),
                    rs.getString("judul"),
                    rs.getString("penulis"),
                    String.valueOf(rs.getInt("jumlah"))
                });
            }
        } catch (SQLException e) {
            System.err.println("Gagal mengambil daftar buku: " + e.getMessage());
        }

        return list;
    }

    public ArrayList<String[]> cariBuku(String keyword) {
        ArrayList<String[]> list = new ArrayList<>();
        String sql = "SELECT id, judul, penulis, jumlah FROM buku WHERE judul LIKE ? OR penulis LIKE ? ORDER BY judul ASC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            stmt.setString(1, kw);
            stmt.setString(2, kw);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[]{
                        String.valueOf(rs.getInt("id")),
                        rs.getString("judul"),
                        rs.getString("penulis"),
                        String.valueOf(rs.getInt("jumlah"))
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal mencari buku: " + e.getMessage());
        }

        return list;
    }

    public boolean tambahBuku(String judul, String penulis, int jumlah) {
        String sql = "INSERT INTO buku (judul, penulis, jumlah) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, judul);
            stmt.setString(2, penulis);
            stmt.setInt(3, jumlah);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal menambahkan buku: " + e.getMessage());
            return false;
        }
    }

    public boolean updateBuku(int id, String judul, String penulis, int jumlah) {
        String sql = "UPDATE buku SET judul = ?, penulis = ?, jumlah = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, judul);
            stmt.setString(2, penulis);
            stmt.setInt(3, jumlah);
            stmt.setInt(4, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal mengupdate buku: " + e.getMessage());
            return false;
        }
    }

    public boolean hapusBuku(int id) {
        // Periksa apakah buku sedang dipinjam
        if (isBukuDipinjam(id)) {
            System.err.println("Buku masih dalam peminjaman, tidak dapat dihapus.");
            return false;
        }

        String sql = "DELETE FROM buku WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal menghapus buku: " + e.getMessage());
            return false;
        }
    }

    private boolean isBukuDipinjam(int bukuId) {
        String sql = "SELECT COUNT(*) AS total FROM peminjaman WHERE buku_id = ? AND tanggal_dikembalikan IS NULL";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bukuId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total") > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal memeriksa status peminjaman buku: " + e.getMessage());
        }
        
        // Jika error, asumsikan aman untuk menghapus
        return false;
    }

    public String[] getBuku(int id) {
        String sql = "SELECT id, judul, penulis, jumlah FROM buku WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                        String.valueOf(rs.getInt("id")),
                        rs.getString("judul"),
                        rs.getString("penulis"),
                        String.valueOf(rs.getInt("jumlah"))
                    };
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal mengambil data buku: " + e.getMessage());
        }
        
        return null;
    }
}
