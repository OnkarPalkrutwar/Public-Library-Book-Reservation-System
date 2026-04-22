package com.library.service;

import com.library.model.Book;
import com.library.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public List<Book> searchBooks(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return bookRepository.findAll();
        }
        return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(keyword, keyword);
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
    }

    @Transactional
    public void addBook(String title, String author, int copies) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setAvailableCopies(copies);
        bookRepository.save(book);
    }

    /** Increase copies of an existing book */
    @Transactional
    public void addCopies(Long bookId, int additionalCopies) {
        Book book = getBookById(bookId);
        book.setAvailableCopies(book.getAvailableCopies() + additionalCopies);
        bookRepository.save(book);
    }

    @Transactional
    public void decrementCopies(Book book) {
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);
    }

    @Transactional
    public void incrementCopies(Book book) {
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);
    }
}
