package com.perpustakaan.controller;

import com.perpustakaan.dao.BookDAO;
import com.perpustakaan.dao.MemberDAO;
import com.perpustakaan.dao.TransactionDAO;
import com.perpustakaan.model.Book;
import com.perpustakaan.model.Member;
import com.perpustakaan.model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

public class WelcomeController {
    @FXML private Label welcomeLabel;
    @FXML private VBox kelolaBukuCard;
    @FXML private VBox kelolaAnggotaCard;
    @FXML private VBox pengembalianCard;
    @FXML private ListView<String> aktivitasListView;
    @FXML private ListView<String> perhatianListView;

    private MainViewController mainViewController;
    private TransactionDAO transactionDAO;
    private MemberDAO memberDAO;
    private BookDAO bookDAO;

    public WelcomeController() {
        this.transactionDAO = new TransactionDAO();
        this.memberDAO = new MemberDAO();
        this.bookDAO = new BookDAO();
    }
    
    /**
     * Metode ini dipanggil oleh MainViewController untuk mengirimkan data penting.
     * @param username Nama user yang login.
     * @param mainController Referensi ke MainViewController untuk navigasi.
     */
    public void initData(String username, MainViewController mainController) {
        this.mainViewController = mainController;
        welcomeLabel.setText("Selamat Datang, " + username + "!");
        
        // Muat data dinamis ke dalam ListView
        loadAktivitasTerbaru();
        loadPerluPerhatian();
    }

    /**
     * Metode initialize() dijalankan otomatis oleh JavaFX.
     * Bisa dibiarkan kosong karena event handler sudah diatur di FXML.
     */
    @FXML
    public void initialize() {
    }

    /**
     * Memuat 5 aktivitas transaksi terakhir ke dalam ListView.
     */
    private void loadAktivitasTerbaru() {
        ObservableList<String> items = FXCollections.observableArrayList();
        transactionDAO.getAllTransactions().stream()
            .sorted(Comparator.comparing(Transaction::getLoanDate).reversed()) // Urutkan dari yang terbaru
            .limit(5) // Ambil 5 teratas
            .forEach(t -> {
                String memberName = memberDAO.getMemberById(t.getMemberId()).map(Member::getFullName).orElse("?");
                String bookTitle = bookDAO.getBookByIsbn(t.getBookIsbn()).map(Book::getTitle).orElse("?");
                String status = t.getStatus() == Transaction.Status.DIPINJAM ? "meminjam" : "mengembalikan";
                items.add(memberName + " " + status + " '" + bookTitle + "'");
            });
        aktivitasListView.setItems(items);
    }

    /**
     * Memuat daftar buku yang terlambat ke dalam ListView.
     */
    private void loadPerluPerhatian() {
        ObservableList<String> items = FXCollections.observableArrayList();
        transactionDAO.getAllTransactions().stream()
            .filter(t -> t.getStatus() == Transaction.Status.DIPINJAM && LocalDate.now().isAfter(t.getDueDate()))
            .forEach(t -> {
                long lateDays = ChronoUnit.DAYS.between(t.getDueDate(), LocalDate.now());
                String bookTitle = bookDAO.getBookByIsbn(t.getBookIsbn()).map(Book::getTitle).orElse("?");
                items.add("'" + bookTitle + "' terlambat " + lateDays + " hari");
            });

        if (items.isEmpty()) {
            // Tampilkan pesan jika tidak ada yang terlambat
            perhatianListView.setPlaceholder(new Label("Tidak ada buku yang terlambat. Kerja bagus!"));
        } else {
            perhatianListView.setItems(items);
        }

        // Memberi warna merah pada setiap item di daftar "Perlu Perhatian"
        perhatianListView.setCellFactory(_ -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("overdue");
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    getStyleClass().add("overdue");
                }
            }
        });
    }

    /**
     * Metode-metode ini dipanggil dari FXML saat kartu aksi di-klik.
     * Anotasi @FXML wajib ada agar FXML bisa "melihat" metode ini.
     */
    @FXML
    private void goToKelolaBuku(MouseEvent event) {
        if (mainViewController != null) {
            mainViewController.navigateTo("KelolaBukuView.fxml", "theme-dashboard.css");
        }
    }
    
    @FXML
    private void goToKelolaAnggota(MouseEvent event) {
        if (mainViewController != null) {
            mainViewController.navigateTo("KelolaAnggotaView.fxml", "theme-dashboard.css");
        }
    }

    @FXML
    private void goToPengembalian(MouseEvent event) {
        if (mainViewController != null) {
            mainViewController.navigateTo("PengembalianView.fxml", "theme-dashboard.css");
        }
    }
}