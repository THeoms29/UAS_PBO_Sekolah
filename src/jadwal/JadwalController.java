package jadwal;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;

public class JadwalController {
    private JadwalView view;
    private JadwalModel model;

    public JadwalController(JadwalView view, JadwalModel model) {
        this.view = view;
        this.model = model;

        initComboBox();
        addListeners();
    }

  private void initComboBox() {
    try {
        view.getCbKelas().removeAllItems();
        view.cbFilterKelas.removeAllItems();
        List<String[]> kelasList = model.getAllKelas();
        for (String[] k : kelasList) {
            view.getCbKelas().addItem(k[0] + ". " + k[1]);
            view.cbFilterKelas.addItem(k[0] + ". " + k[1]);
        }

        view.getCbMapel().removeAllItems();
        List<String[]> mapelList = model.getAllMapel();
        for (String[] m : mapelList) {
            view.getCbMapel().addItem(m[0] + ". " + m[1]);
        }

        view.getCbGuru().removeAllItems();
        view.cbFilterGuru.removeAllItems();
        List<String[]> guruList = model.getAllGuru();
        for (String[] g : guruList) {
            view.getCbGuru().addItem(g[0] + ". " + g[1]);
            view.cbFilterGuru.addItem(g[0] + ". " + g[1]);
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(view, "Gagal memuat data: " + e.getMessage());
    }
}


    private void addListeners() {
        view.btnSimpan.addActionListener(e -> simpanJadwal());

        view.cbFilterKelas.addActionListener(e -> {
            if (view.cbFilterKelas.getSelectedItem() != null)
                tampilkanJadwal("kelas");
        });

        view.cbFilterGuru.addActionListener(e -> {
            if (view.cbFilterGuru.getSelectedItem() != null)
                tampilkanJadwal("guru");
        });

        view.btnExportCSV.addActionListener(e -> eksporCSV());
        view.btnExportPDF.addActionListener(e -> eksporPDF());
        view.btnImporCSV.addActionListener(e -> imporCSV());
        view.btnDownloadTemplate.addActionListener(e -> downloadTemplate());
    }

    private void simpanJadwal() {
        try {
            String hari = (String) view.getCbHari().getSelectedItem();
            int jam = Integer.parseInt( view.getFieldJamKe().getText());
            int kelasId = Integer.parseInt(view.getCbKelas().getSelectedItem().toString().split("\\. ")[0]);
            int mapelId = Integer.parseInt(view.getCbMapel().getSelectedItem().toString().split("\\. ")[0]);
            int guruId = Integer.parseInt(view.getCbGuru().getSelectedItem().toString().split("\\. ")[0]);

            if (model.isBentrok(hari, jam, kelasId, guruId)) {
                JOptionPane.showMessageDialog(view, "Jadwal bentrok dengan jadwal lain!");
                return;
            }

            model.insertJadwal(hari, jam, kelasId, mapelId, guruId);
            JOptionPane.showMessageDialog(view, "Jadwal berhasil disimpan.");
            tampilkanJadwal("kelas");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Gagal simpan: " + ex.getMessage());
        }
    }

    private void tampilkanJadwal(String mode) {
        try {
            DefaultTableModel tbl = (DefaultTableModel) view.tableJadwal.getModel();
            tbl.setRowCount(0);
            List<String[]> data;

            if (mode.equals("kelas")) {
                if (view.cbFilterKelas.getSelectedItem() == null) return;
                int id = Integer.parseInt(view.cbFilterKelas.getSelectedItem().toString().split("\\. ")[0]);
                data = model.getJadwalByKelas(id);
                for (String[] row : data) {
                    tbl.addRow(new Object[]{row[0], row[1], view.cbFilterKelas.getSelectedItem().toString().split("\\. ", 2)[1], row[2], row[3]});
                }
            } else {
                if (view.cbFilterGuru.getSelectedItem() == null) return;
                int id = Integer.parseInt(view.cbFilterGuru.getSelectedItem().toString().split("\\. ")[0]);
                data = model.getJadwalByGuru(id);
                for (String[] row : data) {
                    tbl.addRow(new Object[]{row[0], row[1], row[2], row[3], view.cbFilterGuru.getSelectedItem().toString().split("\\. ", 2)[1]});
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Gagal menampilkan jadwal: " + e.getMessage());
        }
    }

    private void eksporCSV() {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Simpan sebagai CSV");
            chooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
            if (chooser.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
                String path = chooser.getSelectedFile().getAbsolutePath();
                if (!path.toLowerCase().endsWith(".csv")) {
                    path += ".csv";
                }
                FileWriter writer = new FileWriter(path);
                DefaultTableModel model = (DefaultTableModel) view.tableJadwal.getModel();
                for (int i = 0; i < model.getColumnCount(); i++) {
                    writer.write(model.getColumnName(i) + (i < model.getColumnCount() - 1 ? "," : "\n"));
                }
                for (int i = 0; i < model.getRowCount(); i++) {
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        writer.write(model.getValueAt(i, j).toString() + (j < model.getColumnCount() - 1 ? "," : "\n"));
                    }
                }
                writer.close();
                JOptionPane.showMessageDialog(view, "Berhasil ekspor ke CSV.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Gagal ekspor: " + ex.getMessage());
        }
    }
//------------------------------------------
    private void eksporPDF() {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Simpan sebagai PDF");
            chooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));
            if (chooser.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
                String path = chooser.getSelectedFile().getAbsolutePath();
                if (!path.toLowerCase().endsWith(".pdf")) {
                    path += ".pdf";
                }
                
                Document doc = new Document(PageSize.A4);
                PdfWriter.getInstance(doc, new java.io.FileOutputStream(path));
                doc.open();

                // === HEADER SECTION ===
                // Logo/Institution name (you can add logo here if needed)
                Paragraph institution = new Paragraph("SMPN ADILUWIH 1", 
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK));
                institution.setAlignment(Element.ALIGN_CENTER);
                doc.add(institution);
                
                Paragraph address = new Paragraph("Jl. Pendidikan No. 123, Surabaya, Jawa Timur", 
                    FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY));
                address.setAlignment(Element.ALIGN_CENTER);
                doc.add(address);
                
                // Separator line
                Paragraph separator = new Paragraph();
                separator.add(new Chunk(new LineSeparator(1f, 100f, BaseColor.BLACK, Element.ALIGN_CENTER, -2)));
                doc.add(separator);
                doc.add(new Paragraph(" ")); // Space
                
                // === TITLE SECTION ===
                Paragraph title = new Paragraph("JADWAL PELAJARAN", 
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK));
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(10);
                doc.add(title);
                
                // Determine what type of schedule this is
                String scheduleType = "";
                String scheduleFor = "";
                if (view.cbFilterKelas.getSelectedItem() != null && 
                    !view.cbFilterKelas.getSelectedItem().toString().isEmpty()) {
                    scheduleType = "KELAS";
                    scheduleFor = view.cbFilterKelas.getSelectedItem().toString().split("\\. ", 2)[1];
                } else if (view.cbFilterGuru.getSelectedItem() != null && 
                        !view.cbFilterGuru.getSelectedItem().toString().isEmpty()) {
                    scheduleType = "GURU";
                    scheduleFor = view.cbFilterGuru.getSelectedItem().toString().split("\\. ", 2)[1];
                }
                
                if (!scheduleType.isEmpty()) {
                    Paragraph subtitle = new Paragraph(scheduleType + ": " + scheduleFor, 
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK));
                    subtitle.setAlignment(Element.ALIGN_CENTER);
                    subtitle.setSpacingAfter(15);
                    doc.add(subtitle);
                }
                
                // === TABLE SECTION ===
                if (view.tableJadwal.getRowCount() > 0) {
                    PdfPTable table = new PdfPTable(view.tableJadwal.getColumnCount());
                    table.setWidthPercentage(100);
                    table.setSpacingBefore(10);
                    
                    // Set column widths based on content
                    float[] columnWidths = {1.5f, 1f, 2f, 2.5f, 2.5f}; // Adjust based on your columns
                    table.setWidths(columnWidths);
                    
                    // Header row styling
                    Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
                    for (int i = 0; i < view.tableJadwal.getColumnCount(); i++) {
                        PdfPCell headerCell = new PdfPCell(new Phrase(view.tableJadwal.getColumnName(i), headerFont));
                        headerCell.setBackgroundColor(new BaseColor(52, 58, 64)); // Dark gray
                        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        headerCell.setPadding(8);
                        table.addCell(headerCell);
                    }
                    
                    // Data rows styling
                    Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
                    Font dataBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.BLACK);
                    
                    for (int i = 0; i < view.tableJadwal.getRowCount(); i++) {
                        for (int j = 0; j < view.tableJadwal.getColumnCount(); j++) {
                            Object val = view.tableJadwal.getValueAt(i, j);
                            String cellValue = val != null ? val.toString() : "";
                            
                            // Use bold font for first two columns (Hari and Jam)
                            Font cellFont = (j <= 1) ? dataBoldFont : dataFont;
                            PdfPCell dataCell = new PdfPCell(new Phrase(cellValue, cellFont));
                            
                            // Alternate row colors
                            if (i % 2 == 0) {
                                dataCell.setBackgroundColor(new BaseColor(248, 249, 250)); // Light gray
                            } else {
                                dataCell.setBackgroundColor(BaseColor.WHITE);
                            }
                            
                            dataCell.setHorizontalAlignment(j <= 1 ? Element.ALIGN_CENTER : Element.ALIGN_LEFT);
                            dataCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                            dataCell.setPadding(6);
                            dataCell.setBorder(Rectangle.BOX);
                            dataCell.setBorderColor(new BaseColor(206, 212, 218));
                            
                            table.addCell(dataCell);
                        }
                    }
                    
                    doc.add(table);
                } else {
                    Paragraph noData = new Paragraph("Tidak ada data jadwal untuk ditampilkan.", 
                        FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 11, BaseColor.GRAY));
                    noData.setAlignment(Element.ALIGN_CENTER);
                    noData.setSpacingBefore(20);
                    doc.add(noData);
                }
                
                // === FOOTER SECTION ===
                doc.add(new Paragraph(" ")); // Space
                doc.add(new Paragraph(" ")); // Space
                
                // Generation info
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMMM yyyy 'pukul' HH:mm");
                java.util.Locale localeID = new java.util.Locale("id", "ID");
                sdf = new java.text.SimpleDateFormat("dd MMMM yyyy 'pukul' HH:mm", localeID);
                
                Paragraph generatedInfo = new Paragraph("Dokumen ini dibuat secara otomatis pada " + sdf.format(new java.util.Date()), 
                    FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY));
                generatedInfo.setAlignment(Element.ALIGN_CENTER);
                doc.add(generatedInfo);
                
                // Add signature area for formal documents
                doc.add(new Paragraph(" ")); // Space
                doc.add(new Paragraph(" ")); // Space
                
                // Create a table for signature
                PdfPTable signatureTable = new PdfPTable(2);
                signatureTable.setWidthPercentage(100);
                signatureTable.setWidths(new float[]{1f, 1f});
                
                // Left cell - empty for now
                PdfPCell leftCell = new PdfPCell(new Phrase(""));
                leftCell.setBorder(Rectangle.NO_BORDER);
                leftCell.setMinimumHeight(60);
                signatureTable.addCell(leftCell);
                
                // Right cell - signature area
                PdfPCell rightCell = new PdfPCell();
                rightCell.setBorder(Rectangle.NO_BORDER);
                rightCell.setMinimumHeight(60);
                
                Paragraph signatureLocation = new Paragraph("Surabaya, " + 
                    new java.text.SimpleDateFormat("dd MMMM yyyy", localeID).format(new java.util.Date()),
                    FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK));
                signatureLocation.setAlignment(Element.ALIGN_CENTER);
                
                Paragraph signatureTitle = new Paragraph("Kepala Sekolah",
                    FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK));
                signatureTitle.setAlignment(Element.ALIGN_CENTER);
                signatureTitle.setSpacingBefore(40);
                
                Paragraph signatureName = new Paragraph("_______________________",
                    FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK));
                signatureName.setAlignment(Element.ALIGN_CENTER);
                signatureName.setSpacingBefore(5);
                
                rightCell.addElement(signatureLocation);
                rightCell.addElement(signatureTitle);
                rightCell.addElement(signatureName);
                
                signatureTable.addCell(rightCell);
                doc.add(signatureTable);

                doc.close();
                JOptionPane.showMessageDialog(view, "PDF berhasil diekspor dengan format formal!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Gagal ekspor PDF: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void imporCSV() {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Pilih File CSV untuk Impor");
            chooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
            
            if (chooser.showOpenDialog(view) == JFileChooser.APPROVE_OPTION) {
                String path = chooser.getSelectedFile().getAbsolutePath();
                BufferedReader reader = new BufferedReader(new FileReader(path));
                
                String headerLine = reader.readLine(); // Baca header
                if (headerLine == null) {
                    reader.close();
                    JOptionPane.showMessageDialog(view, "File CSV kosong!");
                    return;
                }
                
                // Deteksi delimiter berdasarkan header
                String delimiter = ","; // default
                if (headerLine.contains(";") && headerLine.split(";").length >= 5) {
                    delimiter = ";";
                } else if (headerLine.contains(",") && headerLine.split(",").length >= 5) {
                    delimiter = ",";
                } else {
                    reader.close();
                    JOptionPane.showMessageDialog(view, "Format header CSV tidak valid!\nHeader harus berisi: Hari,Jam Ke,Kelas,Mapel,Guru");
                    return;
                }
                
                // Verifikasi header
                String[] headers = headerLine.split(delimiter);
                if (headers.length < 5) {
                    reader.close();
                    JOptionPane.showMessageDialog(view, "Header CSV tidak lengkap!\nDibutuhkan minimal 5 kolom: Hari, Jam Ke, Kelas, Mapel, Guru");
                    return;
                }
                
                int berhasil = 0;
                int gagal = 0;
                List<String> errorMessages = new ArrayList<>();
                String line;
                int lineNumber = 1; // Mulai dari baris kedua (setelah header)
                
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    try {
                        // Skip baris kosong
                        if (line.trim().isEmpty()) {
                            continue;
                        }
                        
                        String[] data = line.split(delimiter);
                        if (data.length >= 5) {
                            String hari = data[0].trim();
                            String jamStr = data[1].trim();
                            String namaKelas = data[2].trim();
                            String namaMapel = data[3].trim();
                            String namaGuru = data[4].trim();
                            
                            // Validasi hari
                            String[] validDays = {"Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu"};
                            boolean validDay = false;
                            for (String day : validDays) {
                                if (day.equalsIgnoreCase(hari)) {
                                    hari = day; // Normalisasi kapitalisasi
                                    validDay = true;
                                    break;
                                }
                            }
                            if (!validDay) {
                                errorMessages.add("Baris " + lineNumber + ": Hari '" + hari + "' tidak valid");
                                gagal++;
                                continue;
                            }
                            
                            // Validasi jam
                            int jamKe;
                            try {
                                jamKe = Integer.parseInt(jamStr);
                                if (jamKe < 1 || jamKe > 10) {
                                    errorMessages.add("Baris " + lineNumber + ": Jam ke '" + jamStr + "' harus antara 1-10");
                                    gagal++;
                                    continue;
                                }
                            } catch (NumberFormatException e) {
                                errorMessages.add("Baris " + lineNumber + ": Jam ke '" + jamStr + "' bukan angka valid");
                                gagal++;
                                continue;
                            }
                            
                            // Cari ID berdasarkan nama
                            int kelasId = model.getKelasIdByName(namaKelas);
                            int mapelId = model.getMapelIdByName(namaMapel);
                            int guruId = model.getGuruIdByName(namaGuru);
                            
                            if (kelasId == -1) {
                                errorMessages.add("Baris " + lineNumber + ": Kelas '" + namaKelas + "' tidak ditemukan");
                                gagal++;
                                continue;
                            }
                            if (mapelId == -1) {
                                errorMessages.add("Baris " + lineNumber + ": Mata pelajaran '" + namaMapel + "' tidak ditemukan");
                                gagal++;
                                continue;
                            }
                            if (guruId == -1) {
                                errorMessages.add("Baris " + lineNumber + ": Guru '" + namaGuru + "' tidak ditemukan");
                                gagal++;
                                continue;
                            }
                            
                            // Cek bentrok
                            if (model.isBentrok(hari, jamKe, kelasId, guruId)) {
                                errorMessages.add("Baris " + lineNumber + ": Jadwal bentrok - " + hari + " jam " + jamKe + " (" + namaKelas + "/" + namaGuru + ")");
                                gagal++;
                                continue;
                            }
                            
                            // Insert jadwal
                            model.insertJadwal(hari, jamKe, kelasId, mapelId, guruId);
                            berhasil++;
                        } else {
                            errorMessages.add("Baris " + lineNumber + ": Data tidak lengkap (kurang dari 5 kolom)");
                            gagal++;
                        }
                    } catch (Exception e) {
                        errorMessages.add("Baris " + lineNumber + ": Error - " + e.getMessage());
                        gagal++;
                    }
                }
                
                reader.close();
                
                // Tampilkan hasil impor
                StringBuilder message = new StringBuilder();
                message.append("Impor selesai!\n");
                message.append("Delimiter yang digunakan: '").append(delimiter).append("'\n");
                message.append("Berhasil: ").append(berhasil).append(" data\n");
                message.append("Gagal: ").append(gagal).append(" data\n");
                
                if (!errorMessages.isEmpty()) {
                    message.append("\nDetail Error:\n");
                    int maxErrors = Math.min(errorMessages.size(), 15); // Batasi maksimal 15 error yang ditampilkan
                    for (int i = 0; i < maxErrors; i++) {
                        message.append("- ").append(errorMessages.get(i)).append("\n");
                    }
                    if (errorMessages.size() > 15) {
                        message.append("... dan ").append(errorMessages.size() - 15).append(" error lainnya.\n");
                    }
                }
                
                JOptionPane.showMessageDialog(view, message.toString());
                
                // Refresh tampilan jika ada data yang berhasil diimpor
                if (berhasil > 0 && view.cbFilterKelas.getSelectedItem() != null) {
                    tampilkanJadwal("kelas");
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Gagal impor CSV: " + ex.getMessage());
        }
    }
    
    private void downloadTemplate() {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Simpan Template CSV");
            chooser.setSelectedFile(new java.io.File("template_jadwal.csv"));
            chooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
            
            if (chooser.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
                String path = chooser.getSelectedFile().getAbsolutePath();
                if (!path.toLowerCase().endsWith(".csv")) {
                    path += ".csv";
                }
                
                FileWriter writer = new FileWriter(path);
                
                // Header
                writer.write("Hari,Jam Ke,Kelas,Mapel,Guru\n");
                
                // Contoh data
                writer.write("Senin,1,X IPA 1,Matematika,Ahmad Fauzi S.Pd\n");
                writer.write("Senin,2,X IPA 1,Bahasa Indonesia,Sari Indah S.Kom\n");
                writer.write("Selasa,1,X IPA 1,Fisika,Ahmad Fauzi S.Pd\n");
                writer.write("Selasa,2,X IPA 1,Bahasa Inggris,Bambang Tri S.Mat\n");
                
                writer.close();
                
                JOptionPane.showMessageDialog(view, 
                    "Template berhasil disimpan!\n\n" +
                    "Format CSV:\n" +
                    "- Hari: Senin/Selasa/Rabu/Kamis/Jumat/Sabtu\n" +
                    "- Jam Ke: 1-10\n" +
                    "- Kelas: Nama kelas sesuai database\n" +
                    "- Mapel: Nama mata pelajaran sesuai database\n" +
                    "- Guru: Nama guru sesuai database\n\n" +
                    "Pastikan nama kelas, mapel, dan guru sesuai dengan data yang ada di sistem!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Gagal membuat template: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JadwalView view = new JadwalView();
            JadwalModel model = new JadwalModel();
            new JadwalController(view, model);

            view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            view.setVisible(true);
        });
    }
}