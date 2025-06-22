package com.perpustakaan.dao;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import com.perpustakaan.model.Transaction;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    // File path untuk menyimpan data transaksi
    private static final String CSV_FILE_PATH = "data/transactions.csv";
    // Format tanggal yang digunakan dalam file CSV
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Membaca semua transaksi dari file CSV.
     */
    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(CSV_FILE_PATH))) {
            reader.skip(1); // Lewati header
            List<String[]> records = reader.readAll();
            for (String[] record : records) {
                Transaction transaction = new Transaction(
                        record[0],
                        record[1],
                        record[2],
                        LocalDate.parse(record[3], DATE_FORMATTER),
                        LocalDate.parse(record[4], DATE_FORMATTER),
                        record[5].isEmpty() ? null : LocalDate.parse(record[5], DATE_FORMATTER),
                        Transaction.Status.valueOf(record[6]));
                transactions.add(transaction);
            }
        } catch (IOException | CsvException e) {
            System.err.println("Error membaca file CSV transaksi: " + e.getMessage());
        }
        return transactions;
    }

    /**
     * Mencari transaksi berdasarkan ID transaksi.
     */
    public void addTransaction(Transaction newTransaction) {
        List<Transaction> transactions = getAllTransactions();
        transactions.add(newTransaction);
        writeAllTransactions(transactions);
    }

    /**
     * Menghapus transaksi berdasarkan ID transaksi.
     */
    public void updateTransaction(Transaction updatedTransaction) {
        List<Transaction> transactions = getAllTransactions();
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getTransactionId().equals(updatedTransaction.getTransactionId())) {
                transactions.set(i, updatedTransaction);
                break;
            }
        }
        writeAllTransactions(transactions);
    }

    /**
     * Menghapus transaksi berdasarkan ID transaksi.
     */
    private void writeAllTransactions(List<Transaction> transactions) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE_PATH))) {
            String[] header = { "transactionId", "memberId", "bookIsbn", "loanDate", "dueDate", "returnDate",
                    "status" };
            writer.writeNext(header);

            for (Transaction transaction : transactions) {
                String[] record = {
                        transaction.getTransactionId(),
                        transaction.getMemberId(),
                        transaction.getBookIsbn(),
                        transaction.getLoanDate().format(DATE_FORMATTER),
                        transaction.getDueDate().format(DATE_FORMATTER),
                        transaction.getReturnDate() == null ? "" : transaction.getReturnDate().format(DATE_FORMATTER),
                        transaction.getStatus().name()
                };
                writer.writeNext(record);
            }
        } catch (IOException e) {
            System.err.println("Error menulis ke file CSV transaksi: " + e.getMessage());
        }
    }
}