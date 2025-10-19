#  Telehealth System (THS-Enhanced)

> **COIT20258 – Software Engineering (Assignment 3)**  
> Developed by **Fahim** & **Raqib**

---

##  Overview

**THS-Enhanced** is a Java-based distributed **Telehealth System** designed to connect doctors and patients remotely.  
It supports appointment booking, virtual consultations, prescription management, and PDF report exports.

The system implements a **3-tier architecture (MVC)** with a **JavaFX client**, **multi-threaded server**, and **MySQL database** backend.

---

## Project Structure
```ths-enhanced/
├── src/main/java/com/fahim/ths/
│   ├── controller/        # JavaFX controllers (Doctor, Patient, Admin)
│   ├── model/             # Entity classes (Appointment, User, Prescription)
│   ├── service/           # Business logic and communication layer
│   ├── util/              # Database utilities and helper classes
│   ├── server/            # Multi-threaded server implementation
│   └── Main.java          # Client entry point (JavaFX GUI)
│
├── src/main/resources/fxml/   # FXML layout files for the GUI
├── src/main/resources/sql/    # Database initialization scripts
├── pom.xml                    # Maven build file (if used)
└── README.md
```


---

##  Setup & Run Instructions (IntelliJ IDEA)

### Prerequisites
Make sure you have the following installed:
- **JDK 21 or later**
- **JavaFX SDK 21 or later** → [Download Here](https://gluonhq.com/products/javafx/)
- **MySQL Server 8.0+**
- **IntelliJ IDEA** (Community or Ultimate)

---