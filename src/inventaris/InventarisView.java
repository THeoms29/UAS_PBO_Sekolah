package inventaris;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;


public class InventarisView extends JFrame{
    public JTextField txtNama=new JTextField();
    public JTextField txtLokasi=new JTextField();
    public JTextField txtJumlah=new JTextField();
    public JComboBox<String>cbKondisi=new JComboBox<>(new String[]{"Baik","Rusak","Hilang"});
    public JButton btnSimpan=new JButton("Simpan");
    public JButton btnEdit=new JButton("Edit");
    public JButton btnHapus=new JButton("Hapus");
    public JButton btnExportCSV=new JButton("Ekspor ke Excel");
    public JButton btnExportPDF=new JButton("Ekspor ke PDF");
    public JTable table=new JTable();
    public DefaultTableModel tableModel;


    public InventarisView(){
        setTitle("Modul Inventaris Sekolah");
        setSize(700,500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        JLabel lblNama=new JLabel("Nama Barang:");
        JLabel lblLokasi=new JLabel("Lokasi:");
        JLabel lblJumlah=new JLabel("Jumlah:");
        JLabel lblKondisi=new JLabel("Kondisi:");
        lblNama.setBounds(20,20,100,25);
        txtNama.setBounds(130,20,200,25);
        lblLokasi.setBounds(20,60,100,25);
        txtLokasi.setBounds(130,60,200,25);
        lblJumlah.setBounds(20,100,100,25);
        txtJumlah.setBounds(130,100,200,25);
        lblKondisi.setBounds(20,140,100,25);
        cbKondisi.setBounds(130,140,200,25);
        btnSimpan.setBounds(350,20,120,30);
        btnEdit.setBounds(350,60,120,30);
        btnHapus.setBounds(350,100,120,30);
        btnExportCSV.setBounds(480,20,150,30);
        btnExportPDF.setBounds(480,60,150,30);
        tableModel=new DefaultTableModel(new String[]{"ID","Nama","Lokasi","Jumlah","Kondisi"},0);
        table.setModel(tableModel);
        JScrollPane scrollPane=new JScrollPane(table);
        scrollPane.setBounds(20,200,640,240);
        add(lblNama);add(txtNama);
        add(lblLokasi);add(txtLokasi);
        add(lblJumlah);add(txtJumlah);
        add(lblKondisi);add(cbKondisi);
        add(btnSimpan);add(btnEdit);add(btnHapus);
        add(btnExportCSV);add(btnExportPDF);
        add(scrollPane);
    }
}
