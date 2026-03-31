# PlanbookAI – System Architecture Document (SAD)

**Project:** PlanbookAI – AI Tools Portal for High School Teachers  
**Abbreviation:** PBA  
**Version:** 1.0  
**Tech Stack:** Spring Boot · React.js · MySQL · Supabase · Gemini AI · Docker · AWS

---

## Table of Contents

1. [System Architecture Diagram](#1-system-architecture-diagram)
2. [Database Schema (ERD)](#2-database-schema-erd)
3. [API Design Overview](#3-api-design-overview)
4. [Component & Module Breakdown](#4-component--module-breakdown)

---

## 1. System Architecture Diagram

### 1.1 High-Level Architecture Overview

PlanbookAI follows an **N-Tier Architecture** pattern with clear separation between presentation, business logic, and data layers. All communication between tiers is conducted via RESTful APIs secured with JWT.

```
┌─────────────────────────────────────────────────────────────────────┐
│                          CLIENT LAYER                               │
│                                                                     │
│   ┌──────────────────────┐      ┌──────────────────────────────┐   │
│   │  Web Admin Portal    │      │   Teacher Dashboard (React)  │   │
│   │  (React.js)          │      │   - Lesson Plans             │   │
│   │  - Admin Panel       │      │   - Exam Generation          │   │
│   │  - Manager Dashboard │      │   - OCR Grading              │   │
│   │  - Staff Workspace   │      │   - Question Bank            │   │
│   └──────────┬───────────┘      └──────────────┬───────────────┘   │
└──────────────┼────────────────────────────────┼────────────────────┘
               │  HTTPS / REST                  │  HTTPS / REST
               ▼                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        API GATEWAY LAYER                            │
│                                                                     │
│              ┌─────────────────────────────────┐                   │
│              │   API Gateway / Load Balancer    │                   │
│              │   - Rate Limiting                │                   │
│              │   - JWT Validation               │                   │
│              │   - Request Routing              │                   │
│              └────────────────┬────────────────┘                   │
└───────────────────────────────┼─────────────────────────────────────┘
                                │
┌───────────────────────────────▼─────────────────────────────────────┐
│                       APPLICATION LAYER                              │
│                      (Spring Boot Services)                          │
│                                                                      │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌────────────┐ │
│  │  Auth Service│ │ User Service │ │ Lesson Plan  │ │  Question  │ │
│  │  (JWT/Login) │ │ (RBAC)       │ │ Service      │ │  Bank Svc  │ │
│  └──────────────┘ └──────────────┘ └──────────────┘ └────────────┘ │
│                                                                      │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌────────────┐ │
│  │  Exam Service│ │  OCR Service │ │ Grading Svc  │ │ AI Prompt  │ │
│  │  (Gen & Mgmt)│ │  (Scan/Parse)│ │ (Auto-grade) │ │ Service    │ │
│  └──────────────┘ └──────────────┘ └──────────────┘ └────────────┘ │
│                                                                      │
│  ┌──────────────┐ ┌──────────────┐                                  │
│  │ Subscription │ │  Reporting & │                                  │
│  │ & Order Svc  │ │  Analytics   │                                  │
│  └──────────────┘ └──────────────┘                                  │
└──────────────────────────┬──────────────────────────────────────────┘
                           │
       ┌───────────────────┼───────────────────┐
       ▼                   ▼                   ▼
┌──────────────┐  ┌────────────────┐  ┌─────────────────────┐
│  DATA LAYER  │  │ EXTERNAL AI    │  │  FILE STORAGE LAYER  │
│              │  │ SERVICES       │  │                      │
│  ┌─────────┐ │  │ ┌────────────┐ │  │  ┌───────────────┐  │
│  │  MySQL  │ │  │ │ Gemini AI  │ │  │  │   Supabase    │  │
│  │  (Core  │ │  │ │ (Text Gen  │ │  │  │   Storage     │  │
│  │  Data)  │ │  │ │  & Prompts)│ │  │  │  (Docs, PDFs, │  │
│  └─────────┘ │  │ └────────────┘ │  │  │   Images,     │  │
│              │  │ ┌────────────┐ │  │  │   Answer      │  │
│              │  │ │ OCR Engine │ │  │  │   Sheets)     │  │
│              │  │ │ (Vision AI)│ │  │  └───────────────┘  │
│              │  │ └────────────┘ │  │                      │
└──────────────┘  └────────────────┘  └─────────────────────┘
```

### 1.2 Deployment Architecture (AWS + Docker)

```
┌──────────────────────────────────────────────────────────────────┐
│                        AWS CLOUD                                 │
│                                                                  │
│   ┌───────────────────────────────────────────────────────┐     │
│   │                  VPC (Virtual Private Cloud)          │     │
│   │                                                       │     │
│   │  ┌─────────────┐        ┌──────────────────────────┐ │     │
│   │  │  CloudFront │───────▶│    Application Load       │ │     │
│   │  │  (CDN)      │        │    Balancer (ALB)         │ │     │
│   │  └─────────────┘        └──────────┬───────────────┘ │     │
│   │                                    │                  │     │
│   │              ┌─────────────────────┴──────────────┐  │     │
│   │              │         ECS / EC2 Cluster           │  │     │
│   │              │  ┌─────────────┐ ┌───────────────┐ │  │     │
│   │              │  │  Frontend   │ │   Backend API  │ │  │     │
│   │              │  │  Container  │ │   Container    │ │  │     │
│   │              │  │  (React)    │ │  (Spring Boot) │ │  │     │
│   │              │  └─────────────┘ └───────────────┘ │  │     │
│   │              └────────────────────────────────────┘  │     │
│   │                                    │                  │     │
│   │                    ┌───────────────▼──────────┐       │     │
│   │                    │      RDS (MySQL)          │       │     │
│   │                    │   (Private Subnet)        │       │     │
│   │                    └──────────────────────────┘       │     │
│   └───────────────────────────────────────────────────────┘     │
│                                                                  │
│   External:  Supabase Storage · Gemini AI API                   │
└──────────────────────────────────────────────────────────────────┘
```

---

## 2. Database Schema (ERD)

### 2.1 Entity Overview

| Entity | Description |
|--------|-------------|
| `users` | All system users (Admin, Manager, Staff, Teacher) |
| `roles` | Role definitions (Admin, Manager, Staff, Teacher) |
| `subscription_packages` | Tiered service plans offered to teachers |
| `orders` | Teacher subscription purchase records |
| `curriculum_frameworks` | Lesson plan templates designed by Admin |
| `lesson_plans` | Teacher-created lesson plans |
| `question_banks` | Categorized question repository |
| `questions` | Individual questions with metadata |
| `exams` | Generated exam papers |
| `exam_questions` | Junction table: exam ↔ questions |
| `answer_sheets` | Scanned student answer sheets |
| `grading_results` | Grading output per student per exam |
| `ai_prompt_templates` | Reusable AI prompt templates by Staff |
| `student_results` | Aggregated student performance records |

---

### 2.2 Full Database Schema

```sql
-- ============================================================
-- USERS & ROLES
-- ============================================================

CREATE TABLE roles (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    name        ENUM('ADMIN','MANAGER','STAFF','TEACHER') NOT NULL UNIQUE,
    description TEXT,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id          INT NOT NULL,
    full_name        VARCHAR(255) NOT NULL,
    email            VARCHAR(255) NOT NULL UNIQUE,
    password_hash    VARCHAR(512) NOT NULL,
    phone            VARCHAR(20),
    avatar_url       VARCHAR(512),
    is_active        BOOLEAN DEFAULT TRUE,
    email_verified   BOOLEAN DEFAULT FALSE,
    created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- ============================================================
-- SUBSCRIPTION & ORDERS
-- ============================================================

CREATE TABLE subscription_packages (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    price           DECIMAL(12,2) NOT NULL,
    duration_days   INT NOT NULL,
    features        JSON,
    is_active       BOOLEAN DEFAULT TRUE,
    created_by      BIGINT,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE orders (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT NOT NULL,
    package_id      INT NOT NULL,
    status          ENUM('PENDING','ACTIVE','EXPIRED','CANCELLED') DEFAULT 'PENDING',
    amount_paid     DECIMAL(12,2),
    payment_method  VARCHAR(100),
    started_at      DATETIME,
    expires_at      DATETIME,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (package_id) REFERENCES subscription_packages(id)
);

-- ============================================================
-- CURRICULUM & LESSON PLANS
-- ============================================================

CREATE TABLE curriculum_frameworks (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    title           VARCHAR(255) NOT NULL,
    subject         VARCHAR(100),
    grade_level     VARCHAR(50),
    description     TEXT,
    structure       JSON,       -- Defines components: objectives, activities, assessment
    created_by      BIGINT,
    is_published    BOOLEAN DEFAULT FALSE,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE lesson_plans (
    id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
    teacher_id            BIGINT NOT NULL,
    framework_id          INT,
    title                 VARCHAR(255) NOT NULL,
    subject               VARCHAR(100),
    grade_level           VARCHAR(50),
    topic                 VARCHAR(255),
    objectives            TEXT,
    activities            TEXT,
    assessment            TEXT,
    materials             TEXT,
    duration_minutes      INT,
    ai_generated          BOOLEAN DEFAULT FALSE,
    status                ENUM('DRAFT','PUBLISHED') DEFAULT 'DRAFT',
    created_at            DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES users(id),
    FOREIGN KEY (framework_id) REFERENCES curriculum_frameworks(id)
);

-- ============================================================
-- QUESTION BANK
-- ============================================================

CREATE TABLE question_banks (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(255) NOT NULL,
    subject         VARCHAR(100),
    grade_level     VARCHAR(50),
    description     TEXT,
    created_by      BIGINT,
    is_published    BOOLEAN DEFAULT FALSE,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE questions (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    bank_id         INT NOT NULL,
    created_by      BIGINT NOT NULL,
    content         TEXT NOT NULL,
    type            ENUM('MULTIPLE_CHOICE','SHORT_ANSWER','FILL_IN_BLANK') NOT NULL,
    difficulty      ENUM('EASY','MEDIUM','HARD') DEFAULT 'MEDIUM',
    topic           VARCHAR(255),
    options         JSON,           -- For multiple choice: [{label, text, is_correct}]
    correct_answer  TEXT,
    explanation     TEXT,
    ai_generated    BOOLEAN DEFAULT FALSE,
    is_approved     BOOLEAN DEFAULT FALSE,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (bank_id) REFERENCES question_banks(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- ============================================================
-- EXAMS
-- ============================================================

CREATE TABLE exams (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    teacher_id      BIGINT NOT NULL,
    title           VARCHAR(255) NOT NULL,
    subject         VARCHAR(100),
    grade_level     VARCHAR(50),
    topic           VARCHAR(255),
    total_questions INT DEFAULT 0,
    duration_mins   INT,
    randomized      BOOLEAN DEFAULT FALSE,
    version_count   INT DEFAULT 1,
    status          ENUM('DRAFT','PUBLISHED','CLOSED') DEFAULT 'DRAFT',
    ai_generated    BOOLEAN DEFAULT FALSE,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES users(id)
);

CREATE TABLE exam_questions (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    exam_id         BIGINT NOT NULL,
    question_id     BIGINT NOT NULL,
    order_index     INT DEFAULT 0,
    version_number  INT DEFAULT 1,
    points          DECIMAL(5,2) DEFAULT 1.00,
    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id)
);

-- ============================================================
-- OCR GRADING
-- ============================================================

CREATE TABLE answer_sheets (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    exam_id         BIGINT NOT NULL,
    teacher_id      BIGINT NOT NULL,
    student_name    VARCHAR(255),
    student_code    VARCHAR(100),
    file_url        VARCHAR(512) NOT NULL,   -- Stored in Supabase
    ocr_status      ENUM('PENDING','PROCESSING','COMPLETED','FAILED') DEFAULT 'PENDING',
    ocr_raw_data    JSON,
    uploaded_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (exam_id) REFERENCES exams(id),
    FOREIGN KEY (teacher_id) REFERENCES users(id)
);

CREATE TABLE grading_results (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    answer_sheet_id BIGINT NOT NULL,
    exam_id         BIGINT NOT NULL,
    student_name    VARCHAR(255),
    student_code    VARCHAR(100),
    total_score     DECIMAL(6,2),
    max_score       DECIMAL(6,2),
    percentage      DECIMAL(5,2),
    answer_detail   JSON,   -- [{question_id, student_answer, correct_answer, is_correct, points}]
    teacher_feedback TEXT,
    ai_feedback      TEXT,
    graded_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (answer_sheet_id) REFERENCES answer_sheets(id),
    FOREIGN KEY (exam_id) REFERENCES exams(id)
);

-- ============================================================
-- AI PROMPT TEMPLATES
-- ============================================================

CREATE TABLE ai_prompt_templates (
    id              INT PRIMARY KEY AUTO_INCREMENT,
    created_by      BIGINT NOT NULL,
    title           VARCHAR(255) NOT NULL,
    purpose         ENUM('LESSON_PLAN','QUESTION_GEN','EXERCISE_GEN','FEEDBACK') NOT NULL,
    prompt_text     TEXT NOT NULL,
    variables       JSON,           -- [{name, description, example}]
    is_approved     BOOLEAN DEFAULT FALSE,
    approved_by     BIGINT,
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (approved_by) REFERENCES users(id)
);
```

---

### 2.3 ERD Relationship Summary

```
users ──────────────< orders >──────────────── subscription_packages
  │
  ├──────────────< lesson_plans >──────────── curriculum_frameworks
  │
  ├──────────────< question_banks
  │                    └──< questions
  │
  ├──────────────< exams
  │                    └──< exam_questions >── questions
  │
  ├──────────────< answer_sheets >──────────── exams
  │                    └──< grading_results
  │
  └──────────────< ai_prompt_templates
```

---

## 3. API Design Overview

### 3.1 General Conventions

| Convention | Standard |
|------------|----------|
| **Base URL** | `https://api.planbookai.com/api/v1` |
| **Protocol** | HTTPS only |
| **Format** | JSON (application/json) |
| **Authentication** | Bearer JWT (`Authorization: Bearer <token>`) |
| **Versioning** | URI-based (`/api/v1/`) |
| **Error Format** | `{ "status": 4xx, "error": "...", "message": "...", "timestamp": "..." }` |
| **Pagination** | `?page=0&size=20&sort=createdAt,desc` |

### 3.2 HTTP Status Codes

| Code | Meaning |
|------|---------|
| `200` | OK – Successful GET/PUT |
| `201` | Created – Successful POST |
| `204` | No Content – Successful DELETE |
| `400` | Bad Request – Validation error |
| `401` | Unauthorized – Invalid/missing JWT |
| `403` | Forbidden – Insufficient role |
| `404` | Not Found |
| `409` | Conflict – Duplicate resource |
| `500` | Internal Server Error |

---

### 3.3 API Endpoints

#### 🔐 Authentication

| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| `POST` | `/auth/register` | Register new teacher account | Public |
| `POST` | `/auth/login` | Login and receive JWT token | Public |
| `POST` | `/auth/refresh` | Refresh JWT access token | Authenticated |
| `POST` | `/auth/logout` | Invalidate token | Authenticated |
| `POST` | `/auth/forgot-password` | Send reset password email | Public |
| `POST` | `/auth/reset-password` | Reset password with token | Public |

---

#### 👤 User Management

| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| `GET` | `/users` | List all users (paginated) | Admin |
| `POST` | `/users` | Create a new user | Admin |
| `GET` | `/users/{id}` | Get user by ID | Admin |
| `PUT` | `/users/{id}` | Update user info | Admin |
| `DELETE` | `/users/{id}` | Deactivate user | Admin |
| `GET` | `/users/me` | Get current user profile | All |
| `PUT` | `/users/me` | Update own profile | All |

---

#### 📦 Subscription Packages & Orders

| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| `GET` | `/packages` | List all active packages | All |
| `POST` | `/packages` | Create new package | Manager |
| `PUT` | `/packages/{id}` | Update package | Manager |
| `DELETE` | `/packages/{id}` | Deactivate package | Manager |
| `POST` | `/orders` | Purchase a package | Teacher |
| `GET` | `/orders` | List all orders | Manager |
| `GET` | `/orders/my` | Teacher's own orders | Teacher |
| `GET` | `/orders/{id}` | Get order detail | Manager, Teacher |
| `PUT` | `/orders/{id}/status` | Update order status | Manager |

---

#### 📋 Curriculum Frameworks (Lesson Plan Templates)

| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| `GET` | `/frameworks` | List frameworks | All |
| `POST` | `/frameworks` | Create framework | Admin |
| `GET` | `/frameworks/{id}` | Get framework detail | All |
| `PUT` | `/frameworks/{id}` | Update framework | Admin |
| `DELETE` | `/frameworks/{id}` | Delete framework | Admin |
| `PUT` | `/frameworks/{id}/publish` | Publish framework | Admin |

---

#### 📝 Lesson Plans

| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| `GET` | `/lesson-plans` | List teacher's lesson plans | Teacher |
| `POST` | `/lesson-plans` | Create lesson plan (manual) | Teacher |
| `POST` | `/lesson-plans/ai-generate` | Generate lesson plan via AI | Teacher |
| `GET` | `/lesson-plans/{id}` | Get lesson plan detail | Teacher |
| `PUT` | `/lesson-plans/{id}` | Update lesson plan | Teacher |
| `DELETE` | `/lesson-plans/{id}` | Delete lesson plan | Teacher |
| `PUT` | `/lesson-plans/{id}/publish` | Publish lesson plan | Teacher |

**Sample Request – AI Generate Lesson Plan:**
```json
POST /lesson-plans/ai-generate
{
  "subject": "Chemistry",
  "grade_level": "10",
  "topic": "Atomic Structure",
  "objectives": "Students will understand electron configuration",
  "duration_minutes": 45,
  "framework_id": 3,
  "prompt_template_id": 7
}
```

---

#### ❓ Question Bank

| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| `GET` | `/question-banks` | List question banks | All |
| `POST` | `/question-banks` | Create a question bank | Staff, Teacher |
| `GET` | `/question-banks/{id}` | Get bank detail | All |
| `PUT` | `/question-banks/{id}` | Update bank | Staff, Teacher |
| `DELETE` | `/question-banks/{id}` | Delete bank | Staff, Teacher |
| `GET` | `/question-banks/{id}/questions` | List questions in bank | All |
| `POST` | `/questions` | Create question manually | Staff, Teacher |
| `POST` | `/questions/ai-generate` | Generate questions via AI | Teacher |
| `GET` | `/questions/{id}` | Get question detail | All |
| `PUT` | `/questions/{id}` | Update question | Staff, Teacher |
| `DELETE` | `/questions/{id}` | Delete question | Staff, Teacher |
| `PUT` | `/questions/{id}/approve` | Approve a question | Manager |

**Sample Request – AI Generate Questions:**
```json
POST /questions/ai-generate
{
  "bank_id": 5,
  "subject": "Chemistry",
  "topic": "Periodic Table",
  "difficulty": "MEDIUM",
  "type": "MULTIPLE_CHOICE",
  "count": 10
}
```

---

#### 📄 Exams

| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| `GET` | `/exams` | List teacher's exams | Teacher |
| `POST` | `/exams` | Create exam manually | Teacher |
| `POST` | `/exams/ai-generate` | Generate exam from AI + question bank | Teacher |
| `GET` | `/exams/{id}` | Get exam detail | Teacher |
| `PUT` | `/exams/{id}` | Update exam | Teacher |
| `DELETE` | `/exams/{id}` | Delete exam | Teacher |
| `PUT` | `/exams/{id}/publish` | Publish exam | Teacher |
| `GET` | `/exams/{id}/questions` | List questions in exam | Teacher |
| `POST` | `/exams/{id}/questions` | Add questions to exam | Teacher |
| `DELETE` | `/exams/{id}/questions/{qid}` | Remove question from exam | Teacher |

---

#### 🖨️ OCR Grading

| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| `POST` | `/answer-sheets/upload` | Upload scanned answer sheets | Teacher |
| `GET` | `/answer-sheets` | List uploaded sheets | Teacher |
| `GET` | `/answer-sheets/{id}` | Get sheet detail + OCR data | Teacher |
| `POST` | `/answer-sheets/{id}/process` | Trigger OCR processing | Teacher |
| `GET` | `/grading-results` | List grading results by exam | Teacher |
| `GET` | `/grading-results/{id}` | Get individual result | Teacher |
| `PUT` | `/grading-results/{id}/feedback` | Add teacher/AI feedback | Teacher |

**Sample Request – Upload Answer Sheet:**
```
POST /answer-sheets/upload
Content-Type: multipart/form-data

{
  exam_id: 12,
  files: [<file1.jpg>, <file2.jpg>]
}
```

---

#### 🤖 AI Prompt Templates

| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| `GET` | `/prompt-templates` | List all templates | Staff, Manager |
| `POST` | `/prompt-templates` | Create template | Staff |
| `GET` | `/prompt-templates/{id}` | Get template detail | Staff, Manager |
| `PUT` | `/prompt-templates/{id}` | Update template | Staff |
| `DELETE` | `/prompt-templates/{id}` | Delete template | Staff |
| `PUT` | `/prompt-templates/{id}/approve` | Approve template | Manager |

---

#### 📊 Analytics & Reporting

| Method | Endpoint | Description | Role |
|--------|----------|-------------|------|
| `GET` | `/analytics/revenue` | Revenue overview | Admin, Manager |
| `GET` | `/analytics/users` | User growth metrics | Admin |
| `GET` | `/analytics/exams/{id}/results` | Exam score analytics | Teacher |
| `GET` | `/analytics/students` | Student progress by teacher | Teacher |

---

## 4. Component & Module Breakdown

### 4.1 Frontend – React.js Application

```
src/
├── app/
│   ├── store.js                  # Redux store
│   └── routes.jsx                # Route definitions
│
├── features/
│   ├── auth/
│   │   ├── LoginPage.jsx
│   │   ├── RegisterPage.jsx
│   │   └── authSlice.js
│   │
│   ├── dashboard/
│   │   ├── AdminDashboard.jsx
│   │   ├── ManagerDashboard.jsx
│   │   ├── StaffDashboard.jsx
│   │   └── TeacherDashboard.jsx
│   │
│   ├── users/
│   │   ├── UserList.jsx
│   │   ├── UserForm.jsx
│   │   └── usersSlice.js
│   │
│   ├── lesson-plans/
│   │   ├── LessonPlanList.jsx
│   │   ├── LessonPlanEditor.jsx
│   │   ├── AIGenerateForm.jsx
│   │   └── lessonPlansSlice.js
│   │
│   ├── question-bank/
│   │   ├── QuestionBankList.jsx
│   │   ├── QuestionForm.jsx
│   │   ├── AIQuestionGenerator.jsx
│   │   └── questionBankSlice.js
│   │
│   ├── exams/
│   │   ├── ExamList.jsx
│   │   ├── ExamBuilder.jsx
│   │   ├── AIExamGenerator.jsx
│   │   └── examsSlice.js
│   │
│   ├── grading/
│   │   ├── AnswerSheetUploader.jsx
│   │   ├── OCRResultViewer.jsx
│   │   ├── GradingResultList.jsx
│   │   └── gradingSlice.js
│   │
│   ├── subscriptions/
│   │   ├── PackageList.jsx
│   │   ├── OrderHistory.jsx
│   │   └── subscriptionsSlice.js
│   │
│   └── analytics/
│       ├── RevenueChart.jsx
│       ├── StudentProgressChart.jsx
│       └── ExamResultSummary.jsx
│
├── components/
│   ├── layout/
│   │   ├── Navbar.jsx
│   │   ├── Sidebar.jsx
│   │   └── Footer.jsx
│   ├── ui/
│   │   ├── Button.jsx
│   │   ├── Modal.jsx
│   │   ├── Table.jsx
│   │   ├── Form.jsx
│   │   └── FileUploader.jsx
│   └── guards/
│       └── RoleGuard.jsx        # Role-based route protection
│
├── services/
│   ├── api.js                   # Axios instance + interceptors
│   ├── authService.js
│   ├── lessonPlanService.js
│   ├── questionService.js
│   ├── examService.js
│   ├── gradingService.js
│   └── analyticsService.js
│
└── utils/
    ├── jwtUtils.js
    ├── formatters.js
    └── validators.js
```

---

### 4.2 Backend – Spring Boot Application

```
src/main/java/com/planbookai/
│
├── config/
│   ├── SecurityConfig.java          # JWT + Spring Security setup
│   ├── CorsConfig.java
│   ├── SwaggerConfig.java
│   └── GeminiAIConfig.java          # AI client configuration
│
├── controller/
│   ├── AuthController.java
│   ├── UserController.java
│   ├── SubscriptionController.java
│   ├── OrderController.java
│   ├── CurriculumFrameworkController.java
│   ├── LessonPlanController.java
│   ├── QuestionBankController.java
│   ├── QuestionController.java
│   ├── ExamController.java
│   ├── AnswerSheetController.java
│   ├── GradingController.java
│   ├── PromptTemplateController.java
│   └── AnalyticsController.java
│
├── service/
│   ├── AuthService.java
│   ├── UserService.java
│   ├── LessonPlanService.java
│   ├── QuestionService.java
│   ├── ExamService.java
│   ├── OCRService.java              # Calls Gemini Vision API
│   ├── GradingService.java
│   ├── AIGenerationService.java     # Calls Gemini Text API
│   ├── SubscriptionService.java
│   └── AnalyticsService.java
│
├── repository/
│   ├── UserRepository.java
│   ├── LessonPlanRepository.java
│   ├── QuestionRepository.java
│   ├── ExamRepository.java
│   ├── AnswerSheetRepository.java
│   ├── GradingResultRepository.java
│   └── PromptTemplateRepository.java
│
├── model/
│   ├── entity/
│   │   ├── User.java
│   │   ├── Role.java
│   │   ├── LessonPlan.java
│   │   ├── Question.java
│   │   ├── Exam.java
│   │   ├── ExamQuestion.java
│   │   ├── AnswerSheet.java
│   │   ├── GradingResult.java
│   │   ├── SubscriptionPackage.java
│   │   ├── Order.java
│   │   └── AIPromptTemplate.java
│   └── dto/
│       ├── request/
│       │   ├── LoginRequest.java
│       │   ├── LessonPlanRequest.java
│       │   ├── AIGenerateRequest.java
│       │   ├── ExamRequest.java
│       │   └── UploadAnswerSheetRequest.java
│       └── response/
│           ├── AuthResponse.java
│           ├── LessonPlanResponse.java
│           ├── GradingResultResponse.java
│           └── PagedResponse.java
│
├── security/
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── UserDetailsServiceImpl.java
│
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── UnauthorizedException.java
│   └── AIServiceException.java
│
└── util/
    ├── FileStorageUtil.java         # Supabase file operations
    ├── OCRUtil.java
    └── PromptBuilder.java           # Constructs prompts for Gemini
```

---

### 4.3 Module Interaction Map

```
┌─────────────┐     AI Generate      ┌───────────────────┐
│  Lesson Plan│ ──────────────────▶  │  AIGenerationSvc  │
│  Module     │                       │  (Gemini AI)      │
└─────────────┘                       └───────────────────┘
                                              ▲
┌─────────────┐     AI Generate              │
│  Question   │ ─────────────────────────────┘
│  Bank Module│
└──────┬──────┘
       │ Supplies questions
       ▼
┌─────────────┐     Upload Sheets    ┌───────────────────┐
│  Exam       │ ◀────────────────    │  OCR Service      │
│  Module     │     grade results    │  (Gemini Vision)  │
└──────┬──────┘                       └─────────┬─────────┘
       │                                         │
       └──────────────────┬──────────────────────┘
                          ▼
                 ┌─────────────────┐
                 │  Grading Module │
                 │  + Feedback     │
                 └────────┬────────┘
                          │
                          ▼
                 ┌─────────────────┐
                 │  Analytics &    │
                 │  Reporting      │
                 └─────────────────┘
```

---

### 4.4 Role-Based Access Control (RBAC) Matrix

| Feature | Admin | Manager | Staff | Teacher |
|---------|-------|---------|-------|---------|
| User Management | ✅ Full | ❌ | ❌ | ❌ |
| Curriculum Frameworks | ✅ Full | ❌ | 👁️ View | 👁️ View |
| Subscription Packages | ❌ | ✅ Full | ❌ | 👁️ View |
| Orders | ❌ | ✅ Full | ❌ | Own only |
| Lesson Plans | ❌ | ❌ | ✅ Sample | Own only |
| Question Bank | ❌ | ✅ Approve | ✅ Create | Own only |
| AI Prompt Templates | ❌ | ✅ Approve | ✅ CRUD | ❌ |
| Exams | ❌ | ❌ | ❌ | Own only |
| OCR / Grading | ❌ | ❌ | ❌ | Own only |
| Analytics – Revenue | ✅ | ✅ | ❌ | ❌ |
| Analytics – Students | ❌ | ❌ | ❌ | Own only |

---

*Document generated for PlanbookAI Capstone Project – Version 1.0*
