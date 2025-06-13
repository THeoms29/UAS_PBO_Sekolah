package registrasi;

import javax.swing.JOptionPane;
import main.MainController;

public class RegistrasiAdminController {
    private final RegistrasiAdminView view;
    private final RegistrasiAdminModel model;
    private final MainController mainController; // Referensi ke controller utama

    public RegistrasiAdminController(RegistrasiAdminView view, RegistrasiAdminModel model, MainController mainController) {
        this.view = view;
        this.model = model;
        this.mainController = mainController;

        // Hubungkan action listener dari view ke metode di controller ini
        this.view.setRegistrasiAction(e -> handleRegistrasi());
        this.view.setKeluarAction(e -> System.exit(0)); // Keluar dari aplikasi
    }

    private void handleRegistrasi() {
        // 1. Ambil data dari View
        String nama = view.getNama();
        String username = view.getUsername();
        String password = view.getPassword();
        String konfirmasi = view.getKonfirmasiPassword();

        // 2. Lakukan Validasi
        if (nama.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Semua field harus diisi!", "Error Validasi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(konfirmasi)) {
            JOptionPane.showMessageDialog(view, "Password dan Konfirmasi Password tidak cocok!", "Error Validasi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3. Panggil Model untuk menyimpan data
        boolean isSuccess = model.createAdmin(nama, username, password);

        // 4. Beri feedback ke pengguna dan lanjutkan alur aplikasi
        if (isSuccess) {
            JOptionPane.showMessageDialog(view, "User Administrator berhasil dibuat! Aplikasi akan melanjutkan ke halaman login.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            view.dispose(); // Tutup jendela registrasi
            mainController.onFirstUserRegistered(); // Beritahu MainController untuk melanjutkan
        } else {
            JOptionPane.showMessageDialog(view, "Gagal membuat user. Username mungkin sudah ada atau terjadi kesalahan database.", "Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }
}