package absensi;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class AbsensiView extends JFrame {
    public JComboBox<String> comboKelas;
    public JTable tableSiswa;
    public JButton btnSimpan;
    public JButton btnMuat;
    public JLabel statusLabel;
    public JComboBox<String> comboBulan;
    public JComboBox<String> comboTahun;
    public JTextField fieldNamaSiswa;
    public JTextField fieldNis;
    public JButton btnTambahSiswa;
    public JButton btnLihatRekap; 
    public JTable tableRekap;
    public DefaultTableModel modelRekap;
    public JButton btnKeWaliKelas;

    public DefaultTableModel tableModel;

    public AbsensiView() {
        setTitle("Modul Absensi Siswa");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 500);
        setLocationRelativeTo(null);
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
                    // Gambar background yang menyesuaikan ukuran panel
                    g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        panelBackground.setLayout(new BorderLayout());
        setContentPane(panelBackground);

        comboKelas = new JComboBox<>();
        btnMuat = new JButton("Muat Siswa");
        btnSimpan = new JButton("Simpan Absensi");
        statusLabel = new JLabel("");

        btnMuat.setBackground(new Color(70, 130, 180));
        btnMuat.setForeground(Color.WHITE);
        btnMuat.setOpaque(true);
        btnMuat.setBorderPainted(false);

        btnSimpan.setBackground(new Color(70, 130, 180));
        btnSimpan.setForeground(Color.WHITE);
        btnSimpan.setOpaque(true);
        btnSimpan.setBorderPainted(false);

        fieldNamaSiswa = new JTextField(10);
        fieldNis = new JTextField(10);
        btnTambahSiswa = new JButton("Tambah Siswa");

        btnTambahSiswa.setBackground(new Color(70, 130, 180));
        btnTambahSiswa.setForeground(Color.WHITE);
        btnTambahSiswa.setOpaque(true);
        btnTambahSiswa.setBorderPainted(false);

        String[] kolom = {"ID", "Nama", "NIS", "Status"};
        tableModel = new DefaultTableModel(null, kolom) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };

        tableSiswa = new JTable(tableModel);
        tableSiswa.setOpaque(false);
        ((DefaultTableModel) tableSiswa.getModel()).setRowCount(0);

        JScrollPane scrollPane = new JScrollPane(tableSiswa);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        String[] statusOptions = {"Hadir", "Izin", "Sakit", "Alpha"};
        JComboBox<String> comboBoxStatus = new JComboBox<>(statusOptions);
        TableColumn statusColumn = tableSiswa.getColumnModel().getColumn(3);
        statusColumn.setCellEditor(new DefaultCellEditor(comboBoxStatus));

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Pilih Kelas:"));
        topPanel.add(comboKelas);
        topPanel.add(btnMuat);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(btnSimpan, BorderLayout.WEST);
        bottomPanel.add(statusLabel, BorderLayout.CENTER);

        comboBulan = new JComboBox<>(new String[]{
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"
        });

        comboTahun = new JComboBox<>();
        int currentYear = java.time.Year.now().getValue();
        for (int i = currentYear - 5; i <= currentYear + 1; i++) {
            comboTahun.addItem(String.valueOf(i));
        }

        btnLihatRekap = new JButton("Lihat Rekap");
        btnLihatRekap.setBackground(new Color(70, 130, 180));
        btnLihatRekap.setForeground(Color.WHITE);
        btnLihatRekap.setOpaque(true);
        btnLihatRekap.setBorderPainted(false);

        btnKeWaliKelas = new JButton("Ke Wali Kelas");
        btnKeWaliKelas.setBackground(new Color(70, 130, 180));
        btnKeWaliKelas.setForeground(Color.WHITE);
        btnKeWaliKelas.setOpaque(true);
        btnKeWaliKelas.setBorderPainted(false);
        

        JPanel panelRekapTop = new JPanel();
        panelRekapTop.add(new JLabel("Bulan:"));
        panelRekapTop.add(comboBulan);
        panelRekapTop.add(new JLabel("Tahun:"));
        topPanel.add(new JLabel("Nama:"));
        topPanel.add(fieldNamaSiswa);
        topPanel.add(new JLabel("NIS:"));
        topPanel.add(fieldNis);
        topPanel.add(btnTambahSiswa);
        panelRekapTop.add(comboTahun);
        panelRekapTop.add(btnLihatRekap);
        panelRekapTop.add(btnKeWaliKelas);

        String[] kolomRekap = {"Nama", "NIS", "Hadir", "Izin", "Sakit", "Alpha"};
        modelRekap = new DefaultTableModel(kolomRekap, 0);
        tableRekap = new JTable(modelRekap);
        tableRekap.setOpaque(false);
        
        JScrollPane scrollRekap = new JScrollPane(tableRekap);
        scrollRekap.setOpaque(false);
        scrollRekap.getViewport().setOpaque(false);

        JPanel panelRekap = new JPanel(new BorderLayout());
        panelRekap.setOpaque(false);
        panelRekap.add(panelRekapTop, BorderLayout.NORTH);
        panelRekap.add(scrollRekap, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                scrollPane, panelRekap);
        splitPane.setDividerLocation(450);
        splitPane.setOpaque(false);

        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER); // <--- Perbaikan di sini
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void setSimpanAction(ActionListener al) {
        btnSimpan.addActionListener(al);
    }

    public void setTambahSiswaAction(ActionListener al) {
    btnTambahSiswa.addActionListener(al);
    }

    public void setMuatAction(ActionListener al) {
        btnMuat.addActionListener(al);
    }

    public void setLihatRekapAction(ActionListener al) {
        btnLihatRekap.addActionListener(al);
    }

    public void setKeWaliKelasAction(ActionListener al) {
        btnKeWaliKelas.addActionListener(al);
    }

    public void setStatusColumnAsDropdown() {
        String[] statusOptions = {"Hadir", "Izin", "Sakit", "Alpha"};
        JComboBox<String> comboBoxStatus = new JComboBox<>(statusOptions);
        TableColumn statusColumn = tableSiswa.getColumnModel().getColumn(3);
        statusColumn.setCellEditor(new DefaultCellEditor(comboBoxStatus));
    }
}
