package absensi;

import java.sql.Date;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;

import main.MainController;
import NilaiSiswa.*;

public class AbsensiController {
    private AbsensiModel model;
    private AbsensiView view;
    private MainController mainController;

    public AbsensiController(AbsensiModel model, AbsensiView view, MainController mainController) {
        this.model = model;
        this.view = view;
        this.mainController = mainController;

        if (mainController == null) {
            JOptionPane.showMessageDialog(view, "Sesi login tidak valid, gagal memuat modul Absensi!", "Error", JOptionPane.ERROR_MESSAGE);
            view.dispose();
            return;
        }

        isiComboKelas();

        view.setMuatAction(e -> muatSiswa());
        view.setSimpanAction(e -> simpanAbsensi());
        view.setLihatRekapAction(e -> lihatRekapBulanan());
        view.setKeWaliKelasAction(e -> keWaliKelas());

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

    private void keWaliKelas() {
        try {
            if (mainController == null) {
                JOptionPane.showMessageDialog(view, "Sesi login tidak valid, gagal membuka Wali Kelas!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            WaliKelasView waliKelasView = new WaliKelasView();
            WaliKelasModel waliKelasModel = new WaliKelasModel();
            new WaliKelasController(waliKelasView, waliKelasModel, mainController);
            waliKelasView.setVisible(true);
            view.dispose(); // Tutup view absensi
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Gagal membuka Wali Kelas: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AbsensiModel model = new AbsensiModel();
            AbsensiView view = new AbsensiView();
            MainController mainController = new MainController(); // Sementara, harus disesuaikan
            new AbsensiController(model, view, mainController);
            view.setVisible(true);
        });
    } 
}