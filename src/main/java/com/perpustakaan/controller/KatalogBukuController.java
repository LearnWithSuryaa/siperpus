package com.perpustakaan.controller;

import com.perpustakaan.dao.BookDAO;
import com.perpustakaan.dao.TransactionDAO;
import com.perpustakaan.model.Book;
import com.perpustakaan.model.Transaction;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Collectors;

public class KatalogBukuController {

    @FXML private BorderPane rootBorderPane;
    @FXML private TilePane bookTilePane;
    @FXML private TextField searchField;
    @FXML private ChoiceBox<String> categoryFilterChoiceBox;
    @FXML private ChoiceBox<String> sortChoiceBox;
    @FXML private VBox detailPanel;
    @FXML private Label titleLabel;
    @FXML private Label authorLabel;
    @FXML private ChoiceBox<Integer> durasiChoiceBox;
    @FXML private Label dueDateLabel;
    @FXML private Button borrowButton;
    @FXML private HBox limitWarningBox;

    private BookDAO bookDAO;
    private TransactionDAO transactionDAO;
    private String currentMemberId;
    private static final int MAX_LOANS = 3;
    private boolean isLimitReached = false;
    private Book selectedBook = null;
    
    // List untuk menampung semua buku yang akan ditampilkan
    private ObservableList<Book> allBooksList = FXCollections.observableArrayList();

    // Map untuk mengasosiasikan kategori dengan warna dan ikon
   private static final Map<String, String[]> KATEGORI_STYLE_MAP = new HashMap<>();
    static {
        KATEGORI_STYLE_MAP.put("novel", new String[]{"#2980b9", "icon_cat_book"});
        KATEGORI_STYLE_MAP.put("fiksi", new String[]{"#8e44ad", "icon_cat_fiksi"});
        KATEGORI_STYLE_MAP.put("sains", new String[]{"#27ae60", "icon_cat_sains"});
        KATEGORI_STYLE_MAP.put("sejarah", new String[]{"#d35400", "icon_cat_sejarah"});
        KATEGORI_STYLE_MAP.put("biografi", new String[]{"#7f8c8d", "icon_cat_biografi"});
        KATEGORI_STYLE_MAP.put("bisnis", new String[]{"#16a085", "icon_cat_bisnis"});
        KATEGORI_STYLE_MAP.put("pengembangan diri", new String[]{"#f39c12", "icon_cat_pengembangan"});
        KATEGORI_STYLE_MAP.put("puisi", new String[]{"#c0392b", "icon_cat_puisi"});
        KATEGORI_STYLE_MAP.put("komedi", new String[]{"#e84393", "icon_cat_komedi"});
        KATEGORI_STYLE_MAP.put("default", new String[]{"#34495e", "icon_cat_default"});
    }

    /**
     * Konstruktor untuk inisialisasi DAO.
     * Biasanya dipanggil oleh JavaFX saat controller di-load.
     */
    public KatalogBukuController() {
        bookDAO = new BookDAO();
        transactionDAO = new TransactionDAO();
    }

    /**
     * Metode ini dipanggil oleh MainViewController untuk mengirimkan data penting.
     * @param memberId ID dari anggota yang sedang login.
     */
    public void initData(String memberId) {
        this.currentMemberId = memberId;
        checkLoanLimitAndLoadBooks();
    }

    /**
     * Metode initialize() dijalankan otomatis oleh JavaFX.
     * Digunakan untuk setup awal seperti mengatur pilihan kategori dan sorting.
     */
    @FXML
    public void initialize() {
        rootBorderPane.setRight(null);
        if (limitWarningBox != null) {
            limitWarningBox.setVisible(false);
            limitWarningBox.setManaged(false);
        }
        setupChoiceBox();
        setupFiltersAndSorting();
    }
    
    /**
     * Metode ini dipanggil saat pengguna mengklik tombol "Kembali" di detail buku.
     * Digunakan untuk menyembunyikan panel detail buku.
     */
    private void checkLoanLimitAndLoadBooks() {
        long activeLoans = transactionDAO.getAllTransactions().stream()
                .filter(t -> t.getMemberId().equals(currentMemberId) && t.getStatus() == Transaction.Status.DIPINJAM)
                .count();
        this.isLimitReached = activeLoans >= MAX_LOANS;
        
        if (limitWarningBox != null) {
            limitWarningBox.setVisible(isLimitReached);
            limitWarningBox.setManaged(isLimitReached);
        }
        
        loadAllBooks();
    }
    
    /**
     * Memuat semua buku dari database dan menyiapkan filter serta sorting.
     * Dipanggil saat halaman pertama kali dimuat atau saat filter/sorting diubah.
     */
    private void loadAllBooks() {
        allBooksList.setAll(bookDAO.getAllBooks());
    }
    
    /**
     * Mengatur filter dan sorting untuk daftar buku.
     * Menggunakan FilteredList dan SortedList untuk mengelola data buku.
     */
    private void setupFiltersAndSorting() {
        ObservableList<String> categories = FXCollections.observableArrayList("Semua Kategori");
        categories.addAll(allBooksList.stream().map(Book::getCategory).distinct().sorted().collect(Collectors.toList()));
        categoryFilterChoiceBox.setItems(categories);
        categoryFilterChoiceBox.setValue("Semua Kategori");

        sortChoiceBox.setItems(FXCollections.observableArrayList("Judul (A-Z)", "Penulis (A-Z)"));
        sortChoiceBox.setValue("Judul (A-Z)");

        FilteredList<Book> filteredData = new FilteredList<>(allBooksList, _ -> true); // Menggunakan _
        
        searchField.textProperty().addListener((_, _, _) -> applyFilters(filteredData));
        categoryFilterChoiceBox.getSelectionModel().selectedItemProperty().addListener((_, _, _) -> applyFilters(filteredData));

        SortedList<Book> sortedData = new SortedList<>(filteredData);
        sortChoiceBox.getSelectionModel().selectedItemProperty().addListener((_, _, val) -> {
            if ("Penulis (A-Z)".equals(val)) {
                sortedData.setComparator(Comparator.comparing(Book::getAuthor));
            } else {
                sortedData.setComparator(Comparator.comparing(Book::getTitle));
            }
        });
        sortedData.setComparator(Comparator.comparing(Book::getTitle));
        
        displayBookCards(sortedData);
        sortedData.addListener((javafx.collections.ListChangeListener.Change<? extends Book> _) -> 
            displayBookCards(sortedData));
    }
    
    /**
     * Menerapkan filter berdasarkan teks pencarian dan kategori yang dipilih.
     * @param filteredData FilteredList yang akan diterapkan filter.
     */
    private void applyFilters(FilteredList<Book> filteredData) {
        String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String categoryFilter = categoryFilterChoiceBox.getValue();

        filteredData.setPredicate(book -> {
            boolean textMatch = searchText.isEmpty() || 
                                 book.getTitle().toLowerCase().contains(searchText) || 
                                 book.getAuthor().toLowerCase().contains(searchText);
            
            boolean categoryMatch = "Semua Kategori".equals(categoryFilter) || 
                                     book.getCategory().equalsIgnoreCase(categoryFilter);
            
            return textMatch && categoryMatch;
        });
    }

    /**
     * Menampilkan kartu buku di dalam TilePane.
     * Menggunakan VBox untuk setiap kartu buku yang berisi informasi buku.
     * @param books Daftar buku yang akan ditampilkan.
     */
    private void displayBookCards(List<Book> books) {
        bookTilePane.getChildren().clear();
        for (Book book : books) {
            bookTilePane.getChildren().add(createBookCard(book));
        }
    }

    /**
     * Membuat kartu buku yang berisi informasi buku seperti judul, penulis, dan status.
     * Kartu ini juga memiliki ikon kategori dan warna latar belakang yang sesuai.
     * @param book Objek Book yang akan ditampilkan di kartu.
     * @return VBox yang berisi informasi buku dalam format kartu.
     */
    private VBox createBookCard(Book book) {
        VBox card = new VBox(10);
        card.getStyleClass().add("book-card");
        
        boolean isAvailable = "Tersedia".equalsIgnoreCase(book.getStatus());
        if (!isAvailable) {
            card.getStyleClass().add("book-card-unavailable");
        }
        
        String[] style = KATEGORI_STYLE_MAP.getOrDefault(book.getCategory().toLowerCase(), KATEGORI_STYLE_MAP.get("default"));
        String colorHex = style[0];
        String iconFileName = style[1];

        Region background = new Region();
        background.setStyle("-fx-background-color: " + colorHex + ";");

        ImageView iconView = new ImageView();
        try {
            Image iconImage = new Image(getClass().getResourceAsStream("/images/" + iconFileName + ".png"));
            iconView.setImage(iconImage);
        } catch (Exception e) {
            System.err.println("Gagal memuat ikon kategori: " + iconFileName + ".png");
        }
        iconView.setFitHeight(48);
        iconView.setFitWidth(48);
        iconView.setPreserveRatio(true);

        StackPane coverContainer = new StackPane(background, iconView);
        coverContainer.getStyleClass().add("book-cover-generated");
        
        Label statusBadge = new Label(book.getStatus());
        statusBadge.getStyleClass().add("status-badge");
        statusBadge.getStyleClass().add(isAvailable ? "status-tersedia" : "status-dipinjam");
        
        StackPane finalCover = new StackPane(coverContainer, statusBadge);
        StackPane.setAlignment(statusBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(statusBadge, new Insets(8));
        
        Label titleLabel = new Label(book.getTitle());
        titleLabel.getStyleClass().add("book-card-title");
        titleLabel.setWrapText(true);
        Label authorLabel = new Label(book.getAuthor());
        authorLabel.getStyleClass().add("book-card-author");

        card.getChildren().addAll(finalCover, titleLabel, authorLabel);
        
        card.setOnMouseClicked(_ -> {
            if (isAvailable && !isLimitReached) {
                this.selectedBook = book;
                showBookDetails(book);
            }
        });
        return card;
    }

    /**
     * Mengatur pilihan durasi peminjaman buku.
     * Durasi ini akan digunakan untuk menghitung tanggal jatuh tempo peminjaman.
     */
    private void setupChoiceBox() {
        durasiChoiceBox.setItems(FXCollections.observableArrayList(3, 7, 14, 30));
        durasiChoiceBox.setValue(7);
        durasiChoiceBox.getSelectionModel().selectedItemProperty().addListener((_, _, _) -> updateDueDateLabel());
    }

    /**
     * Menampilkan detail buku yang dipilih di panel detail.
     * Jika buku tidak ditemukan, akan menampilkan pesan error.
     * @param book Objek Book yang akan ditampilkan detailnya.
     */
    private void showBookDetails(Book book) {
        if (book != null) {
            this.titleLabel.setText(book.getTitle());
            this.authorLabel.setText(book.getAuthor());
            borrowButton.setDisable(false);
            updateDueDateLabel();
            
            if (rootBorderPane.getRight() == null) {
                rootBorderPane.setRight(detailPanel);
                animateSidePanel(true);
            }
        }
    }
    
    /**
     * Menyembunyikan panel detail buku jika tidak ada buku yang dipilih.
     * Juga mengatur selectedBook menjadi null.
     */
    private void hideDetailPanel() {
        if (rootBorderPane.getRight() != null) {
            animateSidePanel(false);
        }
        selectedBook = null;
    }
    
    /**
     * Animasi untuk menampilkan atau menyembunyikan panel detail buku.
     * Menggunakan TranslateTransition untuk animasi geser.
     * @param show true untuk menampilkan panel, false untuk menyembunyikan.
     */
    private void animateSidePanel(boolean show) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(350), detailPanel);
        if (show) {
            tt.setFromX(detailPanel.getWidth() > 0 ? detailPanel.getWidth() : 300);
            tt.setToX(0);
        } else {
            tt.setFromX(0);
            tt.setToX(detailPanel.getWidth());
            tt.setOnFinished(_ -> rootBorderPane.setRight(null)); // Menggunakan _
        }
        tt.play();
    }

    /**
     * Mengupdate label tanggal jatuh tempo berdasarkan durasi yang dipilih.
     * Tanggal jatuh tempo dihitung dari hari ini ditambah durasi peminjaman.
     */
    private void updateDueDateLabel() {
        if (durasiChoiceBox.getValue() != null) {
            LocalDate dueDate = LocalDate.now().plusDays(durasiChoiceBox.getValue());
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.of("id", "ID"));
            dueDateLabel.setText(dueDate.format(formatter));
        }
    }

    /**
     * Menangani aksi klik pada tombol "Pinjam" untuk meminjam buku yang dipilih.
     * Akan membuat transaksi baru dan memperbarui status buku.
     * Jika tidak ada buku yang dipilih atau durasi tidak dipilih, akan mengabaikan aksi ini.
     */
    @FXML
    private void handleBorrowButton() {
        if (selectedBook == null || durasiChoiceBox.getValue() == null) return;
        Transaction newTransaction = new Transaction(UUID.randomUUID().toString(), currentMemberId, selectedBook.getIsbn(), LocalDate.now(), LocalDate.now().plusDays(durasiChoiceBox.getValue()), null, Transaction.Status.DIPINJAM);
        transactionDAO.addTransaction(newTransaction);
        selectedBook.setStatus("Dipinjam");
        bookDAO.updateBook(selectedBook);
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Anda berhasil meminjam buku: " + selectedBook.getTitle(), ButtonType.OK);
        alert.setTitle("Peminjaman Berhasil");
        alert.setHeaderText("Harap kembalikan sebelum: " + dueDateLabel.getText());
        alert.showAndWait();
        hideDetailPanel();
        checkLoanLimitAndLoadBooks();
    }
}