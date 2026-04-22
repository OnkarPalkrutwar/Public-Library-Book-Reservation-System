package com.library.controller;

import com.library.model.Book;
import com.library.model.Reservation;
import com.library.service.BookService;
import com.library.service.ReservationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/librarian")
public class LibrarianController {

    private final BookService bookService;
    private final ReservationService reservationService;

    public LibrarianController(BookService bookService, ReservationService reservationService) {
        this.bookService = bookService;
        this.reservationService = reservationService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Book> books = bookService.getAllBooks();
        List<Reservation> activeReservations = reservationService.getActiveReservations();

        long totalBooks = books.size();
        int totalCopies = books.stream().mapToInt(Book::getAvailableCopies).sum();
        long pendingReturns = activeReservations.size();

        model.addAttribute("totalBooks", totalBooks);
        model.addAttribute("totalCopies", totalCopies);
        model.addAttribute("pendingReturns", pendingReturns);
        return "librarian/dashboard";
    }

    @GetMapping("/addBook")
    public String addBookPage(Model model) {
        List<Book> books = bookService.getAllBooks();
        model.addAttribute("books", books);
        return "librarian/addBook";
    }

    @PostMapping("/addBook")
    public String addBook(@RequestParam String title,
                          @RequestParam String author,
                          @RequestParam int copies,
                          RedirectAttributes redirectAttributes) {
        bookService.addBook(title, author, copies);
        redirectAttributes.addFlashAttribute("success", "Book added successfully!");
        return "redirect:/librarian/addBook";
    }

    @PostMapping("/addCopies")
    public String addCopies(@RequestParam Long bookId,
                            @RequestParam int additionalCopies,
                            RedirectAttributes redirectAttributes) {
        bookService.addCopies(bookId, additionalCopies);
        redirectAttributes.addFlashAttribute("success", "Copies added successfully!");
        return "redirect:/librarian/addBook";
    }

    @GetMapping("/returnQueue")
    public String returnQueue(Model model) {
        List<Reservation> reservations = reservationService.getActiveReservations();
        model.addAttribute("reservations", reservations);
        return "librarian/returnQueue";
    }

    @PostMapping("/markReturned")
    public String markReturned(@RequestParam Long reservationId,
                               RedirectAttributes redirectAttributes) {
        reservationService.markReturned(reservationId);
        redirectAttributes.addFlashAttribute("success", "Book marked as returned!");
        return "redirect:/librarian/returnQueue";
    }
}
