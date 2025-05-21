package peminjaman;

import buku.BukuController;
import buku.BukuModel;
import buku.BukuView;
import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class PeminjamanController {
    private final PeminjamanModel model;
    private final PeminjamanView view;

    public PeminjamanController(PeminjamanModel model, PeminjamanView view) {
        this.model = model;
        this.view = view;

        isiComboSiswa();
        isiComboBuku();

        view.setSimpanAction(e -> simpanPeminjaman());
        view.setKembalikanAction(e -> prosesPengembalian());
        view.setMuatRiwayatAction(e -> tampilkanRiwayat());
        view.setCariSiswaAction(e -> cariSiswa());
        view.setCariRiwayatAction(e -> cariRiwayat());
        view.setKeBukuAction(e -> bukaModulBuku()); // Tambahkan navigasi ke modul buku
        view.setExportCsvAction(e -> eksporRiwayatPeminjaman());

        resetTanggalDikembalikan();
        tampilkanRiwayat(); // load data awal
    }

    private void isiComboSiswa() {
        view.comboSiswa.removeAllItems();
        ArrayList<String[]> siswaList = model.getDaftarSiswa();
        for (String[] s : siswaList) {
            view.comboSiswa.addItem(s[0] + " - " + s[1]);
        }
    }

    private void isiComboBuku() {
        view.comboBuku.removeAllItems();
        ArrayList<String[]> bukuList = model.getDaftarBuku();
        for (String[] b : bukuList) {
            view.comboBuku.addItem(b[0] + " - " + b[1]);
        }
    }

    private void cariSiswa() {
        String keyword = view.getInputPencarianSiswa().toLowerCase();
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

    private void cariRiwayat() {
        String keyword = view.getInputPencarianRiwayat().toLowerCase();
        ArrayList<String[]> data = model.getRiwayatPeminjaman();
        String[] kolom = {"ID", "Siswa", "Buku", "Pinjam", "Jatuh Tempo", "Dikembalikan", "Denda"};
        DefaultTableModel tableModel = new DefaultTableModel(kolom, 0);

        for (String[] row : data) {
            boolean cocok = false;
            for (String field : row) {
                if (field != null && field.toLowerCase().contains(keyword)) {
                    cocok = true;
                    break;
                }
            }
            if (cocok) {
                tableModel.addRow(row);
            }
        }

        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(view, "Tidak ditemukan data yang cocok.");
        }

        view.tableRiwayat.setModel(tableModel);
    }

    private void simpanPeminjaman() {
        try {
            int siswaId = Integer.parseInt(view.comboSiswa.getSelectedItem().toString().split(" - ")[0]);
            int bukuId = Integer.parseInt(view.comboBuku.getSelectedItem().toString().split(" - ")[0]);
            Date tglPinjam = new Date(((java.util.Date) view.tanggalPinjam.getValue()).getTime());
            Date tglKembali = new Date(((java.util.Date) view.tanggalKembali.getValue()).getTime());

            boolean berhasil = model.tambahPeminjaman(siswaId, bukuId, tglPinjam, tglKembali);

            if (berhasil) {
                JOptionPane.showMessageDialog(view, "Peminjaman berhasil disimpan.");
                tampilkanRiwayat();
            } else {
                JOptionPane.showMessageDialog(view, "Gagal menyimpan peminjaman.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Input tidak valid: " + e.getMessage());
        }
    }

    private void prosesPengembalian() {
        int baris = view.tableRiwayat.getSelectedRow();
        if (baris == -1) {
            JOptionPane.showMessageDialog(view, "Pilih baris peminjaman terlebih dahulu.");
            return;
        }

        try {
            int idPeminjaman = Integer.parseInt(view.tableRiwayat.getValueAt(baris, 0).toString());
            Date tglKembali = Date.valueOf(view.tableRiwayat.getValueAt(baris, 4).toString());
            Date tglDikembalikan = new Date(((java.util.Date) view.tanggalDikembalikan.getValue()).getTime());

            int denda = model.hitungDenda(tglKembali, tglDikembalikan);
            boolean sukses = model.kembalikanBuku(idPeminjaman, tglDikembalikan, denda);

            if (sukses) {
                JOptionPane.showMessageDialog(view, "Pengembalian berhasil. Denda: Rp " + denda);
                tampilkanRiwayat();
                resetTanggalDikembalikan();
            } else {
                JOptionPane.showMessageDialog(view, "Gagal memproses pengembalian.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Input tidak valid: " + e.getMessage());
        }
    }

    private void tampilkanRiwayat() {
        ArrayList<String[]> data = model.getRiwayatPeminjaman();
        String[] kolom = {"ID", "Siswa", "Buku", "Pinjam", "Jatuh Tempo", "Dikembalikan", "Denda"};
        DefaultTableModel tableModel = new DefaultTableModel(kolom, 0);
        for (String[] row : data) {
            tableModel.addRow(row);
        }
        view.tableRiwayat.setModel(tableModel);
    }

    private void resetTanggalDikembalikan() {
        view.tanggalDikembalikan.setValue(new java.util.Date());
    }
    
    // Metode untuk membuka modul buku
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
                } else {
                    JOptionPane.showMessageDialog(view, 
                        "Gagal mengekspor data.",
                        "Ekspor Gagal", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, 
                "Terjadi kesalahan: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PeminjamanModel model = new PeminjamanModel();
            PeminjamanView view = new PeminjamanView();
            new PeminjamanController(model, view);
            view.setVisible(true);
        });
    }
}