# ⚡ Electricity Bill Management System (EBMS)
## Spring Boot + MySQL + Thymeleaf


## ✅ STEP-BY-STEP RUN INSTRUCTIONS

### 1️⃣ Prerequisites
- Java 17+ installed → `java -version`
- Maven installed  → `mvn -version`
- MySQL 8.x running (MySQL Workbench)

---

### 2️⃣ Database Setup
Open MySQL Workbench → Run this SQL:
```sql
CREATE DATABASE electricity_bill_db;
```
That's all! Spring Boot creates the tables automatically.

---

### 3️⃣ Configure Database
Open `src/main/resources/application.properties`
Change these if your MySQL credentials differ:
```properties
spring.datasource.username=root
spring.datasource.password=root   ← change to YOUR MySQL password
```

---

### 4️⃣ Run the Application
```bash
cd electricity-bill-system
mvn spring-boot:run
```
OR in IntelliJ/Eclipse → Run `ElectricityBillApplication.java`

---

### 5️⃣ Access the Application
Open browser → http://localhost:8080

---

## 🔐 DEFAULT LOGIN CREDENTIALS

| Role  | Username | Password  |
|-------|----------|-----------|
| Admin | admin    | admin123  |

→ Admin is created automatically on first startup.
→ Customers can register at: http://localhost:8080/auth/register

---

## 🗺️ URL MAP

| URL                        | Access     | Description         |
|----------------------------|------------|---------------------|
| /auth/login                | Public     | Login page          |
| /auth/register             | Public     | Register page       |
| /admin/dashboard           | ADMIN only | Admin dashboard     |
| /admin/customers           | ADMIN only | Customer CRUD       |
| /admin/bills               | ADMIN only | Bill management     |
| /admin/users               | ADMIN only | User management     |
| /customer/dashboard        | CUSTOMER   | Customer dashboard  |
| /customer/bills            | CUSTOMER   | View own bills      |
| /customer/profile          | CUSTOMER   | View profile        |
| /admin/bills/pdf/{id}      | ADMIN      | Download bill PDF   |
| /admin/bills/qr/{id}       | ADMIN      | View QR code        |

---

## ⚡ BILL CALCULATION LOGIC

| Units         | Rate per Unit | Example (200 units)          |
|---------------|---------------|-------------------------------|
| 0 – 100       | ₹1.50         | 100 × 1.50 = ₹150            |
| 101 – 300     | ₹3.00         | 100 × 3.00 = ₹300            |
| Above 300     | ₹5.00         | (units-300) × 5.00            |
| Fixed Charge  | ₹50.00        | Always added                  |

Example: 200 units → ₹150 + ₹300 + ₹50 (fixed) = **₹500**

---

## 📁 PROJECT STRUCTURE

```
electricity-bill-system/
├── pom.xml
├── database_setup.sql
├── README.md
└── src/main/
    ├── java/com/ebms/
    │   ├── ElectricityBillApplication.java
    │   ├── config/
    │   │   ├── SecurityConfig.java       ← Spring Security
    │   │   └── DataInitializer.java      ← Auto-creates admin
    │   ├── controller/
    │   │   ├── AuthController.java       ← Login/Register
    │   │   ├── AdminController.java      ← Admin pages
    │   │   └── CustomerController.java   ← Customer pages
    │   ├── entity/
    │   │   ├── User.java
    │   │   ├── Customer.java
    │   │   ├── Bill.java
    │   │   └── Payment.java
    │   ├── repository/
    │   │   ├── UserRepository.java
    │   │   ├── CustomerRepository.java
    │   │   ├── BillRepository.java
    │   │   └── PaymentRepository.java
    │   └── service/impl/
    │       ├── UserServiceImpl.java
    │       ├── CustomerServiceImpl.java
    │       └── BillServiceImpl.java
    └── resources/
        ├── application.properties
        ├── static/
        │   ├── css/style.css
        │   └── js/app.js
        └── templates/
            ├── auth/login.html
            ├── auth/register.html
            ├── admin/dashboard.html
            ├── admin/customers.html
            ├── admin/bills.html
            ├── admin/users.html
            ├── customer/dashboard.html
            ├── customer/bills.html
            └── customer/profile.html
```

---

## 🎯 VIVA EXPLANATION

### Q: How is security implemented?
**Spring Security** with BCrypt password encoding. All protected URLs require authentication. 
`SecurityConfig.java` maps `/admin/**` → ADMIN role, `/customer/**` → CUSTOMER role.
Direct URL access without login → redirected to `/auth/login`.

### Q: How is the bill calculated?
Slab-based calculation in `BillServiceImpl.calcUnits()`:
- 0-100 units: flat ₹1.5/unit
- 101-300 units: first 100@₹1.5 + rest@₹3
- Above 300: slabs above + excess@₹5
- Fixed charge ₹50 always added

### Q: What is the architecture?
Layered: Controller → Service → Repository → Database (MySQL via JPA/Hibernate)

### Q: What is role-based access?
Two roles: ADMIN (full system access) and CUSTOMER (own bills only).
Spring Security enforces this at URL level.

### Q: How are PDFs generated?
iText7 library creates PDF in-memory and streams it as a download response.

### Q: How is QR code generated?
Google ZXing library encodes bill data as QR and returns PNG bytes.

### Q: What is the penalty logic?
A scheduled job (`@Scheduled` cron) runs daily at 1 AM.
Bills past due date get 2% penalty added to total amount and status → OVERDUE.

---

## 🛠️ TECH STACK

| Layer      | Technology               |
|------------|--------------------------|
| Backend    | Spring Boot 3.2, Java 17 |
| Security   | Spring Security 6        |
| ORM        | Spring Data JPA + Hibernate |
| Database   | MySQL 8.x                |
| Frontend   | Thymeleaf + HTML/CSS/JS  |
| Charts     | Chart.js                 |
| PDF        | iText7                   |
| QR Code    | Google ZXing             |
| Icons      | Font Awesome 6           |
| Build      | Maven                    |
