package inventaris;

import shared.Koneksi;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventarisModel {
    public List<Object[]>getDataInventaris(){
        List<Object[]>data=new ArrayList<>();
        try(Connection conn=Koneksi.getConnection()){
            String query="SELECT*FROM inventaris";
            PreparedStatement stmt=conn.prepareStatement(query);
            ResultSet rs=stmt.executeQuery();
            while(rs.next()){
                data.add(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nama_barang"),
                    rs.getString("lokasi"),
                    rs.getInt("jumlah"),
                    rs.getString("kondisi")
                });
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return data;
    }
    public void tambahInventaris(String nama,String lokasi,int jumlah,String kondisi){
        try(Connection conn=Koneksi.getConnection()){
            String sql="INSERT INTO inventaris (nama_barang, lokasi, jumlah, kondisi) VALUES (?, ?, ?, ?)";
            PreparedStatement ps=conn.prepareStatement(sql);
            ps.setString(1, nama);
            ps.setString(2, lokasi);
            ps.setInt(3, jumlah);
            ps.setString(4, kondisi);
            ps.executeUpdate();
        }catch(SQLException e){
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
}
