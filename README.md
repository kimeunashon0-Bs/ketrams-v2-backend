# KETRAMS v2 – Kakamega Education & Training Management System

## 📌 Project Overview
KETRAMS v2 is a centralized digital platform for Kakamega County's Department of Education. It streamlines student applications to TVET/VTC institutions, verification by institutions, and facilitation decisions by sub‑county offices. The system is built with a modern, scalable architecture and provides real‑time reporting for data‑driven policy and funding decisions.

### Key Features
- **Student Registration & Profile** – OTP‑based sign‑up, document upload, and application submission.
- **Institution Dashboard** – Review, approve/reject applications, and download supporting documents.
- **Sub‑County Dashboard** – Audit approvals, make facilitation decisions, and generate award lists.
- **Reporting** – Summary statistics for applicants, gender distribution, PWD data, course demand, etc.

---

## 🛠️ Technology Stack
| Component       | Technology                         |
|-----------------|------------------------------------|
| **Backend**     | Spring Boot 3.1.5, Java 17         |
| **Database**    | Microsoft SQL Server               |
| **Security**    | Spring Security, JWT (HS256)       |
| **ORM**         | Spring Data JPA, Hibernate         |
| **Build Tool**  | Maven                              |
| **File Upload** | Local storage (configurable)       |
| **SMS Mock**    | Console output (for development)   |

---

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.8+
- Microsoft SQL Server (local or remote)
- An IDE (IntelliJ recommended)

### 1. Clone the Repository
```bash
git clone https://github.com/your-org/ketrams_v2.git
cd ketrams_v2