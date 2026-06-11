# 💰 ExpensePro v2 - Smart Expense Tracker

> A Full-Stack Expense Tracking Web Application built using Java, Spring Boot, MySQL, HTML, CSS, and JavaScript to help users manage expenses, monitor spending patterns, and track financial activities efficiently.

---

## 📌 Project Overview

ExpensePro v2 is a personal finance management application that enables users to record, manage, and analyze their daily expenses through an intuitive dashboard. The application demonstrates full-stack development concepts including user authentication, database management, REST APIs, frontend-backend integration, and data visualization.

---

## 🚀 Features

* Secure user registration and login functionality
* Expense creation, update, and deletion
* Category-wise expense management
* Interactive dashboard for expense tracking
* Financial reports and analytics
* Responsive user interface
* MySQL database integration
* RESTful API architecture
* Role-based application structure
* Frontend and backend integration

---

## 🛠️ Tech Stack

| Layer           | Technology            |
| --------------- | --------------------- |
| Backend         | Java, Spring Boot     |
| Frontend        | HTML, CSS, JavaScript |
| Database        | MySQL                 |
| Build Tool      | Maven                 |
| API Testing     | Postman               |
| Version Control | Git & GitHub          |

---

## 📂 Project Structure

```text
expense-pro/
│
├── backend/
│   ├── src/main/java/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── model/
│   │   ├── security/
│   │   └── config/
│   ├── src/main/resources/
│   └── pom.xml
│
├── frontend/
│   ├── index.html
│   ├── dashboard.html
│   ├── expenses.html
│   ├── reports.html
│   ├── settings.html
│   ├── css/
│   └── js/
│
├── database/
│   └── schema.sql
│
└── README.md
```

---

## ⚙️ Installation & Setup

### 1. Clone Repository

```bash
git clone https://github.com/Ajay0206/smart-expense-tracker-v2-final.git
```

### 2. Configure MySQL Database

Create a database and execute the schema file:

```sql
CREATE DATABASE expense_tracker;
```

Import:

```text
database/schema.sql
```

### 3. Update Database Configuration

Edit:

```text
backend/src/main/resources/application.properties
```

Update:

```properties
spring.datasource.username=root
spring.datasource.password=your_password
```

### 4. Run Backend Application

```bash
cd backend
mvn spring-boot:run
```

### 5. Run Frontend

Open the frontend files using Live Server in VS Code or any local web server.

---

## 📸 Screenshots

### Login Page

![Login Page](Login_page.png)

### Dashboard

![Dashboard](Dash_Board.png)

### Expense Management

![Expenses](Expenses_page.png)

### Reports

![Reports](Reports.png)

---

## 🎯 Learning Outcomes

Through this project, I gained practical experience in:

* Full-stack web application development
* Java Spring Boot application architecture
* REST API development and integration
* MySQL database design and SQL queries
* Frontend development using HTML, CSS, and JavaScript
* Authentication and security concepts
* Debugging and troubleshooting application issues
* Git and GitHub version control workflows

---

## 👨‍💻 Author

**Ajay Kumar Nellore**

* BCA Graduate
* Aspiring Full-Stack Developer
* Email: [nelloreajaykumar6@gmail.com](mailto:nelloreajaykumar6@gmail.com)
* GitHub: https://github.com/Ajay0206
* LinkedIn: https://www.linkedin.com/in/ajay-kumar-nellore-83b188247/

---

## 📜 License

This project is developed for educational and portfolio purposes.
