package main;

import java.awt.*;
import javax.swing.*;
import login.LoginModel.User;

public class MainMenuView extends JFrame {
    // Konstanta untuk pengaturan UI
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 24);
    private static final Font SUBTITLE_FONT = new Font("Arial", Font.BOLD, 16);
    private static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 14);
    private static final Font INFO_FONT = new Font("Arial", Font.PLAIN, 12);
    
    // Warna tema
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private static final Color SECONDARY_COLOR = new Color(41, 128, 185);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color CARD_COLOR = Color.WHITE;
    
    // Komponen navigasi
    public JButton btnAbsensi;
    public JButton btnNilaiSiswa;
    public JButton btnJadwalPelajaran;
    public JButton btnPeminjamanBuku;
    public JButton btnInventaris;
    public JButton btnLogout;
    public JButton btnManajemenUser;
    
    // Label informasi user
    public JLabel userInfoLabel;
    public JLabel timeLabel;
    
    private User currentUser;

    public MainMenuView(User user) {
        this.currentUser = user;
        setTitle("Sistem Informasi SMPN 1 Adiluwih - Menu Utama");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initComponents();
        
        // Update waktu setiap detik
        startTimeUpdate();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Panel header
        add(createHeaderPanel(), BorderLayout.NORTH);
        
        // Panel menu utama
        add(createMenuPanel(), BorderLayout.CENTER);
        
        // Panel footer
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(WINDOW_WIDTH, 120));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Panel kiri - Informasi sekolah
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(PRIMARY_COLOR);

        JLabel titleLabel = new JLabel("SISTEM INFORMASI");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel schoolLabel = new JLabel("SMPN 1 Adiluwih");
        schoolLabel.setFont(SUBTITLE_FONT);
        schoolLabel.setForeground(Color.WHITE);
        schoolLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(titleLabel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(schoolLabel);

        // Panel kanan - Informasi user dan waktu
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(PRIMARY_COLOR);

        userInfoLabel = new JLabel("Selamat datang, " + currentUser.getNama());
        userInfoLabel.setFont(INFO_FONT);
        userInfoLabel.setForeground(Color.WHITE);
        userInfoLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel roleLabel = new JLabel("Role: " + formatRole(currentUser.getRole()));
        roleLabel.setFont(INFO_FONT);
        roleLabel.setForeground(Color.WHITE);
        roleLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        timeLabel = new JLabel();
        timeLabel.setFont(INFO_FONT);
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        rightPanel.add(userInfoLabel);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(roleLabel);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(timeLabel);

        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createMenuPanel() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridBagLayout());
        menuPanel.setBackground(BACKGROUND_COLOR);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        // Baris pertama
        gbc.gridx = 0; gbc.gridy = 0;
        btnAbsensi = createMenuButton("Absensi Siswa", "📝", SECONDARY_COLOR);
        menuPanel.add(btnAbsensi, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        btnNilaiSiswa = createMenuButton("Nilai Siswa", "📊", new Color(46, 204, 113));
        menuPanel.add(btnNilaiSiswa, gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        btnJadwalPelajaran = createMenuButton("Jadwal Pelajaran", "📅", new Color(155, 89, 182));
        menuPanel.add(btnJadwalPelajaran, gbc);

        // Baris kedua
        gbc.gridx = 0; gbc.gridy = 1;
        btnPeminjamanBuku = createMenuButton("Peminjaman Buku", "📚", new Color(230, 126, 34));
        menuPanel.add(btnPeminjamanBuku, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        btnInventaris = createMenuButton("Inventaris", "📦", new Color(52, 73, 94));
        menuPanel.add(btnInventaris, gbc);

        // Tombol Manajemen User dan Logout berdampingan jika user adalah kepala sekolah
        JPanel bottomButtonPanel = new JPanel(new GridLayout(1, 2, 15, 0)); 
        bottomButtonPanel.setOpaque(false); // Transparan

        btnManajemenUser = createMenuButton("Manajemen User", "👥", new Color(112, 128, 144));
        btnLogout = createMenuButton("Logout", "🚪", new Color(231, 76, 60));

        gbc.gridx = 2; gbc.gridy = 1;
        menuPanel.add(createBottomRightPanel(), gbc);

        return menuPanel;
    }

    private JPanel createBottomRightPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 0));
        panel.setOpaque(false);

        btnManajemenUser = createMenuButton("Manajemen User", "👥", new Color(112, 128, 144));
        btnLogout = createMenuButton("Logout", "🚪", new Color(231, 76, 60));

        // Hanya tampilkan tombol Manajemen User jika role adalah kepala_sekolah
        if (currentUser != null && "kepala_sekolah".equals(currentUser.getRole())) {
            panel.add(btnManajemenUser);
        } else {
            // Tambahkan komponen kosong agar tombol Logout tetap di posisi yang sama
            panel.add(new JLabel("")); 
        }
        panel.add(btnLogout);
        return panel;
    }

    private JButton createMenuButton(String text, String icon, Color backgroundColor) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout());
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setPreferredSize(new Dimension(200, 120));

        // Icon (emoji sebagai teks besar)
        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        iconLabel.setForeground(Color.WHITE);

        // Text
        JLabel textLabel = new JLabel(text, SwingConstants.CENTER);
        textLabel.setFont(BUTTON_FONT);
        textLabel.setForeground(Color.WHITE);

        button.add(iconLabel, BorderLayout.CENTER);
        button.add(textLabel, BorderLayout.SOUTH);

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = backgroundColor;
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(originalColor.darker());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(originalColor);
            }
        });

        return button;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(127, 140, 141));
        footerPanel.setPreferredSize(new Dimension(WINDOW_WIDTH, 30));
        footerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JLabel footerLabel = new JLabel("© 2024 SMPN 1 Adiluwih - Sistem Informasi Sekolah");
        footerLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        footerLabel.setForeground(Color.WHITE);

        footerPanel.add(footerLabel);
        return footerPanel;
    }

    private String formatRole(String role) {
        if (role == null) return "User";
        
        switch (role.toLowerCase()) {
            case "guru": return "Guru";
            case "staff": return "Staff";
            case "kepala_sekolah": return "Kepala Sekolah";
            default: return "User";
        }
    }

    private void startTimeUpdate() {
        Timer timer = new Timer(1000, e -> {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
            timeLabel.setText(sdf.format(new java.util.Date()));
        });
        timer.start();
        
        // Set waktu awal
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
        timeLabel.setText(sdf.format(new java.util.Date()));
    }

    // Action listeners untuk setiap menu
    public void setAbsensiAction(java.awt.event.ActionListener action) {
        btnAbsensi.addActionListener(action);
    }

    public void setNilaiSiswaAction(java.awt.event.ActionListener action) {
        btnNilaiSiswa.addActionListener(action);
    }

    public void setJadwalPelajaranAction(java.awt.event.ActionListener action) {
        btnJadwalPelajaran.addActionListener(action);
    }

    public void setPeminjamanBukuAction(java.awt.event.ActionListener action) {
        btnPeminjamanBuku.addActionListener(action);
    }

    public void setInventarisAction(java.awt.event.ActionListener action) {
        btnInventaris.addActionListener(action);
    }

    public void setLogoutAction(java.awt.event.ActionListener action) {
        btnLogout.addActionListener(action);
    }

    public void setManajemenUserAction(java.awt.event.ActionListener action) {
        if (btnManajemenUser != null) {
            btnManajemenUser.addActionListener(action);
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }
}