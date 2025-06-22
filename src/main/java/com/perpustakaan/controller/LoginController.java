package com.perpustakaan.controller;

import com.perpustakaan.dao.MemberDAO;
import com.perpustakaan.model.Member;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class LoginController {

    @FXML private ImageView logoImageView;
    @FXML private TextField usernameField;
    @FXML private PasswordField pinField;
    @FXML private Label statusLabel;
    @FXML private Button loginButton;
    @FXML private Button aboutButton;

    private MemberDAO memberDAO;

    /**
     * Konstruktor untuk inisialisasi DAO.
     * Biasanya dipanggil oleh JavaFX saat controller di-load.
     */
    public LoginController() {
        this.memberDAO = new MemberDAO();
    }

    /**
     * Metode ini dijalankan otomatis oleh JavaFX saat controller diinisialisasi.
     * Digunakan untuk setup awal seperti memuat logo, mengatur tombol login interaktif, dan reset error.
     */
    @FXML
    public void initialize() {
        loadLogo();
        setupInteractiveLoginButton();
        setupErrorResetListeners();
    }

    /**
     * Memuat gambar logo dari resources.
     * Jika gagal, akan mencetak pesan error ke konsol.
     */
    private void loadLogo() {
        try {
            Image logo = new Image(getClass().getResourceAsStream("/images/logo-pixel.png"));
            logoImageView.setImage(logo);
        } catch (Exception e) {
            System.err.println("Gagal memuat gambar logo: logo-pixel.png");
        }
    }

    /**
     * Mengatur tombol login agar hanya aktif jika kedua field diisi.
     * Menggunakan binding untuk mengikat status disable ke kondisi field.
     */
    private void setupInteractiveLoginButton() {
        loginButton.disableProperty().bind(
            Bindings.createBooleanBinding(() ->
                usernameField.getText().trim().isEmpty() ||
                pinField.getText().trim().isEmpty(),
                usernameField.textProperty(),
                pinField.textProperty()
            )
        );
    }
    
    /**
     * Menambahkan listener pada field input untuk menyembunyikan label status saat pengguna mengetik.
     * Ini membantu menghindari kebingungan jika pengguna ingin mencoba login lagi setelah gagal.
     */
    private void setupErrorResetListeners() {
        usernameField.textProperty().addListener((_, _, _) -> statusLabel.setVisible(false));
        pinField.textProperty().addListener((_, _, _) -> statusLabel.setVisible(false));
    }

    /**
     * Metode ini dipanggil saat tombol login ditekan.
     * Memeriksa kredensial pengguna dan menavigasi ke dashboard jika berhasil.
     * Jika gagal, menampilkan pesan error.
     */
    @FXML
    private void handleLoginButtonAction() throws IOException {
        String idInput = usernameField.getText();
        String passwordInput = pinField.getText();

        Optional<Member> memberOpt = memberDAO.getMemberById(idInput);
        if (memberOpt.isPresent() && memberOpt.get().getPin().equals(passwordInput)) {
            navigateToDashboard("Mahasiswa", idInput);
            return;
        }
        if (idInput.equalsIgnoreCase("admin") && passwordInput.equals("admin123")) {
            navigateToDashboard("Admin", idInput);
            return;
        }
        showError("ID PENGGUNA ATAU PASSWORD SALAH");
    }
    
    /**
     * Metode ini dipanggil saat tombol "Tentang Aplikasi" ditekan.
     * Menampilkan dialog informasi tentang aplikasi perpustakaan.
     */
    @FXML
    private void handleAboutButton() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tentang Aplikasi");
        alert.setHeaderText("Sistem Informasi Perpustakaan (Pixel Edition)");
        alert.setContentText("Aplikasi ini adalah hasil dari proses belajar dan pengembangan bersama.");
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/com/perpustakaan/view/base.css").toExternalForm());
        dialogPane.getStylesheets().add(getClass().getResource("/com/perpustakaan/view/theme-dashboard.css").toExternalForm());
        
        alert.showAndWait();
    }

    /**
     * Metode ini digunakan untuk menavigasi ke dashboard sesuai dengan peran pengguna.
     * @param role Peran pengguna (Admin atau Mahasiswa)
     * @param username ID pengguna yang berhasil login
     * @throws IOException Jika terjadi kesalahan saat memuat FXML
     */
    private void navigateToDashboard(String role, String username) throws IOException {
        System.out.println("Login Berhasil sebagai " + role + " dengan ID: " + username);
        Stage stage = (Stage) loginButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/perpustakaan/view/MainView.fxml"));
        Parent mainViewRoot = loader.load();
        MainViewController controller = loader.getController();
        
        String themeFile = "theme-dashboard.css";
        controller.initData(role, username, themeFile);
        
        Scene scene = new Scene(mainViewRoot, 1280, 770);
        
        String baseCss = getClass().getResource("/com/perpustakaan/view/base.css").toExternalForm();
        String themeCss = getClass().getResource("/com/perpustakaan/view/" + themeFile).toExternalForm();
        scene.getStylesheets().addAll(baseCss, themeCss);
        
        stage.setScene(scene);
        stage.setTitle("Dashboard Perpustakaan");
        stage.setResizable(true);
        stage.centerOnScreen();
    }
    
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
    }
}