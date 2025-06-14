package peminjaman;

import java.awt.*;
import java.util.Calendar;
import javax.swing.*;
import javax.swing.table.DefaultTableModel; 

public class PeminjamanView extends JFrame {
    //Konstanta untuk pengaturan UI
    private static final int FORM_HGAP = 10;
    private static final int FORM_VGAP = 5;
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String[] MONTH_NAMES = {
        "Januari", "Februari", "Maret", "April", "Mei", "Juni", 
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    };
    private static final String[] TABLE_HEADERS = {
        "ID", "Siswa", "Buku", "Pinjam", "Jatuh Tempo", "Dikembalikan", "Denda"
    };
    
    //Komponen untuk pemilihan siswa dan buku
    public JComboBox<String> comboSiswa;
    public JComboBox<String> comboBuku;
    
    //Komponen untuk tanggal
    public JSpinner tanggalPinjam;
    public JSpinner tanggalKembali;
    public JSpinner tanggalDikembalikan;

    //Komponen untuk tabel riwayat
    public JTable tableRiwayat;
    public DefaultTableModel modelRiwayat;

    //Buttons
    public JButton btnSimpan;
    public JButton btnKembalikan;
    public JButton btnLihatRiwayat;
    public JButton btnKeBuku;

    // Label status
    public JLabel statusLabel;

    //Komponen untuk pencarian
    public JTextField searchField;
    public JButton btnCariSiswa;
    public JTextField searchRiwayatField;
    public JButton btnCariRiwayat;

    //Komponen untuk ekspor data
    public JButton btnExportCsv;
    public JButton btnExportPdf;
    public JComboBox<String> comboBulan;
    public JComboBox<String> comboTahun;

    //Konstruktor untuk PeminjamanView
    public PeminjamanView() {
        setTitle("Modul Peminjaman Buku");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();

    }

    private void initComponents() {
        //inisialisasi model tabel
        modelRiwayat = new DefaultTableModel(TABLE_HEADERS, 0);
        tableRiwayat = new JTable(modelRiwayat);
        
        //inisialisasi panel utama dengan BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false); // Set panel utama tidak opaque agar transparan
        
        //panel navigasi
        mainPanel.add(createNavigationPanel(), BorderLayout.NORTH);
        
        //panel utama (form + tabel)
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false); // Set panel tengah tidak opaque agar transparan
        centerPanel.add(createFormPanel(), BorderLayout.NORTH);
        centerPanel.add(createTablePanel(), BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        // Set panel utama opaque agar background transparan
        
        //status label
        statusLabel = new JLabel(" ");
        statusLabel.setOpaque(false); // Set label status tidak opaque agar transparan
        statusLabel.setForeground(new Color(10, 255,255));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        // === PANEL LATAR DENGAN GAMBAR ===
        JPanel panelBackground = new JPanel() {
        private Image bg;

        {
            try {
                bg = new ImageIcon(getClass().getResource("/shared/Asset/BG1.jpeg")).getImage();
            } catch (Exception e) {
                System.out.println("Gambar background tidak ditemukan: " + e.getMessage());
                bg = null;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg != null) {
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
            panelBackground.setLayout(new BorderLayout());

            
            //panel utama ke frame
            add(mainPanel);

            panelBackground.add(mainPanel, BorderLayout.CENTER);
            setContentPane(panelBackground);
        }
    
    //Membuat panel navigasi
    private JPanel createNavigationPanel() {
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        navPanel.setOpaque(false); // Set panel navigasi tidak opaque agar transparan
        btnKeBuku = new JButton("Ke Manajemen Buku");
        navPanel.add(btnKeBuku);
        return navPanel;
    }
    
    //Membuat panel form peminjaman
    private JPanel createFormPanel() {
        JPanel panelForm = new JPanel(new GridLayout(8, 2, FORM_HGAP, FORM_VGAP));
        panelForm.setBorder(BorderFactory.createTitledBorder("Form Peminjaman"));

        //Inisialisasi komponen pencarian siswa
        searchField = new JTextField();
        btnCariSiswa = new JButton("Cari");

        btnCariSiswa.setBackground(new Color(70, 130, 180)); // Warna biru
        btnCariSiswa.setForeground(Color.WHITE);
        btnCariSiswa.setOpaque(true);
        btnCariSiswa.setBorderPainted(false);

        JPanel panelCari = new JPanel(new BorderLayout(5, 0));
        panelCari.add(searchField, BorderLayout.CENTER);
        panelCari.add(btnCariSiswa, BorderLayout.EAST);
        
        //Inisialisasi komponen pilihan siswa dan buku
        comboSiswa = new JComboBox<>();
        comboBuku = new JComboBox<>();

        //Inisialisasi komponen tanggal
        tanggalPinjam = createDateSpinner();
        tanggalKembali = createDateSpinner();
        tanggalDikembalikan = createDateSpinner();
        tanggalDikembalikan.setEnabled(false);

        //Inisialisasi tombol aksi
        btnSimpan = new JButton("Simpan Peminjaman");
        btnKembalikan = new JButton("Proses Pengembalian");
        btnLihatRiwayat = new JButton("Lihat Riwayat");

        btnSimpan.setBackground(new Color(70, 130, 180)); // Warna biru
        btnSimpan.setForeground(Color.WHITE);
        btnSimpan.setOpaque(true);
        btnSimpan.setBorderPainted(false);

        btnKembalikan.setBackground(new Color(34, 139, 34)); // Warna hijau
        btnKembalikan.setForeground(Color.WHITE);
        btnKembalikan.setOpaque(true);
        btnKembalikan.setBorderPainted(false);

        btnKeBuku.setBackground(new Color(255, 69, 0)); // Warna merah
        btnKeBuku.setForeground(Color.WHITE);
        btnKeBuku.setOpaque(true);
        btnKeBuku.setBorderPainted(false);

        //Tambahkan komponen ke panel form
        panelForm.add(new JLabel("Cari Siswa (Nama):"));
        panelForm.add(panelCari);
        panelForm.add(new JLabel("Nama Siswa:"));
        panelForm.add(comboSiswa);
        panelForm.add(new JLabel("Judul Buku:"));
        panelForm.add(comboBuku);
        panelForm.add(new JLabel("Tanggal Pinjam:"));
        panelForm.add(tanggalPinjam);
        panelForm.add(new JLabel("Tanggal Kembali (Rencana):"));
        panelForm.add(tanggalKembali);
        panelForm.add(new JLabel("Tanggal Dikembalikan:"));
        panelForm.add(tanggalDikembalikan);
        panelForm.add(btnSimpan);
        panelForm.add(btnKembalikan);

        return panelForm;
    }
    
    //Utilitas untuk membuat JSpinner tanggal dengan format yang seragam

    private JSpinner createDateSpinner() {
        JSpinner spinner = new JSpinner(
            new SpinnerDateModel(new java.util.Date(), null, null, Calendar.DAY_OF_MONTH)
        );
        spinner.setEditor(new JSpinner.DateEditor(spinner, DATE_FORMAT));
        return spinner;
    }
    
    //Membuat panel tabel riwayat
    private JPanel createTablePanel() {
        JPanel panelTabel = new JPanel(new BorderLayout());
        panelTabel.setBorder(BorderFactory.createTitledBorder("Riwayat Peminjaman"));
        
        //Panel pencarian riwayat
        searchRiwayatField = new JTextField();
        btnCariRiwayat = new JButton("Cari Riwayat");
        btnCariRiwayat.setBackground(new Color(70, 130, 180)); // Warna biru
        btnCariRiwayat.setForeground(Color.WHITE);
        btnCariRiwayat.setOpaque(true);
        btnCariRiwayat.setBorderPainted(false);

        JPanel panelCariRiwayat = new JPanel(new BorderLayout(5, 0));
        panelCariRiwayat.add(searchRiwayatField, BorderLayout.CENTER);
        panelCariRiwayat.add(btnCariRiwayat, BorderLayout.EAST);
        panelTabel.add(panelCariRiwayat, BorderLayout.NORTH);
        
        //Panel tabel
        panelTabel.add(new JScrollPane(tableRiwayat), BorderLayout.CENTER);
        
        //Panel bagian bawah (ekspor dan tombol lihat riwayat)
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(btnLihatRiwayat = new JButton("Lihat Riwayat"), BorderLayout.NORTH);

        btnLihatRiwayat.setBackground(new Color(70, 130, 190)); // Warna biru
        btnLihatRiwayat.setForeground(Color.WHITE);
        btnLihatRiwayat.setOpaque(true);
        btnLihatRiwayat.setBorderPainted(false);

        southPanel.add(createExportPanel(), BorderLayout.SOUTH);
        panelTabel.add(southPanel, BorderLayout.SOUTH);
        
        return panelTabel;
    }
    
    //panel ekspor data
    private JPanel createExportPanel() {
        JPanel panelEkspor = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelEkspor.setBorder(BorderFactory.createTitledBorder("Ekspor Data"));
        
        //Set bulan saat ini sebagai default
        Calendar cal = Calendar.getInstance();
        comboBulan = new JComboBox<>(MONTH_NAMES);
        comboBulan.setSelectedIndex(cal.get(Calendar.MONTH));
        
        //Komponen untuk memilih tahun (5 tahun ke belakang dan 5 tahun ke depan)
        String[] tahun = new String[11];
        int tahunSekarang = cal.get(Calendar.YEAR);
        int indexTahunSekarang = 5; //Posisi tahun sekarang di array
        
        for (int i = 0; i < tahun.length; i++) {
            tahun[i] = String.valueOf(tahunSekarang - indexTahunSekarang + i);
        }
        comboTahun = new JComboBox<>(tahun);
        comboTahun.setSelectedIndex(indexTahunSekarang);
        
        btnExportCsv = new JButton("Export ke CSV");
        btnExportPdf = new JButton("Export ke PDF");
        
        btnExportCsv.setBackground(new Color(70, 130, 180)); // Warna biru
        btnExportCsv.setForeground(Color.WHITE);
        btnExportCsv.setOpaque(true);
        btnExportCsv.setBorderPainted(false);
        btnExportPdf.setBackground(new Color(70, 130, 180)); // Warna biru
        btnExportPdf.setForeground(Color.WHITE);
        btnExportPdf.setOpaque(true);
        btnExportPdf.setBorderPainted(false);

        panelEkspor.add(new JLabel("Bulan:"));
        panelEkspor.add(comboBulan);
        panelEkspor.add(new JLabel("Tahun:"));
        panelEkspor.add(comboTahun);
        panelEkspor.add(btnExportCsv);
        panelEkspor.add(btnExportPdf);
        
        return panelEkspor;
    }

    public void setSimpanAction(java.awt.event.ActionListener act) {
        btnSimpan.addActionListener(act);
    }

    public void setKembalikanAction(java.awt.event.ActionListener act) {
        btnKembalikan.addActionListener(act);
    }

    public void setMuatRiwayatAction(java.awt.event.ActionListener act) {
        btnLihatRiwayat.addActionListener(act);
    }

    public void setCariSiswaAction(java.awt.event.ActionListener act) {
        btnCariSiswa.addActionListener(act);
    }

    public void setCariRiwayatAction(java.awt.event.ActionListener act) {
        btnCariRiwayat.addActionListener(act);
    }

    public void setKeBukuAction(java.awt.event.ActionListener act) {
        btnKeBuku.addActionListener(act);
    }

    public void setExportCsvAction(java.awt.event.ActionListener act) {
        btnExportCsv.addActionListener(act);
    }

    public void setExportPdfAction(java.awt.event.ActionListener act) {
        btnExportPdf.addActionListener(act);
    }

    public String getInputPencarianSiswa() {
        return searchField.getText().trim();
    }

    public String getInputPencarianRiwayat() {
        return searchRiwayatField.getText().trim();
    }

    public void enableTanggalDikembalikan(boolean enable) {
        tanggalDikembalikan.setEnabled(enable);
        if (!enable) {
            // Set ulang ke tanggal hari ini agar valid
            tanggalDikembalikan.setValue(new java.util.Date());
        }
    }

    public int getSelectedBulan() {
        return comboBulan.getSelectedIndex() + 1; 
    }

    public int getSelectedTahun() {
        return Integer.parseInt(comboTahun.getSelectedItem().toString());
    }
    
}
