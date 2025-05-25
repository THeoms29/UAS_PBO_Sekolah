package NilaiSiswa;

import java.util.*;

public class NilaiController {
    private final InputNilaiView view;
    private final NilaiModel model;

    private int kelasId; 
    private final int mapelId;
    private final String semester;
    private final int guruId;

    public NilaiController(InputNilaiView view, NilaiModel model, int guruId, int kelasId, int mapelId, String semester) {
        this.view = view;
        this.model = model;
        this.guruId = guruId;
        this.kelasId = kelasId;
        this.mapelId = mapelId;
        this.semester = semester;

        view.setController(this);
    }

    public void loadDataAwal() {
        Map<String, String> guruMapel = model.getNamaGuruDanMapel(guruId, mapelId);
        String labelGuruMapel = "Guru: "
            + guruMapel.getOrDefault("nama_guru", "Unknown")
            + " - Mapel: "
            + guruMapel.getOrDefault("nama_mapel", "Unknown");
        view.setLabelGuruMapel(labelGuruMapel);

        List<Map<String, Object>> siswaList = model.getDataSiswaKelas(kelasId);
        view.setSiswaList(siswaList);

        List<Map<String, Object>> dataGabungan = model.getNilaiByKelasDanMapel(kelasId, mapelId, semester);
        view.setTableData(dataGabungan);
    }

    public void kelasDipilih(int kelasIdBaru) {
        this.kelasId = kelasIdBaru;

        List<Map<String, Object>> siswa = model.getDataSiswaKelas(kelasIdBaru);
        List<Map<String, Object>> nilai = model.getNilaiByKelasDanMapel(kelasIdBaru, mapelId, semester);

        view.setSiswaList(siswa);
        view.setTableData(nilai);
    }

    public boolean simpanNilai(List<Map<String, Object>> dataNilai) {
        try {
            for (Map<String, Object> nilai : dataNilai) {
                int siswaId = (int) nilai.get("siswa_id");
                int nilaiUH = parseNilai(nilai.get("nilai_uh"));
                int nilaiUTS = parseNilai(nilai.get("nilai_uts"));
                int nilaiUAS = parseNilai(nilai.get("nilai_uas"));

                model.simpanNilai(siswaId, mapelId, nilaiUH, nilaiUTS, nilaiUAS, semester);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private int parseNilai(Object nilaiObj) {
        if (nilaiObj == null) return 0;
        if (nilaiObj instanceof Integer) {
            return (Integer) nilaiObj;
        } else if (nilaiObj instanceof String) {
            try {
                return Integer.parseInt((String) nilaiObj);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public static void main(String[] args) {
        InputNilaiView view = new InputNilaiView();
        NilaiModel model = new NilaiModel();

        int guruId = 1;
        int kelasId = 1;
        int mapelId = 1;
        String semester = "2024-1";

        NilaiController controller = new NilaiController(view, model, guruId, kelasId, mapelId, semester);
        controller.loadDataAwal();

        view.setVisible(true);
    }
}
