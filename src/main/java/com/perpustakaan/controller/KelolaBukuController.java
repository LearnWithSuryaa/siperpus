package com.perpustakaan.controller;

import com.perpustakaan.dao.BookDAO;
import com.perpustakaan.model.Book;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class KelolaBukuController {

    @FXML private TilePane bookTilePane;
    @FXML private VBox formPanel;
    @FXML private Label formTitleLabel;
    @FXML private TextField isbnField;
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField categoryField;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Button updateButton;
    @FXML private Button addButton;

    private BookDAO bookDAO;
    private Book selectedBook = null;

    // Map untuk menyimpan kategori buku dengan warna dan ikon yang sesuai
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
     * Konstruktor untuk inisialisasi BookDAO.
     */
    public KelolaBukuController() {
        this.bookDAO = new BookDAO();
    }

    /**
     * Metode ini dijalankan otomatis oleh JavaFX saat controller diinisialisasi.
     * Digunakan untuk setup awal seperti memuat data buku, mengatur kategori, dan mengatur event handler.
     */
    @FXML
    public void initialize() {
        formPanel.setVisible(false);
        formPanel.setManaged(false);
        statusComboBox.setItems(FXCollections.observableArrayList("Tersedia", "Dipinjam"));
        loadBookCards();
    }

    /**
     * Memuat semua kartu buku dari database dan menampilkannya di TilePane.
     * Kartu buku dibuat berdasarkan data yang diambil dari BookDAO.
     */
    private void loadBookCards() {
        bookTilePane.getChildren().clear();
        for (Book book : bookDAO.getAllBooks()) {
            bookTilePane.getChildren().add(createBookCard(book));
        }
    }

    /**
     * Membuat kartu buku dengan informasi yang diberikan.
     * Kartu ini berisi gambar, judul, penulis, dan status buku.
     * @param book Objek Book yang berisi informasi buku.
     * @return VBox yang berisi elemen-elemen kartu buku.
     */
    private VBox createBookCard(Book book) {
        VBox card = new VBox(10);
        card.getStyleClass().add("book-card");

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
        if ("Tersedia".equalsIgnoreCase(book.getStatus())) {
            statusBadge.getStyleClass().add("status-tersedia");
        } else {
            statusBadge.getStyleClass().add("status-dipinjam");
        }
        
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
            selectedBook = book;
            showBookDetailsForEdit(book);
        });
        
        return card;
    }
    
    /**
     * Menampilkan detail buku yang dipilih untuk diedit.
     * Mengisi form dengan data buku yang ada dan mengubah judul form.
     * @param book Objek Book yang berisi informasi buku yang akan diedit.
     */
    private void showBookDetailsForEdit(Book book) {
        formTitleLabel.setText("Edit Buku");
        isbnField.setText(book.getIsbn());
        isbnField.setDisable(true);
        titleField.setText(book.getTitle());
        authorField.setText(book.getAuthor());
        categoryField.setText(book.getCategory());
        statusComboBox.setValue(book.getStatus());
        
        addButton.setVisible(false);
        updateButton.setVisible(true);
        formPanel.setVisible(true);
        formPanel.setManaged(true);
    }

    /**
     * Menangani aksi klik pada tombol "Tambah Buku Baru".
     * Mengatur form untuk menambah buku baru dan mengosongkan field input.
     */
    @FXML
    private void handleTambahBukuButton() {
        selectedBook = null;
        clearForm();
        formTitleLabel.setText("Tambah Buku Baru");
        isbnField.setDisable(false);
        
        addButton.setVisible(true);
        updateButton.setVisible(false);
        formPanel.setVisible(true);
        formPanel.setManaged(true);
    }
    
    /**
     * Menangani aksi klik pada tombol "Kembali ke Daftar Buku".
     * Menyembunyikan form dan mengosongkan field input.
     */
    @FXML
    private void handleAddFormButton() {
        if (isbnField.getText().isEmpty() || titleField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error Validasi", "ISBN dan Judul tidak boleh kosong.");
            return;
        }
        if (bookDAO.getBookByIsbn(isbnField.getText()).isPresent()) {
            showAlert(Alert.AlertType.ERROR, "Error", "ISBN sudah terdaftar.");
            return;
        }
        
        Book newBook = new Book(isbnField.getText(), titleField.getText(), authorField.getText(), categoryField.getText(), statusComboBox.getValue());
        bookDAO.addBook(newBook);
        loadBookCards();
        handleClearFormButton();
        showAlert(Alert.AlertType.INFORMATION, "Sukses", "Buku baru berhasil ditambahkan.");
    }

    /**
     * Menangani aksi klik pada tombol "Perbarui Data Buku".
     * Memperbarui data buku yang sudah ada berdasarkan ISBN.
     * Validasi dilakukan untuk memastikan ISBN tidak kosong dan buku dengan ISBN tersebut sudah ada.
     */
    @FXML
    private void handleUpdateFormButton() {
        if (selectedBook != null) {
            Book updatedBook = new Book(isbnField.getText(), titleField.getText(), authorField.getText(), categoryField.getText(), statusComboBox.getValue());
            bookDAO.updateBook(updatedBook);
            loadBookCards();
            handleClearFormButton();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data buku berhasil diperbarui.");
        }
    }

    /**
     * Menangani aksi klik pada tombol "Hapus Buku".
     * Menghapus buku yang dipilih setelah konfirmasi dari pengguna.
     * Jika buku berhasil dihapus, kartu buku akan diperbarui.
     */
    @FXML
    private void handleDeleteButton() {
        if (selectedBook != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Konfirmasi Hapus");
            alert.setHeaderText("Hapus Buku: " + selectedBook.getTitle());
            alert.setContentText("Apakah Anda yakin ingin menghapus buku ini?");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                bookDAO.deleteBook(selectedBook.getIsbn());
                loadBookCards();
                handleClearFormButton();
            }
        }
    }

    /**
     * Menangani aksi klik pada tombol "Bersihkan Formulir".
     * Mengosongkan semua field input dan menyembunyikan form.
     * Juga mengatur selectedBook menjadi null untuk menandakan tidak ada buku yang dipilih.
     */
    @FXML
    private void handleClearFormButton() {
        clearForm();
        formPanel.setVisible(false);
        formPanel.setManaged(false);
        selectedBook = null;
    }
    
    /**
     * Mengosongkan semua field input pada form.
     * Juga mengatur status combo box ke "Tersedia" dan mengizinkan pengeditan ISBN.
     */
    private void clearForm() {
        isbnField.clear();
        titleField.clear();
        authorField.clear();
        categoryField.clear();
        statusComboBox.setValue("Tersedia");
        isbnField.setEditable(true);
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
        alert.showAndWait();
    }
}