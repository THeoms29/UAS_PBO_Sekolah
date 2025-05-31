package jadwal;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionListener;

public class JadwalView extends JFrame {

    public JComboBox<String> comboKelas, comboMapel, comboGuru, comboHari;
    public JComboBox<String> cbFilterKelas, cbFilterGuru;
    public JTextField fieldJamKe;
    public JButton btnSimpan, btnExportPDF, btnImporCSV, btnTambahMapel;
    public JButton btnExportCSV, btnDownloadTemplate; // ✅ Tambahan tombol baru
    public JTable tableJadwal;
    public DefaultTableModel modelTabel;

    public JadwalView() {
        setTitle("Jadwal Pelajaran");
        setSize(700, 600);
        setLayout(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Inisialisasi komponen
        comboKelas = new JComboBox<>();
        comboMapel = new JComboBox<>();
        comboGuru = new JComboBox<>();
        comboHari = new JComboBox<>(new String[]{"Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu"});
        fieldJamKe = new JTextField();
        cbFilterKelas = new JComboBox<>();
        cbFilterGuru = new JComboBox<>();

        btnSimpan = new JButton("Simpan");
        btnExportPDF = new JButton("Ekspor PDF");
        btnImporCSV = new JButton("Impor dari CSV");
        btnTambahMapel = new JButton("Tambah Mapel");

        btnExportCSV = new JButton("Ekspor CSV"); // ✅ Tambahan inisialisasi
        btnDownloadTemplate = new JButton("Download Template"); // ✅ Tambahan inisialisasi

        // Label
        JLabel l1 = new JLabel("Kelas");
        JLabel l2 = new JLabel("Mapel");
        JLabel l3 = new JLabel("Guru");
        JLabel l4 = new JLabel("Hari");
        JLabel l5 = new JLabel("Jam Ke");
        JLabel lblFilterKelas = new JLabel("Filter Kelas");
        JLabel lblFilterGuru = new JLabel("Filter Guru");

        // Posisi Label dan Input
        l1.setBounds(30, 20, 100, 25);
        comboKelas.setBounds(150, 20, 200, 25);

        l2.setBounds(30, 50, 100, 25);
        comboMapel.setBounds(150, 50, 200, 25);

        l3.setBounds(30, 80, 100, 25);
        comboGuru.setBounds(150, 80, 200, 25);

        l4.setBounds(30, 110, 100, 25);
        comboHari.setBounds(150, 110, 200, 25);

        l5.setBounds(30, 140, 100, 25);
        fieldJamKe.setBounds(150, 140, 200, 25);

        lblFilterKelas.setBounds(30, 170, 100, 25);
        cbFilterKelas.setBounds(150, 170, 200, 25);

        lblFilterGuru.setBounds(30, 200, 100, 25);
        cbFilterGuru.setBounds(150, 200, 200, 25);

        btnSimpan.setBounds(400, 20, 150, 30);
        btnExportPDF.setBounds(400, 60, 150, 30);
        btnImporCSV.setBounds(400, 100, 150, 30);
        btnTambahMapel.setBounds(400, 140, 150, 30);
        btnExportCSV.setBounds(400, 180, 150, 30); // ✅ Tambahan posisi
        btnDownloadTemplate.setBounds(400, 220, 150, 30); // ✅ Tambahan posisi

        // Tambahkan semua komponen
        add(l1); add(comboKelas);
        add(l2); add(comboMapel);
        add(l3); add(comboGuru);
        add(l4); add(comboHari);
        add(l5); add(fieldJamKe);
        add(lblFilterKelas); add(cbFilterKelas);
        add(lblFilterGuru); add(cbFilterGuru);
        add(btnSimpan);
        add(btnExportPDF);
        add(btnImporCSV);
        add(btnTambahMapel);
        add(btnExportCSV); // ✅ Tambahkan ke frame
        add(btnDownloadTemplate); // ✅ Tambahkan ke frame

        // Tabel
        modelTabel = new DefaultTableModel(new String[]{"Hari", "Jam Ke", "Kelas", "Mapel", "Guru"}, 0);
        tableJadwal = new JTable(modelTabel);
        JScrollPane scrollPane = new JScrollPane(tableJadwal);
        scrollPane.setBounds(30, 250, 620, 280);
        add(scrollPane);
    }

    // Aksi
    public void setSimpanAction(ActionListener action) {
        btnSimpan.addActionListener(action);
    }

    public void setExportPDFAction(ActionListener action) {
        btnExportPDF.addActionListener(action);
    }

    public void setComboKelasAction(ActionListener action) {
        comboKelas.addActionListener(action);
    }

    public void setImporCSVAction(ActionListener action) {
        btnImporCSV.addActionListener(action);
    }

    public void setTambahMapelAction(ActionListener al) {
        btnTambahMapel.addActionListener(al);
    }

    // ✅ Tambahan method setter untuk tombol baru
    public void setExportCSVAction(ActionListener al) {
        btnExportCSV.addActionListener(al);
    }

    public void setDownloadTemplateAction(ActionListener al) {
        btnDownloadTemplate.addActionListener(al);
    }

    // Getter
    public JComboBox<String> getCbKelas() {
        return comboKelas;
    }

    public JComboBox<String> getCbMapel() {
        return comboMapel;
    }

    public JComboBox<String> getCbGuru() {
        return comboGuru;
    }

    public JComboBox<String> getCbHari() {
        return comboHari;
    }

    public JComboBox<String> getCbFilterKelas() {
        return cbFilterKelas;
    }

    public JComboBox<String> getCbFilterGuru() {
        return cbFilterGuru;
    }

    public JTextField getFieldJamKe() {
        return fieldJamKe;
    }

    public JTable getTableJadwal() {
        return tableJadwal;
    }
}
