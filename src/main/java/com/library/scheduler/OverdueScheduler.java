package com.library.scheduler;

import com.library.model.Reservation;
import com.library.service.NotificationService;
import com.library.service.ReservationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OverdueScheduler {

    private final ReservationService reservationService;
    private final NotificationService notificationService;

    public OverdueScheduler(ReservationService reservationService,
                            NotificationService notificationService) {
        this.reservationService = reservationService;
        this.notificationService = notificationService;
    }

    /**
     * Runs daily at midnight.
     * Finds all ACTIVE reservations past their due date,
     * marks them OVERDUE, and creates a notification for the member.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void checkOverdueReservations() {
        List<Reservation> overdueList = reservationService.findOverdueReservations();

        for (Reservation reservation : overdueList) {
            reservationService.markOverdue(reservation);

            String message = "Your reservation for \"" + reservation.getBook().getTitle()
                    + "\" is overdue! It was due on " + reservation.getDueDate() + ". "
                    + "Please return it as soon as possible.";

            notificationService.createNotification(reservation.getUser(), message);
        }

        if (!overdueList.isEmpty()) {
            System.out.println("Overdue scheduler: marked " + overdueList.size() + " reservation(s) as OVERDUE.");
        }
    }
}
