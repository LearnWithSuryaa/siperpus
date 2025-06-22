package com.perpustakaan.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class PeminjamanDetail {
    private String transactionId;
    private String memberId; // <-- FIELD BARU DITAMBAHKAN
    private String bookIsbn;
    private String judulBuku;
    private LocalDate tanggalPinjam;
    private LocalDate tanggalKembali; // Due date
    private long terlambatHari;
    private long denda;

    public PeminjamanDetail(Transaction transaction, Book book) {
        this.transactionId = transaction.getTransactionId();
        this.memberId = transaction.getMemberId(); // <-- Mengambil ID member dari transaksi
        this.bookIsbn = book.getIsbn();
        this.judulBuku = book.getTitle();
        this.tanggalPinjam = transaction.getLoanDate();
        this.tanggalKembali = transaction.getDueDate();
        
        if (LocalDate.now().isAfter(transaction.getDueDate())) {
            this.terlambatHari = ChronoUnit.DAYS.between(transaction.getDueDate(), LocalDate.now());
            this.denda = this.terlambatHari * 1000;
        } else {
            this.terlambatHari = 0;
            this.denda = 0;
        }
    }

    // Getters
    public String getTransactionId() { return transactionId; }
    public String getMemberId() { return memberId; } // <-- GETTER BARU DITAMBAHKAN
    public String getBookIsbn() { return bookIsbn; }
    public String getJudulBuku() { return judulBuku; }
    public LocalDate getTanggalPinjam() { return tanggalPinjam; }
    public LocalDate getTanggalKembali() { return tanggalKembali; }
    public String getTerlambatHari() { return terlambatHari > 0 ? terlambatHari + " hari" : "-"; }
    public String getDenda() { return denda > 0 ? "Rp " + denda : "-"; }
}