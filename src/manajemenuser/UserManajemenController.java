package manajemenuser;

import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class UserManajemenController {
    private final UserManajemenView view;
    private final UserManajemenModel model;

    public UserManajemenController(UserManajemenView view, UserManajemenModel model) {
        this.view = view;
        this.model = model;

        // Inisialisasi listeners
        this.view.setSimpanAction(e -> handleSimpan());
        this.view.setUpdateAction(e -> handleUpdate());
        this.view.setHapusAction(e -> handleHapus());
        this.view.setBatalAction(e -> view.resetForm());

        // Muat data saat pertama kali controller dibuat
        loadUserTable();
    }

    private void loadUserTable() {
        List<Object[]> users = model.getAllUsers();
        DefaultTableModel tableModel = (DefaultTableModel) view.tableModel;
        tableModel.setRowCount(0); // Hapus data lama
        for (Object[] user : users) {
            tableModel.addRow(user);
        }
    }

    private void handleSimpan() {
        String nama = view.getNama();
        String username = view.getUsername();
        String password = view.getPassword();
        String role = view.getRole();

        if (nama.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Semua field harus diisi untuk pengguna baru!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (model.addUser(nama, username, password, role)) {
            JOptionPane.showMessageDialog(view, "Pengguna baru berhasil disimpan.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            view.resetForm();
            loadUserTable();
        } else {
            JOptionPane.showMessageDialog(view, "Gagal menyimpan pengguna baru.", "Gagal", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdate() {
        try {
            int id = Integer.parseInt(view.getId());
            String nama = view.getNama();
            String username = view.getUsername();
            String password = view.getPassword();
            String role = view.getRole();

            if (nama.isEmpty() || username.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Nama dan Username tidak boleh kosong!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (model.updateUser(id, nama, username, password, role)) {
                JOptionPane.showMessageDialog(view, "Data pengguna berhasil diupdate.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                view.resetForm();
                loadUserTable();
            } else {
                JOptionPane.showMessageDialog(view, "Gagal mengupdate data pengguna.", "Gagal", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(view, "Pilih pengguna dari tabel terlebih dahulu.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleHapus() {
        try {
            int id = Integer.parseInt(view.getId());
            
            int confirm = JOptionPane.showConfirmDialog(view, 
                "Apakah Anda yakin ingin menghapus pengguna ini?", 
                "Konfirmasi Hapus", 
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                String resultMessage = model.deleteUser(id);
    
                if (resultMessage == null) { // null berarti sukses
                    JOptionPane.showMessageDialog(view, "Pengguna berhasil dihapus.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    view.resetForm();
                    loadUserTable();
                } else { // pesan error
                    JOptionPane.showMessageDialog(view, resultMessage, "Gagal Menghapus", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(view, "Pilih pengguna dari tabel terlebih dahulu.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}