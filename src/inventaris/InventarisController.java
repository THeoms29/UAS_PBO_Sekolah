package inventaris;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.List;

public class InventarisController {
    private InventarisModel model;
    private InventarisView view;
    
    public InventarisController(InventarisModel model, InventarisView view){
        this.model = model;
        this.view = view;
        
        initializeEventHandlers();
        initializeYearComboBox(); // TAMBAHAN BARU
        refreshTable();
    }
    
    private void initializeEventHandlers() {
        // Simpan button handler
        view.btnSimpan.addActionListener(e -> {
            try {
                String nama = view.txtNama.getText().trim();
                String lokasi = view.txtLokasi.getText().trim();
                String jumlahText = view.txtJumlah.getText().trim();
                String kondisi = view.cbKondisi.getSelectedItem().toString();
                
                // Validasi input
                if (nama.isEmpty() || lokasi.isEmpty() || jumlahText.isEmpty()) {
                    JOptionPane.showMessageDialog(view, "Semua field harus diisi!");
                    return;
                }
                
                int jumlah = Integer.parseInt(jumlahText);
                if (jumlah < 0) {
                    JOptionPane.showMessageDialog(view, "Jumlah tidak boleh negatif!");
                    return;
                }
                
                model.tambahInventaris(nama, lokasi, jumlah, kondisi);
                refreshTable();
                clearForm();
                JOptionPane.showMessageDialog(view, "Data berhasil disimpan!");
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(view, "Jumlah harus berupa angka!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(view, "Error: " + ex.getMessage());
            }
        });
        
        // Edit button handler
        view.btnEdit.addActionListener(e -> {
            int row = view.table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(view, "Pilih data yang akan diedit!");
                return;
            }
            
            try {
                int id = (int) view.tableModel.getValueAt(row, 0);
                String nama = view.txtNama.getText().trim();
                String lokasi = view.txtLokasi.getText().trim();
                String jumlahText = view.txtJumlah.getText().trim();
                String kondisi = view.cbKondisi.getSelectedItem().toString();
                
                // Validasi input
                if (nama.isEmpty() || lokasi.isEmpty() || jumlahText.isEmpty()) {
                    JOptionPane.showMessageDialog(view, "Semua field harus diisi!");
                    return;
                }
                
                int jumlah = Integer.parseInt(jumlahText);
                if (jumlah < 0) {
                    JOptionPane.showMessageDialog(view, "Jumlah tidak boleh negatif!");
                    return;
                }
                
                model.updateInventaris(id, nama, lokasi, jumlah, kondisi);
                refreshTable();
                clearForm();
                JOptionPane.showMessageDialog(view, "Data berhasil diupdate!");
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(view, "Jumlah harus berupa angka!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(view, "Error: " + ex.getMessage());
            }
        });
        
        // Hapus button handler
        view.btnHapus.addActionListener(e -> {
            int row = view.table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(view, "Pilih data yang akan dihapus!");
                return;
            }
            
            int confirm = JOptionPane.showConfirmDialog(view, 
                "Apakah Anda yakin ingin menghapus data ini?", 
                "Konfirmasi Hapus", 
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    int id = (int) view.tableModel.getValueAt(row, 0);
                    model.hapusInventaris(id);
                    refreshTable();
                    clearForm();
                    JOptionPane.showMessageDialog(view, "Data berhasil dihapus!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(view, "Error: " + ex.getMessage());
                }
            }
        });
        
        // 8. UPDATE Table selection handler dalam initializeEventHandlers()
        // Ganti yang lama dengan ini:
        view.table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = view.table.getSelectedRow();
                if (row != -1) {
                    view.txtNama.setText(view.tableModel.getValueAt(row, 1).toString());
                    view.txtLokasi.setText(view.tableModel.getValueAt(row, 2).toString());
                    view.txtJumlah.setText(view.tableModel.getValueAt(row, 3).toString());
                    view.cbKondisi.setSelectedItem(view.tableModel.getValueAt(row, 4).toString());
                    // Kolom ke-5 (index 5) adalah tanggal, tidak perlu ditampilkan di form
                }
            }
        });
        
        // Export handlers
        view.btnExportCSV.addActionListener(e -> eksporKeCSV());
        view.btnExportPDF.addActionListener(e -> eksporKePDF());
        view.btnFilter.addActionListener(e -> applyFilter());
        view.btnResetFilter.addActionListener(e -> resetFilter());
    }

    // 2. TAMBAHAN method initializeYearComboBox()
    private void initializeYearComboBox() {
        List<Integer> years = model.getAvailableYears();
        view.populateYearComboBox(years);
    }
    
    private void refreshTable() {
        view.tableModel.setRowCount(0);
        for (Object[] row : model.getDataInventaris()) {
            view.tableModel.addRow(row);
        }
    }
    
    // 7. UPDATE method clearForm() - tambahkan clear selection table
    private void clearForm() {
        view.txtNama.setText("");
        view.txtLokasi.setText("");
        view.txtJumlah.setText("");
        view.cbKondisi.setSelectedIndex(0);
        view.table.clearSelection();
    }

    // 4. TAMBAHAN method applyFilter()
    private void applyFilter() {
        try {
            int selectedMonth = view.getSelectedMonth();
            int selectedYear = view.getSelectedYear();
            
            List<Object[]> filteredData;
            
            if (selectedMonth == 0 && selectedYear == 0) {
                // Semua bulan dan semua tahun
                filteredData = model.getDataInventaris();
            } else if (selectedMonth == 0 && selectedYear != 0) {
                // Semua bulan tapi tahun tertentu
                filteredData = model.getDataInventarisByYear(selectedYear);
            } else if (selectedMonth != 0 && selectedYear == 0) {
                // Bulan tertentu tapi semua tahun - gunakan tahun sekarang
                int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
                filteredData = model.getDataInventarisByMonth(selectedMonth, currentYear);
            } else {
                // Bulan dan tahun tertentu
                filteredData = model.getDataInventarisByMonth(selectedMonth, selectedYear);
            }
            
            // Update table dengan data yang sudah difilter
            view.tableModel.setRowCount(0);
            for (Object[] row : filteredData) {
                view.tableModel.addRow(row);
            }
            
            // Show info
            JOptionPane.showMessageDialog(view, 
                "Filter diterapkan. Ditemukan " + filteredData.size() + " data.",
                "Filter", 
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Error saat menerapkan filter: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // 5. TAMBAHAN method resetFilter()
    private void resetFilter() {
        view.cbBulan.setSelectedIndex(0); // Semua Bulan
        view.cbTahun.setSelectedIndex(0); // Semua Tahun
        refreshTable(); // Refresh dengan semua data
        JOptionPane.showMessageDialog(view, "Filter direset. Menampilkan semua data.");
    }
    
    private void eksporKeCSV() {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Simpan file CSV");
            
            // Set file filter untuk CSV
            FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files (*.csv)", "csv");
            chooser.setFileFilter(filter);
            chooser.setAcceptAllFileFilterUsed(false);
            
            // Set default filename
            chooser.setSelectedFile(new File("inventaris_data.csv"));
            
            if (chooser.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                String filePath = selectedFile.getAbsolutePath();
                
                // Pastikan file berekstensi .csv
                if (!filePath.toLowerCase().endsWith(".csv")) {
                    filePath += ".csv";
                    selectedFile = new File(filePath);
                }
                
                // Cek jika file sudah ada
                if (selectedFile.exists()) {
                    int option = JOptionPane.showConfirmDialog(view, 
                        "File sudah ada. Apakah ingin menimpa file tersebut?", 
                        "File Sudah Ada", 
                        JOptionPane.YES_NO_OPTION);
                    if (option != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                
                FileWriter fw = new FileWriter(selectedFile);
                
                // Write headers
                for (int i = 0; i < view.tableModel.getColumnCount(); i++) {
                    fw.write("\"" + view.tableModel.getColumnName(i) + "\"");
                    if (i < view.tableModel.getColumnCount() - 1) {
                        fw.write(",");
                    }
                }
                fw.write("\n");
                
                // Write data
                for (int i = 0; i < view.tableModel.getRowCount(); i++) {
                    for (int j = 0; j < view.tableModel.getColumnCount(); j++) {
                        Object value = view.tableModel.getValueAt(i, j);
                        fw.write("\"" + (value != null ? value.toString() : "") + "\"");
                        if (j < view.tableModel.getColumnCount() - 1) {
                            fw.write(",");
                        }
                    }
                    fw.write("\n");
                }
                
                fw.close();
                JOptionPane.showMessageDialog(view, 
                    "Data berhasil diekspor ke CSV!\nFile disimpan di: " + selectedFile.getAbsolutePath());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Gagal ekspor CSV: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    private void eksporKePDF() {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Simpan file PDF");
            
            // Set file filter untuk PDF
            FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF Files (*.pdf)", "pdf");
            chooser.setFileFilter(filter);
            chooser.setAcceptAllFileFilterUsed(false);
            
            // Set default filename
            chooser.setSelectedFile(new File("inventaris_laporan.pdf"));
            
            if (chooser.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                String filePath = selectedFile.getAbsolutePath();
                
                // Pastikan file berekstensi .pdf
                if (!filePath.toLowerCase().endsWith(".pdf")) {
                    filePath += ".pdf";
                    selectedFile = new File(filePath);
                }
                
                // Cek jika file sudah ada
                if (selectedFile.exists()) {
                    int option = JOptionPane.showConfirmDialog(view, 
                        "File sudah ada. Apakah ingin menimpa file tersebut?", 
                        "File Sudah Ada", 
                        JOptionPane.YES_NO_OPTION);
                    if (option != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                
                Document document = new Document(PageSize.A4);
                PdfWriter.getInstance(document, new FileOutputStream(selectedFile));
                document.open();
                
                // Add title
                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
                Paragraph title = new Paragraph("Laporan Inventaris Sekolah", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                document.add(title);
                
                // Add date
                Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
                Paragraph date = new Paragraph("Tanggal: " + new java.util.Date().toString(), dateFont);
                date.setAlignment(Element.ALIGN_RIGHT);
                date.setSpacingAfter(15);
                document.add(date);
                
                // Create table
                PdfPTable table = new PdfPTable(view.tableModel.getColumnCount());
                table.setWidthPercentage(100);
                
                // Set column widths
                float[] columnWidths = {1f, 3f, 2f, 1.5f, 1.5f, 2f}; // Tambah 1 kolom untuk tanggal
                table.setWidths(columnWidths);
                
                // Add headers
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
                for (int i = 0; i < view.tableModel.getColumnCount(); i++) {
                    PdfPCell cell = new PdfPCell(new Phrase(view.tableModel.getColumnName(i), headerFont));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    cell.setPadding(8);
                    table.addCell(cell);
                }
                
                // Add data
                Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
                for (int i = 0; i < view.tableModel.getRowCount(); i++) {
                    for (int j = 0; j < view.tableModel.getColumnCount(); j++) {
                        Object value = view.tableModel.getValueAt(i, j);
                        String cellValue = value != null ? value.toString() : "";
                        
                        PdfPCell cell = new PdfPCell(new Phrase(cellValue, dataFont));
                        cell.setPadding(6);
                        
                        // Center align for ID and Jumlah columns
                        if (j == 0 || j == 3) {
                            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        } else {
                            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        }
                        
                        table.addCell(cell);
                    }
                }
                
                document.add(table);
                
                // Add footer
                Paragraph footer = new Paragraph("\nTotal Item: " + view.tableModel.getRowCount(), dateFont);
                footer.setSpacingBefore(15);
                document.add(footer);
                
                document.close();
                JOptionPane.showMessageDialog(view, 
                    "PDF berhasil diekspor!\nFile disimpan di: " + selectedFile.getAbsolutePath());
                    
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Gagal ekspor PDF: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                InventarisModel model = new InventarisModel();
                InventarisView view = new InventarisView();
                new InventarisController(model, view);
                
                view.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error starting application: " + e.getMessage());
            }
        });
    }
}