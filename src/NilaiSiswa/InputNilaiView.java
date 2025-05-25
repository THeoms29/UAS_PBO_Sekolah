package NilaiSiswa;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class InputNilaiView extends JFrame {
    private JLabel labelGuruMapel;
    private JComboBox<String> comboKelas;
    private JComboBox<Map<String, Object>> comboSiswa;
    private JTextField txtNilaiUH, txtNilaiUTS, txtNilaiUAS;
    private JButton btnTambahNilai, btnSimpan;
    private JTable tableNilai;
    private DefaultTableModel tableModel;

    private NilaiController controller;

    public InputNilaiView() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Input Nilai Siswa");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        labelGuruMapel = new JLabel("Guru dan Mata Pelajaran");
        labelGuruMapel.setFont(new Font("Arial", Font.BOLD, 16));
        labelGuruMapel.setAlignmentX(Component.LEFT_ALIGNMENT);

        comboKelas = new JComboBox<>();
        comboKelas.setAlignmentX(Component.LEFT_ALIGNMENT);
        comboKelas.addActionListener(e -> {
            if (comboKelas.getSelectedItem() != null) {
                try {
                    int kelasId = Integer.parseInt(comboKelas.getSelectedItem().toString());
                    controller.kelasDipilih(kelasId);
                } catch (NumberFormatException ex) {
                    // ignore
                }
            }
        });

        comboSiswa = new JComboBox<>();
        comboSiswa.setAlignmentX(Component.LEFT_ALIGNMENT);
        comboSiswa.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Map) {
                    Map<String, Object> siswa = (Map<String, Object>) value;
                    value = siswa.get("nama") + " - " + siswa.get("nis");
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });

        txtNilaiUH = new JTextField();
        txtNilaiUTS = new JTextField();
        txtNilaiUAS = new JTextField();

        Dimension dim = new Dimension(50, 25);
        txtNilaiUH.setPreferredSize(dim);
        txtNilaiUTS.setPreferredSize(dim);
        txtNilaiUAS.setPreferredSize(dim);

        btnTambahNilai = new JButton("Tambah/Update Nilai");
        btnSimpan = new JButton("Simpan Semua");

        JPanel panelForm = new JPanel();
        panelForm.setLayout(new BoxLayout(panelForm, BoxLayout.Y_AXIS));
        panelForm.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        panelForm.add(labelGuruMapel);
        panelForm.add(Box.createVerticalStrut(10));

        panelForm.add(new JLabel("Pilih Kelas:"));
        panelForm.add(comboKelas);
        panelForm.add(Box.createVerticalStrut(10));

        panelForm.add(new JLabel("Pilih Siswa:"));
        panelForm.add(comboSiswa);
        panelForm.add(Box.createVerticalStrut(10));

        JPanel panelNilai = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelNilai.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelNilai.add(new JLabel("Nilai UH:"));
        panelNilai.add(txtNilaiUH);
        panelNilai.add(new JLabel("Nilai UTS:"));
        panelNilai.add(txtNilaiUTS);
        panelNilai.add(new JLabel("Nilai UAS:"));
        panelNilai.add(txtNilaiUAS);
        panelForm.add(panelNilai);

        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelButtons.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelButtons.add(btnTambahNilai);
        panelButtons.add(btnSimpan);
        panelForm.add(panelButtons);

        tableModel = new DefaultTableModel(
                new String[]{"NIS", "Nama", "NIS", "Nilai UH", "Nilai UTS", "Nilai UAS", "Nilai Akhir"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 3 && column <= 5;
            }
        };
        tableNilai = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tableNilai);
        scrollPane.setPreferredSize(new Dimension(850, 300));

        setLayout(new BorderLayout(10, 10));
        add(panelForm, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        btnTambahNilai.addActionListener(e -> tambahAtauUpdateNilaiDiTabel());
        btnSimpan.addActionListener(e -> simpanSemuaNilai());
    }

    public void setController(NilaiController controller) {
        this.controller = controller;
    }

    public void setLabelGuruMapel(String text) {
        labelGuruMapel.setText(text);
    }

    public void setKelasList(List<String> kelasList) {
        comboKelas.removeAllItems();
        for (String kelas : kelasList) {
            comboKelas.addItem(kelas);
        }
    }

    public void setSiswaList(List<Map<String, Object>> siswaList) {
        comboSiswa.removeAllItems();
        for (Map<String, Object> siswa : siswaList) {
            comboSiswa.addItem(siswa);
        }
    }

    public void setTableData(List<Map<String, Object>> data) {
        tableModel.setRowCount(0);
        for (Map<String, Object> row : data) {
            Object nilaiUH = row.get("nilai_uh") == null ? "" : row.get("nilai_uh");
            Object nilaiUTS = row.get("nilai_uts") == null ? "" : row.get("nilai_uts");
            Object nilaiUAS = row.get("nilai_uas") == null ? "" : row.get("nilai_uas");

            double nilaiAkhir = 0;
            try {
                int uh = nilaiUH.toString().isEmpty() ? 0 : Integer.parseInt(nilaiUH.toString());
                int uts = nilaiUTS.toString().isEmpty() ? 0 : Integer.parseInt(nilaiUTS.toString());
                int uas = nilaiUAS.toString().isEmpty() ? 0 : Integer.parseInt(nilaiUAS.toString());
                nilaiAkhir = (uh + uts + uas) / 3.0;
            } catch (NumberFormatException ex) {
                nilaiAkhir = 0;
            }

            tableModel.addRow(new Object[]{
                    row.get("siswa_id"),
                    row.get("nama"),
                    row.get("nis"),
                    nilaiUH,
                    nilaiUTS,
                    nilaiUAS,
                    String.format("%.2f", nilaiAkhir)
            });
        }
    }

    private void tambahAtauUpdateNilaiDiTabel() {
        Map<String, Object> siswa = (Map<String, Object>) comboSiswa.getSelectedItem();
        if (siswa == null) {
            JOptionPane.showMessageDialog(this, "Pilih siswa terlebih dahulu.");
            return;
        }

        int nilaiUH = parseIntSafe(txtNilaiUH.getText());
        int nilaiUTS = parseIntSafe(txtNilaiUTS.getText());
        int nilaiUAS = parseIntSafe(txtNilaiUAS.getText());

        boolean ditemukan = false;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(siswa.get("siswa_id"))) {
                tableModel.setValueAt(nilaiUH, i, 3);
                tableModel.setValueAt(nilaiUTS, i, 4);
                tableModel.setValueAt(nilaiUAS, i, 5);
                double nilaiAkhir = (nilaiUH + nilaiUTS + nilaiUAS) / 3.0;
                tableModel.setValueAt(String.format("%.2f", nilaiAkhir), i, 6);
                ditemukan = true;
                break;
            }
        }

        if (!ditemukan) {
            double nilaiAkhir = (nilaiUH + nilaiUTS + nilaiUAS) / 3.0;
            tableModel.addRow(new Object[]{
                    siswa.get("siswa_id"),
                    siswa.get("nama"),
                    siswa.get("nis"),
                    nilaiUH,
                    nilaiUTS,
                    nilaiUAS,
                    String.format("%.2f", nilaiAkhir)
            });
        }

        txtNilaiUH.setText("");
        txtNilaiUTS.setText("");
        txtNilaiUAS.setText("");
    }

    private void simpanSemuaNilai() {
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("siswa_id", tableModel.getValueAt(i, 0));
            row.put("nilai_uh", parseIntSafe(tableModel.getValueAt(i, 3).toString()));
            row.put("nilai_uts", parseIntSafe(tableModel.getValueAt(i, 4).toString()));
            row.put("nilai_uas", parseIntSafe(tableModel.getValueAt(i, 5).toString()));
            data.add(row);
        }

        boolean berhasil = controller.simpanNilai(data);
        JOptionPane.showMessageDialog(this, berhasil ? "Nilai berhasil disimpan!" : "Gagal menyimpan nilai!");
    }

    private int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
