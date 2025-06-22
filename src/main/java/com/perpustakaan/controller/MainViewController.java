package com.perpustakaan.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MainViewController {

    @FXML
    private BorderPane mainPane;
    @FXML
    private StackPane contentArea;
    @FXML
    private VBox sidebarMenuBox;
    @FXML
    private Label sidebarTitle;
    @FXML
    private Label clockLabel;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label userIdLabel;

    private String currentUserRole;
    private String currentUsername;
    private String currentThemeFile;
    private final ToggleGroup menuToggleGroup = new ToggleGroup();

    /**
     * Inisialisasi controller setelah FXML dimuat.
     * Metode ini akan dipanggil oleh JavaFX secara otomatis.
     */
    @FXML
    public void initialize() {
        startClock();
    }

    /**
     * Inisialisasi data pengguna dan tema awal.
     * @param role Peran pengguna (Admin atau Mahasiswa).
     * @param username Nama pengguna yang sedang login.
     * @param initialThemeFile Nama file tema awal yang akan digunakan.
     */
    public void initData(String role, String username, String initialThemeFile) {
        this.currentUserRole = role;
        this.currentUsername = username;
        this.currentThemeFile = initialThemeFile;

        userNameLabel.setText(role.equalsIgnoreCase("Admin") ? "Administrator" : username);
        userIdLabel.setText(role);

        setupDashboardByRole();
    }

    /**
     * Memulai jam digital yang akan memperbarui label setiap detik.
     */
    private void startClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, _ -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            clockLabel.setText(LocalTime.now().format(formatter));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    /**
     * Mengatur menu sidebar berdasarkan peran pengguna.
     * Menghapus semua tombol yang ada dan membuat tombol baru sesuai dengan peran.
     */
    private void setupDashboardByRole() {
        sidebarMenuBox.getChildren().clear();

        String dashboardTheme = "theme-dashboard.css";

        if ("Admin".equalsIgnoreCase(currentUserRole)) {
            sidebarTitle.setText("ADMIN PANEL");
            createMenuButton("Beranda", "icon_home", "WelcomeView.fxml", dashboardTheme, true);
            createMenuButton("Kelola Buku", "icon_book", "KelolaBukuView.fxml", dashboardTheme, false);
            createMenuButton("Kelola Anggota", "icon_users", "KelolaAnggotaView.fxml", dashboardTheme, false);
            createMenuButton("Pengembalian", "icon_undo", "PengembalianView.fxml", dashboardTheme, false);
            createMenuButton("Laporan", "icon_chart", "LaporanView.fxml", dashboardTheme, false); // Sementara pakai
                                                                                                  // tema dashboard
        } else if ("Mahasiswa".equalsIgnoreCase(currentUserRole)) {
            sidebarTitle.setText("MEMBER AREA");
            createMenuButton("Beranda", "icon_home", "BerandaMemberView.fxml", dashboardTheme, true);
            createMenuButton("Katalog Buku", "icon_book", "KatalogBukuView.fxml", dashboardTheme, false);
            createMenuButton("Buku Saya", "icon_ticket", "BukuSayaView.fxml", dashboardTheme, false);
            createMenuButton("Profil", "icon_user", "ProfilView.fxml", dashboardTheme, false);
        }
    }

    /**
     * Membuat tombol menu sidebar dengan ikon dan aksi navigasi.
     * @param text Teks yang ditampilkan pada tombol.
     * @param iconFileName Nama file ikon tanpa ekstensi.
     * @param fxmlFile Nama file FXML yang akan dimuat saat tombol ditekan.
     * @param cssFile Nama file CSS tema yang akan diterapkan saat tombol ditekan.
     * @param selectedByDefault Apakah tombol ini harus dipilih secara default saat inisialisasi.
     */
    private void createMenuButton(String text, String iconFileName, String fxmlFile, String cssFile,
            boolean selectedByDefault) {
        ImageView icon = new ImageView();
        try {
            Image image = new Image(getClass().getResourceAsStream("/images/" + iconFileName + ".png"));
            icon.setImage(image);
        } catch (Exception e) {
            System.err.println("Gagal memuat ikon: " + iconFileName + ".png");
        }
        icon.setFitHeight(16);
        icon.setFitWidth(16);

        ToggleButton button = new ToggleButton(text);
        button.setGraphic(icon);
        button.getStyleClass().add("sidebar-button");
        button.setToggleGroup(menuToggleGroup);

        button.setUserData(new String[] { fxmlFile, cssFile });

        button.setOnAction(_ -> {
            String[] data = (String[]) button.getUserData();
            navigateTo(data[0], data[1]);
        });

        sidebarMenuBox.getChildren().add(button);

        if (selectedByDefault) {
            navigateTo(fxmlFile, cssFile);
        }
    }

    /**
     * Menangani aksi klik pada tombol "Beranda" di sidebar.
     * Ini akan memuat tampilan beranda sesuai dengan peran pengguna.
     */
    @FXML
    void handleLogout(ActionEvent event) throws IOException {
        Stage currentStage = (Stage) mainPane.getScene().getWindow();
        Parent loginRoot = FXMLLoader.load(getClass().getResource("/com/perpustakaan/view/LoginView.fxml"));

        Scene loginScene = new Scene(loginRoot, 960, 560); 
        
        String baseCss = getClass().getResource("/com/perpustakaan/view/base.css").toExternalForm();
        String themeCss = getClass().getResource("/com/perpustakaan/view/theme-login.css").toExternalForm();
        loginScene.getStylesheets().addAll(baseCss, themeCss);

        currentStage.setScene(loginScene);
        currentStage.setTitle("Sistem Informasi Perpustakaan");
        currentStage.setResizable(false); 
        currentStage.centerOnScreen();
    }

    /**
     * Navigasi ke tampilan baru berdasarkan nama file FXML dan tema yang diberikan.
     * Jika tema berubah, stylesheet lama akan dihapus dan yang baru ditambahkan.
     * @param fxmlFile Nama file FXML yang akan dimuat.
     * @param newThemeFile Nama file tema CSS yang akan diterapkan.
     */
    public void navigateTo(String fxmlFile, String newThemeFile) {
        Scene scene = mainPane.getScene();
        if (scene != null && this.currentThemeFile != null && !this.currentThemeFile.equals(newThemeFile)) {
            String oldThemePath = getClass().getResource("/com/perpustakaan/view/" + this.currentThemeFile)
                    .toExternalForm();
            scene.getStylesheets().remove(oldThemePath);

            String newThemePath = getClass().getResource("/com/perpustakaan/view/" + newThemeFile).toExternalForm();
            scene.getStylesheets().add(newThemePath);

            this.currentThemeFile = newThemeFile;
        }

        loadView(fxmlFile);
        for (Node node : sidebarMenuBox.getChildren()) {
            if (node instanceof ToggleButton) {
                ToggleButton btn = (ToggleButton) node;
                String[] data = (String[]) btn.getUserData();
                btn.setSelected(fxmlFile.equals(data[0]));
            }
        }
    }

    /**
     * Memuat tampilan berdasarkan nama file FXML yang diberikan.
     * Jika file tidak ditemukan, akan menampilkan pesan di area konten.
     * @param fxmlFileName Nama file FXML yang akan dimuat.
     */
    private void loadView(String fxmlFileName) {
        try {
            URL fxmlUrl = getClass().getResource("/com/perpustakaan/view/" + fxmlFileName);
            if (fxmlUrl == null) {
                contentArea.getChildren().setAll(new Label("View untuk " + fxmlFileName + " belum dibuat."));
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent view = loader.load();
            Object controller = loader.getController();

            if (controller instanceof WelcomeController) {
                ((WelcomeController) controller).initData(currentUsername, this);
            } else if (controller instanceof BerandaMemberController) {
                ((BerandaMemberController) controller).initData(currentUsername, this);
            } else if (controller instanceof KatalogBukuController) {
                ((KatalogBukuController) controller).initData(currentUsername);
            } else if (controller instanceof BukuSayaController) {
                ((BukuSayaController) controller).initData(currentUsername, this);
            } else if (controller instanceof ProfilController) {
                ((ProfilController) controller).initData(currentUsername);
            }

            contentArea.getChildren().setAll(view);
            animateView(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Menerapkan animasi fade-in pada node yang diberikan.
     * Animasi ini akan membuat node muncul secara bertahap.
     * @param node Node yang akan dianimasikan.
     */
    private void animateView(Parent node) {
        FadeTransition ft = new FadeTransition(Duration.millis(350), node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }
}