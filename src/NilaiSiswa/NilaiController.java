package NilaiSiswa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.util.logging.Logger;
import shared.Koneksi;

public class NilaiController {
    private static final Logger LOGGER = Logger.getLogger(NilaiController.class.getName());
    private InputNilaiView view;
    private NilaiModel model;
    private int guruId;
    private int mapelId;

    public NilaiController(InputNilaiView view, NilaiModel model, int guruId, int mapelId) {
        this.view = view;
        this.model = model;
        this.guruId = guruId;
        this.mapelId = mapelId;
    }

    public void loadDataAwal() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            List<Map<String, Object>> daftarKelas = model.getDataKelas();
            if (daftarKelas == null || daftarKelas.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Tidak ada data kelas tersedia.", 
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
                LOGGER.warning("Tidak ada data kelas tersedia.");
                return;
            }
            view.setComboBoxKelas(daftarKelas);
            String namaKelas = (String) daftarKelas.get(0).get("nama_kelas");
            String semester = getCurrentSemester();
            if (semester == null) {
                LOGGER.warning("Semester tidak valid saat memuat data awal.");
                return;
            }
            view.setLabelGuruMapel("Guru: " + model.getNamaGuru(guruId) + " | Mapel: " + model.getNamaMapel(mapelId) + " | Semester: " + semester);
            kelasDipilih(namaKelas, semester);
            LOGGER.info("Data awal dimuat untuk kelas: " + namaKelas + ", semester: " + semester);
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(view, "Driver MySQL tidak ditemukan.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.severe("Driver MySQL tidak ditemukan: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Gagal memuat data. Pastikan koneksi database aktif.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.severe("Gagal memuat data awal: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Terjadi kesalahan: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.severe("Error tak terduga: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void kelasDipilih(String namaKelas, String semester) {
        try {
            if (namaKelas == null || semester == null) {
                JOptionPane.showMessageDialog(view, "Kelas atau semester tidak valid.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                LOGGER.severe("Kelas atau semester tidak valid: kelas=" + namaKelas + ", semester=" + semester);
                return;
            }
            int kelasId = -1;
            List<Map<String, Object>> daftarKelas = model.getDataKelas();
            if (daftarKelas == null) {
                JOptionPane.showMessageDialog(view, "Daftar kelas tidak tersedia.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                LOGGER.severe("Daftar kelas tidak tersedia.");
                return;
            }
            for (Map<String, Object> kelas : daftarKelas) {
                if (kelas.get("nama_kelas").equals(namaKelas)) {
                    kelasId = (int) kelas.get("id");
                    break;
                }
            }
            if (kelasId == -1) {
                JOptionPane.showMessageDialog(view, "Kelas tidak ditemukan: " + namaKelas, 
                    "Error", JOptionPane.ERROR_MESSAGE);
                LOGGER.severe("Kelas tidak ditemukan: " + namaKelas);
                return;
            }
            List<Map<String, Object>> daftarSiswa = model.getDataSiswaKelas(kelasId);
            view.setSiswaList(daftarSiswa);
            
            // Ambil data nilai dan periksa duplikasi
            List<Map<String, Object>> daftarNilai = model.getNilaiByKelasDanMapel(kelasId, mapelId, semester);
            List<Map<String, Object>> filteredNilai = new ArrayList<>();
            Set<Integer> siswaIds = new HashSet<>();
            for (Map<String, Object> nilai : daftarNilai) {
                int siswaId = (int) nilai.get("siswa_id");
                if (siswaIds.contains(siswaId)) {
                    LOGGER.warning("Duplikasi siswa_id ditemukan di daftarNilai: " + siswaId);
                    continue;
                }
                siswaIds.add(siswaId);
                filteredNilai.add(nilai);
            }
            view.setTableData(filteredNilai);
            view.setLabelGuruMapel("Guru: " + model.getNamaGuru(guruId) + " | Mapel: " + model.getNamaMapel(mapelId) + " | Semester: " + semester);
            LOGGER.info("Data kelas dimuat: kelas=" + namaKelas + ", semester=" + semester + ", jumlah siswa: " + filteredNilai.size());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Gagal memuat data kelas. Pastikan koneksi database aktif.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.severe("Gagal memuat data kelas: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Terjadi kesalahan: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.severe("Error tak terduga: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean simpanNilai(List<Map<String, Object>> dataNilai) {
        if (dataNilai == null || dataNilai.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Tidak ada data nilai untuk disimpan.", 
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            LOGGER.warning("Tidak ada data nilai untuk disimpan.");
            return false;
        }

        try {
            model.startTransaction();
            boolean allSuccess = model.simpanNilai(dataNilai, mapelId);
            if (allSuccess) {
                model.commitTransaction();
                LOGGER.info("Semua nilai berhasil disimpan untuk " + dataNilai.size() + " siswa.");
                return true;
            } else {
                model.rollbackTransaction();
                JOptionPane.showMessageDialog(view, "Gagal menyimpan beberapa data nilai.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                LOGGER.severe("Gagal menyimpan beberapa data nilai.");
                return false;
            }
        } catch (SQLException e) {
            try {
                model.rollbackTransaction();
            } catch (SQLException ex) {
                LOGGER.severe("Gagal rollback transaksi: " + ex.getMessage());
            }
            JOptionPane.showMessageDialog(view, "Gagal menyimpan nilai: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.severe("Gagal menyimpan nilai: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean testInputNilai(int siswaId, Integer nilaiUH, Integer nilaiUTS, Integer nilaiUAS, String semester) {
        if (semester == null || (!semester.equals("1") && !semester.equals("2"))) {
            JOptionPane.showMessageDialog(view, "Semester tidak valid. Pilih Semester 1 atau 2.", 
                "Error Input", JOptionPane.ERROR_MESSAGE);
            LOGGER.severe("Semester tidak valid: " + semester);
            return false;
        }
        if (nilaiUH == null || nilaiUTS == null || nilaiUAS == null) {
            JOptionPane.showMessageDialog(view, "Semua nilai harus diisi!", 
                "Error Input", JOptionPane.ERROR_MESSAGE);
            LOGGER.severe("Nilai kosong untuk siswa ID: " + siswaId);
            return false;
        }
        try {
            if (nilaiUH < 0 || nilaiUH > 100 || nilaiUTS < 0 || nilaiUTS > 100 || nilaiUAS < 0 || nilaiUAS > 100) {
                JOptionPane.showMessageDialog(view, "Nilai harus antara 0-100.", 
                    "Error Input", JOptionPane.ERROR_MESSAGE);
                LOGGER.severe("Nilai di luar rentang: UH=" + nilaiUH + ", UTS=" + nilaiUTS + ", UAS=" + nilaiUAS);
                return false;
            }
            
            // Panggil model untuk menyimpan/memperbarui nilai
            boolean success = model.simpanNilai(siswaId, mapelId, nilaiUH, nilaiUTS, nilaiUAS, semester);
            
            // Setelah berhasil disimpan, muat ulang data dari database
            if (success) {
                Map<String, Object> selectedKelas = (Map<String, Object>) view.getComboKelas().getSelectedItem();
                if (selectedKelas != null) {
                    kelasDipilih((String) selectedKelas.get("nama_kelas"), semester);
                }
                LOGGER.info("Nilai berhasil disimpan untuk siswa ID: " + siswaId);
            } else {
                LOGGER.warning("Gagal menyimpan nilai untuk siswa ID: " + siswaId);
            }
            return success;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Gagal menyimpan nilai: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.severe("Gagal menyimpan nilai untuk siswa ID: " + siswaId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String getCurrentSemester() {
        String selectedSemester = (String) view.getComboSemester().getSelectedItem();
        if (selectedSemester == null || (!selectedSemester.equals("Semester 1") && !selectedSemester.equals("Semester 2"))) {
            JOptionPane.showMessageDialog(view, "Pilih semester yang valid (Semester 1 atau Semester 2)!", 
                "Error Semester", JOptionPane.WARNING_MESSAGE);
            LOGGER.warning("Semester tidak valid: " + selectedSemester);
            return null;
        }
        return selectedSemester.replace("Semester ", "");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                InputNilaiView view = new InputNilaiView();
                Connection conn = Koneksi.getConnection();
                if (conn == null) {
                    JOptionPane.showMessageDialog(null, "Gagal koneksi ke database!", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    LOGGER.severe("Gagal koneksi ke database.");
                    return;
                }
                NilaiModel model = new NilaiModel(conn);

                String guruIdStr = null;
                while (guruIdStr == null || guruIdStr.trim().isEmpty()) {
                    guruIdStr = JOptionPane.showInputDialog("Masukkan ID Guru:");
                    if (guruIdStr == null || guruIdStr.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(null, "ID Guru diperlukan!", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                int guruId = Integer.parseInt(guruIdStr);

                String mapelIdStr = null;
                while (mapelIdStr == null || mapelIdStr.trim().isEmpty()) {
                    mapelIdStr = JOptionPane.showInputDialog("Masukkan ID Mapel:");
                    if (mapelIdStr == null || mapelIdStr.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(null, "ID Mapel diperlukan!", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                int mapelId = Integer.parseInt(mapelIdStr);

                try (PreparedStatement psGuru = conn.prepareStatement("SELECT id FROM users WHERE id = ? AND role = 'guru'")) {
                    psGuru.setInt(1, guruId);
                    if (!psGuru.executeQuery().next()) {
                        JOptionPane.showMessageDialog(null, "ID Guru " + guruId + " tidak valid!", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                        LOGGER.severe("ID Guru tidak valid: " + guruId);
                        return;
                    }
                }
                try (PreparedStatement psMapel = conn.prepareStatement("SELECT id FROM mapel WHERE id = ?")) {
                    psMapel.setInt(1, mapelId);
                    if (!psMapel.executeQuery().next()) {
                        JOptionPane.showMessageDialog(null, "ID Mapel " + mapelId + " tidak valid!", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                        LOGGER.severe("ID Mapel tidak valid: " + mapelId);
                        return;
                    }
                }

                NilaiController controller = new NilaiController(view, model, guruId, mapelId);
                LOGGER.info("View class: " + view.getClass().getName() + ", Controller class: " + controller.getClass().getName());
                view.setController(controller);
                controller.loadDataAwal();
                view.setVisible(true);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "ID Guru atau Mapel harus berupa angka.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                LOGGER.severe("ID Guru atau Mapel tidak valid: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Gagal memulai aplikasi: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
                LOGGER.severe("Gagal memulai aplikasi: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public int getMapelId() {
        return mapelId;
    }
}