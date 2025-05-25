package jadwal;

import jadwal.JadwalModel;
import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.io.*;
import java.util.HashMap;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

public class JadwalController {
    private JadwalModel model;
    private JadwalView view;

    public JadwalController(JadwalModel model, JadwalView view) {
        this.model = model;
        this.view = view;

        isiCombo();

        view.setSimpanAction(e -> simpanJadwal());
        view.setExportPDFAction(e -> eksporKePDF());
        view.setComboKelasAction(e -> tampilkanJadwal());
        view.setImporCSVAction(e -> imporDariCSV()); // ➕ Tambahan ini
    }

    private void isiCombo() {
        ArrayList<String> kelas = model.getComboData("kelas", "nama_kelas");
        for (String k : kelas) view.comboKelas.addItem(k);

        ArrayList<String> mapel = model.getComboData("mapel", "nama_mapel");
        for (String m : mapel) view.comboMapel.addItem(m);

        ArrayList<String> guru = model.getComboData("users", "nama");
        for (String g : guru) view.comboGuru.addItem(g);

        if (view.comboKelas.getItemCount() > 0) tampilkanJadwal();
    }

    private int getId(JComboBox<String> combo) {
        String selected = (String) combo.getSelectedItem();
        if (selected != null && selected.contains(" - ")) {
            return Integer.parseInt(selected.split(" - ")[0]);
        }
        return -1;
    }

    private void simpanJadwal() {
        int kelasId = getId(view.comboKelas);
        int mapelId = getId(view.comboMapel);
        int guruId = getId(view.comboGuru);
        String hari = (String) view.comboHari.getSelectedItem();

        int jamKe;
        try {
            jamKe = Integer.parseInt(view.fieldJamKe.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view, "Jam ke harus angka!");
            return;
        }

        if (model.isBentrok(kelasId, hari, jamKe)) {
            JOptionPane.showMessageDialog(view, "Jadwal bentrok!");
            return;
        }

        boolean sukses = model.simpanJadwal(kelasId, mapelId, guruId, hari, jamKe);
        if (sukses) {
            JOptionPane.showMessageDialog(view, "Jadwal berhasil disimpan.");
            tampilkanJadwal();
        } else {
            JOptionPane.showMessageDialog(view, "Gagal menyimpan jadwal.");
        }
    }

    private void tampilkanJadwal() {
        int kelasId = getId(view.comboKelas);
        ArrayList<String[]> data = model.getJadwalByKelas(kelasId);
        view.modelTabel.setRowCount(0);
        for (String[] baris : data) {
            view.modelTabel.addRow(baris);
        }
    }

    private void eksporKePDF() {
        if (view.modelTabel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(view, "Tidak ada data untuk diekspor.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("jadwal_pelajaran.pdf"));
        int result = chooser.showSaveDialog(view);
        if (result != JFileChooser.APPROVE_OPTION) return;

        try {
            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(chooser.getSelectedFile()));
            doc.open();

            Paragraph title = new Paragraph("Jadwal Pelajaran", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16));
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.addCell("Hari");
            table.addCell("Jam Ke");
            table.addCell("Mapel");
            table.addCell("Guru");

            for (int i = 0; i < view.modelTabel.getRowCount(); i++) {
                for (int j = 0; j < view.modelTabel.getColumnCount(); j++) {
                    table.addCell(view.modelTabel.getValueAt(i, j).toString());
                }
            }

            doc.add(table);
            doc.close();

            JOptionPane.showMessageDialog(view, "PDF berhasil diekspor!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Gagal membuat PDF: " + e.getMessage());
        }
    }

    // 🔽 Fungsi tambahan untuk impor CSV
    private void imporDariCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Pilih file CSV");
        int result = chooser.showOpenDialog(view);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int kelasId = getId(view.comboKelas);
            if (kelasId == -1) {
                JOptionPane.showMessageDialog(view, "Pilih kelas terlebih dahulu sebelum impor.");
                return;
            }

            int berhasil = 0, gagal = 0;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 4) continue;

                String hari = parts[0].trim();
                int jamKe;
                try {
                    jamKe = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException e) {
                    gagal++;
                    continue;
                }

                String mapel = parts[2].trim();
                String guru = parts[3].trim();

                int mapelId = model.getIdByName("mapel", "nama_mapel", mapel);
                int guruId = model.getIdByName("users", "nama", guru);

                if (mapelId == -1 || guruId == -1 || model.isBentrok(kelasId, hari, jamKe)) {
                    gagal++;
                    continue;
                }

                boolean sukses = model.simpanJadwal(kelasId, mapelId, guruId, hari, jamKe);
                if (sukses) berhasil++;
                else gagal++;
            }

            JOptionPane.showMessageDialog(view,
                "Import selesai.\nBerhasil: " + berhasil + "\nGagal: " + gagal);
            tampilkanJadwal();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "Gagal membaca file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JadwalModel model = new JadwalModel();
            JadwalView view = new JadwalView();
            new JadwalController(model, view);
            view.setVisible(true);
        });
    }
}
