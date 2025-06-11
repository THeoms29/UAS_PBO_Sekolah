package NilaiSiswa;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.util.logging.Logger;
import main.MainController;
import shared.Koneksi;

public class NilaiController {
    private static final Logger LOGGER = Logger.getLogger(NilaiController.class.getName());
    private InputNilaiView view;
    private NilaiModel model;
    private MainController mainController;
    private int guruId;
    private int mapelId;
    private String lastSelectedKelas;
    private String lastSelectedSemester; // Added to track last selected semester

    public NilaiController(InputNilaiView view, NilaiModel model, MainController mainController) {
        if (view == null || model == null || mainController == null) {
            throw new IllegalArgumentException("View, model, atau mainController tidak boleh null");
        }
        this.view = view;
        this.model = model;
        this.mainController = mainController;
        this.guruId = mainController.getCurrentUser().getId();
        this.mapelId = mainController.getMapelIdForGuru(guruId);
        LOGGER.info("NilaiController diinisialisasi dengan guruId=" + guruId + ", mapelId=" + mapelId);
    }

    public void loadDataAwal() {
        try {
            List<Map<String, Object>> daftarKelas = model.getDataKelas();
            if (daftarKelas.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Tidak ada data kelas tersedia.", "Error", JOptionPane.WARNING_MESSAGE);
                LOGGER.warning("Tidak ada data kelas tersedia");
                return;
            }
            view.setComboKelas(daftarKelas);
            String namaKelas = lastSelectedKelas != null ? lastSelectedKelas : (String) daftarKelas.get(0).get("nama_kelas");
            String semester = lastSelectedSemester != null ? lastSelectedSemester : view.getComboSemester().getSelectedItem().toString().replace("Semester ", "");
            // Set comboKelas ke kelas yang terakhir dipilih jika ada
            for (int i = 0; i < view.getComboKelas().getItemCount(); i++) {
                Map<String, Object> kelas = view.getComboKelas().getItemAt(i);
                if (kelas.get("nama_kelas").equals(namaKelas)) {
                    view.getComboKelas().setSelectedIndex(i);
                    break;
                }
            }
            // Set comboSemester ke semester yang terakhir dipilih jika ada
            view.getComboSemester().setSelectedItem("Semester " + semester);
            view.setLabelGuruMapel("Guru: " + model.getNamaGuru(guruId) + " | Mapel: " + model.getNamaMapel(mapelId) + " | Semester: " + semester);
            kelasDipilih(namaKelas, semester);
            LOGGER.info("Data awal dimuat untuk kelas: " + namaKelas + ", semester=" + semester);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Gagal memuat data awal: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.severe("Gagal memuat data awal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void kelasDipilih(String namaKelas, String semester) {
        try {
            if (namaKelas == null || semester == null) {
                JOptionPane.showMessageDialog(view, "Kelas atau semester tidak valid.", "Error", JOptionPane.ERROR_MESSAGE);
                LOGGER.severe("Kelas atau semester tidak valid: kelas=" + namaKelas + ", semester=" + semester);
                return;
            }
            lastSelectedKelas = namaKelas;
            lastSelectedSemester = semester; // Update last selected semester
            if (mainController != null) {
                mainController.setLastSelectedKelas(namaKelas);
                mainController.setLastSelectedSemester(semester); // Save to MainController
                LOGGER.info("lastSelectedKelas dan lastSelectedSemester di MainController diperbarui: " + namaKelas + ", " + semester);
            }
            int kelasId = -1;
            List<Map<String, Object>> daftarKelas = model.getDataKelas();
            if (daftarKelas.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Tidak ada kelas tersedia di database.", "Error", JOptionPane.ERROR_MESSAGE);
                LOGGER.severe("Daftar kelas kosong");
                return;
            }
            for (Map<String, Object> kelas : daftarKelas) {
                if (kelas.get("nama_kelas").equals(namaKelas)) {
                    kelasId = (int) kelas.get("id");
                    break;
                }
            }
            if (kelasId == -1) {
                JOptionPane.showMessageDialog(view, "Kelas tidak ditemukan.", "Error", JOptionPane.ERROR_MESSAGE);
                LOGGER.severe("Kelas tidak ditemukan: " + namaKelas);
                return;
            }
            List<Map<String, Object>> daftarSiswa = model.getDataSiswaKelas(kelasId);
            if (daftarSiswa.isEmpty()) {
                LOGGER.warning("Tidak ada siswa untuk kelasId=" + kelasId);
            }
            view.setSiswaList(daftarSiswa);
            List<Map<String, Object>> daftarNilai = model.getNilaiByKelasDanMapel(kelasId, mapelId, semester);
            List<Map<String, Object>> filteredNilai = new ArrayList<>();
            Set<Integer> siswaIds = new HashSet<>();
            for (Map<String, Object> nilai : daftarNilai) {
                int siswaId = (int) nilai.get("siswa_id");
                if (!siswaIds.contains(siswaId)) {
                    siswaIds.add(siswaId);
                    filteredNilai.add(nilai);
                }
            }
            view.setTableData(filteredNilai);
            view.setLabelGuruMapel("Guru: " + model.getNamaGuru(guruId) + " | Mapel: " + model.getNamaMapel(mapelId) + " | Semester: " + semester);
            LOGGER.info("Data kelas dimuat: kelas=" + namaKelas + ", semester=" + semester + ", jumlah nilai=" + filteredNilai.size());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Gagal memuat data kelas: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.severe("Gagal memuat data kelas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean testInputNilai(int siswaId, Integer nilaiUH, Integer nilaiUTS, Integer nilaiUAS, String semester) {
        if (semester == null || (!semester.equals("1") && !semester.equals("2"))) {
            JOptionPane.showMessageDialog(view, "Semester tidak valid. Pilih Semester 1 atau 2.", "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.severe("Semester tidak valid: " + semester);
            return false;
        }
        try {
            // Validasi rentang nilai untuk yang diisi
            if ((nilaiUH != null && (nilaiUH < 0 || nilaiUH > 100)) ||
                (nilaiUTS != null && (nilaiUTS < 0 || nilaiUTS > 100)) ||
                (nilaiUAS != null && (nilaiUAS < 0 || nilaiUAS > 100))) {
                JOptionPane.showMessageDialog(view, "Nilai yang diisi harus antara 0-100.", "Error", JOptionPane.ERROR_MESSAGE);
                LOGGER.severe("Nilai di luar rentang: UH=" + nilaiUH + ", UTS=" + nilaiUTS + ", UAS=" + nilaiUAS);
                return false;
            }
            boolean success = model.simpanNilai(siswaId, mapelId, nilaiUH, nilaiUTS, nilaiUAS, semester);
            if (success) {
                Map<String, Object> selectedKelas = (Map<String, Object>) view.getComboKelas().getSelectedItem();
                if (selectedKelas != null) {
                    kelasDipilih((String) selectedKelas.get("nama_kelas"), semester);
                }
                // Notifikasi WaliKelasView setelah simpan berhasil
                if (mainController != null) {
                    mainController.notifyWaliKelasDataChanged();
                }
                return true;
            } else {
                JOptionPane.showMessageDialog(view, "Gagal menyimpan nilai ke database!", "Error", JOptionPane.ERROR_MESSAGE);
                LOGGER.severe("Gagal menyimpan nilai untuk siswa_id=" + siswaId + ": simpanNilai mengembalikan false");
                return false;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(view, "Gagal menyimpan nilai: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.severe("Gagal menyimpan nilai untuk siswa_id=" + siswaId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean simpanNilai(List<Map<String, Object>> dataNilai, int mapelId) {
        if (dataNilai == null || dataNilai.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Tidak ada data nilai untuk disimpan.", "Warning", JOptionPane.WARNING_MESSAGE);
            LOGGER.warning("Tidak ada data nilai untuk disimpan");
            return false;
        }
        LOGGER.info("Menyimpan " + dataNilai.size() + " baris nilai dengan mapelId=" + mapelId);
        try {
            model.startTransaction();
            boolean allSuccess = model.simpanNilai(dataNilai, mapelId);
            if (allSuccess) {
                model.commitTransaction();
                LOGGER.info("Semua nilai berhasil disimpan untuk " + dataNilai.size() + " siswa");
                // Notifikasi WaliKelasView setelah simpan berhasil
                if (mainController != null) {
                    mainController.notifyWaliKelasDataChanged();
                }
                return true;
            } else {
                model.rollbackTransaction();
                JOptionPane.showMessageDialog(view, "Gagal menyimpan beberapa data nilai.", "Error", JOptionPane.ERROR_MESSAGE);
                LOGGER.severe("Gagal menyimpan beberapa data nilai");
                return false;
            }
        } catch (SQLException e) {
            try {
                model.rollbackTransaction();
            } catch (SQLException ex) {
                LOGGER.severe("Gagal rollback transaksi: " + ex.getMessage());
            }
            JOptionPane.showMessageDialog(view, "Gagal menyimpan nilai: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.severe("Gagal menyimpan nilai: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    void tambahAtauUpdateNilai() {
        Map<String, Object> siswa = (Map<String, Object>) view.getComboSiswa().getSelectedItem();
        if (siswa == null || siswa.get("siswa_id") == null) {
            JOptionPane.showMessageDialog(view, "Pilih siswa terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            LOGGER.warning("Siswa tidak dipilih atau siswa_id null");
            return;
        }

        String uh  = view.getTxtNilaiUH().getText().trim();
        String uts = view.getTxtNilaiUTS().getText().trim();
        String uas = view.getTxtNilaiUAS().getText().trim();

        // Logging untuk memeriksa nilai input
        LOGGER.info("Input nilai: UH='" + uh + "', UTS='" + uts + "', UAS='" + uas + "'");

        // Validasi apakah semua kolom kosong
        if (uh.isEmpty() && uts.isEmpty() && uas.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Setidaknya satu nilai (UH, UTS, atau UAS) harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            LOGGER.warning("Semua kolom nilai kosong");
            // Tambahkan border merah untuk indikasi visual
            view.getTxtNilaiUH().setBorder(new LineBorder(Color.RED));
            view.getTxtNilaiUTS().setBorder(new LineBorder(Color.RED));
            view.getTxtNilaiUAS().setBorder(new LineBorder(Color.RED));
            return;
        }

        try {
            Integer nilaiUH = uh.isEmpty() ? null : Integer.parseInt(uh);
            Integer nilaiUTS = uts.isEmpty() ? null : Integer.parseInt(uts);
            Integer nilaiUAS = uas.isEmpty() ? null : Integer.parseInt(uas);

            // Validasi rentang nilai
            if ((nilaiUH != null && (nilaiUH < 0 || nilaiUH > 100)) ||
                (nilaiUTS != null && (nilaiUTS < 0 || nilaiUTS > 100)) ||
                (nilaiUAS != null && (nilaiUAS < 0 || nilaiUAS > 100))) {
                JOptionPane.showMessageDialog(view, "Nilai yang diisi harus antara 0-100!", "Error", JOptionPane.ERROR_MESSAGE);
                LOGGER.severe("Nilai di luar rentang: UH=" + nilaiUH + ", UTS=" + nilaiUTS + ", UAS=" + nilaiUAS);
                return;
            }

            int siswaId = (int) siswa.get("siswa_id");
            String semester = view.getComboSemester().getSelectedItem().toString().replace("Semester ", "");
            LOGGER.info("Menyimpan nilai - siswa_id=" + siswaId + ", UH=" + nilaiUH + ", UTS=" + nilaiUTS + ", UAS=" + nilaiUAS + ", semester=" + semester);

            boolean success = testInputNilai(siswaId, nilaiUH, nilaiUTS, nilaiUAS, semester);
            if (success) {
                SwingUtilities.invokeLater(() -> {
                    view.getTxtNilaiUH().setText("");
                    view.getTxtNilaiUTS().setText("");
                    view.getTxtNilaiUAS().setText("");
                    Border defaultBorder = UIManager.getBorder("TextField.border");
                    view.getTxtNilaiUH().setBorder(defaultBorder);
                    view.getTxtNilaiUTS().setBorder(defaultBorder);
                    view.getTxtNilaiUAS().setBorder(defaultBorder);
                    LOGGER.info("Menonaktifkan tombol Tambah/Update Nilai setelah penyimpanan sukses");
                    view.getBtnTambahNilai().setEnabled(false);
                    JOptionPane.showMessageDialog(view, "Nilai berhasil disimpan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    LOGGER.info("Nilai berhasil disimpan untuk siswa_id=" + siswaId);
                });
            } else {
                JOptionPane.showMessageDialog(view, "Gagal menyimpan nilai ke database!", "Error", JOptionPane.ERROR_MESSAGE);
                LOGGER.severe("Gagal menyimpan nilai untuk siswa_id=" + siswaId);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(view, "Nilai harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.severe("Gagal parsing nilai: " + e.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Terjadi kesalahan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.severe("Kesalahan tidak terduga saat menyimpan nilai: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String getLastSelectedKelas() {
        return lastSelectedKelas;
    }

    public void setLastSelectedKelas(String kelasNama) {
        this.lastSelectedKelas = kelasNama;
        LOGGER.info("lastSelectedKelas diatur: " + kelasNama);
    }

    public String getLastSelectedSemester() {
        return lastSelectedSemester;
    }

    public void setLastSelectedSemester(String semester) {
        this.lastSelectedSemester = semester;
        LOGGER.info("lastSelectedSemester diatur: " + semester);
    }
}