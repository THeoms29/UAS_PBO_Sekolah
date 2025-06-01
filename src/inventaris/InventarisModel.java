package inventaris;

import shared.Koneksi;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventarisModel {
    // 1. UPDATE method getDataInventaris() - tambahkan kolom tanggal
    public List<Object[]> getDataInventaris() {
        List<Object[]> data = new ArrayList<>();
        try (Connection conn = Koneksi.getConnection()) {
            String query = "SELECT * FROM inventaris ORDER BY tanggal_input DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                data.add(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nama_barang"),
                    rs.getString("lokasi"),
                    rs.getInt("jumlah"),
                    rs.getString("kondisi"),
                    rs.getDate("tanggal_input") // Tambahan kolom tanggal
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }
    // 2. UPDATE method tambahInventaris() - set tanggal saat insert
    public void tambahInventaris(String nama, String lokasi, int jumlah, String kondisi) {
        try (Connection conn = Koneksi.getConnection()) {
            String sql = "INSERT INTO inventaris (nama_barang, lokasi, jumlah, kondisi, tanggal_input) VALUES (?, ?, ?, ?, CURRENT_DATE)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nama);
            ps.setString(2, lokasi);
            ps.setInt(3, jumlah);
            ps.setString(4, kondisi);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void updateInventaris(int id,String nama,String lokasi,int jumlah,String kondisi){
        try(Connection conn=Koneksi.getConnection()){
            String sql="UPDATE inventaris SET nama_barang=?, lokasi=?, jumlah=?, kondisi=? WHERE id=?";
            PreparedStatement ps=conn.prepareStatement(sql);
            ps.setString(1, nama);
            ps.setString(2, lokasi);
            ps.setInt(3, jumlah);
            ps.setString(4, kondisi);
            ps.setInt(5, id);
            ps.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
    public void hapusInventaris(int id){
        try(Connection conn=Koneksi.getConnection()){
            String sql="DELETE FROM inventaris WHERE id=?";
            PreparedStatement ps=conn.prepareStatement(sql);
            ps.setInt(1,id);
            ps.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
    // 3. TAMBAHAN method untuk filter berdasarkan bulan dan tahun
    public List<Object[]> getDataInventarisByMonth(int month, int year) {
        List<Object[]> data = new ArrayList<>();
        try (Connection conn = Koneksi.getConnection()) {
            String query = "SELECT * FROM inventaris WHERE MONTH(tanggal_input) = ? AND YEAR(tanggal_input) = ? ORDER BY tanggal_input DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, month);
            stmt.setInt(2, year);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                data.add(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nama_barang"),
                    rs.getString("lokasi"),
                    rs.getInt("jumlah"),
                    rs.getString("kondisi"),
                    rs.getDate("tanggal_input")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }
    // 4. TAMBAHAN method untuk filter berdasarkan tahun saja
    public List<Object[]> getDataInventarisByYear(int year) {
        List<Object[]> data = new ArrayList<>();
        try (Connection conn = Koneksi.getConnection()) {
            String query = "SELECT * FROM inventaris WHERE YEAR(tanggal_input) = ? ORDER BY tanggal_input DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, year);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                data.add(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nama_barang"),
                    rs.getString("lokasi"),
                    rs.getInt("jumlah"),
                    rs.getString("kondisi"),
                    rs.getDate("tanggal_input")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }
    // 5. TAMBAHAN method untuk mendapatkan daftar tahun yang tersedia
    public List<Integer> getAvailableYears() {
        List<Integer> years = new ArrayList<>();
        try (Connection conn = Koneksi.getConnection()) {
            String query = "SELECT DISTINCT YEAR(tanggal_input) as tahun FROM inventaris ORDER BY tahun DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                years.add(rs.getInt("tahun"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return years;
    }
}
