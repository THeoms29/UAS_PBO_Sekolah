package NilaiSiswa;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WaliKelasController {
    private final WaliKelasView view;
    private final WaliKelasModel model;
    private boolean isFirstLoad = true;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public WaliKelasController(WaliKelasView view, WaliKelasModel model) {
        this.view = view;
        this.model = model;
        initController();
    }

    private void initController() {
        loadKelas();
        view.getComboKelas().addActionListener(e -> {
            updateKelasInfo();
            tampilkanData();
        });
        view.getComboSemester().addActionListener(e -> tampilkanData());
        view.getBtnExportPerKelas().addActionListener(e -> exportPDFPerKelas());
        view.getBtnExportPerSiswa().addActionListener(e -> exportPDFPerSiswa());
        view.getTable().getSelectionModel().addListSelectionListener(e -> handleTableSelection());
        tampilkanData();
        startAutoRefresh();
    }

    private void loadKelas() {
        List<Map<String, Object>> daftarKelas = model.getDaftarKelas();
        DefaultComboBoxModel<String> comboModel = new DefaultComboBoxModel<>();
        for (Map<String, Object> kelas : daftarKelas) {
            comboModel.addElement((String) kelas.get("nama_kelas"));
        }
        view.getComboKelas().setModel(comboModel);
        if (comboModel.getSize() > 0) {
            view.getComboKelas().setSelectedIndex(0);
            updateKelasInfo();
            tampilkanData();
        } else {
            view.setStatusMessage("Tidak ada kelas tersedia");
            JOptionPane.showMessageDialog(view, "Tidak ada kelas tersedia!", "Peringatan", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updateKelasInfo() {
        String namaKelas = (String) view.getComboKelas().getSelectedItem();
        if (namaKelas != null) {
            view.setKelasInfo(namaKelas);
        } else {
            view.setKelasInfo(" ");
        }
    }

    private void tampilkanData() {
        String namaKelas = (String) view.getComboKelas().getSelectedItem();
        if (namaKelas == null || namaKelas.isEmpty()) {
            view.getTableModel().setRowCount(0);
            view.setStatusMessage("Tidak ada kelas yang dipilih");
            return;
        }

        int kelasId = model.getKelasIdByNama(namaKelas);
        if (kelasId == -1) {
            JOptionPane.showMessageDialog(view, "Kelas tidak ditemukan", "Error", JOptionPane.ERROR_MESSAGE);
            view.setStatusMessage("Kelas tidak ditemukan");
            return;
        }

        String semester = ((String) view.getComboSemester().getSelectedItem()).replace("Semester ", "");
        List<Map<String, Object>> data = model.getRekapNilaiDanAbsensi(kelasId, semester);

        DefaultTableModel tableModel = view.getTableModel();
        tableModel.setRowCount(0);

        if (data.isEmpty()) {
            view.setStatusMessage("Tidak ada data untuk kelas " + namaKelas + " pada semester " + semester);
            JOptionPane.showMessageDialog(view, 
                "Tidak ada data siswa, nilai, atau absensi untuk kelas ini.", 
                "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        for (Map<String, Object> row : data) {
            Object[] rowData = new Object[]{
                row.get("nis"),
                row.get("nama"),
                row.get("nilai_uh"),
                row.get("nilai_uts"),
                row.get("nilai_uas"),
                row.get("nilai_akhir"),
                row.get("hadir"),
                row.get("izin"),
                row.get("sakit"),
                row.get("alfa"),
                row.get("total_absensi")
            };
            tableModel.addRow(rowData);
        }
        tableModel.fireTableDataChanged();
        view.getTable().repaint();
        view.setStatusMessage("Data dimuat: " + new java.util.Date());
    }

    private void exportPDF(boolean isPerKelas) {
        String namaKelas = (String) view.getComboKelas().getSelectedItem();
        String semester = ((String) view.getComboSemester().getSelectedItem()).replace("Semester ", "");
        if (namaKelas == null || semester == null) {
            JOptionPane.showMessageDialog(view, "Pilih kelas dan semester terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String safeKelasName = namaKelas.replaceAll("[^a-zA-Z0-9_-]", "_");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(isPerKelas ? "Rekap_Kelas_" + safeKelasName + ".pdf" : "Rekap_Siswa_" + safeKelasName + ".pdf"));
        int userSelection = fileChooser.showSaveDialog(view);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File fileToSave = fileChooser.getSelectedFile();
        if (!fileToSave.getName().toLowerCase().endsWith(".pdf")) {
            fileToSave = new File(fileToSave.getAbsolutePath() + ".pdf");
        }

        try {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, new FileOutputStream(fileToSave));
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 10);

            // Membuat tabel untuk header dengan dua logo dan teks di tengah
            PdfPTable headerTable = new PdfPTable(3);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1, 3, 1}); // Logo kiri, teks tengah, logo kanan

            // Menambahkan logo kiri
            PdfPCell leftImageCell = new PdfPCell();
            leftImageCell.setBorder(Rectangle.NO_BORDER);
            try {
                java.net.URL leftImgURL = getClass().getResource("/shared/Asset/Lambang_Kabupaten_Pringsewu.png");
                if (leftImgURL != null) {
                    Image leftLogo = Image.getInstance(leftImgURL);
                    leftLogo.scaleToFit(100, 100); // Sesuaikan ukuran gambar
                    leftLogo.setAlignment(Element.ALIGN_LEFT);
                    leftImageCell.addElement(leftLogo);
                } else {
                    leftImageCell.addElement(new Phrase("Gambar kiri tidak ditemukan", cellFont));
                }
            } catch (Exception e) {
                leftImageCell.addElement(new Phrase("Gagal memuat gambar kiri", cellFont));
                e.printStackTrace();
            }
            headerTable.addCell(leftImageCell);

            // Menambahkan teks header di tengah
            PdfPCell textCell = new PdfPCell();
            textCell.setBorder(Rectangle.NO_BORDER);
            Paragraph kopSurat = new Paragraph(
                    "PEMERINTAH KABUPATEN PRINGSEWU\n" +
                    "DINAS PENDIDIKAN DAN KEBUDAYAAN\n" +
                    "UPT SMP NEGERI 2 ADILUWIH\n" +
                    "NSS: 20.1.12.0.600012  Terakreditasi A  NPSN: 10804911\n" +
                    "Jln. Dadirejo Waringinsari Timur Kecamatan Adiluwih Kabupaten Pringsewu Provinsi Lampung, Kode Pos: 35674",
                    headerFont);
            kopSurat.setAlignment(Element.ALIGN_CENTER);
            textCell.addElement(kopSurat);
            headerTable.addCell(textCell);

            // Menambahkan logo kanan
            PdfPCell rightImageCell = new PdfPCell();
            rightImageCell.setBorder(Rectangle.NO_BORDER);
            try {
                java.net.URL rightImgURL = getClass().getResource("/shared/Asset/logo smp.jpg");
                if (rightImgURL != null) {
                    Image rightLogo = Image.getInstance(rightImgURL);
                    rightLogo.scaleToFit(100, 100); // Sesuaikan ukuran gambar
                    rightLogo.setAlignment(Element.ALIGN_RIGHT);
                    rightImageCell.addElement(rightLogo);
                } else {
                    rightImageCell.addElement(new Phrase("Gambar kanan tidak ditemukan", cellFont));
                }
            } catch (Exception e) {
                rightImageCell.addElement(new Phrase("Gagal memuat gambar kanan", cellFont));
                e.printStackTrace();
            }
            headerTable.addCell(rightImageCell);

            document.add(headerTable);
            document.add(new Paragraph("\n"));

            // Menambahkan garis di atas judul
            LineSeparator line = new LineSeparator();
            document.add(line);

            Paragraph title = new Paragraph("REKAP NILAI DAN ABSENSI SISWA", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph info = new Paragraph(
                    "Kelas: " + namaKelas + " | Semester: " + semester + " | Tahun Ajaran: 2024/2025",
                    headerFont);
            info.setAlignment(Element.ALIGN_CENTER);
            info.setSpacingAfter(20);
            document.add(info);

            PdfPTable table = new PdfPTable(11);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2, 4, 2, 2, 2, 2, 2, 2, 2, 2, 2});

            String[] headers = {"NIS", "Nama Siswa", "Nilai UH", "Nilai UTS", "Nilai UAS", "Nilai Akhir",
                    "Hadir", "Izin", "Sakit", "Alfa", "Total"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }

            DefaultTableModel model = view.getTableModel();
            if (isPerKelas) {
                for (int i = 0; i < model.getRowCount(); i++) {
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        Object value = model.getValueAt(i, j);
                        String text = value == null ? "" : String.format(j >= 2 && j <= 5 ? "%.2f" : "%s", value);
                        PdfPCell cell = new PdfPCell(new Phrase(text, cellFont));
                        cell.setHorizontalAlignment(j < 2 ? Element.ALIGN_LEFT : Element.ALIGN_CENTER);
                        if (j >= 2 && j <= 5 && value != null) {
                            try {
                                double num = Double.parseDouble(value.toString());
                                if (num < 65) {
                                    cell.setBackgroundColor(new BaseColor(255, 200, 200));
                                } else if (num < 75) {
                                    cell.setBackgroundColor(new BaseColor(255, 255, 200));
                                } else {
                                    cell.setBackgroundColor(new BaseColor(200, 255, 200));
                                }
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        table.addCell(cell);
                    }
                }
            } else {
                int selectedRow = view.getTable().getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(view, "Pilih siswa terlebih dahulu dari tabel!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    document.close();
                    return;
                }
                for (int j = 0; j < model.getColumnCount(); j++) {
                    Object value = model.getValueAt(selectedRow, j);
                    String text = value == null ? "" : String.format(j >= 2 && j <= 5 ? "%.2f" : "%s", value);
                    PdfPCell cell = new PdfPCell(new Phrase(text, cellFont));
                    cell.setHorizontalAlignment(j < 2 ? Element.ALIGN_LEFT : Element.ALIGN_CENTER);
                    if (j >= 2 && j <= 5 && value != null) {
                        try {
                            double num = Double.parseDouble(value.toString());
                            if (num < 65) {
                                cell.setBackgroundColor(new BaseColor(255, 200, 200));
                            } else if (num < 75) {
                                cell.setBackgroundColor(new BaseColor(255, 255, 200));
                            } else {
                                cell.setBackgroundColor(new BaseColor(200, 255, 200));
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    table.addCell(cell);
                }
            }

            document.add(table);

            Paragraph footer = new Paragraph(
                    "Dicetak pada: " + new SimpleDateFormat("dd MMMM yyyy HH:mm").format(new Date()),
                    cellFont);
            footer.setAlignment(Element.ALIGN_RIGHT);
            footer.setSpacingBefore(20);
            document.add(footer);

            Paragraph signature = new Paragraph("Mengetahui,\nWali Kelas\n\n\n____________________", cellFont);
            signature.setAlignment(Element.ALIGN_RIGHT);
            signature.setSpacingBefore(40);
            document.add(signature);

            document.close();
            JOptionPane.showMessageDialog(view, "PDF berhasil disimpan di: " + fileToSave.getAbsolutePath(), 
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Gagal membuat PDF: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void exportPDFPerKelas() {
        exportPDF(true);
    }

    void exportPDFPerSiswa() {
        exportPDF(false);
    }

    private void handleTableSelection() {
        // Implementasi penanganan pemilihan baris tabel (jika diperlukan)
    }

    private void startAutoRefresh() {
        scheduler.scheduleAtFixedRate(this::checkForUpdates, 0, 10, TimeUnit.SECONDS);
    }

    private void checkForUpdates() {
        String namaKelas = (String) view.getComboKelas().getSelectedItem();
        if (namaKelas == null) return;

        int kelasId = model.getKelasIdByNama(namaKelas);
        String semester = ((String) view.getComboSemester().getSelectedItem()).replace("Semester ", "");

        if (isFirstLoad) {
            isFirstLoad = false;
            return;
        }

        if (model.checkForUpdates(kelasId, semester)) {
            SwingUtilities.invokeLater(() -> {
                tampilkanData();
                view.setStatusMessage("Data diperbarui: " + new java.util.Date());
            });
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                WaliKelasView view = new WaliKelasView();
                WaliKelasModel model = new WaliKelasModel();
                WaliKelasController controller = new WaliKelasController(view, model);
                view.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Gagal memulai aplikasi: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}