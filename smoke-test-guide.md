# POSTMAN_BACKEND_CHECK

## Muc tieu

Huong dan chay backend sau khi sua bug va kiem tra nhanh xem API da hoat dong chua bang Postman.

## File Postman da co san

- Collection: `PlanbookAI-API-Collection.postman_collection.json`
- Environment: `PlanbookAI-Environment.postman_environment.json`

## Luu y quan trong truoc khi chay

- Neu muon chay backend bang `mvn spring-boot:run`, chi nen chay database bang Docker.
- Khong nen chay ca backend container va backend local cung luc, vi deu dung cong `8080`.

## Cach chay backend de test bang Postman

### Cach 1: Chay backend local, database bang Docker

Day la cach nen dung de test sau khi vua sua code backend.

1. Mo terminal tai thu muc goc project.
2. Chi chay MySQL:

```cmd
docker-compose up -d db
```

3. Chay backend local:

```cmd
cd backend
```

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot'
$env:Path='C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot\bin;' + $env:Path
& 'C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.1\plugins\maven\lib\maven3\bin\mvn.cmd' spring-boot:run
```

Neu may ban khong co duong dan Maven nhu tren, co the dung Maven/IDE cua ban mien la backend start thanh cong o `http://localhost:8080`.

### Cach 2: Chay full bang Docker

Neu ban muon test nhanh ban container:

```cmd
docker-compose up -d
```

Sau do dung Postman goi truc tiep vao `http://localhost:8080`.

## Kiem tra backend da len chua

Mo 1 trong 2 URL sau tren browser:

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/v3/api-docs`

Neu mo duoc thi backend da len va co the bat dau test bang Postman.

## Import Postman

1. Mo Postman.
2. Import file `PlanbookAI-API-Collection.postman_collection.json`.
3. Import file `PlanbookAI-Environment.postman_environment.json`.
4. Chon environment `PlanbookAI Local`.
5. Xac nhan:
   - `base_url = http://localhost:8080`
   - `api_version = v1`

## Can chinh sua gi trong collection hien tai

Collection hien tai co mot so request cu khong con khop 100% voi backend. Nen sua cac muc duoi day truoc khi bam Send.

### 1. Sua script luu token trong `Register User` va `Login User`

Collection dang dung `jsonData.token`, trong khi backend hien tai tra ve:

- `accessToken`
- `refreshToken`
- `user`

Trong tab `Tests` cua `Register User` va `Login User`, sua ve:

```javascript
const jsonData = pm.response.json();
pm.environment.set("token", jsonData.accessToken);
pm.environment.set("refresh_token", jsonData.refreshToken);
```

Neu khong sua script, ban co the copy tay `accessToken` tu response roi paste vao env `token`.

### 2. Sua body `Register User`

Body dung:

```json
{
  "fullName": "John Teacher",
  "email": "teacher@example.com",
  "password": "Password123!"
}
```

Khong can gui field `role`.

### 3. Sua body `Create Question Bank`

Body mau trong collection dang dung `category`. Backend hien tai can:

```json
{
  "name": "Math Questions - Chapter 1",
  "subject": "Mathematics",
  "gradeLevel": "10",
  "description": "Algebra and geometry problems",
  "isPublished": false
}
```

### 4. Sua body `Generate AI Questions`

Body dung:

```json
{
  "bankId": 1,
  "subject": "Mathematics",
  "topic": "Quadratic Equations",
  "difficulty": "MEDIUM",
  "type": "MULTIPLE_CHOICE",
  "count": 5,
  "saveToDb": false
}
```

Collection cu dang dung `level`, `quantity`, `questionBankId`, cac field nay khong con dung.

### 5. Sua request `Create Order`

Body dung:

```json
{
  "packageId": 1,
  "paymentMethod": "BANK_TRANSFER"
}
```

Collection cu dang gui `subscriptionPackageId` va `price`, khong khop DTO hien tai.

### 6. Khong dung folder `Subscriptions` theo nguyen trang

Backend hien tai dung path:

- `GET /api/v1/packages`
- `POST /api/v1/packages`
- `PUT /api/v1/packages/{id}`
- `DELETE /api/v1/packages/{id}`

Collection dang tro den `/subscriptions`, can sua thanh `/packages` neu muon test nhom nay.

### 7. Bo qua mot so request cu khong con dung

Khong nen dung ngay cac request duoi day neu chua tu sua lai:

- `Questions > Update Question`
- `Orders > Get Order Details`
- `Subscriptions > ...` neu van de path `/subscriptions`

## Tai khoan seed co san de test

Neu `DataSeeder` chay thanh cong, ban co the login bang cac tai khoan sau:

- `admin@planbookai.com` / `admin`
- `manager@planbookai.com` / `admin`
- `staff@planbookai.com` / `admin`
- `teacher@planbookai.com` / `admin`

Nen test it nhat 2 role:

- `teacher` de test Question Bank, AI Generate, tao order
- `admin` de test Users, Orders, Packages

## Thu tu smoke test de kiem tra API

### A. Smoke test voi role Teacher

1. `Authentication > Login User`

Dung body:

```json
{
  "email": "teacher@planbookai.com",
  "password": "admin"
}
```

Ky vong:

- HTTP `200`
- Response co `accessToken`, `refreshToken`, `user`

2. `Authentication > Get Current User`

Ky vong:

- HTTP `200`
- Response co field `role = TEACHER`

3. `Question Banks > Get My Question Banks`

Ky vong:

- HTTP `200`
- Co the la mang rong neu chua co du lieu

4. `Question Banks > Create Question Bank`

Dung body:

```json
{
  "name": "Math Questions - Chapter 1",
  "subject": "Mathematics",
  "gradeLevel": "10",
  "description": "Algebra and geometry problems",
  "isPublished": false
}
```

Ky vong:

- HTTP `201`
- Response tra ve `id`

Sau khi co `id`, luu vao environment `bank_id`.

5. `Question Banks > Get Questions in Bank`

Dung `{{bank_id}}`.

Ky vong:

- HTTP `200`
- Thuong la mang rong neu chua co cau hoi

6. `Questions > Generate AI Questions`

Chi test neu ban da set `GEMINI_API_KEY`. Neu chua set, co the bo qua buoc nay.

Dung body:

```json
{
  "bankId": {{bank_id}},
  "subject": "Mathematics",
  "topic": "Quadratic Equations",
  "difficulty": "MEDIUM",
  "type": "MULTIPLE_CHOICE",
  "count": 3,
  "saveToDb": false
}
```

Ky vong:

- HTTP `200` neu preview
- Response la danh sach cau hoi AI sinh ra

7. `Orders > Create Order`

Dung body:

```json
{
  "packageId": 1,
  "paymentMethod": "BANK_TRANSFER"
}
```

Ky vong:

- HTTP `200`
- Response co `status = PENDING` hoac trang thai khoi tao tuong duong

8. Tao them request thu cong: `GET {{base_url}}/api/{{api_version}}/orders/my`

Ky vong:

- HTTP `200`
- Tra ve danh sach order cua teacher

### B. Smoke test voi role Admin

1. Login lai bang:

```json
{
  "email": "admin@planbookai.com",
  "password": "admin"
}
```

2. `Users > Get All Users (Admin)`

Ky vong:

- HTTP `200`
- Tra ve danh sach user

3. `Orders > Get All Orders`

Ky vong:

- HTTP `200`
- Tra ve danh sach order

4. Tao request thu cong: `GET {{base_url}}/api/{{api_version}}/packages`

Ky vong:

- HTTP `200`
- Tra ve danh sach goi `FREE`, `PRO`, `PREMIUM`

5. Tao request thu cong: `PUT {{base_url}}/api/{{api_version}}/orders/{{order_id}}/status`

Body vi du:

```json
{
  "status": "ACTIVE"
}
```

Ky vong:

- HTTP `200`
- Order duoc cap nhat trang thai

## Checklist dat yeu cau

Co the xem backend da hoat dong on cho Postman neu dat duoc cac dieu kien sau:

- Login teacher thanh cong
- `GET /auth/me` thanh cong
- Tao question bank thanh cong
- Xem question bank cua teacher thanh cong
- Tao order thanh cong
- Login admin thanh cong
- Lay danh sach user thanh cong
- Lay danh sach packages thanh cong

## Neu gap loi

- `401 Unauthorized`
  - Kiem tra env `token` da duoc cap nhat bang `accessToken` chua
  - Login lai roi goi request can auth

- `403 Forbidden`
  - Ban dang dung sai role cho endpoint
  - Thu doi token teacher/admin cho dung

- `400 Bad Request`
  - Kiem tra lai body co dung field moi hay khong
  - Dac biet voi `Question Bank`, `AI Generate`, `Order`

- `500 Internal Server Error`
  - Xem log backend
- Neu lien quan AI, kiem tra `GEMINI_API_KEY`
- Neu lien quan DB, kiem tra MySQL da len chua

- `502 AI_SERVICE_ERROR`
  - Backend da boot duoc, nhung tinh nang AI dang chua co `GEMINI_API_KEY`
  - Cac API khac van test binh thuong
  - Neu muon test `questions/ai-generate`, them `GEMINI_API_KEY` vao `.env` roi rebuild backend container

## Ghi chu cuoi

- File nay uu tien smoke test nhanh de xac nhan backend da song va endpoint chinh da dung.
- Neu can test day du hon, nen tao them 1 Postman collection da duoc cap nhat theo endpoint hien tai thay vi dung nguyen collection cu.
