# CareConnect

A Java Swing desktop application for managing appointments at a free community clinic. Patients can register, book, cancel, and join waitlists, while staff manage doctors, clinic days, slots, and appointment outcomes.

Built as a Java (OOP) + DBMS course project.

## Features

**Patients**
- Register and sign in
- Book and cancel appointments
- Join a waitlist when a slot is full

**Staff**
- Manage doctors, clinic days, and time slots
- Record appointment outcomes

## Tech Stack

| Area | Technology |
|------|------------|
| Language | Java 11+ |
| UI | Swing |
| Database access | JDBC |
| Database | MySQL 8 (`free_clinic`) |
| Build | Plain `javac` (no Maven) |

## Architecture

CareConnect uses a five-layer design:

```
View (Swing screens)
   ↓
Controller (MainFrame, callbacks)
   ↓
Model (Patient, Doctor, Slot, ...)
   ↓
Persistence (7 DAO interfaces, each with a JdbcXxxDAO and a MemoryXxxDAO)
   ↓
Database (tables, views, triggers, stored procedures)
```

**Key design points**
- **The UI never writes SQL.** Screens only call DAO interfaces.
- **DAOFactory** (a singleton factory) creates the DAO objects and chooses MySQL or in-memory implementations once at startup. If MySQL isn't running, the app falls back to in-memory DAOs and still works.
- This demonstrates runtime polymorphism and dependency inversion.

## Double-Booking Prevention

Three database safeguards make sure a slot can't be booked twice:

1. A `FOR UPDATE` row lock inside the stored procedure `sp_book_appointment`
2. A status check: the slot must be `AVAILABLE`
3. A `UNIQUE` constraint on the generated column `active_slot_id`, allowing only one non-cancelled appointment per slot (cancelled appointments become `NULL`, so the slot can be rebooked)

## Database Rules (Triggers)

Triggers enforce the rules no matter which client touches the data:
- Slot status follows the appointment status
- Every appointment status change is audited
- Illegal status transitions are blocked
- Medicine stock can never go negative

## Getting Started

### Prerequisites
- JDK 11 or later
- MySQL 8
- MySQL Connector/J (JDBC driver) jar

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/NafeesathNawfa/CareConnect.git
   cd CareConnect
   ```

2. **Create the database**
   Open the project's `.sql` file in MySQL Workbench (or the MySQL command line) and run it. This creates the `free_clinic` database with its tables, views, triggers, and stored procedures.

3. **Configure the connection**
   In the database connection class, update the JDBC URL, MySQL username, and password to match your setup.

4. **Compile and run**
   Add the MySQL Connector/J jar to your classpath, then:
   ```bash
   javac -cp ".:lib/*" -d out $(find . -name "*.java")
   java -cp "out:lib/*" [MainClass]
   ```
   On Windows, use `;` instead of `:` in the classpath.

> No MySQL? The app starts with in-memory data instead.

## Known Limitations

- Passwords use a weak check; BCrypt or Argon2 should be used
- Database calls run on the Swing Event Dispatch Thread; `SwingWorker` should be used
- Medicine and records exist only in SQL, with no Java screen yet
- Two theme systems exist (`UITheme` and `Theme`) and should be merged

## Authors

- Nafeesath Nawfa L H F
- Janani R
- Poorvika
