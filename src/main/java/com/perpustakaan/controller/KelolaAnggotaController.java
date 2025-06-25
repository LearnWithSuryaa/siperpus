package com.perpustakaan.controller;

import com.perpustakaan.dao.MemberDAO;
import com.perpustakaan.model.Member;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class KelolaAnggotaController {

    @FXML private TextField searchField;
    @FXML private TilePane memberTilePane;
    @FXML private VBox formPanel;
    @FXML private Label formTitleLabel;
    @FXML private Button tambahAnggotaButton;
    @FXML private TextField memberIdField;
    @FXML private PasswordField pinField;
    @FXML private TextField fullNameField;
    @FXML private TextField majorField;
    @FXML private TextField emailField;
    @FXML private TextArea alamatField;
    @FXML private TextField teleponField;
    @FXML private RadioButton lakiRadioButton;
    @FXML private RadioButton perempuanRadioButton;
    @FXML private ToggleGroup genderToggleGroup;
    @FXML private Button updateButton;
    @FXML private Button addButton;
    @FXML private Button clearButton;
    @FXML private Button deleteButton;

    private MemberDAO memberDAO;
    private Member selectedMember = null;

    // List untuk menampung semua anggota yang ada di database
    private final ObservableList<Member> masterMemberList = FXCollections.observableArrayList();

    /**
     * Konstruktor untuk inisialisasi DAO.
     * Biasanya dipanggil oleh JavaFX saat controller di-load.
     */
    public KelolaAnggotaController() {
        memberDAO = new MemberDAO();
    }

    /**
     * Metode ini dijalankan otomatis oleh JavaFX saat controller diinisialisasi.
     * Digunakan untuk setup awal seperti menyembunyikan form, memuat data anggota, dan menyiapkan filter pencarian.
     */
    @FXML
    public void initialize() {
        formPanel.setVisible(false);
        formPanel.setManaged(false);
        masterMemberList.setAll(memberDAO.getAllMembers());
        setupRealtimeSearchFilter();
    }

    /**
     * Memuat data anggota dari database dan menampilkan dalam bentuk kartu.
     * Dipanggil saat controller diinisialisasi.
     */
    private void displayMemberCards(List<Member> members) {
        memberTilePane.getChildren().clear();
        for (Member member : members) {
            memberTilePane.getChildren().add(createMemberCard(member));
        }
    }
    
    /**
     * Mengatur filter pencarian real-time pada field pencarian.
     * Setiap perubahan pada field akan memfilter daftar anggota yang ditampilkan.
     */
    private void setupRealtimeSearchFilter() {
        FilteredList<Member> filteredData = new FilteredList<>(masterMemberList, _ -> true);

        searchField.textProperty().addListener((_, _, newValue) -> {
            filteredData.setPredicate(member -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return member.getFullName().toLowerCase().contains(lowerCaseFilter) ||
                       member.getMemberId().toLowerCase().contains(lowerCaseFilter);
            });
        });

        filteredData.addListener((javafx.collections.ListChangeListener.Change<? extends Member> _) -> 
            displayMemberCards(filteredData));

        displayMemberCards(masterMemberList);
    }

    /**
     * Membuat kartu anggota yang akan ditampilkan di TilePane.
     * @param member Anggota yang akan dibuatkan kartu.
     * @return VBox yang berisi informasi anggota dalam bentuk kartu.
     */
    private VBox createMemberCard(Member member) {
        String iconPath = "/images/icon_user_lk.png";
        if ("Perempuan".equalsIgnoreCase(member.getGender())) {
            iconPath = "/images/icon_user_wm.png";
        }
        ImageView avatar = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
        avatar.setFitHeight(64);
        avatar.setFitWidth(64);

        Label nameLabel = new Label(member.getFullName());
        nameLabel.getStyleClass().add("member-card-name");
        
        Label idLabel = new Label(member.getMemberId());
        idLabel.getStyleClass().add("member-card-id");

        VBox card = new VBox(avatar, nameLabel, idLabel);
        card.getStyleClass().add("member-card");
        card.setAlignment(Pos.CENTER);
        
        card.setOnMouseClicked(_ -> {
            selectedMember = member;
            showMemberDetailsForEdit(member);
        });
        
        return card;
    }
    
    /**
     * Menampilkan detail anggota yang dipilih untuk diedit.
     * Mengisi form dengan data anggota yang dipilih.
     * @param member Anggota yang akan ditampilkan detailnya.
     */
    private void showMemberDetailsForEdit(Member member) {
        formTitleLabel.setText("Edit Anggota");
        memberIdField.setText(member.getMemberId());
        memberIdField.setDisable(true);
        pinField.setText(member.getPin());
        fullNameField.setText(member.getFullName());
        majorField.setText(member.getMajor());
        emailField.setText(member.getEmail());
        alamatField.setText(member.getAddres());
        teleponField.setText(member.getPhoneNumber());
        
        if ("Perempuan".equalsIgnoreCase(member.getGender())) {
            perempuanRadioButton.setSelected(true);
        } else {
            lakiRadioButton.setSelected(true);
        }
        
        addButton.setVisible(false);
        updateButton.setVisible(true);
        formPanel.setVisible(true);
        formPanel.setManaged(true);
    }

    /**
     * Menangani aksi klik pada tombol "Tambah Anggota Baru".
     * Mengosongkan form dan menyiapkan untuk input anggota baru.
     */
    @FXML
    private void handleTambahAnggotaButton() {
        selectedMember = null;
        clearForm();
        formTitleLabel.setText("Tambah Anggota Baru");
        memberIdField.setDisable(false);
        
        addButton.setVisible(true);
        updateButton.setVisible(false);
        formPanel.setVisible(true);
        formPanel.setManaged(true);
    }
    
    /**
     * Menangani aksi klik pada tombol "Tambah Anggota".
     * Validasi input dan menambahkan anggota baru ke database.
     */
    @FXML
    private void handleAddButton() {
        if (memberIdField.getText().isEmpty() || pinField.getText().isEmpty() || fullNameField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error Validasi", "ID (NIM), PIN, dan Nama Lengkap tidak boleh kosong.");
            return;
        }
        if (memberDAO.getMemberById(memberIdField.getText()).isPresent()) {
            showAlert(Alert.AlertType.ERROR, "Error", "ID Anggota (NIM) sudah terdaftar.");
            return;
        }
        
        String gender = ((RadioButton) genderToggleGroup.getSelectedToggle()).getText();
        
        Member newMember = new Member(
                memberIdField.getText(), pinField.getText(), fullNameField.getText(),
                majorField.getText(), emailField.getText(), gender,
                alamatField.getText(), teleponField.getText()
        );
        memberDAO.addMember(newMember);
        masterMemberList.setAll(memberDAO.getAllMembers());
        hideAndClearForm();
        showAlert(Alert.AlertType.INFORMATION, "Sukses", "Anggota baru berhasil ditambahkan.");
    }

    /**
     * Menangani aksi klik pada tombol "Update Anggota".
     * Validasi input dan memperbarui data anggota yang dipilih.
     */
    @FXML
    private void handleUpdateButton() {
        if (selectedMember != null) {
            String gender = ((RadioButton) genderToggleGroup.getSelectedToggle()).getText();
            Member updatedMember = new Member(
                    memberIdField.getText(), pinField.getText(), fullNameField.getText(),
                    majorField.getText(), emailField.getText(), gender, 
                    alamatField.getText(), teleponField.getText()
            );
            memberDAO.updateMember(updatedMember);
            masterMemberList.setAll(memberDAO.getAllMembers());
            hideAndClearForm();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data anggota berhasil diperbarui.");
        }
    }

    /**
     * Menangani aksi klik pada tombol "Hapus Anggota".
     * Menampilkan konfirmasi sebelum menghapus anggota yang dipilih.
     */
    @FXML
    private void handleDeleteButton() {
        if (selectedMember != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Konfirmasi Hapus");
            alert.setHeaderText("Hapus Anggota: " + selectedMember.getFullName());
            alert.setContentText("Apakah Anda yakin ingin menghapus anggota ini?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                memberDAO.deleteMember(selectedMember.getMemberId());
                masterMemberList.setAll(memberDAO.getAllMembers());
                hideAndClearForm();
            }
        }
    }

    /**
     * Menangani aksi klik pada tombol "Bersihkan Formulir".
     * Menyembunyikan form dan mengosongkan semua field input.
     */
    @FXML
    private void handleClearButton() {
        hideAndClearForm();
    }
    
    /**
     * Menyembunyikan form dan mengosongkan semua field input.
     * Juga mengatur ulang anggota yang dipilih.
     */
    private void hideAndClearForm() {
        formPanel.setVisible(false);
        formPanel.setManaged(false);
        clearForm();
        selectedMember = null;
    }
    
    /**
     * Mengosongkan semua field input pada form.
     * Juga mengatur ulang status field ID anggota agar bisa diisi lagi.
     */
    private void clearForm() {
        memberIdField.clear();
        pinField.clear();
        fullNameField.clear();
        majorField.clear();
        emailField.clear();
        alamatField.clear();
        teleponField.clear();
        memberIdField.setEditable(true);
        lakiRadioButton.setSelected(true);
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
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/com/perpustakaan/view/theme-dashboard.css").toExternalForm());
        alert.showAndWait();
    }
}