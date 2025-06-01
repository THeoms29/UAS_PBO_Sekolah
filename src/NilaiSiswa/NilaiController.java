package NilaiSiswa;

import java.util.*;
import javax.swing.*;
import javax.swing.SwingUtilities;

public class NilaiController {
    private final InputNilaiView view;
    private final NilaiModel model;
    private int kelasId;
    private final int mapelId;
    private final String semester;
    private final int guruId;

    public NilaiController(InputNilaiView view, NilaiModel model, int guruId, int mapelId, String semester) {
        this.view = view;
        this.model = model;
        this.guruId = guruId;
        this.mapelId = mapelId;
        this.semester = semester;
        this.view.setController(this);
        
        // Verifikasi koneksi database
        if (!model.cekKoneksiDatabase()) {
            JOptionPane.showMessageDialog(view, 
                "Gagal terhubung ke database! Aplikasi akan ditutup.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        setGuruMapelLabel();
    }

    private void setGuruMapelLabel() {
        try {
            String namaGuru = model.getNamaGuru(guruId);
            String namaMapel = model.getNamaMapel(mapelId);
            
            if (namaGuru == null || namaMapel == null) {
                throw new Exception("Data guru atau mapel tidak ditemukan");
            }
            
            String label = String.format("Guru: %s | Mapel: %s | Semester: %s", 
                                       namaGuru, namaMapel, semester);
            view.setLabelGuruMapel(label);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, 
                "Gagal memuat data guru/mapel: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void loadDataAwal() {
        try {
            List<Map<String, Object>> kelasList = model.getDataKelas();
            
            if (kelasList.isEmpty()) {
                JOptionPane.showMessageDialog(view, 
                    "Tidak ada data kelas tersedia!", 
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            view.setComboBoxKelas(kelasList);
            this.kelasId = (int) kelasList.get(0).get("id");
            kelasDipilih(this.kelasId);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, 
                "Gagal memuat data awal: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void kelasDipilih(int kelasIdBaru) {
        this.kelasId = kelasIdBaru;
        
        try {
            List<Map<String, Object>> siswaList = model.getDataSiswaKelas(kelasIdBaru);
            List<Map<String, Object>> nilaiList = model.getNilaiByKelasDanMapel(kelasIdBaru, mapelId, view.getSelectedSemester());
            
            if (siswaList.isEmpty()) {
                JOptionPane.showMessageDialog(view, 
                    "Tidak ada siswa di kelas ini!", 
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            }
            
            view.setSiswaList(siswaList);
            view.setTableData(nilaiList);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, 
                "Gagal memuat data siswa/nilai: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public boolean simpanNilai(List<Map<String, Object>> dataNilai) {
        if (dataNilai == null || dataNilai.isEmpty()) {
            JOptionPane.showMessageDialog(view, 
                "Tidak ada data nilai untuk disimpan!", 
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            model.startTransaction();
            int totalBerhasil = 0;
            StringBuilder errorDetails = new StringBuilder();
            
            for (Map<String, Object> nilai : dataNilai) {
                try {
                    // Validasi data lebih ketat
                    if (nilai.get("siswa_id") == null || nilai.get("nilai_uh") == null || 
                        nilai.get("nilai_uts") == null || nilai.get("nilai_uas") == null) {
                        throw new IllegalArgumentException("Data nilai tidak lengkap");
                    }
                    
                    int siswaId = (int) nilai.get("siswa_id");
                    int nilaiUH = Integer.parseInt(nilai.get("nilai_uh").toString());
                    int nilaiUTS = Integer.parseInt(nilai.get("nilai_uts").toString());
                    int nilaiUAS = Integer.parseInt(nilai.get("nilai_uas").toString());
                    
                    // Validasi range nilai
                    if (nilaiUH < 0 || nilaiUH > 100 || 
                        nilaiUTS < 0 || nilaiUTS > 100 || 
                        nilaiUAS < 0 || nilaiUAS > 100) {
                        throw new IllegalArgumentException("Nilai harus antara 0-100");
                    }
                    
                    boolean sukses = model.simpanNilai(
                        siswaId, mapelId, nilaiUH, nilaiUTS, nilaiUAS, view.getSelectedSemester());
                    
                    if (sukses) {
                        totalBerhasil++;
                    } else {
                        errorDetails.append("\nGagal menyimpan data siswa ID: ").append(siswaId);
                    }
                } catch (Exception e) {
                    errorDetails.append("\nError pada siswa ID: ")
                                .append(nilai.get("siswa_id"))
                                .append(" - ")
                                .append(e.getMessage());
                }
            }
            
            if (totalBerhasil == dataNilai.size()) {
                model.commitTransaction();
                return true;
            } else {
                model.rollbackTransaction();
                return false;
            }
        } catch (Exception e) {
            model.rollbackTransaction();
            JOptionPane.showMessageDialog(view, 
                "Error sistem saat menyimpan: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        } finally {

        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                InputNilaiView view = new InputNilaiView();
                NilaiModel model = new NilaiModel();
                
                // Parameter contoh - sesuaikan dengan database Anda
                int guruId = 1; 
                int mapelId = 1;
                String semester = "1"; // Sesuaikan dengan semester yang ada di database
                
                NilaiController controller = new NilaiController(view, model, guruId, mapelId, semester);
                controller.loadDataAwal();
                
                view.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, 
                    "Gagal memulai aplikasi: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
}