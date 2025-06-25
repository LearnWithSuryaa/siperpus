package com.perpustakaan.controller;

import com.perpustakaan.dao.BookDAO;
import com.perpustakaan.dao.TransactionDAO;
import com.perpustakaan.model.PeminjamanDetail;
import com.perpustakaan.model.Transaction;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class BukuSayaController {

    @FXML private Label welcomeLabel;
    @FXML private Label subtitleLabel;
    @FXML private ListView<PeminjamanDetail> pinjamanListView;

    private MainViewController mainViewController;
    private BookDAO bookDAO;
    private TransactionDAO transactionDAO;
    private String currentMemberId;

    /**
     * Konstruktor untuk inisialisasi DAO.
     */
    public BukuSayaController() {
        this.bookDAO = new BookDAO();
        this.transactionDAO = new TransactionDAO();
    }
    
    /**
     * Metode ini dipanggil oleh MainViewController untuk mengirimkan data penting.
     * @param memberId ID anggota yang sedang login.
     * @param mainController Referensi ke MainViewController untuk navigasi.
     */
    public void initData(String memberId, MainViewController mainController) {
        this.currentMemberId = memberId;
        this.mainViewController = mainController;
        welcomeLabel.setText("Buku Pinjaman Saya");
        loadPeminjamanData();
    }

    /**
     * Metode initialize() dijalankan otomatis oleh JavaFX.
     * Digunakan untuk setup awal seperti mengatur ListView dan memuat data.
     */
    @FXML
    public void initialize() {
        setupPinjamanListView();
    }

    /**
     * Memuat data peminjaman buku yang sedang aktif untuk anggota saat ini.
     * Data akan diambil dari DAO dan ditampilkan di ListView.
     */
    private void loadPeminjamanData() {
        List<PeminjamanDetail> details = transactionDAO.getAllTransactions().stream()
            .filter(t -> t.getMemberId().equals(currentMemberId) && t.getStatus() == Transaction.Status.DIPINJAM)
            .sorted(Comparator.comparing(Transaction::getDueDate))
            .map(t -> bookDAO.getBookByIsbn(t.getBookIsbn()).map(b -> new PeminjamanDetail(t, b)).orElse(null))
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toList());

        pinjamanListView.setItems(FXCollections.observableArrayList(details));

        if (details.isEmpty()) {
            subtitleLabel.setText("Anda sedang tidak meminjam buku. Ayo cari buku baru!");
            pinjamanListView.setPlaceholder(new Label("Tidak ada pinjaman aktif."));
        } else {
            subtitleLabel.setText("Berikut adalah daftar buku yang sedang Anda pinjam.");
        }
    }
    
    /**
     * Mengatur cell factory untuk ListView agar menampilkan detail peminjaman dengan format yang baik.
     * Setiap item akan menampilkan judul buku, tanggal jatuh tempo, dan status pinjaman.
     */
    private void setupPinjamanListView() {
        pinjamanListView.setCellFactory(_ -> new ListCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.of("id", "ID"));

            @Override
            protected void updateItem(PeminjamanDetail detail, boolean empty) {
                super.updateItem(detail, empty);
                if (empty || detail == null) {
                    setGraphic(null);
                } else {
                    ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/images/icon_book.png")));
                    icon.setFitHeight(48);
                    icon.setFitWidth(48);

                    Label titleLabel = new Label(detail.getJudulBuku());
                    titleLabel.getStyleClass().add("pinjaman-title");

                    Label dueDateLabel = new Label("Jatuh tempo: " + detail.getTanggalKembali().format(formatter));
                    dueDateLabel.getStyleClass().add("pinjaman-due-date");
                    
                    VBox textContainer = new VBox(titleLabel, dueDateLabel);
                    
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Label statusLabel = new Label();
                    statusLabel.getStyleClass().add("pinjaman-card-status");
                    
                    HBox cardLayout = new HBox(icon, textContainer, spacer, statusLabel);
                    cardLayout.setAlignment(Pos.CENTER_LEFT);
                    cardLayout.setSpacing(15);
                    cardLayout.getStyleClass().add("pinjaman-card");

                    boolean isOverdue = LocalDate.now().isAfter(detail.getTanggalKembali());
                    getStyleClass().remove("pinjaman-card-overdue");
                    if (isOverdue) {
                        statusLabel.setText("TERLAMBAT");
                        getStyleClass().add("pinjaman-card-overdue");
                    } else {
                        statusLabel.setText("Aman");
                    }
                    
                    setGraphic(cardLayout);
                }
            }
        });
    }

    /**
     * Metode ini dipanggil dari FXML saat tombol "Katalog Buku" di-klik.
     * Navigasi ke halaman katalog buku.
     */
    @FXML
    private void goToKatalog() {
        if (mainViewController != null) {
            mainViewController.navigateTo("KatalogBukuView.fxml", "theme-dashboard.css");
        }
    }
}