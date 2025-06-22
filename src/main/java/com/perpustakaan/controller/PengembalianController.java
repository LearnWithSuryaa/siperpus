package com.perpustakaan.controller;

import com.perpustakaan.dao.BookDAO;
import com.perpustakaan.dao.MemberDAO;
import com.perpustakaan.dao.TransactionDAO;
import com.perpustakaan.model.Member;
import com.perpustakaan.model.PeminjamanDetail;
import com.perpustakaan.model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class PengembalianController {

    @FXML private TextField searchField;
    @FXML private TableView<PeminjamanDetail> peminjamanTableView;
    @FXML private TableColumn<PeminjamanDetail, String> memberIdColumn;
    @FXML private TableColumn<PeminjamanDetail, String> judulColumn;
    @FXML private TableColumn<PeminjamanDetail, LocalDate> tglKembaliColumn;
    @FXML private TableColumn<PeminjamanDetail, String> dendaColumn;
    @FXML private Button prosesButton;
    @FXML private VBox detailPanel;
    @FXML private Label detailNamaLabel;
    @FXML private Label detailJudulLabel;
    @FXML private Label detailJatuhTempoLabel;
    @FXML private Label dendaDetailLabel;

    private TransactionDAO transactionDAO;
    private BookDAO bookDAO;
    private MemberDAO memberDAO;
    
    // List untuk menampung semua data asli dari peminjaman yang aktif
    private ObservableList<PeminjamanDetail> masterPeminjamanList = FXCollections.observableArrayList();

    /**
     * Konstruktor untuk inisialisasi DAO.
     * Biasanya dipanggil oleh JavaFX saat controller di-load.
     */
    public PengembalianController() {
        this.transactionDAO = new TransactionDAO();
        this.bookDAO = new BookDAO();
        this.memberDAO = new MemberDAO();
    }

    /**
     * Metode ini dijalankan otomatis oleh JavaFX saat controller diinisialisasi.
     * Digunakan untuk setup awal seperti mengatur kolom tabel, memuat data, dan menyiapkan filter.
     */
    @FXML
    public void initialize() {
        showDetailPanel(false);
        setupTableColumns();
        loadActiveLoans(); // Langsung muat data saat halaman dibuka
        setupRealtimeSearchFilter(); // Siapkan filter real-time

        peminjamanTableView.getSelectionModel().selectedItemProperty().addListener(
                (_, _, newSelection) -> showTransaksiDetails(newSelection));
    }

    private void setupTableColumns() {
        memberIdColumn.setCellValueFactory(new PropertyValueFactory<>("memberId"));
        judulColumn.setCellValueFactory(new PropertyValueFactory<>("judulBuku"));
        tglKembaliColumn.setCellValueFactory(new PropertyValueFactory<>("tanggalKembali"));
        dendaColumn.setCellValueFactory(new PropertyValueFactory<>("denda"));
    }

    /**
     * Memuat semua data peminjaman yang aktif ke dalam master list.
     */
    private void loadActiveLoans() {
        masterPeminjamanList.clear();
        List<PeminjamanDetail> details = new ArrayList<>();
        transactionDAO.getAllTransactions().stream()
            .filter(t -> t.getStatus() == Transaction.Status.DIPINJAM)
            .forEach(t -> {
                bookDAO.getBookByIsbn(t.getBookIsbn()).ifPresent(book -> {
                    details.add(new PeminjamanDetail(t, book));
                });
            });
        masterPeminjamanList.setAll(details);
    }

    /**
     * Mengatur filter real-time yang akan menyaring data di tabel saat admin mengetik.
     */
    private void setupRealtimeSearchFilter() {
        // Buat FilteredList yang membungkus data master kita
        FilteredList<PeminjamanDetail> filteredData = new FilteredList<>(masterPeminjamanList, _ -> true);

        // Tambahkan listener ke kolom pencarian
        searchField.textProperty().addListener((_, _, newValue) -> {
            filteredData.setPredicate(peminjaman -> {
                // Jika kolom pencarian kosong, tampilkan semua data
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                // Filter berdasarkan NIM atau Judul Buku
                if (peminjaman.getMemberId().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (peminjaman.getJudulBuku().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false; // Tidak cocok
            });
        });

        // Tampilkan data yang sudah terfilter di tabel
        peminjamanTableView.setItems(filteredData);
    }
    
    /**
     * Menampilkan detail peminjaman yang dipilih di panel detail.
     * Jika tidak ada yang dipilih, panel detail disembunyikan.
     * @param detail Detail peminjaman yang akan ditampilkan.
     */
    private void showTransaksiDetails(PeminjamanDetail detail) {
        if (detail != null) {
            String memberName = memberDAO.getMemberById(detail.getMemberId()).map(Member::getFullName).orElse("?");
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.of("id", "ID"));
            
            detailNamaLabel.setText(memberName + " (" + detail.getMemberId() + ")");
            detailJudulLabel.setText(detail.getJudulBuku());
            detailJatuhTempoLabel.setText(detail.getTanggalKembali().format(formatter));
            dendaDetailLabel.setText(detail.getDenda());

            showDetailPanel(true);
        } else {
            showDetailPanel(false);
        }
    }

    /**
     * Menampilkan atau menyembunyikan panel detail peminjaman.
     * @param show true untuk menampilkan, false untuk menyembunyikan.
     */
    private void showDetailPanel(boolean show) {
        detailPanel.setVisible(show);
        detailPanel.setManaged(show);
        prosesButton.setDisable(!show);
    }

    /**
     * Menangani aksi klik pada tombol "Proses Pengembalian".
     * untuk mengembalikan buku yang dipilih. 
     * Menampilkan konfirmasi sebelum melanjutkan.
     */
    @FXML
    private void handleProsesButton() {
        PeminjamanDetail selected = peminjamanTableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Konfirmasi Pengembalian");
        confirmationAlert.setHeaderText("Proses pengembalian untuk buku: " + selected.getJudulBuku());
        confirmationAlert.setContentText("Denda: " + selected.getDenda() + "\n\nLanjutkan proses?");
        
        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            transactionDAO.getAllTransactions().stream()
                    .filter(t -> t.getTransactionId().equals(selected.getTransactionId())).findFirst()
                    .ifPresent(transaction -> {
                        transaction.setReturnDate(LocalDate.now());
                        transaction.setStatus(Transaction.Status.KEMBALI);
                        transactionDAO.updateTransaction(transaction);
                    });
            bookDAO.getBookByIsbn(selected.getBookIsbn()).ifPresent(book -> {
                book.setStatus("Tersedia");
                bookDAO.updateBook(book);
            });

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Pengembalian Berhasil");
            successAlert.setHeaderText(null);
            successAlert.setContentText("Buku '" + selected.getJudulBuku() + "' telah berhasil dikembalikan.");
            successAlert.showAndWait();

            // Muat ulang data master dan bersihkan panel detail
            loadActiveLoans();
            showDetailPanel(false);
        }
    }
}