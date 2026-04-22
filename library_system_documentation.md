# Public Library Book Reservation System - Documentation

---

## 1. Project Overview

**Project Title:** Public Library Book Reservation System  
**Purpose of the application:** A web-based platform designed to manage the inventory of a public library, allowing members to browse the catalogue, borrow books, and manage their reservations. It also provides librarians with a dashboard to manage book inventory (adding books/copies) and handle the queue for returning books.  

**SDG Alignment:**
- **SDG 4 (Quality Education):** By providing an easy-to-use digital platform to reserve and borrow books, the project improves access to educational resources, knowledge, and literature for a community.
- **SDG 10 (Reduced Inequalities):** The system ensures fair and equal access to public resources. By tracking inventory and automating overdue triggers, it prevents hoarding of educational materials and ensures resources are available for everyone.

---

## 2. Tech Stack Used

- **Java 17:** Core programming language.
- **Spring Boot (3.2.5):** Rapid backend application development framework.
- **Spring Data JPA & Hibernate:** ORM tool used to seamlessly interact with the database without writing boilerplate SQL.
- **Spring Security:** Used to secure endpoints, handle user authentication, and manage role-based access control (Librarian vs Member).
- **Thymeleaf:** Server-side Java template engine used to generate dynamic HTML views.
- **MySQL:** Relational database management system for persistent data storage.
- **Lombok:** Reduces Java boilerplate code (like Getters, Setters, and Constructors).
- **Maven:** Project dependency and build management tool.

---

## 3. Database Design

### 1. `users` Table
- `id` (BIGINT, Primary Key, Auto-increment): Unique identifier.
- `name` (VARCHAR): Full name of the user.
- `email` (VARCHAR, Unique, NOT NULL): User's email (used for login).
- `password` (VARCHAR, NOT NULL): Encrypted password.
- `role` (VARCHAR, NOT NULL): Enum (`MEMBER`, `LIBRARIAN`).

### 2. `books` Table
- `id` (BIGINT, Primary Key, Auto-increment): Unique identifier.
- `title` (VARCHAR, NOT NULL): Title of the book.
- `author` (VARCHAR, NOT NULL): Author's name.
- `available_copies` (INT, NOT NULL): Current available stock.

### 3. `reservations` Table
- `id` (BIGINT, Primary Key, Auto-increment): Unique identifier.
- `user_id` (BIGINT, Foreign Key -> `users(id)`, NOT NULL): The user who borrowed the book.
- `book_id` (BIGINT, Foreign Key -> `books(id)`, NOT NULL): The borrowed book.
- `borrow_date` (DATE, NOT NULL): Date when the reservation was made.
- `due_date` (DATE, NOT NULL): Target return date.
- `status` (VARCHAR, NOT NULL): Enum (`ACTIVE`, `RETURNED`, `OVERDUE`).

### 4. `notifications` Table
- `id` (BIGINT, Primary Key, Auto-increment): Unique identifier.
- `user_id` (BIGINT, Foreign Key -> `users(id)`, NOT NULL): The user receiving the notification.
- `message` (VARCHAR(500), NOT NULL): Content of the alert.
- `sent_at` (TIMESTAMP, NOT NULL): Exact time the notification was triggered.

### Complete MySQL CREATE TABLE SQL Statements:
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL
);

CREATE TABLE books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    available_copies INT NOT NULL
);

CREATE TABLE reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    borrow_date DATE NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(255) NOT NULL,
    CONSTRAINT fk_res_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_res_book FOREIGN KEY (book_id) REFERENCES books(id)
);

CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    message VARCHAR(500) NOT NULL,
    sent_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## 4. Project Structure

```text
c:\Users\HP\IdeaProjects\Public Library Book Reservation System
├── src
│   └── main
│       ├── java
│       │   └── com.library
│       │       ├── LibraryApplication.java           (Main execution point of the Spring Boot App)
│       │       ├── config
│       │       │   ├── DataSeeder.java               (Populates default users and books on startup if DB is empty)
│       │       │   └── SecurityConfig.java           (Configures Spring Security rules, hashing, and login redirects)
│       │       ├── controller
│       │       │   ├── AuthController.java           (Handles mapping for registering and viewing login views)
│       │       │   ├── BookController.java           (Handles mapping for members browsing the book catalogue)
│       │       │   ├── LibrarianController.java      (Handles librarian dashboard, adding books, and processing returns)
│       │       │   └── ReservationController.java    (Handles member dashboards, borrowing logic, and viewing reservations)
│       │       ├── model
│       │       │   ├── Book.java                     (JPA Entity for a Book)
│       │       │   ├── Notification.java             (JPA Entity for User Notifications)
│       │       │   ├── Reservation.java              (JPA Entity for Book Reservations)
│       │       │   └── User.java                     (JPA Entity for System Users)
│       │       ├── repository
│       │       │   ├── BookRepository.java           (JPA Interface for DB Book operations like Search)
│       │       │   ├── NotificationRepository.java   (JPA Interface for finding notifications by User)
│       │       │   ├── ReservationRepository.java    (JPA Interface for DB Reservation operations and finding overdues)
│       │       │   └── UserRepository.java           (JPA Interface for DB User lookups/auth)
│       │       ├── scheduler
│       │       │   └── OverdueScheduler.java         (Background job that checks and marks overdue reservations daily)
│       │       └── service
│       │           ├── BookService.java              (Business logic for finding, adding, and updating book inventory)
│       │           ├── NotificationService.java      (Business logic for creating user alerts)
│       │           ├── ReservationService.java       (Business logic for borrowing limits, updating statuses, and queues)
│       │           └── UserService.java              (Business logic for User auth and explicit registration)
│       └── resources
│           ├── application.properties                (Spring Boot environment config - Database, Server, Hibernate)
│           └── templates
│               ├── librarian
│               │   ├── addBook.html                  (View to add new literature or copies)
│               │   ├── dashboard.html                (View of librarian metrics top-level view)
│               │   └── returnQueue.html              (View of all active reservations awaiting return)
│               ├── member
│               │   ├── catalogue.html                (View showcasing the list of available/searchable library books)
│               │   ├── dashboard.html                (View for Member snapshot/notifications summary)
│               │   └── myReservations.html           (View displaying personal borrowing history)
│               ├── login.html                        (Global Authentication view)
│               └── register.html                     (Global Registration view)
└── pom.xml                                           (Maven dependencies config)
```

---

## 5. All Java Classes — Full Details

**1. `LibraryApplication` (`com.library`)**
- **Purpose**: Bootstrap class.
- **Fields**: None.
- **Methods**: `main(String[] args)` - initializes Spring Boot context via `SpringApplication.run()`.

**2. `SecurityConfig` (`com.library.config`)**
- **Purpose**: Configures authorization, form logins, and passwords.
- **Methods**: 
  - `passwordEncoder()`: Returns BCrypt instance.
  - `authenticationProvider(UserService)`: Binds custom UserDetailsService to DaoAuthenticationProvider.
  - `filterChain(HttpSecurity)`: Establishes URL permissions, configures custom login based on Role, and designates `/logout`.

**3. `DataSeeder` (`com.library.config`)**
- **Purpose**: Preloads DB with demo data.
- **Methods**: `initData(UserService, BookService)`: returns a `CommandLineRunner` that verifies if admin exists, and if not, creates 1 Admin, 1 Member, and 5 demo Books.

**4. `AuthController` (`com.library.controller`)**
- **Purpose**: UI routing for public authentication.
- **Fields**: `UserService userService`.
- **Methods**: 
  - `loginPage()`: Returns `"login"`.
  - `registerPage()`: Returns `"register"`.
  - `registerUser(name, email, password, role, attrs)`: Persists user to DB via Service and redirects to login with flash messages.

**5. `BookController` (`com.library.controller`)**
- **Purpose**: Book browsing logic for Members.
- **Fields**: `BookService bookService`.
- **Methods**: 
  - `catalogue(search, model)`: Checks if query provided, returns search list or total list to catalogue view.

**6. `LibrarianController` (`com.library.controller`)**
- **Purpose**: Admin interfaces for system maintenance.
- **Fields**: `BookService bookService`, `ReservationService reservationService`.
- **Methods**: 
  - `dashboard(model)`: Calculates inventory counts and pending returns to display.
  - `addBookPage(model)`: Renders book entry page.
  - `addBook(title, author, copies, attrs)`: Saves new book entirely.
  - `addCopies(bookId, additionalCopies, attrs)`: Appends stock to existing book.
  - `returnQueue(model)`: Loads `ACTIVE` and `OVERDUE` list.
  - `markReturned(reservationId, attrs)`: Modifies reservation, replenishes book stock.

**7. `ReservationController` (`com.library.controller`)**
- **Purpose**: Member interfaces for lending.
- **Fields**: `ReservationService`, `NotificationService`, `UserService`.
- **Methods**: 
  - `dashboard(userDetails, model)`: Loads user contextual data, notification queue, and counts metrics.
  - `myReservations(userDetails, model)`: Displays borrowing history purely.
  - `borrowBook(userDetails, bookId, dueDate, attrs)`: Triggers borrow logic; displays error if zero copies or bad date.

**8. Model Classes (`com.library.model`)**:
- `Book` -> `id` (Long), `title` (String), `author` (String), `availableCopies` (int), `reservations` (List).
- `User` -> `id` (Long), `name` (String), `email` (String), `password` (String), `role` (Enum), `reservations`(List), `notifications`(List).
- `Reservation` -> `id` (Long), `user` (User), `book` (Book), `borrowDate` (LocalDate), `dueDate` (LocalDate), `status` (Enum - ACTIVE, RETURNED, OVERDUE).
- `Notification` -> `id` (Long), `user` (User), `message` (String), `sentAt` (LocalDateTime).

**Repositories (`com.library.repository`)**:
- `UserRepository`, `BookRepository` (has `findByTitleContaining...`), `ReservationRepository` (has `findByUser...`, `findByStatus...`, `findByStatusAndDueDateBefore`), `NotificationRepository` (has `findByUserOrderBySentAtDesc`).

**9. `BookService` (`com.library.service`)**
- **Purpose**: Handles book repository interactions.
- **Methods**: `getAllBooks()`, `searchBooks(keyword)`, `getBookById(id)`, `addBook(t,a,c)`, `addCopies(id, qty)`, `decrementCopies(book)`, `incrementCopies(book)`.

**10. `ReservationService` (`com.library.service`)**
- **Purpose**: Handles transaction constraints for reserving and returning logic.
- **Methods**: `borrowBook(user, bookId, dueDate)`, `getUserReservations(user)`, `getActiveReservations()`, `markReturned(reservationId)`, `findOverdueReservations()`, `markOverdue(reservation)`.

**11. `NotificationService` (`com.library.service`)**
- **Purpose**: Pushes messages to Users.
- **Methods**: `getUserNotifications(User)`, `createNotification(User, message)`.

**12. `UserService` (`com.library.service`)**
- **Purpose**: Coordinates Spring Security auth logic and user creation.
- **Methods**: `loadUserByUsername(email)`, `registerUser(name, email, password, role)`, `findByEmail(email)`.

**13. `OverdueScheduler` (`com.library.scheduler`)**
- **Purpose**: CRON-based checker for expired dates.
- **Fields**: `ReservationService`, `NotificationService`.
- **Methods**: `checkOverdueReservations()`: Scans daily, marks items overdue, issues notifications to assigned Users.

---

## 6. All Thymeleaf HTML Templates

| File Name & Location | What page it represents | Form Displays / Data Used | Controller Mapping |
| :--- | :--- | :--- | :--- |
| `/login.html` | Authentication | Form submits email/password to `/login`. | `AuthController` |
| `/register.html` | Registration Setup | Form submits Name, Email, PW, Role to `/register`. | `AuthController` |
| `/librarian/dashboard.html` | Admin Summary | Displays dynamic variables: `totalBooks`, `totalCopies`, `pendingReturns`. | `LibrarianController` |
| `/librarian/addBook.html` | Inventory Management | Form 1: create entirely new book. Form 2: add copy to `books` list. | `LibrarianController` |
| `/librarian/returnQueue.html` | Library Return Desk | Displays `reservations` List. Form submits ID to `/librarian/markReturned`. | `LibrarianController` |
| `/member/dashboard.html` | Dashboard Hub | Displays `user` details, `notifications` List, and `activeCount`/`overdueCount`. | `ReservationController`|
| `/member/catalogue.html` | Library Shelves | Displays `books`. Form queries `search`. Form limits Book checkout via `/member/borrow`. | `BookController` |
| `/member/myReservations.html` | Borrowing History | Iterates and displays table of `reservations`. | `ReservationController`|

---

## 7. All API Endpoints / URL Mappings

| HTTP Method | URL | Controller Method | Access Role | What it does |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/login` | `loginPage` | PUBLIC | Shows login form. |
| `POST` | `/login` | (Spring Security) | PUBLIC | Intercepts auth; redirects based on Role via SecurityConfig. |
| `GET` | `/register` | `registerPage` | PUBLIC | Shows registration form. |
| `POST` | `/register` | `registerUser` | PUBLIC | Validates and creates a new User entity. |
| `GET` | `/member/dashboard` | `dashboard` | MEMBER | Sums metrics and pushes notifications to member view. |
| `GET` | `/member/catalogue` | `catalogue` | MEMBER | Lists books, conditionally processes search filters. |
| `POST` | `/member/borrow` | `borrowBook` | MEMBER | Deducts copy count and generates a Reservation. |
| `GET` | `/member/reservations` | `myReservations` | MEMBER | Retrieves all history for the logged-in User. |
| `GET` | `/librarian/dashboard` | `dashboard` | LIBRARIAN | Calculates system-wide KPIs. |
| `GET` | `/librarian/addBook` | `addBookPage` | LIBRARIAN | Views existing books to edit inventory UI. |
| `POST`| `/librarian/addBook` | `addBook` | LIBRARIAN | Creates a brand new database Book row. |
| `POST`| `/librarian/addCopies` | `addCopies` | LIBRARIAN | Bumps the available copies of an existing book. |
| `GET` | `/librarian/returnQueue`| `returnQueue` | LIBRARIAN | Fetches all items not yet returned. |
| `POST`| `/librarian/markReturned`|`markReturned`| LIBRARIAN | Completes reservation lifecycle and adds book copy back. |

---

## 8. Security Configuration

- **Configuration Approach**: Built heavily using Spring Security 6 natively in `SecurityConfig.java` utilizing the standard `@EnableWebSecurity`.
- **Passwords**: Hashed entirely using a `BCryptPasswordEncoder` bean.
- **Routing Rules**: 
  - Static resources (`/css/**`) and Auth (`/register`, `/login`) are `.permitAll()`.
  - Roles are segregated explicitly using `.requestMatchers("/librarian/**").hasRole("LIBRARIAN")` and `.requestMatchers("/member/**").hasRole("MEMBER")`.
- **Login Behavior**: The application utilizes a `.successHandler()` post-login. Because Spring Security default redirects to absolute paths, the handler intercepts the logged-in `Authentication` object, reads their Role `Authority`, and forcefully throws Librarians into `/librarian/dashboard` and Members to `/member/dashboard`.
- **Logout Behavior**: Standard functionality; terminates session via `/logout` and drops the user at `/login?logout`.

---

## 9. Scheduler Details

- **Class Involved:** `OverdueScheduler.java` utilizes the `@Scheduled` annotation over functionality. (Enabled in `LibraryApplication` root via `@EnableScheduling`).
- **Cron Expression:** `0 0 0 * * *` (It fires exactly once daily at Midnight Server Time).
- **Step-by-Step Flow:**
  1. The task queries `ReservationService.findOverdueReservations()`, looking strictly for `status = ACTIVE` where `dueDate < LocalDate.now()`.
  2. For `OverdueList` results, it iterates through them. 
  3. It explicitly flips their enum property via `reservationService.markOverdue(reservation)`.
  4. It constructs an automated message describing the Book title and Due Date.
  5. It dispatches to `NotificationService.createNotification(...)`, dropping a message directly into the Member's dashboard inbox.

---

## 10. Application Flow

**Member Flow:**  
1. **Register/Login** → Logs in and is directed to `/member/dashboard`. 
2. **Review Alerts** → Can view Notifications of active rules / overdue warnings directly on their screen.
3. **Borrow** → Navigates to `/member/catalogue`. Chooses a book, inputs a future `dueDate`, and clicks submit. System decreases book copies by 1 and validates dates.
4. **History** → Visits "My Bookings" via `/member/reservations`. Here, logs will dynamically label statuses as `ACTIVE`, `OVERDUE` etc.
5. **Wait / Conclude** → System will either alert them via Scheduler if they delay, or they submit physical evidence to Librarian to clear it.

**Librarian Flow:**  
1. **Login** → Uses administrative creds, sent to `/librarian/dashboard`. Evaluates global system copy health and return volume queue logic.
2. **Inventory** → Navigates to `/librarian/addBook`. Can construct new entries via the left form. Alternatively, they can spot an existing book running out of stock and augment its numbers in the right form.
3. **Process Returns** → Member hands them a physical book. Librarian goes to `/librarian/returnQueue`. They discover the member's listing under active tasks and click **"Mark Returned"**. The system finalizes the log, sets the Enum to `RETURNED`, and re-adds `+1` to `Book.availableCopies`.

---

## 11. application.properties — Explained

1. `spring.datasource.url=jdbc:mysql://localhost:3306/library_db?createDatabaseIfNotExist=true`  
   > Points the application to local MySQL server to standard port `3306`, inside schema `library_db`, and implicitly builds the table space if it isn't set up.
2. `spring.datasource.username=root`  
   > Sets Database connection username.
3. `spring.datasource.password=Root`  
   > Sets Database connection password.
4. `spring.jpa.hibernate.ddl-auto=update`  
   > Directs Hibernate to auto-assess Java `@Entity` classes and incrementally update MySQL Database tables without dropping existing data.
5. `spring.jpa.show-sql=true`  
   > Echoes all executing backend SQL Queries into the server console to allow for visibility debugging.
6. `server.port=8081`  
   > Migrates Spring Boot out of the standard `8080` port into port `8081` to prevent local network system collisions.
7. `spring.thymeleaf.cache=false`  
   > Prevents HTML caching. This ensures front-end alterations render immediately when pressing 'refresh' natively.
8. `spring.task.scheduling.pool.size=1`  
   > Restricts the internal thread scheduler array footprint to 1. Sufficient for resolving midnight overdues tasks gracefully while preventing sync overlap.

---

## 12. pom.xml Dependencies

1. `spring-boot-starter-web`: Pulls in Tomcat and Spring MVC. Required to construct `/` endpoints and REST logic.
2. `spring-boot-starter-data-jpa`: Adds Hibernate. Permits the mapping of Java OOP models straight to SQL Tables. 
3. `spring-boot-starter-security`: Secures endpoints, blocks anonymous users, handles complex auth encryptions out-of-the-box.
4. `spring-boot-starter-thymeleaf`: Adds Thymeleaf framework to intercept Java Model data and inject it elegantly into server-side generated HTML pages.
5. `thymeleaf-extras-springsecurity6`: Extends basic Thymeleaf layout capabilities by adding `sec:authorize` and Context features directly inside HTML for UI conditional displays. 
6. `mysql-connector-j`: (Scope: `runtime`). Required driver facilitating actual binary communications between the Spring Data abstraction and the installed server of MySQL. 
7. `lombok`: Pre-processor hack. Saves time writing standard `get()`, `set()`, and Constructor initializers by using explicit annotations like `@Getter`. Reduces literal code weight by up to 30%.
