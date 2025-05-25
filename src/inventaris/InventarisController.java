package inventaris;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.FileOutputStream;
import java.io.FileWriter;

public class InventarisController {
    private InventarisModel model;
    private InventarisView view;
    
    public InventarisController(InventarisModel model, InventarisView view){
        this.model = model;
        this.view = view;
        
        initializeEventHandlers();
        refreshTable();
    }
    
    private void initializeEventHandlers() {
        // Simpan button handler
        view.btnSimpan.addActionListener(e -> {
            try {
                String nama = view.txtNama.getText();
                String lokasi = view.txtLokasi.getText();
                String jumlahText = view.txtJumlah.getText();
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
                String nama = view.txtNama.getText();
                String lokasi = view.txtLokasi.getText();
                String jumlahText = view.txtJumlah.getText();
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
        
        // Table selection handler
        view.table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = view.table.getSelectedRow();
                if (row != -1) {
                    view.txtNama.setText(view.tableModel.getValueAt(row, 1).toString());
                    view.txtLokasi.setText(view.tableModel.getValueAt(row, 2).toString());
                    view.txtJumlah.setText(view.tableModel.getValueAt(row, 3).toString());
                    view.cbKondisi.setSelectedItem(view.tableModel.getValueAt(row, 4).toString());
                }
            }
        });
        
        // Export handlers
        view.btnExportCSV.addActionListener(e -> eksporKeCSV());
        view.btnExportPDF.addActionListener(e -> eksporKePDF());
    }
    
    private void refreshTable() {
        view.tableModel.setRowCount(0);
        for (Object[] row : model.getDataInventaris()) {
            view.tableModel.addRow(row);
        }
    }
    
    private void clearForm() {
        view.txtNama.setText("");
        view.txtLokasi.setText("");
        view.txtJumlah.setText("");
        view.cbKondisi.setSelectedIndex(0);
    }
    
    private void eksporKeCSV() {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Simpan file CSV");
            
            if (chooser.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
                String filePath = chooser.getSelectedFile().getAbsolutePath();
                if (!filePath.endsWith(".csv")) {
                    filePath += ".csv";
                }
                
                FileWriter fw = new FileWriter(filePath);
                
                // Write headers
                for (int i = 0; i < view.tableModel.getColumnCount(); i++) {
                    fw.write(view.tableModel.getColumnName(i));
                    if (i < view.tableModel.getColumnCount() - 1) {
                        fw.write(",");
                    }
                }
                fw.write("\n");
                
                // Write data
                for (int i = 0; i < view.tableModel.getRowCount(); i++) {
                    for (int j = 0; j < view.tableModel.getColumnCount(); j++) {
                        fw.write(view.tableModel.getValueAt(i, j).toString());
                        if (j < view.tableModel.getColumnCount() - 1) {
                            fw.write(",");
                        }
                    }
                    fw.write("\n");
                }
                
                fw.close();
                JOptionPane.showMessageDialog(view, "Data berhasil diekspor ke CSV!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Gagal ekspor CSV: " + ex.getMessage());
        }
    }
    
    private void eksporKePDF() {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Simpan file PDF");
            
            if (chooser.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
                String filePath = chooser.getSelectedFile().getAbsolutePath();
                if (!filePath.endsWith(".pdf")) {
                    filePath += ".pdf";
                }
                
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(filePath));
                document.open();
                
                // Add title
                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
                Paragraph title = new Paragraph("Laporan Inventaris Sekolah", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);
                document.add(new Paragraph(" "));
                
                // Create table
                PdfPTable table = new PdfPTable(view.tableModel.getColumnCount());
                table.setWidthPercentage(100);
                
                // Add headers
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
                for (int i = 0; i < view.tableModel.getColumnCount(); i++) {
                    PdfPCell cell = new PdfPCell(new Phrase(view.tableModel.getColumnName(i), headerFont));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell);
                }
                
                // Add data
                Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
                for (int i = 0; i < view.tableModel.getRowCount(); i++) {
                    for (int j = 0; j < view.tableModel.getColumnCount(); j++) {
                        PdfPCell cell = new PdfPCell(new Phrase(view.tableModel.getValueAt(i, j).toString(), dataFont));
                        if (j == 0 || j == 3) { // ID and Jumlah columns
                            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        }
                        table.addCell(cell);
                    }
                }
                
                document.add(table);
                document.close();
                JOptionPane.showMessageDialog(view, "PDF berhasil diekspor!");
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