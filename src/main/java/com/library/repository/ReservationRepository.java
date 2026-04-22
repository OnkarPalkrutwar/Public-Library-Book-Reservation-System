package com.library.repository;

import com.library.model.Reservation;
import com.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserOrderByBorrowDateDesc(User user);

    List<Reservation> findByStatus(Reservation.Status status);

    // For the scheduler: find ACTIVE reservations whose due date has passed
    List<Reservation> findByStatusAndDueDateBefore(Reservation.Status status, LocalDate date);
}
