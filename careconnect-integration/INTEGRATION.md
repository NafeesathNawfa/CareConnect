# Connecting the CareConnect UI to the MySQL backend

CareConnect is a Java Swing desktop app. The "frontend" is the Swing screens in `ui/`; the "backend" is a MySQL database (`free_clinic`) accessed through the DAO layer in `dao/`.

```
Swing screens (ui/screens)
        │  call
        ▼
DAOFactory.getInstance().getXxxDAO()
        │  picks one implementation at startup
        ├── MySQL reachable  → JdbcXxxDAO → DBConnection → MySQL (views + stored procedures)
        └── MySQL unreachable → MemoryXxxDAO (in-memory copy of the same logic)
```

The sidebar shows `● MySQL Live` or `○ Memory Mode`. In memory mode, the login screen footer shows the reason.

## 1. Prerequisites

- JDK 11 or newer
- MySQL 8.0 or newer, with the `mysql` command on your PATH
- `mysql-connector-j-*.jar` (MySQL site, "Platform Independent"), placed in a `lib/` folder in the project root

## 2. Create the database

From the project root:

| OS | Command |
|----|---------|
| Windows | `db\setup_db.bat` |
| Mac / Linux | `bash db/setup_db.sh` |

The script loads, in this order: `01_schema.sql`, `02_logic.sql`, `03_seed.sql`, `05_extensions.sql`. Order matters because the seed calls procedures defined in `02_logic.sql`.

`06_extensions_demo.sql` is optional demo queries.

## 3. Configure the connection

Copy `db.properties.example` to `db.properties` and edit it:

```properties
db.url=jdbc:mysql://localhost:3306/free_clinic?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.pass=your_mysql_password
```

Settings are resolved per key in this order: environment variables (`CARECONNECT_DB_URL`, `CARECONNECT_DB_USER`, `CARECONNECT_DB_PASS`), then `db.properties`, then built-in defaults (`root`/`root` on localhost). `db.properties` is git-ignored.

## 4. Run

- Windows: `run.bat`
- Mac / Linux: `bash run.sh`

Both compile `src/` and start `com.careconnect.Main` with `bin` and `lib/*` on the classpath.

## 5. Verify

1. The sidebar shows `● MySQL Live`.
2. Log in as a patient with phone `8000000001`, or as staff (`admin` / `admin123`).
3. Book an appointment, then check it in MySQL:
   ```sql
   USE free_clinic;
   SELECT * FROM v_appointment_details ORDER BY appointment_id DESC LIMIT 5;
   SELECT * FROM appointment_log ORDER BY log_id DESC LIMIT 5;
   ```
4. Optional: `java -cp "bin;lib/*" com.careconnect.SanityTest` (use `:` instead of `;` on Mac/Linux) runs the data-layer checks.

## 6. How a UI action reaches the database

| UI action | DAO call | SQL object |
|-----------|----------|------------|
| Register patient | `PatientDAO.registerPatient` | `sp_register_patient` |
| Book appointment | `AppointmentDAO.bookAppointment` | `sp_book_appointment` |
| Cancel appointment | `AppointmentDAO.cancelAppointment` | `sp_cancel_appointment` |
| Mark completed / no-show | `AppointmentDAO.setAppointmentOutcome` | `sp_set_appointment_outcome` |
| Open or close a clinic day | `ClinicDayDAO.setClinicDay` | `sp_set_clinic_day` |
| Generate slots | `SlotDAO.generateSlots` | `sp_generate_slots` |
| Block / unblock slot | `SlotDAO.blockSlot` | `sp_block_slot` |
| Join / leave waitlist | `WaitlistDAO.joinWaitlist` / `leaveWaitlist` | `sp_join_waitlist` / `sp_leave_waitlist` |
| Browse slots / appointments | `SlotDAO`, `AppointmentDAO` | `v_available_slots`, `v_appointment_details`, `v_todays_schedule` |

Business rules (no double booking, no booking in the past, only BOOKED appointments can change status) are enforced in the database. Rule violations reach the UI as `SQLException` messages, which the screens show in their status banners.

## 7. Troubleshooting

| Footer / sidebar message | Fix |
|--------------------------|-----|
| `MySQL JDBC driver not on classpath` | Put `mysql-connector-j-*.jar` in `lib/` and launch with `run.bat` / `run.sh` |
| `Cannot connect to MySQL as 'root': Access denied` | Fix `db.user` / `db.pass` in `db.properties` |
| `Communications link failure` | Start the MySQL service; check host and port in `db.url` |
| `Unknown database 'free_clinic'` | Run the setup script (step 2) |
| Booking shows no slots | Seed clinic days are relative to the day you ran setup and past slots can't be booked. Re-run setup, or generate slots in the Admin screen |

## 8. Known limitations

- Staff login accepts `admin123` / `admin` for any staff account and the seed uses a placeholder hash. Replace with real password hashing before real use.
- DAO calls run on the Swing UI thread, so slow queries can freeze the window. Move them into `SwingWorker` if that becomes a problem.
- The connection mode is chosen once at startup. Restart the app after fixing the database.
