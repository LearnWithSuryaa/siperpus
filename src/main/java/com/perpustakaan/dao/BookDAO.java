package com.perpustakaan.dao;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import com.perpustakaan.model.Book;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * BookDAO (Data Access Object) bertanggung jawab untuk semua operasi
 * yang berkaitan dengan data buku, seperti membaca dari dan menulis ke books.csv.
 * Ini mengimplementasikan fungsionalitas CRUD (Create, Read, Update, Delete).
 */
public class BookDAO {
    // Path ke file CSV yang menyimpan data buku
    private static final String CSV_FILE_PATH = "data/books.csv";

    /**
     * Mengambil semua buku dari file CSV.
     * @return sebuah List yang berisi semua objek Book.
     */
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(CSV_FILE_PATH))) {
            reader.skip(1); // Lewati baris header
            List<String[]> records = reader.readAll();
            for (String[] record : records) {
                Book book = new Book(
                    record[0], // isbn
                    record[1], // title
                    record[2], // author
                    record[3], // category
                    record[4]  // status
                );
                books.add(book);
            }
        } catch (IOException | CsvException e) {
            System.err.println("Error membaca file CSV buku: " + e.getMessage());
        }
        return books;
    }

    /**
     * Mencari buku berdasarkan ISBN.
     * @param isbn ISBN buku yang dicari.
     * @return Optional berisi objek Book jika ditemukan, atau kosong jika tidak ditemukan.
     */
    public Optional<Book> getBookByIsbn(String isbn) {
        return getAllBooks().stream()
                .filter(book -> book.getIsbn().equals(isbn))
                .findFirst();
    }

    /**
     * Menambahkan buku baru ke dalam file CSV.
     * @param newBook Objek Book yang akan ditambahkan.
     */
    public void addBook(Book newBook) {
        List<Book> books = getAllBooks();
        books.add(newBook);
        writeAllBooks(books);
    }

    /**
     * Memperbarui data buku yang sudah ada berdasarkan ISBN.
     * @param updatedBook Objek Book dengan data yang diperbarui.
     */
    public void updateBook(Book updatedBook) {
        List<Book> books = getAllBooks();
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getIsbn().equals(updatedBook.getIsbn())) {
                books.set(i, updatedBook);
                break;
            }
        }
        writeAllBooks(books);
    }

    /**
     * Menghapus sebuah buku dari file CSV berdasarkan ISBN.
     * @param isbn ISBN dari buku yang akan dihapus.
     */
    public void deleteBook(String isbn) {
        List<Book> books = getAllBooks();
        books.removeIf(book -> book.getIsbn().equals(isbn));
        writeAllBooks(books);
    }

    /**
     * Metode private untuk menulis ulang seluruh list buku ke file CSV.
     * Ini adalah inti dari operasi Create, Update, dan Delete.
     * @param books List lengkap buku yang akan ditulis ke file.
     */
    private void writeAllBooks(List<Book> books) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE_PATH))) {
            // Tulis header terlebih dahulu
            String[] header = {"isbn", "title", "author", "category", "status"};
            writer.writeNext(header);

            // Tulis data setiap buku
            for (Book book : books) {
                String[] record = {
                    book.getIsbn(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getCategory(),
                    book.getStatus()
                };
                writer.writeNext(record);
            }
        } catch (IOException e) {
            System.err.println("Error menulis ke file CSV buku: " + e.getMessage());
        }
    }
}