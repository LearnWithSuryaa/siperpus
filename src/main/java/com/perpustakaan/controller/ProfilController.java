package com.perpustakaan.controller;

import com.perpustakaan.dao.MemberDAO;
import com.perpustakaan.model.Member;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Optional;

public class ProfilController {

    @FXML private Label welcomeLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label memberIdLabel;
    @FXML private Label majorLabel;
    @FXML private Label genderLabel;
    @FXML private TextField emailField;
    @FXML private TextArea alamatField;
    @FXML private TextField teleponField;
    @FXML private Button ubahPinButton;
    @FXML private VBox pinContainer;
    @FXML private PasswordField pinField;
    @FXML private PasswordField konfirmasiPinField;
    @FXML private Button simpanButton;

    private MemberDAO memberDAO;
    private Member currentMember;
    private boolean isPinFormVisible = false;

    public ProfilController() {
        this.memberDAO = new MemberDAO();
    }

    /**
     * Metode ini dijalankan otomatis oleh JavaFX saat controller diinisialisasi.
     * Digunakan untuk menyembunyikan kontainer PIN pada awalnya.
     */
    @FXML
    public void initialize() {
        pinContainer.setVisible(false);
        pinContainer.setManaged(false);
        pinContainer.setOpacity(0);
    }

    /**
     * Menerima data dari MainViewController dan memulai setup halaman.
     * @param memberId ID dari anggota yang sedang login.
     */
    public void initData(String memberId) {
        Optional<Member> memberOpt = memberDAO.getMemberById(memberId);
        if (memberOpt.isPresent()) {
            this.currentMember = memberOpt.get();
            populateProfileData();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Data anggota tidak ditemukan.");
        }
    }

    /**
     * Mengisi semua field dan label dengan data dari anggota yang login.
     */
    private void populateProfileData() {
        // Mengambil nama depan untuk sapaan
        String firstName = currentMember.getFullName().split(" ")[0];
        welcomeLabel.setText("Profil Saya, " + firstName);
        
        // Mengisi kartu identitas
        fullNameLabel.setText(currentMember.getFullName());
        memberIdLabel.setText(currentMember.getMemberId());
        majorLabel.setText(currentMember.getMajor());
        genderLabel.setText(currentMember.getGender());

        // Mengisi kartu pengaturan akun
        emailField.setText(currentMember.getEmail());
        alamatField.setText(currentMember.getAddres());
        teleponField.setText(currentMember.getPhoneNumber());
    }

    /**
     * Menangani aksi klik pada tombol "Ubah PIN", menampilkan/menyembunyikan form dengan animasi.
     */
    @FXML
    private void handleUbahPinButton() {
        isPinFormVisible = !isPinFormVisible;
        animatePinContainer(isPinFormVisible);
    }

    /**
     * Menjalankan animasi geser dan pudar untuk kontainer PIN.
     */
    private void animatePinContainer(boolean show) {
        pinContainer.setManaged(true);
        pinContainer.setVisible(true);

        FadeTransition ft = new FadeTransition(Duration.millis(300), pinContainer);
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), pinContainer);

        if (show) {
            ft.setFromValue(0);
            ft.setToValue(1);
            tt.setFromY(-20); // Mulai dari sedikit di atas
            tt.setToY(0);     // Turun ke posisi asli
        } else {
            ft.setFromValue(1);
            ft.setToValue(0);
            tt.setFromY(0);
            tt.setToY(-20);
        }
        
        ParallelTransition pt = new ParallelTransition(ft, tt);
        pt.setOnFinished(_ -> {
            if (!show) {
                pinContainer.setManaged(false); // Sembunyikan dari layout setelah animasi selesai
            }
        });
        pt.play();
    }

    /**
     * Menangani aksi klik pada tombol "Simpan Perubahan".
     */
    @FXML
    private void handleSimpanButton() {
        if (currentMember == null) return;

        // Hanya proses validasi PIN jika formnya terlihat
        if (pinContainer.isVisible()) {
            String newPin = pinField.getText();
            String confirmPin = konfirmasiPinField.getText();

            // Jika pengguna mengisi salah satu field PIN, keduanya harus diisi dan cocok
            if (!newPin.isEmpty() || !confirmPin.isEmpty()) {
                if (newPin.length() < 4) {
                    showAlert(Alert.AlertType.ERROR, "Error Validasi", "PIN baru minimal harus 4 digit.");
                    return;
                }
                if (!newPin.equals(confirmPin)) {
                    showAlert(Alert.AlertType.ERROR, "Error Validasi", "PIN baru dan konfirmasi PIN tidak cocok.");
                    return;
                }
                currentMember.setPin(newPin); // Update PIN jika semua valid
            }
        }

        // Update field lain
        currentMember.setEmail(emailField.getText());
        currentMember.setAddres(alamatField.getText());
        currentMember.setPhoneNumber(teleponField.getText());
        
        // Simpan semua perubahan ke file CSV
        memberDAO.updateMember(currentMember);
        showAlert(Alert.AlertType.INFORMATION, "Sukses", "Profil Anda berhasil diperbarui.");
        
        // Reset tampilan perubahan PIN
        if(isPinFormVisible) {
            animatePinContainer(false);
            isPinFormVisible = false;
        }
        pinField.clear();
        konfirmasiPinField.clear();
    }

    /**
     * Menampilkan alert dengan tipe, judul, dan pesan yang diberikan.
     * @param alertType Tipe alert (ERROR, INFORMATION, WARNING, etc.)
     * @param title Judul dari alert
     * @param message Pesan yang akan ditampilkan di dalam alert
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/com/perpustakaan/view/styles.css").toExternalForm());
        alert.showAndWait();
    }
}