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
        showModuleNotImplemented("Absensi Siswa");
    }

    private void openNilaiSiswaModule() {
        showModuleNotImplemented("Nilai Siswa");
    }

    private void openJadwalPelajaranModule() {
        showModuleNotImplemented("Jadwal Pelajaran");
    }

    private void openPeminjamanBukuModule() {
        try {
            // Validasi role user untuk akses modul peminjaman
            if (!hasPermissionForModule("peminjaman")) {
                JOptionPane.showMessageDialog(mainMenuView,
                    "Anda tidak memiliki akses ke modul Peminjaman Buku",
                    "Akses Ditolak", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Buat window baru untuk modul peminjaman
            JFrame peminjamanFrame = new JFrame("Modul Peminjaman Buku");
            peminjamanFrame.setSize(800, 600);
            peminjamanFrame.setLocationRelativeTo(mainMenuView);
            peminjamanFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            // Inisialisasi komponen peminjaman
            PeminjamanModel peminjamanModel = new PeminjamanModel();
            PeminjamanView peminjamanView = new PeminjamanView();
            
            // Set content pane
            peminjamanFrame.setContentPane(peminjamanView.getContentPane());
            
            // Inisialisasi controller
            PeminjamanController peminjamanController = new PeminjamanController(peminjamanModel, peminjamanView);
            
            // Tampilkan window
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
        showModuleNotImplemented("Inventaris");
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