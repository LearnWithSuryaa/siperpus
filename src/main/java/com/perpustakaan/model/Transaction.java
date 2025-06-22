package com.perpustakaan.model;

import java.time.LocalDate;

public class Transaction {
    private String transactionId;
    private String memberId;
    private String bookIsbn;
    private LocalDate loanDate; // Tanggal Pinjam
    private LocalDate dueDate; // Tanggal Jatuh Tempo
    private LocalDate returnDate; // Tanggal Kembali
    private Status status;

    public enum Status {
        DIPINJAM,
        KEMBALI
    }

    // Constructor
    public Transaction(String transactionId, String memberId, String bookIsbn, LocalDate loanDate, LocalDate dueDate, LocalDate returnDate, Status status) {
        this.transactionId = transactionId;
        this.memberId = memberId;
        this.bookIsbn = bookIsbn;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    // Getters dan Setters
    public String getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    public String getMemberId() {
        return memberId;
    }
    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }
    public String getBookIsbn() {
        return bookIsbn;
    }
    public void setBookIsbn(String bookIsbn) {
        this.bookIsbn = bookIsbn;
    }
    public LocalDate getLoanDate() {
        return loanDate;
    }
    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }
    public LocalDate getDueDate() {
        return dueDate;
    }
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    public LocalDate getReturnDate() {
        return returnDate;
    }
    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }
    public Status getStatus() {
        return status;
    }
    public void setStatus(Status status) {
        this.status = status;
    }
}