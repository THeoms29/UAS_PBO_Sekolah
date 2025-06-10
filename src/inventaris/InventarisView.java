package inventaris;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class InventarisView extends JFrame {
    public JTextField txtNama = new JTextField();
    public JTextField txtLokasi = new JTextField();
    public JTextField txtJumlah = new JTextField();
    public JComboBox<String> cbKondisi = new JComboBox<>(new String[]{"Baik", "Rusak", "Hilang"});
    public JButton btnSimpan = new JButton("Simpan");
    public JButton btnEdit = new JButton("Edit");
    public JButton btnHapus = new JButton("Hapus");
    public JButton btnExportCSV = new JButton("Ekspor ke CSV");
    public JButton btnExportPDF = new JButton("Ekspor ke PDF");
    public JTable table = new JTable();
    public DefaultTableModel tableModel;
    public JComboBox<String> cbBulan = new JComboBox<>(new String[]{
        "Semua Bulan", "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    });
    public JComboBox<Integer> cbTahun = new JComboBox<>();
    public JButton btnFilter = new JButton("Filter");
    public JButton btnResetFilter = new JButton("Reset");
    
    public InventarisView() {
        setTitle("Modul Inventaris Sekolah");
        setSize(800, 600);
        setMinimumSize(new Dimension(600, 400));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
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

        btnSimpan.setBackground(new Color(70, 130, 180));
        btnSimpan.setForeground(Color.WHITE);
        btnSimpan.setOpaque(true);
        btnSimpan.setBorderPainted(false);

        btnEdit.setBackground(new Color(70, 130, 180));
        btnEdit.setForeground(Color.WHITE);
        btnEdit.setOpaque(true);
        btnEdit.setBorderPainted(false);

        btnHapus.setBackground(new Color(220, 20, 60));
        btnHapus.setForeground(Color.WHITE);
        btnHapus.setOpaque(true);
        btnHapus.setBorderPainted(false);

        btnExportCSV.setBackground(new Color(34, 139, 34));
        btnExportCSV.setForeground(Color.WHITE);
        btnExportCSV.setOpaque(true);
        btnExportCSV.setBorderPainted(false);

        btnExportPDF.setBackground(new Color(34, 139, 34));
        btnExportPDF.setForeground(Color.WHITE);
        btnExportPDF.setOpaque(true);
        btnExportPDF.setBorderPainted(false);

        btnFilter.setBackground(new Color(70, 130, 180));
        btnFilter.setForeground(Color.WHITE);
        btnFilter.setOpaque(true);
        btnFilter.setBorderPainted(false);

        btnResetFilter.setBackground(new Color(220, 20, 60));
        btnResetFilter.setForeground(Color.WHITE);
        btnResetFilter.setOpaque(true);
        btnResetFilter.setBorderPainted(false);

        initializeComponents();
        setupLayout();
    }
    
    private void initializeComponents() {
        // Set preferred sizes for components
        txtNama.setPreferredSize(new Dimension(200, 25));
        txtLokasi.setPreferredSize(new Dimension(200, 25));
        txtJumlah.setPreferredSize(new Dimension(200, 25));
        cbKondisi.setPreferredSize(new Dimension(200, 25));
        
        // Initialize table model - TAMBAH kolom Tanggal
        tableModel = new DefaultTableModel(new String[]{"ID", "Nama", "Lokasi", "Jumlah", "Kondisi", "Tanggal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        table.setModel(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(200); // Nama
        table.getColumnModel().getColumn(2).setPreferredWidth(150); // Lokasi
        table.getColumnModel().getColumn(3).setPreferredWidth(80);  // Jumlah
        table.getColumnModel().getColumn(4).setPreferredWidth(100); // Kondisi
        table.getColumnModel().getColumn(5).setPreferredWidth(100); // Tanggal
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Create main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(255, 255, 255, 200)); // Semi-transparent white background
        mainPanel.setOpaque(false); // Make main panel transparent
        
        // Create filter panel - TAMBAHAN BARU
        JPanel filterPanel = createFilterPanel();

        // Create top panel for form inputs
        JPanel topPanel = createFormPanel();
        
        // Create table panel
        JPanel tablePanel = createTablePanel();

        // Combine filter and form panels
        JPanel topCombined = new JPanel(new BorderLayout());
        topCombined.add(filterPanel, BorderLayout.NORTH);
        topCombined.add(topPanel, BorderLayout.CENTER);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Form Input"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Row 1: Nama Barang
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Nama Barang:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(txtNama, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(btnSimpan, gbc);
        
        // Row 2: Lokasi
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Lokasi:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(txtLokasi, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(btnEdit, gbc);
        
        // Row 3: Jumlah
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Jumlah:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(txtJumlah, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(btnHapus, gbc);
        
        // Row 4: Kondisi
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Kondisi:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(cbKondisi, gbc);
        
        // Export buttons panel
        JPanel exportPanel = new JPanel(new FlowLayout());
        exportPanel.add(btnExportCSV);
        exportPanel.add(btnExportPDF);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(exportPanel, gbc);
        
        return formPanel;
    }
    
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Data Inventaris"));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(0, 300));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }

    // 4. TAMBAHAN method createFilterPanel()
    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Data"));
        
        filterPanel.add(new JLabel("Bulan:"));
        filterPanel.add(cbBulan);
        
        filterPanel.add(Box.createHorizontalStrut(10));
        
        filterPanel.add(new JLabel("Tahun:"));
        filterPanel.add(cbTahun);
        
        filterPanel.add(Box.createHorizontalStrut(10));
        
        filterPanel.add(btnFilter);
        filterPanel.add(btnResetFilter);
        
        return filterPanel;
    }

    // 5. TAMBAHAN method untuk mengisi combo box tahun
    public void populateYearComboBox(List<Integer> years) {
        cbTahun.removeAllItems();
        cbTahun.addItem(0); // 0 represents "Semua Tahun"
        for (Integer year : years) {
            cbTahun.addItem(year);
        }
    }

    // 6. TAMBAHAN method untuk mendapatkan nilai filter
    public int getSelectedMonth() {
        return cbBulan.getSelectedIndex(); // 0 = Semua Bulan, 1-12 = Jan-Dec
    }
    public int getSelectedYear() {
        Integer selectedYear = (Integer) cbTahun.getSelectedItem();
        return selectedYear != null ? selectedYear : 0; // 0 = Semua Tahun
    }
}