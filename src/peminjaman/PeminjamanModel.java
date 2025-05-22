package peminjaman;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import shared.Koneksi;

public class PeminjamanModel {
    private static final Logger LOGGER = Logger.getLogger(PeminjamanModel.class.getName());
    private static final int DENDA_PER_HARI = 1000; // Konstanta untuk denda
    private final Connection conn;

    public PeminjamanModel() {
        conn = Koneksi.getConnection();
    }

    //getter data semua siswa
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
            LOGGER.log(Level.SEVERE, "Gagal mengambil daftar siswa", e);
        }

        return list;
    }

    //getter data semua buku
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
            LOGGER.log(Level.SEVERE, "Gagal mengambil daftar buku", e);
        }

        return list;
    }

    //getter riwayat peminjaman berdasarkan bulan dan tahun
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
                list = extractPeminjamanDataFromResultSet(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gagal mengambil riwayat peminjaman berdasarkan bulan: " + bulan + ", tahun: " + tahun, e);
        }
    
        return list;
    }

    //menambah data peminjaman baru
    public boolean tambahPeminjaman(int siswaId, int bukuId, Date tglPinjam, Date tglKembali) {
        // Validasi input sederhana
        if (siswaId <= 0 || bukuId <= 0 || tglPinjam == null || tglKembali == null) {
            LOGGER.warning("Input tidak valid untuk tambah peminjaman");
            return false;
        }
        
        // Validasi tanggal kembali harus setelah tanggal pinjam
        if (tglKembali.before(tglPinjam)) {
            LOGGER.warning("Tanggal kembali tidak boleh sebelum tanggal pinjam");
            return false;
        }
        
        String sql = "INSERT INTO peminjaman (siswa_id, buku_id, tanggal_pinjam, tanggal_kembali) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, siswaId);
            stmt.setInt(2, bukuId);
            stmt.setDate(3, tglPinjam);
            stmt.setDate(4, tglKembali);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gagal menambahkan peminjaman", e);
            return false;
        }
    }

    //method untuk memproses pengembalian buku
    public boolean kembalikanBuku(int peminjamanId, Date tglDikembalikan, int denda) {
        if (peminjamanId <= 0 || tglDikembalikan == null || denda < 0) {
            LOGGER.warning("Input tidak valid untuk pengembalian buku");
            return false;
        }
        
        String sql = "UPDATE peminjaman SET tanggal_dikembalikan = ?, denda = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, tglDikembalikan);
            stmt.setInt(2, denda);
            stmt.setInt(3, peminjamanId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gagal mengembalikan buku", e);
            return false;
        }
    }

    //ekspor data ke format csv
    public boolean exportToCSV(ArrayList<String[]> data, String filePath) {
        if (data == null || data.isEmpty() || filePath == null || filePath.isEmpty()) {
            LOGGER.warning("Data atau path file tidak valid untuk ekspor CSV");
            return false;
        }
        
        try (FileWriter writer = new FileWriter(filePath)) {
            // Tulis header
            writer.append("ID,Siswa,Buku,Tanggal Pinjam,Tanggal Kembali,Tanggal Dikembalikan,Denda\n");
            
            // Tulis data
            for (String[] row : data) {
                for (int i = 0; i < row.length; i++) {
                    // Handle koma dalam data
                    String cell = row[i] != null ? row[i].replace("\"", "\"\"") : ""; // Escape quote dengan double quote
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
            LOGGER.log(Level.SEVERE, "Gagal mengekspor data ke CSV", e);
            return false;
        }
    }

    public boolean exportToPDF(ArrayList<String[]> data, String filePath) {
        if (data == null || data.isEmpty() || filePath == null || filePath.isEmpty()) {
            LOGGER.warning("Data atau path file tidak valid untuk ekspor PDF");
            return false;
        }
        
        try {
            // Buat dokumen PDF baru
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            
            // Tambahkan judul
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Paragraph title = new Paragraph("Laporan Riwayat Peminjaman Buku", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            
            // Tambahkan tanggal laporan
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss");
            Paragraph dateInfo = new Paragraph("Tanggal Laporan: " + sdf.format(new java.util.Date()), normalFont);
            dateInfo.setSpacingAfter(20);
            document.add(dateInfo);
            
            // Buat tabel
            PdfPTable table = new PdfPTable(7); // 7 kolom sesuai data
            table.setWidthPercentage(100);
            
            // Tentukan lebar kolom
            float[] columnWidths = {0.5f, 2f, 2f, 1.5f, 1.5f, 1.5f, 1f};
            table.setWidths(columnWidths);
            
            // Header tabel
            String[] headers = {"ID", "Siswa", "Buku", "Tanggal Pinjam", "Tanggal Kembali", "Tanggal Dikembalikan", "Denda"};
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setPadding(5);
                table.addCell(cell);
            }
            
            // Isi tabel
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
            for (String[] row : data) {
                for (int i = 0; i < row.length; i++) {
                    PdfPCell cell = new PdfPCell(new Phrase(row[i], cellFont));
                    // Rata kanan untuk kolom ID dan denda
                    if (i == 0 || i == 6) {
                        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    } 
                    // Rata tengah untuk tanggal
                    else if (i >= 3 && i <= 5) {
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    }
                    // Rata kiri untuk teks
                    else {
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    }
                    cell.setPadding(5);
                    table.addCell(cell);
                }
            }
            
            document.add(table);
            
            // Tambahkan catatan kaki
            Paragraph footer = new Paragraph("Total data: " + data.size() + " peminjaman", normalFont);
            footer.setSpacingBefore(20);
            document.add(footer);
            
            document.close();
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Gagal mengekspor data ke PDF", e);
            return false;
        }
    }

    //getter riwayat peminjaman
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
            list = extractPeminjamanDataFromResultSet(rs);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gagal mengambil riwayat peminjaman", e);
        }

        return list;
    }

    //method mencari riwayat peminjaman
    public ArrayList<String[]> getRiwayatPeminjaman(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getRiwayatPeminjaman(); // Return semua data jika keyword kosong
        }
        
        ArrayList<String[]> list = new ArrayList<>();
        String sql = """
            SELECT p.id, s.nama AS siswa, b.judul AS buku,
                   p.tanggal_pinjam, p.tanggal_kembali,
                   p.tanggal_dikembalikan, p.denda
            FROM peminjaman p
            JOIN siswa s ON p.siswa_id = s.id
            JOIN buku b ON p.buku_id = b.id
            WHERE LOWER(s.nama) LIKE ? OR LOWER(b.judul) LIKE ?
            ORDER BY p.tanggal_pinjam DESC
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String kw = "%" + keyword.toLowerCase() + "%";
            stmt.setString(1, kw);
            stmt.setString(2, kw);

            try (ResultSet rs = stmt.executeQuery()) {
                list = extractPeminjamanDataFromResultSet(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Gagal mencari riwayat peminjaman: " + keyword, e);
        }

        return list;
    }

    //method menghitung denda
    public int hitungDenda(Date tanggalKembali, Date tanggalDikembalikan) {
        if (tanggalKembali == null || tanggalDikembalikan == null) {
            return 0;
        }
        
        long selisih = tanggalDikembalikan.getTime() - tanggalKembali.getTime();
        long hariTerlambat = selisih / (1000 * 60 * 60 * 24);

        return (hariTerlambat > 0) ? (int) hariTerlambat * DENDA_PER_HARI : 0;
    }
    
    //method untuk ekstrak data peminjaman dari resultset
    private ArrayList<String[]> extractPeminjamanDataFromResultSet(ResultSet rs) throws SQLException {
        ArrayList<String[]> list = new ArrayList<>();
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
        return list;
    }
}