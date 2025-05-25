package jadwal;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;

public class JadwalView extends JFrame {

    public JComboBox<String> comboKelas, comboMapel, comboGuru, comboHari;
    public JTextField fieldJamKe;
    public JButton btnSimpan, btnExportPDF, btnImporCSV;
    public JTable tableJadwal;
    public DefaultTableModel modelTabel;

    public JadwalView() {
        setTitle("Jadwal Pelajaran");
        setSize(650, 500);
        setLayout(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Inisialisasi komponen
        comboKelas = new JComboBox<>();
        comboMapel = new JComboBox<>();
        comboGuru = new JComboBox<>();
        comboHari = new JComboBox<>(new String[]{"Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu"});
        fieldJamKe = new JTextField();

        btnSimpan = new JButton("Simpan");
        btnExportPDF = new JButton("Ekspor PDF");
        btnImporCSV = new JButton("Impor dari CSV");

        // Label
        JLabel l1 = new JLabel("Kelas");
        JLabel l2 = new JLabel("Mapel");
        JLabel l3 = new JLabel("Guru");
        JLabel l4 = new JLabel("Hari");
        JLabel l5 = new JLabel("Jam Ke");

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

        btnSimpan.setBounds(380, 50, 120, 30);
        btnExportPDF.setBounds(380, 90, 120, 30);
        btnImporCSV.setBounds(380, 130, 150, 30);

        // Tambahkan semua komponen
        add(l1); add(comboKelas);
        add(l2); add(comboMapel);
        add(l3); add(comboGuru);
        add(l4); add(comboHari);
        add(l5); add(fieldJamKe);
        add(btnSimpan);
        add(btnExportPDF);
        add(btnImporCSV);

        // Tabel
        modelTabel = new DefaultTableModel(new String[]{"Hari", "Jam Ke", "Mapel", "Guru"}, 0);
        tableJadwal = new JTable(modelTabel);
        JScrollPane scrollPane = new JScrollPane(tableJadwal);
        scrollPane.setBounds(30, 200, 580, 230);
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
}
