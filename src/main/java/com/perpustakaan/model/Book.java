package com.perpustakaan.model;

/**
 * Kelas ini adalah representasi data (Model) untuk sebuah buku.
 * Strukturnya mencerminkan kolom-kolom yang ada di file books.csv.
 */
public class Book {
    private String isbn;
    private String title;
    private String author;
    private String category;
    private String status; 
     
    public Book(String isbn, String title, String author, String category, String status) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.category = category;
        this.status = status;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Metode toString() berguna untuk debugging.
     * Memungkinkan kita untuk mencetak informasi objek Book dengan mudah.
     */
    @Override
    public String toString() {
        return "Book{" +
                "isbn='" + isbn + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", category='" + category + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}