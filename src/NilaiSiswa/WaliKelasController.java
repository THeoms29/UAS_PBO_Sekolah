package NilaiSiswa;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

public class WaliKelasController {
    private final WaliKelasView view;
    private final WaliKelasModel model;

    public WaliKelasController(WaliKelasView view) {
        this.view = view;
        this.model = new WaliKelasModel();

        // Listener supaya saat pilih kelas atau semester berubah langsung update tabel
        this.view.getComboKelas().addActionListener(e -> tampilkanData());
        this.view.getComboSemester().addActionListener(e -> tampilkanData());
    }

    public void loadKelas() {
        List<Map<String, Object>> kelasList = model.getDaftarKelas();
        JComboBox<String> combo = view.getComboKelas();
        combo.removeAllItems();

        for (Map<String, Object> kls : kelasList) {
            String namaKelas = (String) kls.get("nama_kelas");
            combo.addItem(namaKelas);
        }
    }

    public void tampilkanData() {
        String namaKelas = (String) view.getComboKelas().getSelectedItem();
        if (namaKelas == null) {
            return;
        }

        int kelasId = model.getKelasIdByNama(namaKelas);

        String semesterCombo = (String) view.getComboSemester().getSelectedItem();
        String semester = semesterCombo.equalsIgnoreCase("Ganjil") ? "1" : "2";

        List<Map<String, Object>> data = model.getRekapNilaiDanAbsensi(kelasId, semester);

        DefaultTableModel tableModel = view.getTableModel();
        tableModel.setRowCount(0);

        for (Map<String, Object> row : data) {
            tableModel.addRow(new Object[] {
                row.get("nis"),
                row.get("nama_siswa"),
                row.get("nilai_akhir"),
                row.get("hadir"),
                row.get("izin"),
                row.get("sakit"),
                row.get("alfa"),
                row.get("total_absensi")
            });
        }
    }

    public void exportPDF() {
        try {
            String namaKelas = (String) view.getComboKelas().getSelectedItem();
            if (namaKelas == null) {
                JOptionPane.showMessageDialog(view, "Pilih kelas terlebih dahulu!");
                return;
            }
            String semester = (String) view.getComboSemester().getSelectedItem();
            String namaFile = "RekapNilaiAbsensi_" + namaKelas.replace(" ", "_") + "_" + semester + ".pdf";

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(namaFile));
            document.open();

            document.add(new Paragraph("Rekap Nilai dan Absensi", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph("Kelas: " + namaKelas));
            document.add(new Paragraph("Semester: " + semester));
            document.add(new Paragraph(" "));

            PdfPTable pdfTable = new PdfPTable(8);
            pdfTable.setWidthPercentage(100);
            String[] headers = {"NIS", "Nama", "Nilai Akhir", "Hadir", "Izin", "Sakit", "Alfa", "Total"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                pdfTable.addCell(cell);
            }

            DefaultTableModel modelTabel = view.getTableModel();
            for (int i = 0; i < modelTabel.getRowCount(); i++) {
                for (int j = 0; j < modelTabel.getColumnCount(); j++) {
                    pdfTable.addCell(String.valueOf(modelTabel.getValueAt(i, j)));
                }
            }

            document.add(pdfTable);
            document.close();

            JOptionPane.showMessageDialog(view, "Berhasil menyimpan PDF: " + namaFile);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Gagal export PDF: " + e.getMessage());
        }
    }

    // Main untuk testing standalone
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WaliKelasView().setVisible(true));
    }
}
