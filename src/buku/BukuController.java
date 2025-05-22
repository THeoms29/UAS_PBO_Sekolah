package buku;

import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import peminjaman.PeminjamanController;
import peminjaman.PeminjamanModel;
import peminjaman.PeminjamanView;

public class BukuController {
    private final BukuModel model;
    private final BukuView view;
    private JFrame parentFrame; // Referensi ke frame induk
    
    // Constructor yang menerima referensi ke frame
    public BukuController(BukuModel model, BukuView view, JFrame parentFrame) {
        this.model = model;
        this.view = view;
        this.parentFrame = parentFrame;
        
        // Inisialisasi action listeners
        view.setSimpanAction(e -> simpanBuku());
        view.setUpdateAction(e -> updateBuku());
        view.setHapusAction(e -> hapusBuku());
        view.setBatalAction(e -> view.resetForm());
        view.setCariAction(e -> cariBuku());
        view.setRefreshAction(e -> refreshTabel());
        view.setKembaliAction(e -> kembaliKeMenuUtama());
        
        // Load data awal
        refreshTabel();
    }
    
    // Constructor default untuk saat modul buku dijalankan secara mandiri
    public BukuController(BukuModel model, BukuView view) {
        this(model, view, null);
    }
    
    private void simpanBuku() {
        String judul = view.getInputJudul();
        String penulis = view.getInputPenulis();
        int jumlah = view.getInputJumlah();
        
        // Validasi input
        if (judul.isEmpty() || penulis.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Judul dan penulis tidak boleh kosong!");
            return;
        }
        
        boolean berhasil = model.tambahBuku(judul, penulis, jumlah);
        
        if (berhasil) {
            JOptionPane.showMessageDialog(view, "Buku berhasil ditambahkan.");
            view.resetForm();
            refreshTabel();
            view.setStatus("Buku berhasil ditambahkan");
        } else {
            JOptionPane.showMessageDialog(view, "Gagal menambahkan buku.");
            view.setStatus("Gagal menambahkan buku");
        }
    }
    
    private void updateBuku() {
        String idText = view.getInputId();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Pilih buku terlebih dahulu!");
            return;
        }
        
        int id = Integer.parseInt(idText);
        String judul = view.getInputJudul();
        String penulis = view.getInputPenulis();
        int jumlah = view.getInputJumlah();
        
        // Validasi input
        if (judul.isEmpty() || penulis.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Judul dan penulis tidak boleh kosong!");
            return;
        }
        
        boolean berhasil = model.updateBuku(id, judul, penulis, jumlah);
        
        if (berhasil) {
            JOptionPane.showMessageDialog(view, "Buku berhasil diupdate.");
            view.resetForm();
            refreshTabel();
            view.setStatus("Buku berhasil diupdate");
        } else {
            JOptionPane.showMessageDialog(view, "Gagal mengupdate buku.");
            view.setStatus("Gagal mengupdate buku");
        }
    }
    
    private void hapusBuku() {
        String idText = view.getInputId();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Pilih buku terlebih dahulu!");
            return;
        }
        
        int id = Integer.parseInt(idText);
        
        // Konfirmasi penghapusan
        int confirm = JOptionPane.showConfirmDialog(view, 
                "Apakah Anda yakin ingin menghapus buku ini?", 
                "Konfirmasi Hapus", 
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean berhasil = model.hapusBuku(id);
            
            if (berhasil) {
                JOptionPane.showMessageDialog(view, "Buku berhasil dihapus.");
                view.resetForm();
                refreshTabel();
                view.setStatus("Buku berhasil dihapus");
            } else {
                JOptionPane.showMessageDialog(view, 
                        "Gagal menghapus buku. Pastikan buku tidak sedang dipinjam.");
                view.setStatus("Gagal menghapus buku");
            }
        }
    }
    
    private void cariBuku() {
        String keyword = view.getInputCari();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Masukkan kata kunci pencarian!");
            return;
        }
        
        ArrayList<String[]> data = model.cariBuku(keyword);
        updateTabel(data);
        
        if (data.isEmpty()) {
            view.setStatus("Buku tidak ditemukan");
        } else {
            view.setStatus("Ditemukan " + data.size() + " hasil pencarian");
        }
    }
    
    private void refreshTabel() {
        ArrayList<String[]> data = model.getDaftarBuku();
        updateTabel(data);
        view.setStatus("Data buku berhasil dimuat. Total: " + data.size() + " buku");
    }
    
    private void updateTabel(ArrayList<String[]> data) {
        DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Judul", "Penulis", "Jumlah"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        for (String[] row : data) {
            tableModel.addRow(row);
        }
        
        view.tableBuku.setModel(tableModel);
    }
    
    private void kembaliKeMenuUtama() {
        // Implementasi untuk kembali ke modul peminjaman
        int confirm = JOptionPane.showConfirmDialog(view, 
                "Kembali ke modul peminjaman?", 
                "Konfirmasi", 
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (parentFrame != null) {
                // Kita menggunakan frame yang sama untuk navigasi
                // Bersihkan konten saat ini
                parentFrame.getContentPane().removeAll();
                
                // Buat dan inisialisasi modul peminjaman
                PeminjamanModel peminjamanModel = new PeminjamanModel();
                PeminjamanView peminjamanView = new PeminjamanView();
                
                // Set panel konten peminjaman ke frame
                parentFrame.setContentPane(peminjamanView.getContentPane());
                
                // Perbarui title
                parentFrame.setTitle("Modul Peminjaman Buku");
                
                // Set controller untuk peminjaman
                new PeminjamanController(peminjamanModel, peminjamanView);
                
                // Refresh panel
                parentFrame.revalidate();
                parentFrame.repaint();
            } else {
                // Jika parentFrame null, kita menggunakan cara lama (buat frame baru)
                view.dispose();
                SwingUtilities.invokeLater(() -> {
                    PeminjamanModel peminjamanModel = new PeminjamanModel();
                    PeminjamanView peminjamanView = new PeminjamanView();
                    new PeminjamanController(peminjamanModel, peminjamanView);
                    peminjamanView.setVisible(true);
                });
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BukuModel model = new BukuModel();
            BukuView view = new BukuView();
            new BukuController(model, view);
            view.setVisible(true);
        });
    }
}