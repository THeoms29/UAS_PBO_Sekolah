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
import java.util.logging.Logger;

import main.MainController;
import shared.Koneksi;

public class WaliKelasController {
    private static final Logger LOGGER = Logger.getLogger(WaliKelasController.class.getName());
    private final WaliKelasView view;
    private final WaliKelasModel model;
    private boolean isFirstLoad = true;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final MainController mainController;
    private int mapelId; // Tambahkan field mapelId

    public WaliKelasController(WaliKelasView view, WaliKelasModel model, MainController mainController) {
        this.view = view;
        this.model = model;
        this.mainController = mainController;
        if (mainController != null) {
            this.mapelId = mainController.getMapelIdForGuru(mainController.getCurrentUser().getId()); // Inisialisasi mapelId
            mainController.setWaliKelasController(this);
        }
        initController();
    }

    private void initController() {
        loadKelas();
        view.getComboKelas().addActionListener(e -> {
            updateKelasInfo();
            tampilkanData();
            if (mainController != null) {
                mainController.setLastSelectedKelas((String) view.getComboKelas().getSelectedItem());
                LOGGER.info("Kelas dipilih: " + view.getComboKelas().getSelectedItem());
            }
        });
        view.getComboSemester().addActionListener(e -> {
            tampilkanData();
            if (mainController != null) {
                String semester = ((String) view.getComboSemester().getSelectedItem()).replace("Semester ", "");
                mainController.setLastSelectedSemester(semester);
                LOGGER.info("Semester dipilih: " + semester);
            }
        });
        view.getBtnExportPerKelas().addActionListener(e -> exportPDFPerKelas());
        view.getBtnExportPerSiswa().addActionListener(e -> exportPDFPerSiswa());
        view.getBtnExportPDFAll().addActionListener(e -> exportPDFAll());
        view.getBtnKeInputNilai().addActionListener(e -> bukaInputNilai());
        view.getBtnInputNilaiBaru().addActionListener(e -> bukaInputNilai());
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
        String[] kelasArray = new String[comboModel.getSize()];
        for (int i = 0; i < comboModel.getSize(); i++) {
            kelasArray[i] = comboModel.getElementAt(i);
        }
        view.setKelasList(kelasArray);
        if (comboModel.getSize() > 0) {
            String lastSelectedKelas = mainController.getLastSelectedKelas();
            if (lastSelectedKelas != null) {
                view.getComboKelas().setSelectedItem(lastSelectedKelas);
            } else {
                view.getComboKelas().setSelectedIndex(0);
            }
            String lastSelectedSemester = mainController.getLastSelectedSemester();
            if (lastSelectedSemester != null) {
                view.getComboSemester().setSelectedItem("Semester " + lastSelectedSemester);
            }
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

    public void tampilkanData() {
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
        List<Map<String, Object>> data = model.getRekapNilaiDanAbsensi(kelasId, semester, mapelId); // Gunakan field mapelId

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
        view.setStatusMessage("Data dimuat: " + new Date());
    }

    public void loadDataAwal() {
        loadKelas();
        tampilkanData();
        LOGGER.info("Data awal dimuat untuk WaliKelasController");
    }

    private void exportPDF(boolean isPerKelas) {
        String namaKelas = (String) view.getComboKelas().getSelectedItem();
        String semester = ((String) view.getComboSemester().getSelectedItem()).replace("Semester ", "");
        if (namaKelas == null || semester == null) {
            JOptionPane.showMessageDialog(view, "Pilih kelas dan semester terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int kelasId = model.getKelasIdByNama(namaKelas);
        List<Map<String, Object>> data = model.getRekapNilaiDanAbsensi(kelasId, semester, mapelId); // Gunakan field mapelId
        if (data.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Tidak ada data untuk diekspor!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            LOGGER.warning("Data kosong untuk kelas: " + namaKelas + ", semester: " + semester);
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

            PdfPTable headerTable = new PdfPTable(3);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1, 3, 1});

            PdfPCell leftImageCell = new PdfPCell();
            leftImageCell.setBorder(Rectangle.NO_BORDER);
            try {
                java.net.URL leftImgURL = getClass().getResource("/shared/Asset/Lambang_Kabupaten_Pringsewu.png");
                if (leftImgURL != null) {
                    Image leftLogo = Image.getInstance(leftImgURL);
                    leftLogo.scaleToFit(200, 100);
                    leftLogo.setAlignment(Element.ALIGN_LEFT);
                    leftImageCell.addElement(leftLogo);
                } else {
                    leftImageCell.addElement(new Phrase("Gambar kiri tidak ditemukan", cellFont));
                }
            } catch (Exception e) {
                leftImageCell.addElement(new Phrase("Gagal memuat gambar kiri", cellFont));
                LOGGER.severe("Gagal memuat gambar kiri: " + e.getMessage());
            }
            headerTable.addCell(leftImageCell);

            PdfPCell textCell = new PdfPCell();
            textCell.setBorder(Rectangle.NO_BORDER);
            Paragraph kopSurat = new Paragraph(
                    "PEMERINTAH KABUPATEN PRINGSEWU\n" +
                    "DINAS PENDIDIKAN DAN KEBUDAYAAN\n" +
                    "UPT SMP NEGERI 1 ADILUWIH\n" +
                    "NSS: 20.1.12.0.600012  Terakreditasi A  NPSN: 10804915\n" +
                    "Jln. Dadirejo Waringinsari Timur Kecamatan Adiluwih Kabupaten Pringsewu Provinsi Lampung, Kode Pos: 35674",
                    headerFont);
            kopSurat.setAlignment(Element.ALIGN_CENTER);
            textCell.addElement(kopSurat);
            headerTable.addCell(textCell);

            PdfPCell rightImageCell = new PdfPCell();
            rightImageCell.setBorder(Rectangle.NO_BORDER);
            try {
                java.net.URL rightImgURL = getClass().getResource("/shared/Asset/logo smp.jpg");
                if (rightImgURL != null) {
                    Image rightLogo = Image.getInstance(rightImgURL);
                    rightLogo.scaleToFit(200, 100);
                    rightLogo.setAlignment(Element.ALIGN_RIGHT);
                    rightImageCell.addElement(rightLogo);
                } else {
                    rightImageCell.addElement(new Phrase("Gambar kanan tidak ditemukan", cellFont));
                }
            } catch (Exception e) {
                rightImageCell.addElement(new Phrase("Gagal memuat gambar kanan", cellFont));
                LOGGER.severe("Gagal memuat gambar kanan: " + e.getMessage());
            }
            headerTable.addCell(rightImageCell);

            document.add(headerTable);
            document.add(new Paragraph("\n"));

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

            for (Map<String, Object> row : data) {
                table.addCell(new PdfPCell(new Phrase(row.get("nis") != null ? row.get("nis").toString() : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(row.get("nama") != null ? row.get("nama").toString() : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(row.get("nilai_uh") != null ? row.get("nilai_uh").toString() : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(row.get("nilai_uts") != null ? row.get("nilai_uts").toString() : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(row.get("nilai_uas") != null ? row.get("nilai_uas").toString() : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(row.get("nilai_akhir") != null ? String.format("%.2f", row.get("nilai_akhir")) : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(row.get("hadir") != null ? row.get("hadir").toString() : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(row.get("izin") != null ? row.get("izin").toString() : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(row.get("sakit") != null ? row.get("sakit").toString() : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(row.get("alfa") != null ? row.get("alfa").toString() : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(row.get("total_absensi") != null ? row.get("total_absensi").toString() : "", cellFont)));
            }

            document.add(table);
            document.close();
            JOptionPane.showMessageDialog(view, "PDF berhasil disimpan di: " + fileToSave.getAbsolutePath(), "Sukses", JOptionPane.INFORMATION_MESSAGE);
            LOGGER.info("PDF berhasil disimpan: " + fileToSave.getAbsolutePath());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Gagal membuat PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.severe("Gagal membuat PDF: " + e.getMessage());
        }
    }

    private void exportPDFPerKelas() {
        exportPDF(true);
    }

    private void exportPDFPerSiswa() {
        exportPDF(false);
    }

    private void exportPDFAll() {
        String namaKelas = (String) view.getComboKelas().getSelectedItem();
        String semester = ((String) view.getComboSemester().getSelectedItem()).replace("Semester ", "");
        if (namaKelas == null || semester == null) {
            JOptionPane.showMessageDialog(view, "Pilih kelas dan semester terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("Rekap_Semua_Siswa_" + namaKelas.replaceAll("[^a-zA-Z0-9_-]", "_") + ".pdf"));
        int userSelection = fileChooser.showSaveDialog(view);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File fileToSave = fileChooser.getSelectedFile();
        if (!fileToSave.getName().toLowerCase().endsWith(".pdf")) {
            fileToSave = new File(fileToSave.getAbsolutePath() + ".pdf");
        }

        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(fileToSave));
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 10);

            // Header
            PdfPTable headerTable = new PdfPTable(3);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1, 3, 1});

            PdfPCell leftImageCell = new PdfPCell();
            leftImageCell.setBorder(Rectangle.NO_BORDER);
            try {
                java.net.URL leftImgURL = getClass().getResource("/shared/Asset/Lambang_Kabupaten_Pringsewu.png");
                if (leftImgURL != null) {
                    Image leftLogo = Image.getInstance(leftImgURL);
                    leftLogo.scaleToFit(200, 100);
                    leftLogo.setAlignment(Element.ALIGN_LEFT);
                    leftImageCell.addElement(leftLogo);
                }
            } catch (Exception e) {
                leftImageCell.addElement(new Phrase("Gagal memuat gambar kiri", cellFont));
            }
            headerTable.addCell(leftImageCell);

            PdfPCell textCell = new PdfPCell();
            textCell.setBorder(Rectangle.NO_BORDER);
            Paragraph kopSurat = new Paragraph(
                    "PEMERINTAH KABUPATEN PRINGSEWU\n" +
                    "DINAS PENDIDIKAN DAN KEBUDAYAAN\n" +
                    "UPT SMP NEGERI 1 ADILUWIH\n" +
                    "NSS: 20.1.12.0.600012  Terakreditasi A  NPSN: 10804915\n" +
                    "Jln. Dadirejo Waringinsari Timur Kecamatan Adiluwih Kabupaten Pringsewu Provinsi Lampung, Kode Pos: 35674",
                    headerFont);
            kopSurat.setAlignment(Element.ALIGN_CENTER);
            textCell.addElement(kopSurat);
            headerTable.addCell(textCell);

            PdfPCell rightImageCell = new PdfPCell();
            rightImageCell.setBorder(Rectangle.NO_BORDER);
            try {
                java.net.URL rightImgURL = getClass().getResource("/shared/Asset/logo smp.jpg");
                if (rightImgURL != null) {
                    Image rightLogo = Image.getInstance(rightImgURL);
                    rightLogo.scaleToFit(200, 100);
                    rightLogo.setAlignment(Element.ALIGN_RIGHT);
                    rightImageCell.addElement(rightLogo);
                }
            } catch (Exception e) {
                rightImageCell.addElement(new Phrase("Gagal memuat gambar kanan", cellFont));
            }
            headerTable.addCell(rightImageCell);

            document.add(headerTable);
            document.add(new Paragraph("\n"));

            LineSeparator line = new LineSeparator();
            document.add(line);

            Paragraph title = new Paragraph("REKAP NILAI DAN ABSENSI SEMUA SISWA", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph info = new Paragraph(
                    "Kelas: " + namaKelas + " | Semester: " + semester + " | Tahun Ajaran: 2024/2025",
                    headerFont);
            info.setAlignment(Element.ALIGN_CENTER);
            info.setSpacingAfter(20);
            document.add(info);

            int kelasId = model.getKelasIdByNama(namaKelas);
            List<Map<String, Object>> data = model.getRekapNilaiDanAbsensi(kelasId, semester, mapelId); // Gunakan field mapelId

            for (Map<String, Object> siswa : data) {
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

                table.addCell(new PdfPCell(new Phrase(siswa.get("nis") != null ? siswa.get("nis").toString() : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(siswa.get("nama") != null ? siswa.get("nama").toString() : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(siswa.get("nilai_uh") != null ? siswa.get("nilai_uh").toString() : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(siswa.get("nilai_uts") != null ? siswa.get("nilai_uts").toString() : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(siswa.get("nilai_uas") != null ? siswa.get("nilai_uas").toString() : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(siswa.get("nilai_akhir") != null ? String.format("%.2f", siswa.get("nilai_akhir")) : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(siswa.get("hadir") != null ? siswa.get("hadir").toString() : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(siswa.get("izin") != null ? siswa.get("izin").toString() : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(siswa.get("sakit") != null ? siswa.get("sakit").toString() : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(siswa.get("alfa") != null ? siswa.get("alfa").toString() : "", cellFont)));
                table.addCell(new PdfPCell(new Phrase(siswa.get("total_absensi") != null ? siswa.get("total_absensi").toString() : "", cellFont)));

                document.add(table);
                document.add(new Paragraph("\n"));
            }

            document.close();
            JOptionPane.showMessageDialog(view, "PDF berhasil disimpan di: " + fileToSave.getAbsolutePath(), "Sukses", JOptionPane.INFORMATION_MESSAGE);
            LOGGER.info("PDF semua siswa berhasil disimpan: " + fileToSave.getAbsolutePath());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Gagal membuat PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.severe("Gagal membuat PDF semua siswa: " + e.getMessage());
        }
    }

    private void bukaInputNilai() {
        String namaKelas = (String) view.getComboKelas().getSelectedItem();
        String semester = ((String) view.getComboSemester().getSelectedItem()).replace("Semester ", "");
        if (namaKelas == null || semester == null) {
            JOptionPane.showMessageDialog(view, "Pilih kelas dan semester terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            LOGGER.warning("Kelas atau semester tidak dipilih saat membuka InputNilai");
            return;
        }

        if (mainController != null) {
            mainController.setLastSelectedKelas(namaKelas);
            mainController.setLastSelectedSemester(semester);
            LOGGER.info("Menyimpan state sebelum navigasi ke InputNilai: kelas=" + namaKelas + ", semester=" + semester);
            
            WaliKelasController waliKelasController = this;
            
            view.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowActivated(java.awt.event.WindowEvent e) {
                    waliKelasController.tampilkanData();
                    LOGGER.info("WaliKelasView diaktifkan kembali, memuat ulang data");
                }
            });
            
            mainController.openNilaiSiswaModule();
            view.setVisible(false);
        }
    }

    private void handleTableSelection() {
        int selectedRow = view.getTable().getSelectedRow();
        if (selectedRow >= 0) {
            String nis = view.getTableModel().getValueAt(selectedRow, 0).toString();
            String nama = view.getTableModel().getValueAt(selectedRow, 1).toString();
            view.setStatusMessage("Siswa dipilih: " + nama + " (NIS: " + nis + ")");
        }
    }

    private void startAutoRefresh() {
        scheduler.scheduleAtFixedRate(() -> {
            SwingUtilities.invokeLater(this::checkDataUpdate);
        }, 0, 10, TimeUnit.SECONDS);
    }

    private void checkDataUpdate() {
        String namaKelas = (String) view.getComboKelas().getSelectedItem();
        String semester = ((String) view.getComboSemester().getSelectedItem()).replace("Semester ", "");
        if (namaKelas != null && semester != null) {
            int kelasId = model.getKelasIdByNama(namaKelas);
            if (kelasId != -1 && model.checkForUpdates(kelasId, semester, mapelId)) { // Tambahkan mapelId
                tampilkanData();
                view.setStatusMessage("Data diperbarui: " + new Date());
                LOGGER.info("Data diperbarui untuk kelas: " + namaKelas + ", semester: " + semester);
            }
        }
    }

    public void notifyDataChanged() {
        SwingUtilities.invokeLater(this::tampilkanData);
        LOGGER.info("Notifikasi perubahan data diterima, memuat ulang data");
    }

    public void shutdownScheduler() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                LOGGER.severe("Gagal menutup scheduler: " + e.getMessage());
            }
            LOGGER.info("Scheduler dimatikan");
        }
    }
}