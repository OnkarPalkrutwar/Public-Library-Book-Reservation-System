package com.library.controller;

import com.library.model.Notification;
import com.library.model.Reservation;
import com.library.model.User;
import com.library.service.NotificationService;
import com.library.service.ReservationService;
import com.library.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/member")
public class ReservationController {

    private final ReservationService reservationService;
    private final NotificationService notificationService;
    private final UserService userService;

    public ReservationController(ReservationService reservationService,
                                 NotificationService notificationService,
                                 UserService userService) {
        this.reservationService = reservationService;
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername());
        List<Notification> notifications = notificationService.getUserNotifications(user);
        List<Reservation> reservations = reservationService.getUserReservations(user);

        // Count active and overdue for the dashboard summary
        long activeCount = reservations.stream()
                .filter(r -> r.getStatus() == Reservation.Status.ACTIVE).count();
        long overdueCount = reservations.stream()
                .filter(r -> r.getStatus() == Reservation.Status.OVERDUE).count();

        model.addAttribute("user", user);
        model.addAttribute("notifications", notifications);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("overdueCount", overdueCount);
        model.addAttribute("totalReservations", reservations.size());
        return "member/dashboard";
    }

    @GetMapping("/reservations")
    public String myReservations(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername());
        List<Reservation> reservations = reservationService.getUserReservations(user);
        model.addAttribute("reservations", reservations);
        return "member/myReservations";
    }

    @PostMapping("/borrow")
    public String borrowBook(@AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam Long bookId,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            reservationService.borrowBook(user, bookId, dueDate);
            redirectAttributes.addFlashAttribute("success", "Book borrowed successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/member/catalogue";
    }
}
