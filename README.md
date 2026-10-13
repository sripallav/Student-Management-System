# Student Management System

A Full-Stack Student Management System built with **Core Java**, **JDBC**, **MySQL**, **HTML5**, **CSS3**, and **JavaScript**.

---

## 🚀 Features

- **Dashboard Analytics**: Real-time KPI cards for Total Students, Active Students, Inactive Students, and Average GPA.
- **Complete Student CRUD**:
  - Add new student records with real-time validation.
  - View full profile details card.
  - Edit existing student information.
  - Delete records with confirmation modal.
- **Search & Filtering**:
  - Fast search by name, roll number, email, or city.
  - Filter by Department / Course (Computer Science, AI, Data Science, IT, Cyber Security, Electronics).
  - Filter by status (Active / Inactive).
- **Export Data**: One-click **Export to CSV** for records.
- **Dark / Light Theme**: Built-in toggle with persistent preference in `localStorage`.
- **Zero Heavy Web Server Setup**: Uses Java standard library `com.sun.net.httpserver.HttpServer`—no Tomcat or external servlet container installation needed.
- **Auto-Initialization**: Automatically creates database tables and inserts seed records on startup if they don't already exist.

---

## 🛠️ Tech Stack

| Component | Technology |
|---|---|
| **Backend** | Core Java 21, JDBC (`mysql-connector-j`), Java HTTP Server (`jdk.httpserver`) |
| **Database** | MySQL 8.0, SQL (`student_management_db`) |
| **Frontend** | Semantic HTML5, Responsive Modern CSS3, Vanilla JavaScript (ES6+ Fetch API) |
| **Icons & Fonts** | Font Awesome 6.5, Google Fonts (Plus Jakarta Sans) |
| **Build Tool** | Apache Maven 3.9+ (or direct `javac` with included `lib/`) |

---

## 📁 Project Structure

```
MarketMind-Ai/
├── pom.xml                                 # Maven configuration
├── run.bat                                 # Windows one-click launcher
├── run.ps1                                 # PowerShell launcher script
├── README.md                               # Project documentation
├── database/
│   └── schema.sql                          # Database creation & seed data
├── lib/
│   └── mysql-connector-j-8.3.0.jar         # Offline MySQL driver jar
└── src/
    └── main/
        ├── java/
        │   └── com/student/
        │       ├── Main.java               # Application entry point
        │       ├── config/
        │       │   └── DatabaseConfig.java # JDBC connection & auto-table init
        │       ├── dao/
        │       │   ├── StudentDAO.java     # DAO Interface
        │       │   └── StudentDAOImpl.java # JDBC DAO Implementation
        │       ├── model/
        │       │   └── Student.java        # Student POJO model
        │       ├── server/
        │       │   ├── AppServer.java      # HTTP Server initialization
        │       │   ├── StudentApiHandler.java # REST API (/api/students)
        │       │   ├── StatsApiHandler.java   # Stats & Health endpoints
        │       │   └── StaticFileHandler.java # Frontend file server
        │       └── util/
        │           └── JsonUtil.java       # Zero-dependency JSON serializer
        └── resources/
            ├── db.properties               # MySQL credentials & port
            └── web/                        # Frontend Web UI
                ├── index.html              # Main dashboard view
                ├── css/
                │   └── style.css           # Modern styles & themes
                └── js/
                    └── app.js              # Frontend logic & API calls
```

---

## ⚙️ Configuration (`src/main/resources/db.properties`)

```properties
db.url=jdbc:mysql://localhost:3306/student_management_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=sripallav@.y
server.port=8080
```

> **Note**: If environment variables `DB_USERNAME` and `DB_PASSWORD` are present on your machine, they will automatically be used.

---

## 🏃 How to Run

### Option 1: Double-Click or Run Batch File (Easiest)
Run `run.bat`:
```cmd
.\run.bat
```

### Option 2: Using Maven
```bash
mvn compile exec:java
```

### Option 3: Using PowerShell Script
```powershell
.\run.ps1
```

Once started, open your web browser at:
👉 **`http://localhost:8080`**

---

## 📡 REST API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/students` | Get all students (supports `?q=...&course=...&status=...`) |
| `GET` | `/api/students/{id}` | Get single student details |
| `POST` | `/api/students` | Create new student |
| `PUT` | `/api/students/{id}` | Update existing student |
| `DELETE` | `/api/students/{id}` | Delete student record |
| `GET` | `/api/stats` | Get dashboard metrics and course distributions |
| `GET` | `/api/health` | Check MySQL database connectivity |
