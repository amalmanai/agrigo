
# 🌱 AgriGo – Smart Agriculture Management System

AgriGo is a **smart agriculture management desktop application** built with **Java, JavaFX, and MySQL**.
It provides a modern and intuitive interface to help manage agricultural operations such as crop tracking, task organization, and user administration.

---

## 📌 Overview

AgriGo aims to digitalize and simplify farm management by offering a centralized system for agricultural operations.

The platform enables:

* 👤 User and admin management
* 🌾 Agricultural task tracking
* 📊 Interactive dashboards
* 🗄️ Persistent MySQL database integration
* 🖥️ Modern JavaFX user interface

The goal is to improve productivity, organization, and decision-making in agricultural environments.

---

## ✨ Key Features

### 👤 User Management

* Secure login and registration system
* Profile management (update personal information)
* Role-based access (Admin / User)
* Session handling

---

### 🌾 Task & Activity Management

* Create, update, and delete agricultural tasks
* Organize farming operations efficiently
* Track task progress and status

---

### 📊 Dashboard System

* Dedicated dashboards for Admin and Users
* Clean and intuitive JavaFX UI
* Quick access to key functionalities
* Overview of agricultural activities

---

### 🗄️ Database Integration

* MySQL relational database
* Persistent data storage
* Structured DAO / Service layer architecture
* Efficient CRUD operations

---

## 🛠️ Tech Stack

### Backend

* Java (JDK 17+)

### Frontend (Desktop UI)

* JavaFX
* FXML
* CSS Styling

### Database

* MySQL

### Build Tool

* Maven

---

## 📁 Project Architecture

```
agrigo/
│
├── models/                  # Data models
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── Controllers/ # UI Controllers (MVC)
│   │   │   ├── Entities/    # Domain entities
│   │   │   ├── Services/    # Business logic layer
│   │   │   ├── Utils/       # Helper & utility classes
│   │   │   └── Tests/       # Unit tests
│   │   │
│   │   └── resources/
│   │       ├── assets/      # Images & icons
│   │       ├── css/         # Stylesheets
│   │       └── *.fxml       # JavaFX views
│
├── user-photos/             # Uploaded profile images
├── pom.xml                  # Maven configuration
└── README.md
```

---

## 🚀 Installation & Setup

### 1️⃣ Clone the repository

```bash
git clone https://github.com/amalmanai/agrigo.git
cd agrigo
```

---

### 2️⃣ Configure MySQL Database

Create a database (e.g. `agrigo`) and update credentials:

```java
String url = "jdbc:mysql://localhost:3306/agrigo";
String user = "root";
String password = "your_password";
```

---

### 3️⃣ Install Requirements

Make sure you have installed:

* Java JDK 17+
* Maven
* JavaFX SDK
* MySQL Server

---

### 4️⃣ Run the Application

Using Maven:

```bash
mvn clean install
mvn javafx:run
```

Or run directly:

```
MainTFX.java
```

from your IDE.

---

## 🖥️ Application Interfaces

AgriGo includes several JavaFX interfaces:

* 🔐 Login screen
* 📝 Registration screen
* 🧑‍💼 Admin dashboard
* 👨‍🌾 User dashboard
* ✏️ Profile management pages

All UI files are located in:

```
src/main/resources/
```

---

## 🚀 Future Improvements

* 🔔 Real-time notifications system
* 📊 Advanced analytics dashboard
* 🌍 Multi-language support (FR/EN/AR)
* ☁️ Cloud database integration
* 📱 Mobile companion application
* 🔐 Enhanced security & encryption

---

## 🤝 Contributing

Contributions are welcome and appreciated!

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push your branch
5. Open a Pull Request

---

## 📄 License

This project is licensed under the **MIT License**.

---

## 👨‍💻 Authors

Developed by the **AgriGo Team** ❤️

🔗 Repository:
[https://github.com/amalmanai/agrigo](https://github.com/amalmanai/agrigo)


