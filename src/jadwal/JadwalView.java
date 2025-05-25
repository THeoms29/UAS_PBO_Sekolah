package jadwal;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;

public class JadwalView extends JFrame {

    public JComboBox<String> comboKelas, comboMapel, comboGuru, comboHari;
    public JTextField fieldJamKe;
    public JButton btnSimpan, btnExportPDF;
    public JTable tableJadwal;
    public DefaultTableModel modelTabel;

    public JadwalView() {
        setTitle("Jadwal Pelajaran");
        setSize(650, 500);
        setLayout(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        comboKelas = new JComboBox<>();
        comboMapel = new JComboBox<>();
        comboGuru = new JComboBox<>();
        comboHari = new JComboBox<>(new String[]{"Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu"});
        fieldJamKe = new JTextField();

        btnSimpan = new JButton("Simpan");
        btnExportPDF = new JButton("Ekspor PDF");

        JLabel l1 = new JLabel("Kelas"), l2 = new JLabel("Mapel"), l3 = new JLabel("Guru"),
                l4 = new JLabel("Hari"), l5 = new JLabel("Jam Ke");

        int y = 20;
        for (Component c : new Component[]{l1, comboKelas, l2, comboMapel, l3, comboGuru, l4, comboHari, l5, fieldJamKe, btnSimpan, btnExportPDF}) {
            c.setBounds(30, y, 250, 25);
            add(c);
            y += 30;
        }

        btnSimpan.setBounds(300, 230, 120, 25);
        btnExportPDF.setBounds(430, 230, 120, 25);

        modelTabel = new DefaultTableModel(new String[]{"Hari", "Jam Ke", "Mapel", "Guru"}, 0);
        tableJadwal = new JTable(modelTabel);
        JScrollPane scrollPane = new JScrollPane(tableJadwal);
        scrollPane.setBounds(30, 270, 580, 170);
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
}
