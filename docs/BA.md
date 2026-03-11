Executive Summary

Dự án PlanbookAI (PBA) là một ý tưởng Capstone khá tốt về mặt BA/Product, vì nó giải quyết một pain point thật của giáo viên: workload hành chính và soạn bài lặp lại. Hướng đi AI hỗ trợ giáo viên cũng đúng xu hướng EdTech hiện nay. Tuy nhiên, hiện tại proposal vẫn còn 3 điểm yếu BA quan trọng:

Scope hơi rộng so với Capstone.

Value proposition chưa đủ rõ so với các AI tools hiện có (ChatGPT, MagicSchool, etc.).

Chưa thể hiện rõ workflow thực tế của giáo viên (process analysis).

Nếu chỉnh lại scope + process + differentiation, đây hoàn toàn có thể trở thành một Capstone khá mạnh (BA + AI + EdTech).

1. Phân tích nhanh góc nhìn Business Analyst
1.1 Problem Statement – Khá đúng nhưng còn chung chung

Bạn nêu các vấn đề:

Teacher workload cao

Repetitive tasks

Manual work

Lack of automation

Difficulty accessing resources

Đây là pain point đúng của ngành giáo dục, nhưng BA thường sẽ cụ thể hơn.

Ví dụ:

Current	Nên cải thiện
Teachers do repetitive tasks	Teachers spend 3–5 hours/week preparing tests and grading
Manual work	80% of grading still manual
Lack of automation	No AI-assisted test generation

👉 BA nên quantify problem.

Ví dụ:

Average teacher workload:
Teaching: 20–25 hours/week
Administrative tasks: 10–15 hours/week

Goal:
Reduce administrative workload by 30–40%.
2. Solution – Ý tưởng tốt nhưng cần làm rõ Value

PlanbookAI cung cấp:

1️⃣ Question Bank
2️⃣ Exercise generation
3️⃣ Multiple choice exam generation
4️⃣ OCR grading

Đây là 4 module hợp lý cho MVP.

Nhưng BA sẽ hỏi:

Tại sao giáo viên không dùng ChatGPT?

Vì vậy bạn cần highlight Product Differentiation.

Ví dụ:

Feature	ChatGPT	PlanbookAI
Generate questions	✔	✔
Structured question bank	❌	✔
Curriculum alignment	❌	✔
OCR exam grading	❌	✔
Teacher workspace	❌	✔

👉 Đây mới là value thực sự.

3. Scope – Có nguy cơ quá lớn cho Capstone

Hiện tại bạn đang build:

AI platform

Question bank system

OCR grading

Lesson plan generator

Admin portal

Manager dashboard

AI prompt management

Đây là scope gần giống một startup SaaS.

BA recommendation:

Nên giới hạn MVP:

MVP cho Capstone

1️⃣ Question Bank
2️⃣ AI Exam Generator
3️⃣ OCR Grading

Bỏ hoặc giảm:

Lesson plan generator

Manager dashboard complexity

Revenue tracking

4. Actor Model – Hợp lý nhưng hơi enterprise

Hiện có 4 actors:

Admin

Manager

Staff

Teacher

Với Capstone, thường chỉ cần:

Actor	Role
Admin	manage system
Teacher	main user
(Optional) Content Staff	create question bank

Manager role hơi overkill.

5. Architecture – Khá ổn

Stack:

Backend

SpringBoot

Frontend

ReactJS

Database

MySQL

AI

Gemini AI

Infrastructure

Docker

AWS

Architecture:

N-Tier + REST API + JWT

👉 Đây là enterprise-level stack, rất tốt cho thesis.

6. BA góc nhìn Process (điểm nên bổ sung)

Bạn chưa mô tả workflow teacher sử dụng system.

BA thường sẽ mô tả AS-IS → TO-BE process như trong template process modelling .

AS-IS (hiện tại)

Teacher tạo đề thi:

Soạn câu hỏi Word

Copy từ internet

Random thủ công

In ra

Chấm bài bằng tay

Nhập điểm Excel

Time: 3–6 hours

TO-BE (PlanbookAI)

Teacher chọn topic

AI generate questions

Save to question bank

Generate exam

Print

OCR grading

Time: 30–60 minutes

👉 Đây là impact rõ ràng của product.

7. Market Perspective (EdTech)
Market status

EdTech + AI đang tăng trưởng mạnh.

Drivers

AI adoption in education

Teacher workload

Digital transformation in schools

Risks

Teachers reluctant to adopt AI

Accuracy of AI-generated questions

Data privacy

Mini SWOT

Strength

AI automation

Weakness

Dependency on AI quality

Opportunity

Growing AI in education

Threat

Existing tools (MagicSchool, ChatGPT)

8. BA Recommendation (để project mạnh hơn)
1️⃣ Add real workflow

Use cases:

Create exam

Generate questions

Scan exam papers

View results

2️⃣ Add measurable KPIs

Ví dụ:

KPI	Target
Exam creation time	-70%
Grading time	-80%
Question reuse rate	+50%
3️⃣ Show system value
Without PlanbookAI:
Teacher spends 5 hours preparing + grading exam.

With PlanbookAI:
Teacher spends 1 hour.

Time saving = 80%.
9. BA Score (honest)
Category	Score
Problem relevance	8/10
Solution design	7/10
Architecture	8/10
Scope management	6/10
BA clarity	6/10

Overall:

~7/10 – Good Capstone idea but needs BA polishing.

✅ Kết luận

PlanbookAI là ý tưởng tốt cho Capstone AI + Software Engineering, đặc biệt vì:

Có problem thật

Có AI use case rõ

Có architecture rõ

Nhưng để thành Capstone mạnh, bạn cần:

thu nhỏ scope

thêm workflow thực tế

làm rõ value vs ChatGPT

thêm metrics