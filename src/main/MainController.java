package main;

import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

import login.LoginModel;
import login.LoginModel.User;
import login.LoginView;
import peminjaman.PeminjamanController;
import peminjaman.PeminjamanModel;
import peminjaman.PeminjamanView;
import absensi.*;
import inventaris.*;
import jadwal.*;

public class MainController {
    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());
    
    private LoginModel loginModel;
    private LoginView loginView;
    private MainMenuView mainMenuView;
    private User currentUser;

    public MainController() {
        initializeLogin();
    }

    private void initializeLogin() {
        try {
            loginModel = new LoginModel();
            loginView = new LoginView();
            setupLoginActions();
            loginView.setVisible(true);
            loginView.focusUsername();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Gagal menginisialisasi login", e);
            JOptionPane.showMessageDialog(null, 
                "Gagal menginisialisasi aplikasi: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void setupLoginActions() {
        loginView.setLoginAction(e -> handleLogin());
        loginView.setExitAction(e -> handleExit());
    }

    private void handleLogin() {
        String username = loginView.getUsername();
        String password = loginView.getPassword();

        if (username.isEmpty() || password.isEmpty()) {
            loginView.setStatus("Username dan password harus diisi!", true);
            return;
        }

        // Validasi login
        User user = loginModel.validateLogin(username, password);
        
        if (user != null) {
            currentUser = user;
            loginView.setStatus("Login berhasil! Membuka menu utama...", false);
            
            // Delay singkat untuk memberikan feedback visual
            Timer timer = new Timer(1000, e -> {
                loginView.dispose();
                openMainMenu();
            });
            timer.setRepeats(false);
            timer.start();
            
        } else {
            loginView.setStatus("Username atau password salah!", true);
            loginView.clearFields();
            loginView.focusUsername();
        }
    }

    private void handleExit() {
        int confirm = JOptionPane.showConfirmDialog(loginView,
                "Apakah Anda yakin ingin keluar?",
                "Konfirmasi Keluar",
                JOptionPane.YES_NO_OPTION);
                
        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private void openMainMenu() {
        try {
            mainMenuView = new MainMenuView(currentUser);
            setupMainMenuActions();
            mainMenuView.setVisible(true);
            
            LOGGER.info("User " + currentUser.getNama() + " berhasil login dengan role " + currentUser.getRole());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Gagal membuka menu utama", e);
            JOptionPane.showMessageDialog(null,
                "Gagal membuka menu utama: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupMainMenuActions() {
        mainMenuView.setAbsensiAction(e -> openAbsensiModule());
        mainMenuView.setNilaiSiswaAction(e -> openNilaiSiswaModule());
        mainMenuView.setJadwalPelajaranAction(e -> openJadwalPelajaranModule());
        mainMenuView.setPeminjamanBukuAction(e -> openPeminjamanBukuModule());
        mainMenuView.setInventarisAction(e -> openInventarisModule());
        mainMenuView.setLogoutAction(e -> handleLogout());
    }

    private void openAbsensiModule() {
        try {
            // Validasi role user untuk akses modul absensi
            if (!hasPermissionForModule("absensi")) {
                JOptionPane.showMessageDialog(mainMenuView,
                    "Anda tidak memiliki akses ke modul Absensi Siswa",
                    "Akses Ditolak", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Buat window baru untuk modul absensi
            JFrame absensiFrame = new JFrame("Modul Absensi Siswa");
            absensiFrame.setSize(900, 600);
            absensiFrame.setLocationRelativeTo(mainMenuView);
            absensiFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            // Inisialisasi komponen absensi
            AbsensiModel absensiModel = new AbsensiModel();
            AbsensiView absensiView = new AbsensiView();
            
            // Set content pane
            absensiFrame.setContentPane(absensiView.getContentPane());
            
            // Inisialisasi controller
            AbsensiController absensiController = new AbsensiController(absensiModel, absensiView);
            
            // Tampilkan window
            absensiFrame.setVisible(true);
            
            LOGGER.info("Modul Absensi Siswa dibuka oleh user: " + currentUser.getNama());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Gagal membuka modul absensi siswa", e);
            JOptionPane.showMessageDialog(mainMenuView,
                "Gagal membuka modul Absensi Siswa: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openNilaiSiswaModule() {
        showModuleNotImplemented("Nilai Siswa");
    }

    private void openJadwalPelajaranModule() {
        try {
            if (!hasPermissionForModule("jadwal")) {
                JOptionPane.showMessageDialog(mainMenuView,
                    "Anda tidak memiliki akses ke modul Jadwal Pelajaran.",
                    "Akses Ditolak", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Inisialisasi komponen Jadwal
            // JadwalView sudah merupakan JFrame, jadi tidak perlu membuat JFrame baru.
            JadwalModel jadwalModel = new JadwalModel();
            JadwalView jadwalView = new JadwalView(); // JadwalView adalah JFrame
            new JadwalController(jadwalView, jadwalModel); // Kirim view dulu baru model sesuai konstruktor JadwalController

            // Atur properti frame JadwalView
            jadwalView.setLocationRelativeTo(mainMenuView);
            // jadwalView.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Ini sudah di set di constructor JadwalView
            jadwalView.setVisible(true);

            LOGGER.info("Modul Jadwal Pelajaran dibuka oleh user: " + currentUser.getNama());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Gagal membuka modul Jadwal Pelajaran", e);
            JOptionPane.showMessageDialog(mainMenuView,
                "Gagal membuka modul Jadwal Pelajaran: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openPeminjamanBukuModule() {
        try {
            if (!hasPermissionForModule("peminjaman")) {
                JOptionPane.showMessageDialog(mainMenuView,
                    "Anda tidak memiliki akses ke modul Peminjaman Buku",
                    "Akses Ditolak", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JFrame peminjamanFrame = new JFrame("Modul Peminjaman Buku"); // Ini adalah parentFrame
            peminjamanFrame.setSize(800, 600);
            peminjamanFrame.setLocationRelativeTo(mainMenuView);
            peminjamanFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            PeminjamanModel peminjamanModel = new PeminjamanModel();
            PeminjamanView peminjamanView = new PeminjamanView(); // PeminjamanView adalah JFrame

            // Set content pane dari peminjamanView ke peminjamanFrame
            peminjamanFrame.setContentPane(peminjamanView.getContentPane());

            // Inisialisasi PeminjamanController dan teruskan peminjamanFrame
            new PeminjamanController(peminjamanModel, peminjamanView, peminjamanFrame); 

            peminjamanFrame.setVisible(true);

            LOGGER.info("Modul Peminjaman Buku dibuka oleh user: " + currentUser.getNama());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Gagal membuka modul peminjaman buku", e);
            JOptionPane.showMessageDialog(mainMenuView,
                "Gagal membuka modul Peminjaman Buku: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openInventarisModule() {
        try {
            // Validasi role user untuk akses modul inventaris
            if (!hasPermissionForModule("inventaris")) {
                JOptionPane.showMessageDialog(mainMenuView,
                    "Anda tidak memiliki akses ke modul Inventaris",
                    "Akses Ditolak", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Buat window baru untuk modul inventaris
            JFrame inventarisFrame = new JFrame("Modul Inventaris Sekolah");
            inventarisFrame.setSize(1000, 700);
            inventarisFrame.setLocationRelativeTo(mainMenuView);
            inventarisFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            inventarisFrame.setMinimumSize(new java.awt.Dimension(800, 600));

            // Inisialisasi komponen inventaris
            InventarisModel inventarisModel = new InventarisModel();
            InventarisView inventarisView = new InventarisView();
            
            // Set agar tidak auto exit saat ditutup dari main
            inventarisView.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            
            // Set content pane
            inventarisFrame.setContentPane(inventarisView.getContentPane());
            
            // Inisialisasi controller
            InventarisController inventarisController = new InventarisController(inventarisModel, inventarisView);
            
            // Tampilkan window
            inventarisFrame.setVisible(true);
            
            LOGGER.info("Modul Inventaris dibuka oleh user: " + currentUser.getNama() + " dengan role: " + currentUser.getRole());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Gagal membuka modul inventaris", e);
            JOptionPane.showMessageDialog(mainMenuView,
                "Gagal membuka modul Inventaris: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showModuleNotImplemented(String moduleName) {
        JOptionPane.showMessageDialog(mainMenuView,
            "Modul " + moduleName + " belum diimplementasikan.\n" +
            "Modul ini akan segera tersedia dalam versi mendatang.",
            "Modul Belum Tersedia", JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean hasPermissionForModule(String module) {
        // Implementasi sederhana permission berdasarkan role
        String role = currentUser.getRole().toLowerCase();
        
        switch (module.toLowerCase()) {
            case "peminjaman":
                // Semua role bisa akses peminjaman buku
                return true;
            case "absensi":
                // Hanya guru dan staff yang bisa akses absensi
                return role.equals("guru") || role.equals("staff") || role.equals("kepala_sekolah");
            case "nilai":
                // Hanya guru dan kepala sekolah yang bisa akses nilai
                return role.equals("guru") || role.equals("kepala_sekolah");
            case "jadwal":
                // Semua role bisa melihat jadwal
                return true;
            case "inventaris":
                // Hanya staff dan kepala sekolah yang bisa akses inventaris
                return role.equals("staff") || role.equals("kepala_sekolah");
            default:
                return false;
        }
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(mainMenuView,
                "Apakah Anda yakin ingin logout?",
                "Konfirmasi Logout",
                JOptionPane.YES_NO_OPTION);
                
        if (confirm == JOptionPane.YES_OPTION) {
            LOGGER.info("User " + currentUser.getNama() + " melakukan logout");
            
            // Tutup main menu
            mainMenuView.dispose();
            
            // Reset current user
            currentUser = null;
            
            // Kembali ke login
            initializeLogin();
        }
    }

    // Method untuk mendapatkan current user (bisa digunakan oleh modul lain)
    public User getCurrentUser() {
        return currentUser;
    }

    // Main method untuk menjalankan aplikasi
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new MainController();
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Gagal menginisialisasi aplikasi", e);
                JOptionPane.showMessageDialog(null,
                    "Gagal menginisialisasi aplikasi: " + e.getMessage(),
                    "Error Sistem", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}