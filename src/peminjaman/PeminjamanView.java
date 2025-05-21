package peminjaman;

import java.awt.*;
import java.util.Calendar;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class PeminjamanView extends JFrame {
    public JComboBox<String> comboSiswa;
    public JComboBox<String> comboBuku;
    public JSpinner tanggalPinjam;
    public JSpinner tanggalKembali;
    public JSpinner tanggalDikembalikan;

    public JTable tableRiwayat;
    public DefaultTableModel modelRiwayat;

    public JButton btnSimpan;
    public JButton btnKembalikan;
    public JButton btnLihatRiwayat;
    public JButton btnKeBuku; // Tombol untuk navigasi ke modul buku

    public JLabel statusLabel;

    public JTextField searchField;
    public JButton btnCariSiswa;

    public JTextField searchRiwayatField;
    public JButton btnCariRiwayat;

    public JButton btnExportCsv;
    public JComboBox<String> comboBulan;
    public JComboBox<String> comboTahun;

    public PeminjamanView() {
        setTitle("Modul Peminjaman Buku");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        // Panel navigasi di bagian atas
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnKeBuku = new JButton("Ke Manajemen Buku");
        navPanel.add(btnKeBuku);
        
        JPanel panelForm = new JPanel(new GridLayout(8, 2, 10, 5));

        searchField = new JTextField();
        btnCariSiswa = new JButton("Cari");

        comboSiswa = new JComboBox<>();
        comboBuku = new JComboBox<>();

        tanggalPinjam = new JSpinner(
            new SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.DAY_OF_MONTH)
        );
        tanggalKembali = new JSpinner(
            new SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.DAY_OF_MONTH)
        );
        tanggalDikembalikan = new JSpinner(
            new SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.DAY_OF_MONTH)
        );

        // Set format tanggal
        tanggalPinjam.setEditor(new JSpinner.DateEditor(tanggalPinjam, "yyyy-MM-dd"));
        tanggalKembali.setEditor(new JSpinner.DateEditor(tanggalKembali, "yyyy-MM-dd"));
        tanggalDikembalikan.setEditor(new JSpinner.DateEditor(tanggalDikembalikan, "yyyy-MM-dd"));

        // Nonaktifkan awal spinner dikembalikan (tanpa set null)
        tanggalDikembalikan.setEnabled(false);

        btnSimpan = new JButton("Simpan Peminjaman");
        btnKembalikan = new JButton("Proses Pengembalian");
        btnLihatRiwayat = new JButton("Lihat Riwayat");

        panelForm.setBorder(BorderFactory.createTitledBorder("Form Peminjaman"));

        // Baris pencarian siswa
        panelForm.add(new JLabel("Cari Siswa (Nama):"));
        JPanel panelCari = new JPanel(new BorderLayout(5, 0));
        panelCari.add(searchField, BorderLayout.CENTER);
        panelCari.add(btnCariSiswa, BorderLayout.EAST);
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

        modelRiwayat = new DefaultTableModel(new String[]{
            "ID", "Siswa", "Buku", "Pinjam", "Jatuh Tempo", "Dikembalikan", "Denda"
        }, 0);

        tableRiwayat = new JTable(modelRiwayat);

        JPanel panelCariRiwayat = new JPanel(new BorderLayout(5, 0));
        searchRiwayatField = new JTextField();
        btnCariRiwayat = new JButton("Cari Riwayat");
        panelCariRiwayat.add(searchRiwayatField, BorderLayout.CENTER);
        panelCariRiwayat.add(btnCariRiwayat, BorderLayout.EAST);

        JPanel panelTabel = new JPanel(new BorderLayout());
        panelTabel.setBorder(BorderFactory.createTitledBorder("Riwayat Peminjaman"));
        panelTabel.add(new JScrollPane(tableRiwayat), BorderLayout.CENTER);
        panelTabel.add(panelCariRiwayat, BorderLayout.NORTH);
        panelTabel.add(btnLihatRiwayat, BorderLayout.SOUTH);

        statusLabel = new JLabel(" ");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(panelForm, BorderLayout.NORTH);
        mainPanel.add(panelTabel, BorderLayout.CENTER);

        add(navPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        JPanel panelEkspor = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelEkspor.setBorder(BorderFactory.createTitledBorder("Ekspor Data"));
        
        // Komponen untuk memilih bulan dan tahun
        String[] namaBulan = {"Januari", "Februari", "Maret", "April", "Mei", "Juni", 
                            "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
        comboBulan = new JComboBox<>(namaBulan);
        
        // Set bulan saat ini sebagai default
        Calendar cal = Calendar.getInstance();
        comboBulan.setSelectedIndex(cal.get(Calendar.MONTH));
        
        // Komponen untuk memilih tahun (5 tahun ke belakang dan 5 tahun ke depan)
        String[] tahun = new String[11];
        int tahunSekarang = cal.get(Calendar.YEAR);
        int indexTahunSekarang = 5; // Posisi tahun sekarang di array
        
        for (int i = 0; i < tahun.length; i++) {
            tahun[i] = String.valueOf(tahunSekarang - indexTahunSekarang + i);
        }
        comboTahun = new JComboBox<>(tahun);
        comboTahun.setSelectedIndex(indexTahunSekarang); // Set tahun sekarang sebagai default
        
        btnExportCsv = new JButton("Export ke CSV");
        
        panelEkspor.add(new JLabel("Bulan:"));
        panelEkspor.add(comboBulan);
        panelEkspor.add(new JLabel("Tahun:"));
        panelEkspor.add(comboTahun);
        panelEkspor.add(btnExportCsv);
        
        // Tambahkan panel ekspor ke panel utama
        // Modifikasi struktur panel untuk menambahkan panel ekspor
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(btnLihatRiwayat, BorderLayout.NORTH);
        southPanel.add(panelEkspor, BorderLayout.SOUTH);
        
        panelTabel.add(southPanel, BorderLayout.SOUTH);
    }

    // Methods penghubung tombol
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

    public String getInputPencarianSiswa() {
        return searchField.getText().trim();
    }

    public String getInputPencarianRiwayat() {
        return searchRiwayatField.getText().trim();
    }

    // Optional: Aktifkan kembali tanggal dikembalikan saat diperlukan
    public void enableTanggalDikembalikan(boolean enable) {
        tanggalDikembalikan.setEnabled(enable);
        if (!enable) {
            // Set ulang ke tanggal hari ini agar valid
            tanggalDikembalikan.setValue(new java.util.Date());
        }
    }

    // Tambahkan metode setter untuk action listener tombol ekspor CSV
    public void setExportCsvAction(java.awt.event.ActionListener act) {
        btnExportCsv.addActionListener(act);
    }

    // Tambahkan metode getter untuk mendapatkan bulan dan tahun yang dipilih
    public int getSelectedBulan() {
        return comboBulan.getSelectedIndex() + 1; // +1 karena indeks dimulai dari 0
    }

    public int getSelectedTahun() {
        return Integer.parseInt(comboTahun.getSelectedItem().toString());
    }
}