package com.library.service;

import com.library.model.Book;
import com.library.model.Reservation;
import com.library.model.User;
import com.library.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookService bookService;

    public ReservationService(ReservationRepository reservationRepository, BookService bookService) {
        this.reservationRepository = reservationRepository;
        this.bookService = bookService;
    }

    /** Member borrows a book — creates an ACTIVE reservation with a selected due date */
    @Transactional
    public void borrowBook(User user, Long bookId, LocalDate dueDate) {
        Book book = bookService.getBookById(bookId);
        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("No copies available");
        }
        if (dueDate == null || !dueDate.isAfter(LocalDate.now())) {
            throw new RuntimeException("Due date must be a future date");
        }

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setBook(book);
        reservation.setBorrowDate(LocalDate.now());
        reservation.setDueDate(dueDate);
        reservation.setStatus(Reservation.Status.ACTIVE);
        reservationRepository.save(reservation);

        bookService.decrementCopies(book);
    }

    public List<Reservation> getUserReservations(User user) {
        return reservationRepository.findByUserOrderByBorrowDateDesc(user);
    }

    /** Get all ACTIVE + OVERDUE reservations (the librarian's return queue) */
    public List<Reservation> getActiveReservations() {
        List<Reservation> active = reservationRepository.findByStatus(Reservation.Status.ACTIVE);
        List<Reservation> overdue = reservationRepository.findByStatus(Reservation.Status.OVERDUE);
        active.addAll(overdue);
        return active;
    }

    /** Librarian marks a reservation as returned */
    @Transactional
    public void markReturned(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        reservation.setStatus(Reservation.Status.RETURNED);
        reservationRepository.save(reservation);

        // Restore the book copy
        bookService.incrementCopies(reservation.getBook());
    }

    /** Used by the scheduler to find overdue reservations */
    public List<Reservation> findOverdueReservations() {
        return reservationRepository.findByStatusAndDueDateBefore(
                Reservation.Status.ACTIVE, LocalDate.now());
    }

    @Transactional
    public void markOverdue(Reservation reservation) {
        reservation.setStatus(Reservation.Status.OVERDUE);
        reservationRepository.save(reservation);
    }
}
