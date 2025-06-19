package main;

import java.awt.Component;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

import login.LoginModel;
import login.LoginView;
import peminjaman.*;
import absensi.*;
import inventaris.*;
import jadwal.*;
import NilaiSiswa.*;
import registrasi.*;
import manajemenuser.*;
import login.LoginModel.User;
import shared.Koneksi;

public class MainController {
    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());
    
    private LoginModel loginModel;
    private LoginView loginView;
    private MainMenuView mainMenuView;
    private User currentUser;
    private WaliKelasController waliKelasController;
    private NilaiController nilaiController;
    private String lastSelectedKelas;
    private String lastSelectedSemester; // Added to track last selected semester

    public MainController() {
        initializeLogin();
    }

    private void initializeLogin() {
        try {
            loginModel = new LoginModel();
            int userCount = loginModel.getUserCount();
    
            if (userCount == 0) {
                LOGGER.info("Database user kosong. Memulai mode registrasi admin pertama.");
                showFirstUserRegistration();
            } else if (userCount > 0) {
                loginView = new LoginView();
                setupLoginActions();
                loginView.setVisible(true);
                loginView.focusUsername();
            } else {
                JOptionPane.showMessageDialog(null, "Tidak dapat terhubung atau memverifikasi database.", "Error Kritis", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
    
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Gagal menginisialisasi aplikasi", e);
            JOptionPane.showMessageDialog(null, "Gagal menginisialisasi aplikasi: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void showFirstUserRegistration() {
        RegistrasiAdminView regView = new RegistrasiAdminView();
        RegistrasiAdminModel regModel = new RegistrasiAdminModel();
        new RegistrasiAdminController(regView, regModel, this); 
        regView.setVisible(true);
    }

    public void onFirstUserRegistered() {
        initializeLogin();
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

        User user = loginModel.validateLogin(username, password);
        
        if (user != null) {
            currentUser = user;
            loginView.setStatus("Login berhasil! Membuka menu utama...", false);
            
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
        mainMenuView.setManajemenUserAction(e -> openUserManagementModule());
    }

    private void openAbsensiModule() {
        try {
            if (!hasPermissionForModule("absensi")) {
                JOptionPane.showMessageDialog(mainMenuView,
                    "Anda tidak memiliki akses ke modul Absensi Siswa",
                    "Akses Ditolak", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JFrame absensiFrame = new JFrame("Modul Absensi Siswa");
            absensiFrame.setSize(900, 600);
            absensiFrame.setLocationRelativeTo(mainMenuView);
            absensiFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            AbsensiModel absensiModel = new AbsensiModel();
            AbsensiView absensiView = new AbsensiView();
            
            absensiFrame.setContentPane(absensiView.getContentPane());
            
            AbsensiController absensiController = new AbsensiController(absensiModel, absensiView, this);
            
            absensiFrame.setVisible(true);
            
            LOGGER.info("Modul Absensi Siswa dibuka oleh user: " + currentUser.getNama());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Gagal membuka modul absensi siswa", e);
            JOptionPane.showMessageDialog(mainMenuView,
                "Gagal membuka modul Absensi Siswa: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

public void openNilaiSiswaModule() {
    if (currentUser == null || !currentUser.getRole().equals("guru")) {
        JOptionPane.showMessageDialog(mainMenuView, "Hanya guru yang dapat mengakses fitur input nilai!", "Peringatan", JOptionPane.WARNING_MESSAGE);
        LOGGER.warning("Akses input nilai ditolak: user=" + (currentUser != null ? currentUser.getRole() : "null"));
        return;
    }

    SwingUtilities.invokeLater(() -> {
        try {
            int userId = currentUser.getId();
            int mapelId = getMapelIdForGuru(userId);
            if (mapelId == -1) {
                JOptionPane.showMessageDialog(mainMenuView, "Tidak ada mata pelajaran yang terkait dengan guru ini!", "Error", JOptionPane.ERROR_MESSAGE);
                LOGGER.severe("Tidak ada mapel_id untuk guru_id=" + userId);
                return;
            }

            InputNilaiView inputNilaiView = new InputNilaiView();
            inputNilaiView.setMainController(this);
            NilaiModel nilaiModel = new NilaiModel(Koneksi.getConnection());
            nilaiController = new NilaiController(inputNilaiView, nilaiModel, this);
            inputNilaiView.setController(nilaiController);
            if (lastSelectedKelas != null) {
                nilaiController.setLastSelectedKelas(lastSelectedKelas);
            }
            if (lastSelectedSemester != null) {
                nilaiController.setLastSelectedSemester(lastSelectedSemester);
            }
            mainMenuView.setVisible(false); // Sembunyikan, bukan dispose
            inputNilaiView.setVisible(true);
            nilaiController.loadDataAwal();
            LOGGER.info("Membuka modul Input Nilai untuk guru_id=" + userId + ", mapel_id=" + mapelId + ", kelas=" + lastSelectedKelas + ", semester=" + lastSelectedSemester);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainMenuView, "Gagal membuka fitur Input Nilai: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            if (mainMenuView != null) {
                mainMenuView.setVisible(true); // Tampilkan kembali jika error
            }
            LOGGER.severe("Gagal membuka Input Nilai: " + e.getMessage());
        }
    });
}
public void openWaliKelasModule() {
    if (currentUser == null || !currentUser.getRole().equals("guru")) {
        JOptionPane.showMessageDialog(mainMenuView, "Hanya guru yang dapat mengakses fitur Wali Kelas!", "Peringatan", JOptionPane.WARNING_MESSAGE);
        LOGGER.warning("Akses Wali Kelas ditolak: user=" + (currentUser != null ? currentUser.getRole() : "null"));
        return;
    }

    SwingUtilities.invokeLater(() -> {
        try {
            if (nilaiController != null && nilaiController.getLastSelectedKelas() != null) {
                lastSelectedKelas = nilaiController.getLastSelectedKelas();
                lastSelectedSemester = nilaiController.getLastSelectedSemester();
                LOGGER.info("lastSelectedKelas dan lastSelectedSemester diperbarui dari NilaiController: " + lastSelectedKelas + ", " + lastSelectedSemester);
            }
            
            WaliKelasView waliKelasView = new WaliKelasView();
            WaliKelasModel waliKelasModel = new WaliKelasModel();
            waliKelasController = new WaliKelasController(waliKelasView, waliKelasModel, this);
            
            if (waliKelasView == null || waliKelasModel == null || waliKelasController == null) {
                throw new RuntimeException("Gagal menginisialisasi komponen Wali Kelas: null reference detected");
            }
            
            waliKelasView.setController(waliKelasController);
            waliKelasView.setMainController(this);
            
            if (lastSelectedKelas != null) {
                waliKelasView.getComboKelas().setSelectedItem(lastSelectedKelas);
                if (lastSelectedSemester != null) {
                    waliKelasView.getComboSemester().setSelectedItem("Semester " + lastSelectedSemester);
                }
                LOGGER.info("lastSelectedKelas dan lastSelectedSemester diterapkan ke WaliKelasView: " + lastSelectedKelas + ", " + lastSelectedSemester);
            }
            
            mainMenuView.setVisible(false); // Sembunyikan, bukan dispose
            waliKelasView.setVisible(true);
            waliKelasController.loadDataAwal();
            LOGGER.info("Modul Wali Kelas dibuka untuk user: " + currentUser.getNama());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainMenuView, "Gagal membuka fitur Wali Kelas: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            if (mainMenuView != null) {
                mainMenuView.setVisible(true); // Tampilkan kembali jika error
            }
            LOGGER.severe("Gagal membuka Wali Kelas: " + e.getMessage());
        }
    });
}

    public int getMapelIdForGuru(int guruId) {
        Connection conn = Koneksi.getConnection();
        if (conn == null) {
            LOGGER.severe("Gagal mendapatkan koneksi database");
            return -1;
        }

        String sql = "SELECT mapel_id FROM jadwal WHERE guru_id = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, guruId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("mapel_id");
            }
        } catch (SQLException e) {
            LOGGER.severe("Gagal mengambil mapel_id untuk guru_id=" + guruId + ": " + e.getMessage());
        }
        return -1;
    }

    private void openJadwalPelajaranModule() {
        try {
            if (!hasPermissionForModule("jadwal")) {
                JOptionPane.showMessageDialog(mainMenuView,
                    "Anda tidak memiliki akses ke modul Jadwal Pelajaran.",
                    "Akses Ditolak", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JadwalModel jadwalModel = new JadwalModel();
            JadwalView jadwalView = new JadwalView();
            new JadwalController(jadwalView, jadwalModel);

            jadwalView.setLocationRelativeTo(mainMenuView);
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

            JFrame peminjamanFrame = new JFrame("Modul Peminjaman Buku");
            peminjamanFrame.setSize(800, 600);
            peminjamanFrame.setLocationRelativeTo(mainMenuView);
            peminjamanFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            PeminjamanModel peminjamanModel = new PeminjamanModel();
            PeminjamanView peminjamanView = new PeminjamanView();

            peminjamanFrame.setContentPane(peminjamanView.getContentPane());

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
            if (!hasPermissionForModule("inventaris")) {
                JOptionPane.showMessageDialog(mainMenuView,
                    "Anda tidak memiliki akses ke modul Inventaris",
                    "Akses Ditolak", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JFrame inventarisFrame = new JFrame("Modul Inventaris Sekolah");
            inventarisFrame.setSize(1000, 700);
            inventarisFrame.setLocationRelativeTo(mainMenuView);
            inventarisFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            inventarisFrame.setMinimumSize(new java.awt.Dimension(800, 600));

            InventarisModel inventarisModel = new InventarisModel();
            InventarisView inventarisView = new InventarisView();
            
            inventarisView.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            
            inventarisFrame.setContentPane(inventarisView.getContentPane());
            
            InventarisController inventarisController = new InventarisController(inventarisModel, inventarisView);
            
            inventarisFrame.setVisible(true);
            
            LOGGER.info("Modul Inventaris dibuka oleh user: " + currentUser.getNama() + " dengan role: " + currentUser.getRole());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Gagal membuka modul inventaris", e);
            JOptionPane.showMessageDialog(mainMenuView,
                "Gagal membuka modul Inventaris: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean hasPermissionForModule(String module) {
        if (currentUser == null) {
            return false;
        }
        String role = currentUser.getRole().toLowerCase();
        
        switch (module.toLowerCase()) {
            case "peminjaman":
                return role.equals("staff") || role.equals("kepala_sekolah");
            case "absensi":
                return role.equals("guru") || role.equals("kepala_sekolah");
            case "jadwal":
                return role.equals("guru") || role.equals("kepala_sekolah");
            case "inventaris":
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
            LOGGER.info("User " + (currentUser != null ? currentUser.getNama() : "unknown") + " melakukan logout");
            
            if (waliKelasController != null) {
                waliKelasController.shutdownScheduler();
                waliKelasController = null;
            }
            if (nilaiController != null) {
                nilaiController = null;
            }
            lastSelectedKelas = null;
            lastSelectedSemester = null; // Reset semester
            
            mainMenuView.dispose();
            
            currentUser = null;
            
            initializeLogin();
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }
    public void showMainMenu() {
    SwingUtilities.invokeLater(() -> {
        try {
            if (mainMenuView == null || !mainMenuView.isDisplayable()) {
                mainMenuView = new MainMenuView(currentUser);
                setupMainMenuActions();
                LOGGER.info("MainMenuView dibuat ulang untuk user: " + currentUser.getNama());
            }
            mainMenuView.setVisible(true);
            mainMenuView.toFront();
            LOGGER.info("MainMenuView ditampilkan kembali");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Gagal menampilkan MainMenuView", e);
            JOptionPane.showMessageDialog(null,
                "Gagal membuka menu utama: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    });
}

    public void setWaliKelasController(WaliKelasController controller) {
        this.waliKelasController = controller;
        LOGGER.info("WaliKelasController diatur di MainController");
    }

    public void notifyWaliKelasDataChanged() {
        if (waliKelasController != null) {
            waliKelasController.notifyDataChanged();
            LOGGER.info("Notifikasi perubahan data dikirim ke WaliKelasController");
        }
    }

    public void setLastSelectedKelas(String kelasNama) {
        this.lastSelectedKelas = kelasNama;
        LOGGER.info("lastSelectedKelas diatur di MainController: " + kelasNama);
    }

    public String getLastSelectedKelas() {
        return lastSelectedKelas;
    }

    public void setLastSelectedSemester(String semester) {
        this.lastSelectedSemester = semester;
        LOGGER.info("lastSelectedSemester diatur di MainController: " + semester);
    }

    public String getLastSelectedSemester() {
        return lastSelectedSemester;
    }

    private void openUserManagementModule() {
        if (!"kepala_sekolah".equals(currentUser.getRole())) {
            JOptionPane.showMessageDialog(mainMenuView, "Anda tidak memiliki hak akses untuk fitur ini.", "Akses Ditolak", JOptionPane.WARNING_MESSAGE);
            return;
        }

        UserManajemenView userView = new UserManajemenView();
        UserManajemenModel userModel = new UserManajemenModel();
        new UserManajemenController(userView, userModel);
        
        userView.setLocationRelativeTo(mainMenuView); // Tampilkan di tengah menu utama
        userView.setVisible(true);
        LOGGER.info("Modul Manajemen User dibuka oleh: " + currentUser.getNama());
    }

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