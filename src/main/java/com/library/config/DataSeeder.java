package com.library.config;

import com.library.model.User;
import com.library.service.BookService;
import com.library.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner initData(UserService userService, BookService bookService) {
        return args -> {
            try {
                // If we can find the admin, the database is already seeded.
                userService.findByEmail("admin@library.com");
            } catch (RuntimeException e) {
                // User not found, which means database is completely empty. Let's seed it!
                System.out.println("Empty database detected. Seeding initial data...");

                // 1. Create a default Librarian
                userService.registerUser("Librarian Admin", "admin@library.com", "admin123", User.Role.LIBRARIAN);
                
                // 2. Create a default Member
                userService.registerUser("Onkar Palkrutwar", "onkar@example.com", "password", User.Role.MEMBER);

                // 3. Add some initial book inventory
                bookService.addBook("The Great Gatsby", "F. Scott Fitzgerald", 3);
                bookService.addBook("1984", "George Orwell", 5);
                bookService.addBook("To Kill a Mockingbird", "Harper Lee", 2);
                bookService.addBook("Pride and Prejudice", "Jane Austen", 4);
                bookService.addBook("The Hobbit", "J.R.R. Tolkien", 1);

                System.out.println("==================================================");
                System.out.println("Database successfully seeded with books & users!");
                System.out.println("Librarian login : admin@library.com | pw: admin123");
                System.out.println("Member login    : onkar@example.com  | pw: password");
                System.out.println("==================================================");
            }
        };
    }
}
