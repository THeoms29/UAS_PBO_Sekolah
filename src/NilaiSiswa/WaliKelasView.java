package NilaiSiswa;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class WaliKelasView extends JFrame {
    private JComboBox<String> comboKelas;
    private JComboBox<String> comboSemester;
    private JButton btnExport;
    private JTable table;
    private DefaultTableModel tableModel;

    private WaliKelasController controller;

public WaliKelasView() {
    setTitle("Rekap Nilai & Absensi - Wali Kelas");
    setSize(900, 500);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    initComponents(); // init dulu semua komponen

    controller = new WaliKelasController(this); // baru buat controller setelah UI siap
    controller.loadKelas(); // load kelas dari model
}
    private void initComponents() {
        JPanel panelTop = new JPanel(new FlowLayout());

        comboKelas = new JComboBox<>();
        comboSemester = new JComboBox<>(new String[] { "Ganjil", "Genap" });

        btnExport = new JButton("Export PDF");

        panelTop.add(new JLabel("Kelas:"));
        panelTop.add(comboKelas);
        panelTop.add(new JLabel("Semester:"));
        panelTop.add(comboSemester);
        panelTop.add(btnExport);

        // Table setup
        String[] kolom = {
            "NIS", "Nama Siswa", "Nilai Akhir", "Hadir", "Izin", "Sakit", "Alfa", "Total"
        };
        tableModel = new DefaultTableModel(null, kolom);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panelTop, BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        // Event listeners
        comboKelas.addActionListener(e -> controller.tampilkanData());
        comboSemester.addActionListener(e -> controller.tampilkanData());
        btnExport.addActionListener(e -> controller.exportPDF());
    }

    // ========== Getter ==========
    public JComboBox<String> getComboKelas() {
        return comboKelas;
    }

    public JComboBox<String> getComboSemester() {
        return comboSemester;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    public JTable getTable() {
        return table;
    }
}
