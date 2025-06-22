package com.perpustakaan.controller;

import com.perpustakaan.dao.BookDAO;
import com.perpustakaan.dao.MemberDAO;
import com.perpustakaan.dao.TransactionDAO;
import com.perpustakaan.model.Book;
import com.perpustakaan.model.Member;
import com.perpustakaan.model.Transaction;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List; 
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BerandaMemberController {

    @FXML private Label welcomeLabel;
    @FXML private Label subtitleLabel;
    @FXML private ListView<Transaction> pinjamanAktifListView;
    @FXML private ListView<String> bukuPopulerListView;

    private MainViewController mainViewController;
    private MemberDAO memberDAO;
    private BookDAO bookDAO;
    private TransactionDAO transactionDAO;
    private String currentMemberId;

    /**
     * Konstruktor untuk inisialisasi DAO.
     * Biasanya dipanggil oleh JavaFX saat controller di-load.
     */
    public BerandaMemberController() {
        this.memberDAO = new MemberDAO();
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

        Optional<Member> memberOpt = memberDAO.getMemberById(memberId);
        memberOpt.ifPresent(member -> welcomeLabel.setText("Halo, " + member.getFullName().split(" ")[0] + "!"));

        loadPinjamanAktif();
        loadBukuPopuler();
    }

    /**
     * Metode initialize() dijalankan otomatis oleh JavaFX.
     * Bisa dibiarkan kosong karena event handler sudah diatur di FXML.
     */
    @FXML
    public void initialize() {
        setupPinjamanAktifListView();
    }

    /**
     * Memuat data peminjaman aktif dan buku populer ke dalam ListView.
     */
    private void loadPinjamanAktif() {
        List<Transaction> activeLoans = transactionDAO.getAllTransactions().stream()
                .filter(t -> t.getMemberId().equals(currentMemberId) && t.getStatus() == Transaction.Status.DIPINJAM)
                .collect(Collectors.toList());

        pinjamanAktifListView.setItems(FXCollections.observableArrayList(activeLoans));
        if (activeLoans.isEmpty()) {
            subtitleLabel.setText("Anda sedang tidak meminjam buku. Ayo cari buku baru!");
        } else {
            subtitleLabel.setText("Berikut adalah daftar buku yang sedang Anda pinjam.");
        }
    }

    /**
     * Memuat daftar buku populer berdasarkan jumlah peminjaman.
     * Hanya menampilkan 5 buku terpopuler.
     */
    private void loadBukuPopuler() {
        Map<String, Long> loanCounts = transactionDAO.getAllTransactions().stream()
                .collect(Collectors.groupingBy(Transaction::getBookIsbn, Collectors.counting()));

        List<String> popularBooks = loanCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> bookDAO.getBookByIsbn(entry.getKey()).map(Book::getTitle).orElse("?"))
                .collect(Collectors.toList());
        
        bukuPopulerListView.setItems(FXCollections.observableArrayList(popularBooks));
    }

    /**
     * Mengatur cell factory untuk ListView pinjamanAktifListView.
     * Menampilkan judul buku dan tanggal jatuh tempo dengan format yang sesuai.
     */
    private void setupPinjamanAktifListView() {
        pinjamanAktifListView.setCellFactory(_ -> new ListCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.of("id", "ID"));

            @Override
            protected void updateItem(Transaction transaction, boolean empty) {
                super.updateItem(transaction, empty);
                if (empty || transaction == null) {
                    setGraphic(null);
                } else {
                    String bookTitle = bookDAO.getBookByIsbn(transaction.getBookIsbn()).map(Book::getTitle).orElse("Judul Tidak Ditemukan");
                    boolean isOverdue = LocalDate.now().isAfter(transaction.getDueDate());

                    Label titleLabel = new Label(bookTitle);
                    titleLabel.getStyleClass().add("pinjaman-title");

                    Label dueDateLabel = new Label("Jatuh tempo: " + transaction.getDueDate().format(formatter));
                    dueDateLabel.getStyleClass().add("pinjaman-due-date");
                    
                    VBox textContainer = new VBox(titleLabel, dueDateLabel);
                    
                    HBox cardLayout = new HBox(textContainer);
                    cardLayout.getStyleClass().add("pinjaman-card");

                    if (isOverdue) {
                        cardLayout.getStyleClass().add("pinjaman-card-overdue");
                    }
                    
                    setGraphic(cardLayout);
                }
            }
        });
    }

    /**
     * Navigasi ke halaman Katalog Buku.
     * Dipanggil saat tombol "Katalog Buku" ditekan.
     */
    @FXML
    private void goToKatalog() {
        if (mainViewController != null) {
            mainViewController.navigateTo("KatalogBukuView.fxml", "theme-dashboard.css");
        }
    }
}