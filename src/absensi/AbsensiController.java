package absensi;

import java.sql.Date;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class AbsensiController {
    private AbsensiModel model;
    private AbsensiView view;

    public AbsensiController(AbsensiModel model, AbsensiView view) {
        this.model = model;
        this.view = view;

        isiComboKelas();

        view.setMuatAction(e -> muatSiswa());
        view.setSimpanAction(e -> simpanAbsensi());
        view.setLihatRekapAction(e -> lihatRekapBulanan());

        view.setExportAction(e -> {
        String kelasDipilih = (String) view.comboKelas.getSelectedItem();
        String bulanStr = (String) view.comboBulan.getSelectedItem();
        String tahunStr = (String) view.comboTahun.getSelectedItem();

        if (kelasDipilih == null || bulanStr == null || tahunStr == null) {
            JOptionPane.showMessageDialog(view, "Pilih kelas, bulan dan tahun terlebih dahulu.");
            return;
        }

        try {
            int bulan = Integer.parseInt(bulanStr);
            int tahun = Integer.parseInt(tahunStr);
            exportRekapBulananKeCSV(kelasDipilih, bulan, tahun);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view, "Bulan atau tahun tidak valid.");
        }
        });

        view.setTambahSiswaAction(e -> {
        String nama = view.fieldNamaSiswa.getText().trim();
        String nis = view.fieldNis.getText().trim();
        String kelas = (String) view.comboKelas.getSelectedItem();

        if (nama.isEmpty() || nis.isEmpty() || kelas == null) {
            JOptionPane.showMessageDialog(view, "Lengkapi semua data siswa.");
            return;
        }

        boolean sukses = model.tambahSiswa(nama, nis, kelas);
        if (sukses) {
            JOptionPane.showMessageDialog(view, "Siswa berhasil ditambahkan.");
            view.fieldNamaSiswa.setText("");
            view.fieldNis.setText("");
            muatSiswa(); // Refresh daftar siswa
        } else {
            JOptionPane.showMessageDialog(view, "Gagal menambahkan siswa.");
        }
        });

        view.setExportPDFAction(e -> {
        String kelasDipilih = (String) view.comboKelas.getSelectedItem();
        String bulanStr = (String) view.comboBulan.getSelectedItem();
        String tahunStr = (String) view.comboTahun.getSelectedItem();

        if (kelasDipilih == null || bulanStr == null || tahunStr == null) {
        JOptionPane.showMessageDialog(view, "Pilih kelas, bulan dan tahun terlebih dahulu.");
        return;
        }

        try {
        int bulan = Integer.parseInt(bulanStr);
        int tahun = Integer.parseInt(tahunStr);
        exportRekapBulananKePDF(kelasDipilih, bulan, tahun);
        } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(view, "Bulan atau tahun tidak valid.");
        }
        });

    }

    private void isiComboKelas() {
        ArrayList<String> kelas = model.getDaftarKelas();
        for (String k : kelas) {
            view.comboKelas.addItem(k);
        }
    }

    private void muatSiswa() {
        String kelasDipilih = (String) view.comboKelas.getSelectedItem();
        if (kelasDipilih == null) return;

        ArrayList<String[]> daftarSiswa = model.getDaftarSiswaByKelas(kelasDipilih);
        String[] kolom = {"ID", "Nama", "NIS", "Status"};
        DefaultTableModel tableModel = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };

        for (String[] siswa : daftarSiswa) {
            tableModel.addRow(new Object[]{siswa[0], siswa[1], siswa[2], "Hadir"});
        }

        view.tableSiswa.setModel(tableModel);
        view.setStatusColumnAsDropdown();
        view.tableModel = tableModel; // <--- Sinkronkan dengan view.tableModel
    }

    private void simpanAbsensi() {
        DefaultTableModel modelTabel = (DefaultTableModel) view.tableSiswa.getModel();
        int rowCount = modelTabel.getRowCount();
        int sukses = 0;

        for (int i = 0; i < rowCount; i++) {
            int siswaId = Integer.parseInt(modelTabel.getValueAt(i, 0).toString());
            String status = modelTabel.getValueAt(i, 3).toString();
            Date tanggal = new Date(System.currentTimeMillis());

            if (model.simpanAbsensi(siswaId, tanggal, status)) {
                sukses++;
            }
        }

        view.statusLabel.setText("Berhasil menyimpan " + sukses + " data.");
    }

    private void lihatRekapBulanan() {
        String kelasDipilih = (String) view.comboKelas.getSelectedItem();
        String bulanStr = (String) view.comboBulan.getSelectedItem();
        String tahunStr = (String) view.comboTahun.getSelectedItem();

        if (kelasDipilih == null || bulanStr == null || tahunStr == null) {
            JOptionPane.showMessageDialog(view, "Pilih kelas, bulan dan tahun terlebih dahulu.");
            return;
        }

        try {
            int bulan = Integer.parseInt(bulanStr);
            int tahun = Integer.parseInt(tahunStr);

            ArrayList<String[]> dataRekap = model.getRekapBulanan(kelasDipilih, bulan, tahun);

            DefaultTableModel modelRekap = (DefaultTableModel) view.modelRekap;
            modelRekap.setRowCount(0); // reset

            for (String[] baris : dataRekap) {
                modelRekap.addRow(baris);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view, "Bulan atau tahun tidak valid.");
        }
    }

    private void exportRekapBulananKeCSV(String namaKelas, int bulan, int tahun) {
    ArrayList<String[]> data = model.getRekapBulanan(namaKelas, bulan, tahun);
    System.out.println("Jumlah data yang didapat: " + data.size());

    if (data.isEmpty()) {
        JOptionPane.showMessageDialog(view, "Tidak ada data untuk diekspor.");
        return;
    }

    String[] headers = {"Nama", "NIS", "Hadir", "Izin", "Sakit", "Alpha"};

    try {
        File folder = new File("ekspor_absensi");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String fileName = String.format("Rekap_Absensi_%s_%02d_%d.csv",
                namaKelas.replaceAll(" ", "_"), bulan, tahun);
        File file = new File(folder, fileName);

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Tulis header
            writer.println(String.join(",", headers));

            // Tulis data baris per baris
            for (String[] row : data) {
                writer.println(String.join(",", row));
            }
        }

        JOptionPane.showMessageDialog(view, "Data berhasil diekspor ke: " + file.getAbsolutePath());
    } catch (IOException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(view, "Gagal ekspor CSV: " + e.getMessage());
    }
    }

    private void exportRekapBulananKePDF(String namaKelas, int bulan, int tahun) {
        ArrayList<String[]> data = model.getRekapBulanan(namaKelas, bulan, tahun);
        if (data.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Tidak ada data untuk diekspor.");
            return;
        }

        String[] headers = {"Nama", "NIS", "Hadir", "Izin", "Sakit", "Alpha"};

        try {
            File folder = new File("ekspor_absensi");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            String fileName = String.format("Rekap_Absensi_%s_%02d_%d.pdf",
                    namaKelas.replaceAll(" ", "_"), bulan, tahun);
            File file = new File(folder, fileName);

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph("Rekap Absensi Bulanan", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" ")); // spasi

            PdfPTable table = new PdfPTable(headers.length);
            table.setWidthPercentage(100);

            // Tambahkan header
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }

            // Tambahkan isi data
            for (String[] row : data) {
                for (String value : row) {
                    table.addCell(value);
                }
            }

            document.add(table);
            document.close();

            JOptionPane.showMessageDialog(view,
                "Data berhasil diekspor ke: " + file.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Gagal ekspor PDF: " + e.getMessage());
        }
        }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AbsensiModel model = new AbsensiModel();
            AbsensiView view = new AbsensiView();
            new AbsensiController(model, view);
            view.setVisible(true);
        });
    }
}
