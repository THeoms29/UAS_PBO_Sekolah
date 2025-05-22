package peminjaman;

import buku.BukuController;
import buku.BukuModel;
import buku.BukuView;
import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class PeminjamanController {
    private static final Logger LOGGER = Logger.getLogger(PeminjamanController.class.getName());
    private static final String[] TABLE_COLUMNS = {"ID", "Siswa", "Buku", "Pinjam", "Jatuh Tempo", "Dikembalikan", "Denda"};
    
    private final PeminjamanModel model;
    private final PeminjamanView view;

    public PeminjamanController(PeminjamanModel model, PeminjamanView view) {
        this.model = model;
        this.view = view;

        initComponents();
    }
    
    //inisialisasi komponen dan handler
    private void initComponents() {
        isiComboSiswa();
        isiComboBuku();

        view.setSimpanAction(e -> simpanPeminjaman());
        view.setKembalikanAction(e -> prosesPengembalian());
        view.setMuatRiwayatAction(e -> tampilkanRiwayat());
        view.setCariSiswaAction(e -> cariSiswa());
        view.setCariRiwayatAction(e -> cariRiwayat());
        view.setKeBukuAction(e -> bukaModulBuku());
        view.setExportCsvAction(e -> eksporRiwayatPeminjaman());
        view.setExportPdfAction(e -> eksporRiwayatPeminjamanKePDF());

        //istener untuk tabel riwayat
        view.tableRiwayat.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = view.tableRiwayat.getSelectedRow() != -1;
                view.enableTanggalDikembalikan(hasSelection);
            }
        });
    
        resetTanggalDikembalikan();
        tampilkanRiwayat();
    }

    //Mengisi ComboBox siswa
    private void isiComboSiswa() {
        view.comboSiswa.removeAllItems();
        ArrayList<String[]> siswaList = model.getDaftarSiswa();
        
        if (siswaList.isEmpty()) {
            view.statusLabel.setText("Tidak ada data siswa");
            return;
        }
        
        for (String[] s : siswaList) {
            view.comboSiswa.addItem(s[0] + " - " + s[1]);
        }
    }

    //Mengisi ComboBox buku
    private void isiComboBuku() {
        view.comboBuku.removeAllItems();
        
        ArrayList<String[]> bukuList = model.getDaftarBuku();
        if (bukuList.isEmpty()) {
            view.statusLabel.setText("Tidak ada data buku");
            return;
        }
        
        for (String[] b : bukuList) {
            view.comboBuku.addItem(b[0] + " - " + b[1]);
        }
    }

    //Mencari siswa berdasarkan keyword
    private void cariSiswa() {
        String keyword = view.getInputPencarianSiswa().toLowerCase();
        if (keyword.isEmpty()) {
            isiComboSiswa(); // Reset ke semua data jika pencarian kosong
            return;
        }
        
        view.comboSiswa.removeAllItems();

        ArrayList<String[]> siswaList = model.getDaftarSiswa();
        boolean ditemukan = false;
        for (String[] s : siswaList) {
            String nama = s[1].toLowerCase();
            if (nama.contains(keyword)) {
                view.comboSiswa.addItem(s[0] + " - " + s[1]);
                ditemukan = true;
            }
        }

        if (!ditemukan) {
            JOptionPane.showMessageDialog(view, "Siswa tidak ditemukan.");
        }
    }

    //Mencari riwayat peminjaman
    private void cariRiwayat() {
        String keyword = view.getInputPencarianRiwayat();
        
        if (keyword.isEmpty()) {
            tampilkanRiwayat(); // Reset ke semua data jika pencarian kosong
            return;
        }
        
        // Menggunakan metode di model untuk pencarian
        ArrayList<String[]> data = model.getRiwayatPeminjaman(keyword);
        updateTableRiwayat(data);
        
        if (data.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Tidak ditemukan data yang cocok.");
        }
    }

    //Menyimpan data peminjaman baru
    private void simpanPeminjaman() {
        try {
            if (view.comboSiswa.getSelectedItem() == null || view.comboBuku.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(view, "Pilih siswa dan buku terlebih dahulu.");
                return;
            }
            
            int siswaId = Integer.parseInt(view.comboSiswa.getSelectedItem().toString().split(" - ")[0]);
            int bukuId = Integer.parseInt(view.comboBuku.getSelectedItem().toString().split(" - ")[0]);
            
            Date tglPinjam = new Date(((java.util.Date) view.tanggalPinjam.getValue()).getTime());
            Date tglKembali = new Date(((java.util.Date) view.tanggalKembali.getValue()).getTime());
            
            // Validasi tanggal
            if (tglKembali.before(tglPinjam)) {
                JOptionPane.showMessageDialog(view, "Tanggal kembali tidak boleh sebelum tanggal pinjam.");
                return;
            }

            boolean berhasil = model.tambahPeminjaman(siswaId, bukuId, tglPinjam, tglKembali);

            if (berhasil) {
                JOptionPane.showMessageDialog(view, "Peminjaman berhasil disimpan.");
                tampilkanRiwayat();
                view.statusLabel.setText("Peminjaman berhasil disimpan");
            } else {
                JOptionPane.showMessageDialog(view, "Gagal menyimpan peminjaman.");
                view.statusLabel.setText("Gagal menyimpan peminjaman");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(view, "ID siswa atau buku tidak valid.");
            LOGGER.log(Level.WARNING, "Format ID tidak valid", e);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Input tidak valid: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error saat menyimpan peminjaman", e);
        }
    }

    //Memproses pengembalian buku
    private void prosesPengembalian() {
        int baris = view.tableRiwayat.getSelectedRow();
        if (baris == -1) {
            JOptionPane.showMessageDialog(view, "Pilih baris peminjaman terlebih dahulu.");
            return;
        }
        
        // Cek apakah sudah dikembalikan
        String statusDikembalikan = view.tableRiwayat.getValueAt(baris, 5).toString();
        if (!statusDikembalikan.equals("-")) {
            JOptionPane.showMessageDialog(view, "Buku ini sudah dikembalikan pada tanggal " + statusDikembalikan);
            return;
        }

        try {
            int idPeminjaman = Integer.parseInt(view.tableRiwayat.getValueAt(baris, 0).toString());
            Date tglKembali = Date.valueOf(view.tableRiwayat.getValueAt(baris, 4).toString());
            Date tglDikembalikan = new Date(((java.util.Date) view.tanggalDikembalikan.getValue()).getTime());

            int denda = model.hitungDenda(tglKembali, tglDikembalikan);
            
            // Konfirmasi pengembalian dengan denda
            int konfirmasi = JOptionPane.showConfirmDialog(view, 
                    "Proses pengembalian dengan denda Rp " + denda + "?", 
                    "Konfirmasi Pengembalian", JOptionPane.YES_NO_OPTION);
                    
            if (konfirmasi != JOptionPane.YES_OPTION) {
                return;
            }
            
            boolean sukses = model.kembalikanBuku(idPeminjaman, tglDikembalikan, denda);

            if (sukses) {
                JOptionPane.showMessageDialog(view, "Pengembalian berhasil. Denda: Rp " + denda);
                tampilkanRiwayat();
                resetTanggalDikembalikan();
                view.statusLabel.setText("Pengembalian berhasil. Denda: Rp " + denda);
            } else {
                JOptionPane.showMessageDialog(view, "Gagal memproses pengembalian.");
                view.statusLabel.setText("Gagal memproses pengembalian");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Input tidak valid: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Error saat proses pengembalian", e);
        }
    }

    //Menampilkan semua riwayat peminjaman
    private void tampilkanRiwayat() {
        ArrayList<String[]> data = model.getRiwayatPeminjaman();
        updateTableRiwayat(data);
    }
    
    //Update tabel riwayat dengan data baru
    private void updateTableRiwayat(ArrayList<String[]> data) {
        DefaultTableModel tableModel = new DefaultTableModel(TABLE_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Mencegah cell dapat diedit
            }
        };
        
        for (String[] row : data) {
            tableModel.addRow(row);
        }
        
        view.tableRiwayat.setModel(tableModel);
        view.tableRiwayat.getColumnModel().getColumn(0).setPreferredWidth(30); // ID
        view.tableRiwayat.getColumnModel().getColumn(1).setPreferredWidth(150); // Siswa
        view.tableRiwayat.getColumnModel().getColumn(2).setPreferredWidth(200); // Buku
        
        // Update status label
        view.statusLabel.setText("Total data: " + data.size() + " peminjaman");
    }

    //Reset tanggal pengembalian ke hari ini
    private void resetTanggalDikembalikan() {
        view.tanggalDikembalikan.setValue(new java.util.Date());
        view.enableTanggalDikembalikan(false);
    }
    
    //membuka modul buku
    private void bukaModulBuku() {
        int confirm = JOptionPane.showConfirmDialog(view, 
                "Buka modul manajemen buku?", 
                "Konfirmasi Navigasi", 
                JOptionPane.YES_NO_OPTION);
                
        if (confirm == JOptionPane.YES_OPTION) {
            // Simpan referensi ke JFrame
            JFrame currentFrame = view;
            
            // Tutup panel konten saat ini
            currentFrame.getContentPane().removeAll();
            
            // Buat dan inisialisasi modul buku
            BukuModel bukuModel = new BukuModel();
            BukuView bukuView = new BukuView();
            
            // Ganti JFrame dengan panel konten dari bukuView
            currentFrame.setContentPane(bukuView.getContentPane());
            
            // Perbarui title
            currentFrame.setTitle("Modul Manajemen Buku");
            
            // Set controller untuk buku
            BukuController bukuController = new BukuController(bukuModel, bukuView, currentFrame);
            
            // Refresh panel
            currentFrame.revalidate();
            currentFrame.repaint();
        }
    }

    //ekspor riwayat peminjaman ke csv
    private void eksporRiwayatPeminjaman() {
        try {
            int bulan = view.getSelectedBulan();
            int tahun = view.getSelectedTahun();
            
            // Ambil data berdasarkan bulan dan tahun
            ArrayList<String[]> data = model.getRiwayatPeminjamanByBulan(bulan, tahun);
            
            if (data.isEmpty()) {
                JOptionPane.showMessageDialog(view, 
                    "Tidak ada data peminjaman untuk bulan " + bulan + " tahun " + tahun,
                    "Data Kosong", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Buat file chooser dialog
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Simpan Data Peminjaman");
            
            // Set default filename dan filter
            String defaultFileName = "peminjaman_" + bulan + "_" + tahun + ".csv";
            fileChooser.setSelectedFile(new File(defaultFileName));
            fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
            
            int userSelection = fileChooser.showSaveDialog(view);
            
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                String filePath = fileToSave.getAbsolutePath();
                
                // Pastikan file berakhiran .csv
                if (!filePath.toLowerCase().endsWith(".csv")) {
                    filePath += ".csv";
                }
                
                // Ekspor data ke CSV
                boolean success = model.exportToCSV(data, filePath);
                
                if (success) {
                    JOptionPane.showMessageDialog(view, 
                        "Data berhasil diekspor ke " + filePath,
                        "Ekspor Berhasil", JOptionPane.INFORMATION_MESSAGE);
                    view.statusLabel.setText("Data berhasil diekspor ke CSV");
                } else {
                    JOptionPane.showMessageDialog(view, 
                        "Gagal mengekspor data.",
                        "Ekspor Gagal", JOptionPane.ERROR_MESSAGE);
                    view.statusLabel.setText("Gagal mengekspor data ke CSV");
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, 
                "Terjadi kesalahan: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.log(Level.SEVERE, "Error pada proses ekspor CSV", e);
        }
    }

    //ekspor riwayat peminjaman ke pdf
    private void eksporRiwayatPeminjamanKePDF() {
        try {
            int bulan = view.getSelectedBulan();
            int tahun = view.getSelectedTahun();
            
            // Ambil data berdasarkan bulan dan tahun
            ArrayList<String[]> data = model.getRiwayatPeminjamanByBulan(bulan, tahun);
            
            if (data.isEmpty()) {
                JOptionPane.showMessageDialog(view, 
                    "Tidak ada data peminjaman untuk bulan " + bulan + " tahun " + tahun,
                    "Data Kosong", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Buat file chooser dialog
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Simpan Laporan Peminjaman PDF");
            
            // Set default filename dan filter
            String defaultFileName = "laporan_peminjaman_" + bulan + "_" + tahun + ".pdf";
            fileChooser.setSelectedFile(new File(defaultFileName));
            fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));
            
            int userSelection = fileChooser.showSaveDialog(view);
            
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                String filePath = fileToSave.getAbsolutePath();
                
                // Pastikan file berakhiran .pdf
                if (!filePath.toLowerCase().endsWith(".pdf")) {
                    filePath += ".pdf";
                }
                
                // Ekspor data ke PDF
                boolean success = model.exportToPDF(data, filePath);
                
                if (success) {
                    JOptionPane.showMessageDialog(view, 
                        "Laporan PDF berhasil disimpan ke " + filePath,
                        "Ekspor Berhasil", JOptionPane.INFORMATION_MESSAGE);
                    view.statusLabel.setText("Laporan berhasil diekspor ke PDF");
                } else {
                    JOptionPane.showMessageDialog(view, 
                        "Gagal mengekspor laporan ke PDF.",
                        "Ekspor Gagal", JOptionPane.ERROR_MESSAGE);
                    view.statusLabel.setText("Gagal mengekspor laporan ke PDF");
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, 
                "Terjadi kesalahan: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.log(Level.SEVERE, "Error pada proses ekspor PDF", e);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set look and feel sistem
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                PeminjamanModel model = new PeminjamanModel();
                PeminjamanView view = new PeminjamanView();
                new PeminjamanController(model, view);
                view.setVisible(true);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Gagal menginisialisasi aplikasi", e);
            }
        });
    }
}