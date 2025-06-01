package jadwal;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;

public class JadwalView extends JFrame {

    public JComboBox<String> comboKelas, comboMapel, comboGuru, comboHari;
    public JComboBox<String> cbFilterKelas, cbFilterGuru;
    public JTextField fieldJamKe;
    public JButton btnSimpan, btnExportPDF, btnImporCSV, btnTambahMapel;
    public JButton btnExportCSV, btnDownloadTemplate;
    public JTable tableJadwal;
    public DefaultTableModel modelTabel;

    public JadwalView() {
        setTitle("Jadwal Pelajaran");
        setSize(800, 700);
        setMinimumSize(new Dimension(600, 500));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        initializeComponents();
        setupLayout();
    }
    
    private void initializeComponents() {
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
        btnExportCSV = new JButton("Ekspor CSV");
        btnDownloadTemplate = new JButton("Download Template");

        // Set preferred sizes untuk konsistensi
        Dimension comboSize = new Dimension(200, 25);
        comboKelas.setPreferredSize(comboSize);
        comboMapel.setPreferredSize(comboSize);
        comboGuru.setPreferredSize(comboSize);
        comboHari.setPreferredSize(comboSize);
        cbFilterKelas.setPreferredSize(comboSize);
        cbFilterGuru.setPreferredSize(comboSize);
        fieldJamKe.setPreferredSize(comboSize);

        // Inisialisasi tabel
        modelTabel = new DefaultTableModel(new String[]{"Hari", "Jam Ke", "Kelas", "Mapel", "Guru"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        tableJadwal = new JTable(modelTabel);
        tableJadwal.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableJadwal.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        tableJadwal.getColumnModel().getColumn(0).setPreferredWidth(100); // Hari
        tableJadwal.getColumnModel().getColumn(1).setPreferredWidth(80);  // Jam Ke
        tableJadwal.getColumnModel().getColumn(2).setPreferredWidth(100); // Kelas
        tableJadwal.getColumnModel().getColumn(3).setPreferredWidth(150); // Mapel
        tableJadwal.getColumnModel().getColumn(4).setPreferredWidth(150); // Guru
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Create main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create filter panel
        JPanel filterPanel = createFilterPanel();
        
        // Create form panel
        JPanel formPanel = createFormPanel();
        
        // Create action buttons panel
        JPanel actionPanel = createActionPanel();
        
        // Create table panel
        JPanel tablePanel = createTablePanel();
        
        // Combine form and action panels
        JPanel formAndAction = new JPanel(new BorderLayout());
        formAndAction.add(formPanel, BorderLayout.CENTER);
        formAndAction.add(actionPanel, BorderLayout.EAST);
        
        // Combine filter with table panel
        JPanel tableWithFilter = new JPanel(new BorderLayout());
        tableWithFilter.add(filterPanel, BorderLayout.NORTH);
        tableWithFilter.add(tablePanel, BorderLayout.CENTER);
        
        mainPanel.add(formAndAction, BorderLayout.NORTH);
        mainPanel.add(tableWithFilter, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Data"));
        
        filterPanel.add(new JLabel("Filter Kelas:"));
        filterPanel.add(cbFilterKelas);
        
        filterPanel.add(Box.createHorizontalStrut(20));
        
        filterPanel.add(new JLabel("Filter Guru:"));
        filterPanel.add(cbFilterGuru);
        
        return filterPanel;
    }
    
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Form Input Jadwal"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Row 1: Kelas
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Kelas:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(comboKelas, gbc);
        
        // Row 2: Mapel
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Mapel:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(comboMapel, gbc);
        
        // Row 3: Guru
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Guru:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(comboGuru, gbc);
        
        // Row 4: Hari
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Hari:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(comboHari, gbc);
        
        // Row 5: Jam Ke
        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Jam Ke:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(fieldJamKe, gbc);
        
        return formPanel;
    }
    
    private JPanel createActionPanel() {
        JPanel actionPanel = new JPanel(new GridBagLayout());
        actionPanel.setBorder(BorderFactory.createTitledBorder("Aksi"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Set preferred size untuk tombol agar konsisten
        Dimension buttonSize = new Dimension(150, 30);
        btnSimpan.setPreferredSize(buttonSize);
        btnExportPDF.setPreferredSize(buttonSize);
        btnImporCSV.setPreferredSize(buttonSize);
        btnTambahMapel.setPreferredSize(buttonSize);
        btnExportCSV.setPreferredSize(buttonSize);
        btnDownloadTemplate.setPreferredSize(buttonSize);
        
        // Add buttons vertically
        gbc.gridx = 0; gbc.gridy = 0;
        actionPanel.add(btnSimpan, gbc);
        
        gbc.gridy = 1;
        actionPanel.add(btnExportPDF, gbc);
        
        gbc.gridy = 2;
        actionPanel.add(btnImporCSV, gbc);
        
        gbc.gridy = 3;
        actionPanel.add(btnTambahMapel, gbc);
        
        gbc.gridy = 4;
        actionPanel.add(btnExportCSV, gbc);
        
        gbc.gridy = 5;
        actionPanel.add(btnDownloadTemplate, gbc);
        
        return actionPanel;
    }
    
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Data Jadwal Pelajaran"));
        
        JScrollPane scrollPane = new JScrollPane(tableJadwal);
        scrollPane.setPreferredSize(new Dimension(0, 300));
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }

    // Aksi - Method setter untuk action listeners
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

    public void setExportCSVAction(ActionListener al) {
        btnExportCSV.addActionListener(al);
    }

    public void setDownloadTemplateAction(ActionListener al) {
        btnDownloadTemplate.addActionListener(al);
    }

    // Getter methods
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