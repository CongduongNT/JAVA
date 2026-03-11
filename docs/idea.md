- English: PlanbookAI - Build an AI Tools Portal for High School Teachers.
- Vietnamese: PlanbookAI - Xây dựng cổng công cụ AI dành cho giáo viên trung học phổ thông.
- Abbreviation: PBA
- Within the Capstone Project scope, PlanbookAI – an AI Tool Portal for High School Teachers – focuses on supporting Chemistry teachers, due to limited time for development.
3.2 Context & Problem Statement:
- Teachers in high schools handle both teaching responsibilities and administrative tasks, including lesson planning, grading, attendance tracking, and reporting.
- Repetitive Tasks: Many of these tasks are cyclical, occurring daily or weekly, such as taking grading assignments, and preparing lesson plans. This repetition adds to teachers’ workload.
- Manual Work: A large portion of these tasks are done manually or with fragmented tools, which leads to inefficiencies and a lot of time spent on administrative work.
- Lack of Automation: Existing systems do not have intelligent automation, meaning that teachers still perform many tasks by hand, reducing overall productivity and increasing stress.
- Difficulty in Accessing Resources: Teachers face challenges in finding and using appropriate templates, teaching materials, and reference resources, making their work less efficient.
3.3 Our solution:
- PlanbookAI – AI Tool Portal for High School Teachers is a platform that supports teachers in their teaching tasks through specialized AI tools, offering utilities such as lesson planning, exam creation, and grading. The platform helps teachers reduce manual work and improve teaching efficiency. Additionally, each teacher has their own workspace to store reference materials and organize their teaching resources conveniently.
* Key Features of the PlanbookAI - Build an AI Tools Portal for High School Teachers include:
a. Question Bank Management
● The platform provides a centralized system for storing, organizing, and categorizing questions by subject, topic, and difficulty level. Teachers can easily retrieve and reuse questions for exercises, quizzes, and exams. This feature helps maintain a consistent and comprehensive repository of assessment materials across the school.
b. Exercise Creation
● Teachers can generate exercises automatically based on selected topics, learning objectives, and student proficiency levels. Exercises may include a variety of formats such as short answer, fill-in-the-blank, or multiple choice. This reduces time spent on manual preparation while ensuring alignment with the curriculum.
c. Multiple Choice Exam Generation
● The system allows teachers to quickly generate multiple choice test papers tailored to specific topics, grade levels, and cognitive goals. Teachers can define the number of questions, randomize question order, and assign different versions for test security. All test items are sourced from the question bank to ensure consistency.
d. OCR-Based Exam Grading
● The platform supports Optical Character Recognition (OCR) to scan and grade student answer sheets automatically. It works with both printed and handwritten responses. This feature provides rapid feedback, minimizes grading errors, and reduces the manual burden of processing test results.

Over time, the system will integrate additional tools to further enhance its functionality and provide even more support to teachers in their teaching tasks.
3.4 Actors
a. Admin
● User Management: Create, update, and manage user accounts and roles.
● System Configuration: Configure global system settings and behavior.
● Curriculum Framework Management: Design and manage templates used for lesson plan creation, which may consist of multiple structured components (e.g., objectives, activities, assessments).
● Revenue Tracking: View and manage financial metrics, including subscriptions, sales, and total revenue.
b. Manager
● Package Management: Create and manage service or subscription packages offered to users.
● Order Management: View and track customer orders and subscriptions.
● Content Approval: Review and approve content created by staff, including lesson plans, question banks, and AI prompts, before they are published or used.
c. Staff
● Create Sample Lesson Plans: Develop structured sample lesson plans based on predefined templates.
● Build Question Bank: Create categorized sample questions by topic, subject, or level.
● CRUD Prompting Templates: Create, read, update, and delete AI prompt templates that guide the generation of educational content using AI.
d. Teacher
● Create Lesson Plans & Test Content: Design personalized lesson plans and generate test materials using the platform’s templates and AI capabilities.
● Use OCR Tools: Convert scanned or printed teaching materials into digital content through integrated Optical Character Recognition (OCR) features.
● Grade & Feedback: (Only applicable for multiple choice questions) – The system will automatically grade using OCR, and teachers can provide feedback (handwritten or AI-suggested).
Note: For practice exercises, teachers will grade and manage them outside the system.
● View student results & analysis: Monitor student progress and scores, and adjust teaching methods accordingly.
3.5. Non-functional requirements:
- API Consistency & Compatibility: Use RESTful standards for consistent, simple, and scalable cross-platform APIs.
- Performance & Scalability: Ensure fast response and high concurrency, with flexible system scaling.
- Role-based Access Control: Restrict features by user role (Admin, Manager, Staff, Teacher) to enhance security.
3.6. Main Proposal Content (System Requirements and Deliverables)
3.6.1 Documentation Requirements
- Complete software development lifecycle documentation using UML 2.0
- Required documents:
+ User Requirements Document (URD)
+ Software Requirements Specification (SRS)
+ Software Architecture Document (SAD)
+ Detailed Design Document (DDD)
+ Implementation Documentation
+ Test Documentation
+ Installation Guide
+ Source Code Documentation
+ Deployment Package Documentation
3.6.2 Technical Stack
- Backend: SpringBoot
- Frontend: ReactJs
- API: RESTful API
- Database: MySQL
- Third-party Services: Superbase, Gemini AI
- Deployment: Docker, AWS
3.6.3 System Components
Applications:
- Web-based Administrative Portal
- Manager Dashboard
- RESTful API Services
Data Storage:
- Relational Database (MySQL): Core business data
Architecture & Infrastructure:
- N-Tier Architecture
- Authentication: JWT (JSON Web Tokens)
- API Communication:
+ REST APIs