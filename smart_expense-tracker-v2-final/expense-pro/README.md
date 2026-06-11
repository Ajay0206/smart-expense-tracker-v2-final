# 💸 ExpensePro v2 — Smart Expense Tracker
### Full-Stack: Java Spring Boot + MySQL + HTML/CSS/JS

> A production-grade, interview-ready expense tracking app demonstrating
> authentication, CRUD, SQL, charts, analytics, and PDF export.

---

## 📁 Project Structure

```
expense-pro/
├── database/
│   └── schema.sql                    ← MySQL schema, indexes, seed data, views
│
├── backend/
│   ├── pom.xml                       ← Maven dependencies
│   └── src/main/
│       ├── resources/
│       │   └── application.properties
│       └── java/com/expensepro/
│           ├── ExpenseProApplication.java
│           ├── model/
│           │   ├── User.java
│           │   ├── Category.java
│           │   ├── Expense.java
│           │   └── SavingsGoal.java
│           ├── repository/
│           │   └── Repositories.java  ← All 4 JPA repos + custom JPQL queries
│           ├── security/
│           │   ├── JwtUtil.java
│           │   └── JwtAuthFilter.java ← Also contains UserDetailsServiceImpl
│           ├── config/
│           │   └── SecurityConfig.java
│           ├── service/
│           │   ├── ExpenseService.java ← All business logic + analytics
│           │   └── PdfService.java     ← iText PDF generation
│           └── controller/
│               ├── AuthController.java
│               └── Controllers.java   ← ExpenseController + ReportController
│
└── frontend/
    ├── index.html        ← Login / Register (split-screen design)
    ├── dashboard.html    ← Main dashboard: 4 stats + 3 charts + category list
    ├── expenses.html     ← Full CRUD table with search/filter/pagination
    ├── reports.html      ← Analytics: bar, pie, area charts + PDF export
    ├── settings.html     ← Profile, currency, budget settings
    ├── css/
    │   └── style.css     ← Complete dark design system (700+ lines)
    └── js/
        ├── utils.js      ← API helper, formatters, toast, chart defaults
        └── sidebar.js    ← Shared sidebar renderer
```

---

## 🚀 Quick Start (4 Steps)

### Step 1 — MySQL Setup
```bash
mysql -u root -p
```
Inside MySQL:
```sql
source /path/to/expense-pro/database/schema.sql;
```

### Step 2 — Configure Backend
Edit `backend/src/main/resources/application.properties`:
```properties
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### Step 3 — Run Backend
```bash
cd expense-pro/backend
mvn spring-boot:run
```
Wait for: `ExpensePro API started on :8080`

### Step 4 — Open Frontend
```bash
cd expense-pro/frontend
python -m http.server 5500
```
Visit: **http://localhost:5500**

OR use VS Code Live Server (right-click `index.html` → Open with Live Server)

---

## 🔌 REST API Reference

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | ❌ | Create account |
| POST | `/api/auth/login` | ❌ | Login → returns JWT |
| GET | `/api/expenses` | ✅ | List expenses (paginated + filterable) |
| POST | `/api/expenses` | ✅ | Add expense |
| PUT | `/api/expenses/{id}` | ✅ | Update expense |
| DELETE | `/api/expenses/{id}` | ✅ | Delete expense |
| GET | `/api/expenses/dashboard` | ✅ | Full analytics dashboard |
| GET | `/api/expenses/categories` | ✅ | List categories |
| PATCH | `/api/expenses/profile` | ✅ | Update profile/currency/budget |
| GET | `/api/reports/pdf?year=&month=` | ✅ | Download PDF report |

### Query params for GET `/api/expenses`:
| Param | Type | Description |
|-------|------|-------------|
| `page` | int | Page number (default 0) |
| `size` | int | Page size (default 20) |
| `categoryId` | Long | Filter by category |
| `from` | date | Start date (YYYY-MM-DD) |
| `to` | date | End date (YYYY-MM-DD) |
| `q` | string | Title search |

### Auth header (all protected routes):
```
Authorization: Bearer <jwt_token>
```

---

## 🧪 Test the API (curl)

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@test.com","password":"secret123","fullName":"John Doe"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"secret123"}'

# Add expense (use token from login)
curl -X POST http://localhost:8080/api/expenses \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Lunch","amount":150,"categoryId":1,"expenseDate":"2025-05-15","paymentMode":"UPI"}'

# Dashboard
curl http://localhost:8080/api/expenses/dashboard \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 🎯 Features & What They Teach

| Feature | Concepts |
|---------|---------|
| JWT Auth | Spring Security, BCrypt, stateless auth, filter chain |
| CRUD Expenses | REST verbs, validation, JPA transactions |
| MySQL Schema | Normalization, foreign keys, indexes, views |
| Dashboard Analytics | Aggregate JPQL, BigDecimal math, DTO mapping |
| Search + Filter | Dynamic queries, pagination, Spring Data Page |
| PDF Export | iText API, byte streams, HTTP response headers |
| Chart.js | Line, bar, doughnut, area charts |
| CORS Config | Cross-origin, preflight, allowed origins |

---

## 🗄️ Database Tables

| Table | Purpose |
|-------|---------|
| `users` | Accounts with currency + monthly limit |
| `categories` | 12 default + custom user categories |
| `expenses` | All transactions with date, mode, tags |
| `budgets` | Monthly per-category budget limits |
| `savings_goals` | Financial goals with progress tracking |

---

## 🔒 Security Notes

- Passwords hashed with **BCrypt (cost=12)**
- JWT tokens expire after **24 hours**
- All routes except `/api/auth/**` require valid JWT
- **CORS** restricted to localhost dev origins
- SQL injection prevented via **JPA parameterised queries**
- Input validation via **Jakarta Bean Validation** (`@NotBlank`, `@Positive`, etc.)

---

## 💡 Interview Talking Points

1. **Why JWT over sessions?** Stateless, horizontally scalable, no server-side session store needed
2. **BCrypt cost factor 12?** Adaptive — slower means harder brute-force; cost 12 ≈ 250ms on modern hardware
3. **Why service layer?** Separation of concerns — controllers handle HTTP, services handle business rules
4. **Custom JPQL queries?** Avoids N+1 problems, pushes aggregation down to the database where it's faster
5. **iText PDF?** Streaming binary response, `Content-Disposition: attachment` header triggers browser download
6. **Page<T> return?** Spring Data pagination avoids loading entire table into memory

---

*ExpensePro v2 — Built as a portfolio project demonstrating full-stack Java development.*
