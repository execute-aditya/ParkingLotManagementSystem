# 🅿 Parking Lot Management System

<div align="center">

![Java](https://img.shields.io/badge/Java-11+-orange?style=for-the-badge&logo=java)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=for-the-badge&logo=mysql)
![JDBC](https://img.shields.io/badge/JDBC-MySQL_Connector-green?style=for-the-badge)
![Eclipse](https://img.shields.io/badge/IDE-Eclipse-purple?style=for-the-badge&logo=eclipse)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)

**A terminal-based Java application for managing parking lot operations including real-time space allocation, dynamic fare calculation, surge pricing, and revenue tracking.**

</div>

---

## 📌 Table of Contents

- [About the Project](#-about-the-project)
- [Features](#-features)
- [OOP Concepts Used](#-oop-concepts-used)
- [Project Structure](#-project-structure)
- [Database Design](#-database-design)
- [Rate Card](#-rate-card)
- [Tech Stack](#-tech-stack)
- [Setup & Installation](#-setup--installation)
- [How to Run](#-how-to-run)
- [Menu Options](#-menu-options)
- [Sample Output](#-sample-output)
- [Author](#-author)

---

## 📖 About the Project

The **Parking Lot Management System** is a Java console application that simulates a real-world parking lot. It automates vehicle entry/exit, spot allocation, fare calculation, payment processing, and revenue reporting — all through a simple terminal menu.

This project was built to demonstrate core **Object-Oriented Programming** concepts, **DAO design pattern**, **MySQL database integration**, and real-world features like **dynamic surge pricing** and **monthly pass management**.

---

## ✨ Features

| Feature | Description |
|---|---|
| 🚗 **Vehicle Entry** | Registers vehicle (if new) and assigns the nearest available spot |
| 🚪 **Vehicle Exit** | Calculates fare, processes payment, and releases the spot |
| 📍 **Nearest Spot Finder** | Finds closest free spot based on floor number |
| 💰 **Dynamic Pricing** | Surge pricing automatically applied at 70%, 85%, 95% occupancy |
| 🕐 **Grace Period** | First 15 minutes are always free |
| 🗓️ **Monthly Pass** | Flat monthly fee — exits are always ₹0 for pass holders |
| 📋 **Entry/Exit Logs** | Full audit trail with timestamps and duration |
| 📊 **Revenue Reports** | Today's revenue, total revenue, and occupancy analytics |
| 🔄 **Auto-Renewal Ready** | Monthly pass expiry date tracking |
| 💳 **Multiple Payment Modes** | CASH, CARD, UPI supported |

---

## 🏗 OOP Concepts Used

### 1. Inheritance
```
Vehicle  (abstract parent)
   ├── Car    → requires CAR spot
   ├── Bike   → requires BIKE spot  (smallest)
   ├── Van    → requires VAN spot
   └── Truck  → requires TRUCK spot (largest)
```
All common properties (license plate, owner name, phone) are written **once** in `Vehicle` and inherited by all subclasses.

---

### 2. Polymorphism
**Method Overriding** — each subclass implements `getRequiredSpotType()` differently:
```java
new Car().getRequiredSpotType()    // → "CAR"
new Bike().getRequiredSpotType()   // → "BIKE"
new Truck().getRequiredSpotType()  // → "TRUCK"
```

**Fare Strategy** — `FareCalculator.of(rate)` returns the correct calculator at runtime:
```java
FareCalculator calc = FareCalculator.of(rate);
// Returns HourlyFare, DailyFare, or MonthlyPassFare
double amount = calc.calculateFare(duration, surge, hasPass);
```

---

### 3. Encapsulation
All model fields are `private`. Access is only through getters and setters:
```java
vehicle.setLicensePlate("MH12AB1234");   // ✅ correct
vehicle.licensePlate = "MH12AB1234";     // ❌ not allowed
```

---

### 4. Abstraction
`Vehicle` and `FareCalculator` are **abstract** — you cannot instantiate them directly. They define the contract, subclasses provide the implementation.

---

### Design Patterns
| Pattern | Where Used |
|---|---|
| **Strategy Pattern** | `FareCalculator` — 3 fare strategies selected at runtime |
| **Singleton Pattern** | `DBConnection` — only one DB connection throughout the app |
| **DAO Pattern** | One DAO class per database table |
| **Factory Method** | `FareCalculator.of(rate)` and `ParkingService.createVehicleByType()` |

---

## 📁 Project Structure

```
ParkingLotSystem/
├── src/
│   └── com/parking/
│       ├── model/
│       │   ├── ParkingLot.java
│       │   ├── Vehicle.java         ← abstract
│       │   ├── Car.java             ← extends Vehicle
│       │   ├── Bike.java            ← extends Vehicle
│       │   ├── Van.java             ← extends Vehicle
│       │   ├── Truck.java           ← extends Vehicle
│       │   ├── Spot.java
│       │   ├── Booking.java
│       │   ├── Payment.java
│       │   └── Rate.java
│       ├── dao/
│       │   ├── VehicleDAO.java
│       │   ├── SpotDAO.java
│       │   ├── BookingDAO.java
│       │   ├── PaymentDAO.java
│       │   └── RateDAO.java
│       ├── service/
│       │   ├── FareCalculator.java  ← abstract + 3 strategies
│       │   ├── ParkingService.java
│       │   └── RevenueService.java
│       ├── util/
│       │   ├── DBConnection.java
│       │   └── ConsoleHelper.java
│       └── main/
│           └── Main.java            ← Entry Point
├── sql/
│   ├── schema.sql                   ← Run this first
│   └── dummy_data.sql               ← Optional test data
├── lib/
│   └── mysql-connector-j-8.x.x.jar ← Add manually
├── .gitignore
└── README.md
```

---

## 🗄 Database Design

**7 Tables:**

```
parking_lot ──< spot
parking_lot ──< booking
parking_lot ──< rate
vehicle     ──< booking
spot        ──< booking
booking     ──< entry_exit_log
booking     ──< payment
```

| Table | Purpose |
|---|---|
| `parking_lot` | Lot info — name, address, total spots |
| `spot` | Each physical spot — type, floor, occupied status |
| `vehicle` | Registered vehicles — type, owner, monthly pass |
| `booking` | Parking sessions — links vehicle + spot + fare type |
| `entry_exit_log` | Entry/exit timestamps and duration |
| `payment` | Full payment breakdown with surge and discount |
| `rate` | Pricing config per vehicle type and fare type |

**Spot Distribution (60 total):**
- 🏍 BIKE — 20 spots (Floor 0 & 1)
- 🚗 CAR — 25 spots (Floor 0, 1 & 2)
- 🚐 VAN — 10 spots (Floor 2 & 3)
- 🚛 TRUCK — 5 spots (Floor 3)

---

## 💵 Rate Card

| Vehicle | Hourly | Daily | Monthly |
|---|---|---|---|
| 🏍 BIKE | ₹15 / hr | ₹80 / day | ₹800 / month |
| 🚗 CAR | ₹40 / hr | ₹200 / day | ₹2,000 / month |
| 🚐 VAN | ₹60 / hr | ₹300 / day | ₹3,000 / month |
| 🚛 TRUCK | ₹80 / hr | ₹450 / day | ₹4,500 / month |

**Grace Period:** First 15 minutes are free for all vehicles.

**Surge Pricing:**
| Occupancy | Multiplier |
|---|---|
| Below 70% | ×1.00 (normal) |
| 70% – 84% | ×1.25 (+25%) |
| 85% – 94% | ×1.50 (+50%) |
| 95%+ | ×2.00 (double) |

---

## 🛠 Tech Stack

- **Language:** Java 11+
- **Database:** MySQL 8.0
- **Connectivity:** JDBC (MySQL Connector/J)
- **IDE:** Eclipse IDE
- **Pattern:** 3-Layer Architecture (Presentation → Service → DAO)

---

## ⚙ Setup & Installation

### Prerequisites
- Java JDK 11+ → [Download](https://adoptium.net)
- MySQL 8.x → [Download](https://dev.mysql.com/downloads/mysql/)
- Eclipse IDE → [Download](https://eclipse.org/downloads)
- MySQL Connector/J → [Download](https://dev.mysql.com/downloads/connector/j/)

---

### 1. Clone the Repository
```bash
git clone https://github.com/execute-aditya/ParkingLotManagementSystem.git
```

### 2. Set Up the Database
Open MySQL Workbench or MySQL CLI and run:
```bash
source /path/to/ParkingLotSystem/sql/schema.sql
```
This creates the database, all 7 tables, 60 spots, and 12 rate entries automatically.

Optionally load test data:
```bash
source /path/to/ParkingLotSystem/sql/dummy_data.sql
```

### 3. Add MySQL Connector JAR
- Download `mysql-connector-j-8.x.x.jar`
- Place it inside the `lib/` folder
- In Eclipse: right-click the JAR → **Build Path → Add to Build Path**

### 4. Update Database Credentials
Open `src/com/parking/util/DBConnection.java`:
```java
private static final String DB_USER     = "root";
private static final String DB_PASSWORD = "your_password_here";
```

### 5. Import into Eclipse
```
File → Import → General → Existing Projects into Workspace
→ Browse to cloned folder → Finish
```

---

## ▶ How to Run

```
Right-click Main.java → Run As → Java Application
```

> ⚠️ Click inside the Eclipse Console tab before typing input.

---

## 📋 Menu Options

```
╔══════════════════════════════════════════════════════════════╗
║        🅿   PARKING LOT MANAGEMENT SYSTEM   🅿              ║
╚══════════════════════════════════════════════════════════════╝

  1.  Vehicle Entry         → Park a vehicle
  2.  Vehicle Exit          → Checkout and pay
  3.  Check Spot Availability
  4.  View Active Bookings
  5.  Monthly Pass Management
  6.  Entry/Exit Log
  7.  Revenue & Occupancy Report
  8.  View Rate Card
  9.  Register Vehicle
  10. View All Vehicles
  0.  Exit
```

---

## 💻 Sample Output

**Vehicle Entry:**
```
  License Plate        : MH12AB1234
  Vehicle Type         : 2 (CAR)
  Owner Name           : Rahul Sharma
  Fare Type            : 1 (HOURLY)

  ✔  Vehicle parked successfully!
  Booking ID  : #1
  Spot        : C-01
  Fare Type   : HOURLY
```

**Vehicle Exit:**
```
  ── FARE BREAKDOWN ──────────────────────
  Duration      : 140 min
  Grace period  : 15 min (free)
  Billable      : 125 min → 3 hour(s)
  Hourly rate   : ₹40.00
  Base fare     : ₹120.00
  Surge (×1.00) : ₹0.00
  TOTAL         : ₹120.00

  ✔  Checkout complete!
  Payment ID  : #1
  Total Paid  : ₹120.00
  Mode        : UPI
```

**Revenue Report:**
```
  ══════════════════════════════════════════════
          REVENUE & OCCUPANCY REPORT
  ══════════════════════════════════════════════
  Today's Revenue    : ₹1,115.00
  Total Revenue      : ₹1,115.00
  Active Bookings    : 8
  Occupancy          : 8 / 60 (13.3%)
  Surge Multiplier   : ×1.00
  ══════════════════════════════════════════════
```

---

## 👨‍💻 Author

**Aditya Dhembare**

[![GitHub](https://img.shields.io/badge/GitHub-execute--aditya-black?style=for-the-badge&logo=github)](https://github.com/execute-aditya)
[![Gmail](https://img.shields.io/badge/Gmail-adityadhembare27@gmail.com-red?style=for-the-badge&logo=gmail)](mailto:adityadhembare27@gmail.com)

---

<div align="center">
⭐ If you found this project helpful, give it a star on GitHub!
</div>
