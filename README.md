# ðŸŒ± AgriGo

AgriGo is a smart agriculture management desktop application developed using **Java**, **JavaFX**, and **MySQL**. The platform helps manage agricultural activities such as crop management, task organization, and user administration through an intuitive graphical interface.

---

## ðŸ“– Overview

The goal of AgriGo is to simplify agricultural management by providing farmers and administrators with an organized and user-friendly system.

The application includes:

- User management
- Agricultural task management
- Dashboard interfaces
- Database integration
- JavaFX graphical interfaces

AgriGo focuses on improving productivity and making farm management easier through technology.

---

## âœ¨ Features

### ðŸ‘¤ User Management
- User registration and login
- Modify user information
- Admin and user interfaces
- Session management

---

### ðŸŒ¾ Agricultural Task Management
- Create and manage agricultural tasks
- Organize farming activities
- Track task progress

---

### ðŸ“Š Dashboard System
- Interactive dashboards
- Separate interfaces for admins and users
- Clean JavaFX GUI design

---

### ðŸ—„ï¸ Database Integration
- MySQL database connectivity
- Persistent data storage
- Service and utility classes for database operations

---

## ðŸ› ï¸ Tech Stack

### Programming Language
- Java

### GUI Framework
- JavaFX
- FXML
- CSS

### Database
- MySQL

### Build Tool
- Maven

---

## ðŸ“‚ Project Structure

```bash
agrigo/
â”‚
â”œâ”€â”€ models/                     # Database or application models
â”œâ”€â”€ src/
â”‚   â”œâ”€â”€ main/
â”‚   â”‚   â”œâ”€â”€ java/
â”‚   â”‚   â”‚   â”œâ”€â”€ Controllers/    # Application controllers
â”‚   â”‚   â”‚   â”œâ”€â”€ Entities/       # Entity classes
â”‚   â”‚   â”‚   â”œâ”€â”€ Services/       # Business logic and services
â”‚   â”‚   â”‚   â”œâ”€â”€ Tests/          # Test classes
â”‚   â”‚   â”‚   â””â”€â”€ Utils/          # Utility classes
â”‚   â”‚   â”‚
â”‚   â”‚   â””â”€â”€ resources/
â”‚   â”‚       â”œâ”€â”€ assets/         # Images and assets
â”‚   â”‚       â”œâ”€â”€ css/            # Stylesheets
â”‚   â”‚       â””â”€â”€ *.fxml          # JavaFX interfaces
â”‚
â”œâ”€â”€ user-photos/                # User uploaded photos
â”œâ”€â”€ pom.xml                     # Maven configuration
â”œâ”€â”€ README.md
â””â”€â”€ .gitignore
```

---

## ðŸš€ Installation & Setup

### 1ï¸âƒ£ Clone the Repository

```bash
git clone https://github.com/amalmanai/agrigo.git
cd agrigo
```

---

### 2ï¸âƒ£ Configure the Database

Create a MySQL database and update your database credentials inside the utility/database configuration files.

Example:

```java
String url = "jdbc:mysql://localhost:3306/agrigo";
String user = "root";
String password = "your_password";
```

---

### 3ï¸âƒ£ Install Dependencies

Make sure you have installed:

- Java JDK 17+
- Maven
- JavaFX SDK
- MySQL

---

### 4ï¸âƒ£ Run the Project

Using Maven:

```bash
mvn clean install
mvn javafx:run
```

Or run the `MainTFX.java` file directly from your IDE.

---

## ðŸŽ¨ User Interfaces

The project contains multiple JavaFX interfaces such as:

- Login interface
- Registration interface
- Admin dashboard
- User dashboard
- User modification pages

FXML files are located inside:

```bash
src/main/resources/
```

---

## ðŸ“¸ Future Improvements

- Add real-time notifications
- Improve dashboard analytics
- Add multilingual support
- Cloud database integration
- Mobile companion application
- Enhanced security system

---

## ðŸ¤ Contributing

Contributions are welcome!

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

---

## ðŸ“„ License

This project is licensed under the MIT License.

---

## ðŸ‘¨â€ðŸ’» Authors

Developed by the AgriGo Team â¤ï¸

GitHub Repository:

https://github.com/amalmanai/agrigo

