package com.perpustakaan.controller;

import com.perpustakaan.dao.BookDAO;
import com.perpustakaan.dao.MemberDAO;
import com.perpustakaan.dao.TransactionDAO;
import com.perpustakaan.model.Book;
import com.perpustakaan.model.Member;
import com.perpustakaan.model.Transaction;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class LaporanController {

    @FXML
    private DatePicker dariTanggalPicker;
    @FXML
    private DatePicker sampaiTanggalPicker;
    @FXML
    private CheckBox pinjamCheckBox;
    @FXML
    private CheckBox kembaliCheckBox;
    @FXML
    private TextField searchField;
    @FXML
    private Label peminjamanLabel;
    @FXML
    private Label pengembalianLabel;
    @FXML
    private Label terlambatLabel;
    @FXML
    private Label dendaLabel;
    @FXML
    private ListView<Transaction> aktivitasListView;

    private BookDAO bookDAO;
    private MemberDAO memberDAO;
    private TransactionDAO transactionDAO;

    /**
     * Konstruktor untuk inisialisasi DAO.
     * Biasanya dipanggil oleh JavaFX saat controller di-load.
     */
    public LaporanController() {
        this.bookDAO = new BookDAO();
        this.memberDAO = new MemberDAO();
        this.transactionDAO = new TransactionDAO();
    }

    /**
     * Metode ini dijalankan otomatis oleh JavaFX saat controller diinisialisasi.
     * Digunakan untuk setup awal seperti mengatur cell factory ListView dan menerapkan filter awal.
     */
    @FXML
    public void initialize() {
        setupListViewCellFactory();
        handleTerapkanFilter();

        ImageView placeholderIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/icon_chart.png"))); 
        placeholderIcon.setFitHeight(48); 
        placeholderIcon.setFitWidth(48);
        placeholderIcon.setOpacity(0.5);
        Label placeholderLabel = new Label("Tidak ada aktivitas untuk filter ini.");
        VBox placeholder = new VBox(placeholderIcon, placeholderLabel);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setSpacing(10);
        
        aktivitasListView.setPlaceholder(placeholder);
    }

    /**
     * Metode ini dipanggil oleh MainViewController untuk mengirimkan data penting.
     * Biasanya digunakan untuk setup awal atau reset filter.
     */
    @FXML
    private void handleTerapkanFilter() {
        List<Transaction> allTransactions = transactionDAO.getAllTransactions();
        LocalDate startDate = dariTanggalPicker.getValue();
        LocalDate endDate = sampaiTanggalPicker.getValue();
        String searchQuery = searchField.getText().toLowerCase();

        List<Transaction> finalFiltered = allTransactions.stream()
                .filter(t -> (startDate == null || !t.getLoanDate().isBefore(startDate)))
                .filter(t -> (endDate == null || !t.getLoanDate().isAfter(endDate)))
                .filter(t -> (pinjamCheckBox.isSelected() && t.getStatus() == Transaction.Status.DIPINJAM) ||
                        (kembaliCheckBox.isSelected() && t.getStatus() == Transaction.Status.KEMBALI))
                .filter(t -> {
                    if (searchQuery.isEmpty())
                        return true;
                    String memberName = memberDAO.getMemberById(t.getMemberId()).map(Member::getFullName).orElse("")
                            .toLowerCase();
                    String bookTitle = bookDAO.getBookByIsbn(t.getBookIsbn()).map(Book::getTitle).orElse("")
                            .toLowerCase();
                    return memberName.contains(searchQuery) || bookTitle.contains(searchQuery);
                })
                .sorted(Comparator.comparing(Transaction::getLoanDate).reversed())
                .collect(Collectors.toList());

        updateStatistikCards(finalFiltered);
        aktivitasListView.setItems(FXCollections.observableArrayList(finalFiltered));
    }

    /**
     * Metode ini dipanggil oleh tombol reset filter untuk mengembalikan semua filter ke kondisi awal.
     */
    @FXML
    private void handleResetFilter() {
        dariTanggalPicker.setValue(null);
        sampaiTanggalPicker.setValue(null);
        searchField.clear();
        pinjamCheckBox.setSelected(true);
        kembaliCheckBox.setSelected(true);
        handleTerapkanFilter();
    }

    /**
     * Metode ini dipanggil oleh tombol cetak untuk mencetak laporan.
     */
    private void updateStatistikCards(List<Transaction> transactions) {
        long totalPinjam = transactions.stream().filter(t -> t.getStatus() == Transaction.Status.DIPINJAM).count();
        long totalKembali = transactions.stream().filter(t -> t.getStatus() == Transaction.Status.KEMBALI).count();
        long totalTerlambat = transactions.stream()
                .filter(t -> t.getStatus() == Transaction.Status.KEMBALI && t.getReturnDate() != null
                        && t.getReturnDate().isAfter(t.getDueDate()))
                .count();
        long totalDenda = transactions.stream()
                .filter(t -> t.getStatus() == Transaction.Status.KEMBALI && t.getReturnDate() != null
                        && t.getReturnDate().isAfter(t.getDueDate()))
                .mapToLong(t -> ChronoUnit.DAYS.between(t.getDueDate(), t.getReturnDate()) * 1000)
                .sum();

        peminjamanLabel.setText(String.valueOf(totalPinjam));
        pengembalianLabel.setText(String.valueOf(totalKembali));
        terlambatLabel.setText(String.valueOf(totalTerlambat));

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.of("id", "ID"));
        dendaLabel.setText(currencyFormat.format(totalDenda));
    }

    /**
     * Mengatur cell factory untuk ListView agar menampilkan item dengan layout custom.
     * Setiap item akan berupa 'kartu' yang berisi ikon, nama anggota, judul buku, dan tanggal transaksi.
     */
    private void setupListViewCellFactory() {
        aktivitasListView.setCellFactory(_ -> new ListCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy",
                    Locale.of("id", "ID"));

            @Override
            protected void updateItem(Transaction transaction, boolean empty) {
                super.updateItem(transaction, empty);
                if (empty || transaction == null) {
                    setGraphic(null);
                } else {
                    // Membuat 'kartu' custom untuk setiap item
                    HBox cardLayout = new HBox(15);
                    cardLayout.setAlignment(Pos.CENTER_LEFT);

                    ImageView icon = new ImageView();
                    icon.setFitHeight(32);
                    icon.setFitWidth(32);

                    String memberName = memberDAO.getMemberById(transaction.getMemberId()).map(Member::getFullName)
                            .orElse("?");
                    String bookTitle = bookDAO.getBookByIsbn(transaction.getBookIsbn()).map(Book::getTitle).orElse("?");

                    Label mainText = new Label();
                    mainText.setWrapText(true);

                    LocalDate displayDate = transaction.getStatus() == Transaction.Status.KEMBALI
                            && transaction.getReturnDate() != null
                                    ? transaction.getReturnDate()
                                    : transaction.getLoanDate();
                    Label dateText = new Label(displayDate.format(formatter));
                    dateText.getStyleClass().add("page-subtitle");

                    // Logika untuk memilih ikon dan teks berdasarkan status transaksi
                    if (transaction.getStatus() == Transaction.Status.DIPINJAM) {
                        icon.setImage(new Image(getClass().getResourceAsStream("/images/icon_ticket.png")));
                        mainText.setText(memberName + " meminjam buku '" + bookTitle + "'");
                    } else { // Status KEMBALI
                        icon.setImage(new Image(getClass().getResourceAsStream("/images/icon_undo.png")));
                        mainText.setText(memberName + " mengembalikan buku '" + bookTitle + "'");
                    }

                    VBox textContainer = new VBox(mainText, dateText);
                    textContainer.setSpacing(5);

                    cardLayout.getChildren().addAll(icon, textContainer);
                    setGraphic(cardLayout);
                }
            }
        });
    }
}