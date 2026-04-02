# POSTMAN CHECKLIST BY FOLDER

## Muc tieu

Checklist nay duoc sap theo dung folder request trong Postman collection hien tai:

- `Authentication`
- `Users`
- `Question Banks`
- `Questions`
- `Orders`
- `Subscriptions`

Muc tieu:

- tick theo tung request trong Postman
- ghi nhanh expected status
- canh bao request nao dang "pass gia"
- bat som `400`, `401`, `403`, `404`, `409`, `500`, `502`, `503`

## Truoc khi tick checklist

### Environment

- [ ] Chon environment `PlanbookAI Local`
- [ ] `base_url = http://localhost:8080`
- [ ] `api_version = v1`
- [ ] Backend dang `Up`
- [ ] MySQL dang `healthy`

### Token script can sua truoc

Trong `Register User` va `Login User`, sua script `Tests` thanh:

```javascript
const jsonData = pm.response.json();
pm.environment.set("token", jsonData.accessToken);
pm.environment.set("refresh_token", jsonData.refreshToken);
```

- [ ] Da sua script luu token

### Tai khoan co san de test

- [ ] Teacher: `teacher@planbookai.com / admin`
- [ ] Admin: `admin@planbookai.com / admin`
- [ ] Manager: `manager@planbookai.com / admin`

Luu y:

- Muon test theo role, KHONG dung `Register User` de tao admin/manager.
- Dung cac tai khoan seed san hoac login admin roi goi API user management.

## Folder: Authentication

### Register User

- [ ] `Register User` voi email moi
Expected: `200`
Note: backend hien tai tra `AuthResponse` ngay sau khi register, khong phai `201`
Canh bao: register KHONG phan role theo body; self-registration hien tai luon tao `TEACHER`

- [ ] `Register User` voi email da ton tai
Expected: `409`
Canh bao: neu ra `500` thi bug exception mapping

- [ ] `Register User` voi body rong/null
Expected: nen la `400`
Canh bao: request DTO chua co validation chat, co the ra status khac

Body dung:

```json
{
  "fullName": "John Teacher",
  "email": "teacher-new@example.com",
  "password": "Password123!"
}
```

- [ ] Khong dung `role: "ADMIN"` trong request `Register User`
Expected: field nay bi bo qua
Canh bao: neu response van la `TEACHER` thi do la hanh vi dung theo code hien tai, khong phai bug

### Login User

- [ ] `Login User` voi teacher dung mat khau
Expected: `200`

- [ ] `Login User` sai password
Expected: `401`

- [ ] `Login User` email khong ton tai
Expected: `401`

- [ ] `Login User` body thieu field
Expected: nen la `400`
Canh bao: co the ra `401` hoac `500` vi validation chua day du

Body teacher:

```json
{
  "email": "teacher@planbookai.com",
  "password": "admin"
}
```

### Get Current User

- [ ] `Get Current User` voi teacher token
Expected: `200`

- [ ] `Get Current User` khong gui token
Expected: `401`

- [ ] `Get Current User` voi token rac
Expected: `401`

### Refresh Token

- [ ] `Refresh Token` voi refresh token hop le
Expected: `200`

- [ ] `Refresh Token` voi token rac
Expected: nen la `401`
Canh bao: neu ra `500` thi bug exception handling

### Logout

- [ ] `Logout` voi token hop le
Expected: `200`

## Folder: Users

### Get All Users (Admin)

- [ ] Admin goi `Get All Users (Admin)`
Expected: `200`

- [ ] Teacher goi `Get All Users (Admin)`
Expected: `403`
Canh bao: neu ra `200` thi bug security

### Get User by ID

- [ ] Admin goi `Get User by ID`
Expected: `200`

- [ ] Teacher goi `Get User by ID`
Expected: nen la `403` hoac chi doc chinh minh
Canh bao: neu ra `200` thi bug security, vi route nay dang thieu `@PreAuthorize`

- [ ] `Get User by ID` voi id khong ton tai
Expected: `404`

### Create New User

- [ ] Admin goi `Create New User` voi body hop le
Expected: `201`

- [ ] Teacher goi `Create New User`
Expected: nen la `403`
Canh bao: neu ra `201` thi bug security nghiem trong

- [ ] `Create New User` voi email da ton tai
Expected: `400`

- [ ] `Create New User` voi roleId sai
Expected: `400`

- [ ] `Create New User` voi body sai format
Expected: `400`

Body hop le:

```json
{
  "fullName": "New User",
  "email": "newuser@example.com",
  "password": "Password123!",
  "phone": "0123456789",
  "roleId": 4
}
```

### Update User

- [ ] Admin goi `Update User` voi body hop le
Expected: `200`

- [ ] Teacher goi `Update User`
Expected: nen la `403`
Canh bao: neu ra `200` thi bug security

- [ ] `Update User` voi id khong ton tai
Expected: `404`

- [ ] `Update User` voi email duplicate
Expected: `400`

### Delete User

- [ ] Admin goi `Delete User`
Expected: nen la `204`

- [ ] Teacher goi `Delete User`
Expected: nen la `403`
Canh bao: neu ra `204` thi bug security

- [ ] `Delete User` voi id khong ton tai
Expected: `404`

## Folder: Question Banks

### Get My Question Banks

- [ ] Teacher goi `Get My Question Banks`
Expected: `200`

- [ ] Khong gui token
Expected: `401`

### Get Question Bank Detail

- [ ] `Get Question Bank Detail` voi `bank_id` hop le
Expected: `200`

- [ ] `Get Question Bank Detail` voi `bank_id` khong ton tai
Expected: `404`

### Create Question Bank

- [ ] `Create Question Bank` voi body hop le
Expected: `201`

- [ ] `Create Question Bank` voi `name = ""`
Expected: `400`

Body hop le:

```json
{
  "name": "Math Questions - Chapter 1",
  "subject": "Mathematics",
  "gradeLevel": "10",
  "description": "Algebra and geometry problems",
  "isPublished": false
}
```

### Update Question Bank

- [ ] `Update Question Bank` voi id hop le
Expected: `200`

- [ ] `Update Question Bank` voi id khong ton tai
Expected: `404`

- [ ] `Update Question Bank` voi body thieu `name`
Expected: `400`

### Get Questions in Bank

- [ ] `Get Questions in Bank` voi `bank_id` hop le
Expected: `200`
Note: response dang phan trang, doc du lieu trong `content`

- [ ] `Get Questions in Bank` voi bank rong
Expected: `200`, `content: []`

- [ ] `Get Questions in Bank` voi `page = 0`, `size = 10`
Expected: `200`

- [ ] `Get Questions in Bank` filter theo `topic`
Expected: `200`, chi tra cau hoi khop topic

- [ ] `Get Questions in Bank` filter theo `difficulty`
Expected: `200`, chi tra cau hoi dung muc do

- [ ] `Get Questions in Bank` filter theo `type`
Expected: `200`, chi tra cau hoi dung loai

- [ ] `Get Questions in Bank` voi `page = -1`
Expected: `400`

- [ ] `Get Questions in Bank` voi `size = 0`
Expected: `400`

- [ ] `Get Questions in Bank` voi `difficulty` khong hop le
Expected: `400`

- [ ] `Get Questions in Bank` voi `type` khong hop le
Expected: `400`

### Delete Question Bank

- [ ] `Delete Question Bank` voi id hop le
Expected: `204`

- [ ] `Delete Question Bank` voi id khong ton tai
Expected: `404`

## Folder: Questions

### Create Question

- [ ] `Create Question` voi body hop le
Expected: `201`

- [ ] `Create Question` voi thieu `bankId`
Expected: `400`

- [ ] `Create Question` voi thieu `content`
Expected: `400`

- [ ] `Create Question` voi `bankId` khong ton tai
Expected: `404`

- [ ] `Create Question` vao bank khong thuoc teacher hien tai
Expected: `403`

Body dung:

```json
{
  "bankId": 1,
  "content": "What is the capital of France?",
  "type": "MULTIPLE_CHOICE",
  "difficulty": "EASY",
  "topic": "Geography",
  "options": [
    { "label": "A", "text": "Paris", "isCorrect": true },
    { "label": "B", "text": "London", "isCorrect": false },
    { "label": "C", "text": "Berlin", "isCorrect": false },
    { "label": "D", "text": "Madrid", "isCorrect": false }
  ],
  "correctAnswer": "Paris",
  "explanation": "Paris is the capital city of France."
}
```

### Get Question Detail

- [ ] `Get Question Detail` voi `question_id` hop le
Expected: `200`

- [ ] `Get Question Detail` voi `question_id` khong ton tai
Expected: `404`

### Generate AI Questions

- [ ] Neu CHUA set `GEMINI_API_KEY`, goi `Generate AI Questions`
Expected: `502`
Note: day la expected, khong phai bug startup

- [ ] Neu DA set `GEMINI_API_KEY`, goi `Generate AI Questions` voi body hop le
Expected: `200` neu `saveToDb=false`

- [ ] `Generate AI Questions` voi `count = 0`
Expected: `400`

- [ ] `Generate AI Questions` voi thieu `bankId`
Expected: `400`

- [ ] `Generate AI Questions` voi `bankId` khong ton tai
Expected: `404`

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

### Update Question

- [ ] `Update Question` voi id hop le
Expected: `200`

- [ ] `Update Question` voi id khong ton tai
Expected: `404`

- [ ] `Update Question` voi body thieu `content`
Expected: `400`

- [ ] `Update Question` vao question thuoc bank khong duoc phep sua
Expected: `403`

### Delete Question

- [ ] `Delete Question` voi id hop le
Expected: `204`

- [ ] `Delete Question` voi id khong ton tai
Expected: `404`

### Request thu cong can them

- [ ] Tao request thu cong `POST {{base_url}}/api/{{api_version}}/questions/save-batch`
Expected: `201` neu hop le

- [ ] Test `save-batch` voi `bankId = null`
Expected: nen la `400`
Canh bao: hien tai co nguy co `500`

- [ ] Test `save-batch` voi `type = "abc"` hoac `difficulty = "abc"`
Expected: nen la `400`
Canh bao: hien tai de ra `500` do `Enum.valueOf(...)`

## Folder: Orders

### Get All Orders

- [ ] Admin goi `Get All Orders`
Expected: `200`

- [ ] Teacher goi `Get All Orders`
Expected: `403`

### Create Order

- [ ] Teacher goi `Create Order` voi package active
Expected: `200`

- [ ] Admin goi `Create Order`
Expected: `403`

- [ ] `Create Order` voi `packageId` khong ton tai
Expected: `404`

- [ ] `Create Order` voi `packageId = null`
Expected: nen la `400`
Canh bao: hien tai co nguy co `500`

- [ ] `Create Order` voi package da deactivate
Expected: nen la `400` hoac `409`
Canh bao: hien tai can theo doi xem co bi day thanh `500` khong

Body dung:

```json
{
  "packageId": 1,
  "paymentMethod": "BANK_TRANSFER"
}
```

### Get Order Details

- [ ] Bo qua request nay trong collection
Ly do: backend hien tai khong co `GET /api/v1/orders/{id}`

### Request thu cong can them

- [ ] Tao request thu cong `GET {{base_url}}/api/{{api_version}}/orders/my`
Teacher expected: `200`

- [ ] Tao request thu cong `PUT {{base_url}}/api/{{api_version}}/orders/{{order_id}}/status`
Admin expected: `200`

- [ ] Test `PUT /orders/{id}/status` voi body `{ "status": "ACTIVE" }`
Expected: `200`

- [ ] Test `PUT /orders/{id}/status` voi body `{ "status": "abc" }`
Expected: nen la `400`
Canh bao: hien tai co nguy co `500` do `Enum.valueOf(...)`

## Folder: Subscriptions

Folder nay trong collection dang cu, backend hien tai da doi sang `/packages`.

### Viec can lam truoc

- [ ] Duplicate hoac rename folder `Subscriptions` thanh `Packages`
- [ ] Sua URL tu `/subscriptions` thanh `/packages`

### Get All Subscriptions

- [ ] Sua request nay thanh `GET {{base_url}}/api/{{api_version}}/packages`
Teacher/Admin expected: `200`

- [ ] Goi khong token
Expected: `401`

### Create Subscription

- [ ] Sua request nay thanh `POST {{base_url}}/api/{{api_version}}/packages`
Manager/Admin expected: `200`

- [ ] Teacher goi request nay
Expected: `403`

- [ ] Body hop le
Expected: `200`

- [ ] Body thieu `name`, `price`, `durationDays`
Expected: nen la `400`
Canh bao: hien tai co nguy co `500`

Body hop le:

```json
{
  "name": "SCHOOL_PLAN",
  "description": "Plan for schools",
  "price": 199000,
  "durationDays": 30,
  "features": "{\"lesson_plans\":50}",
  "isActive": true
}
```

### Request thu cong nen them trong folder Packages

- [ ] `PUT {{base_url}}/api/{{api_version}}/packages/{{package_id}}`
Manager/Admin expected: `200`

- [ ] `DELETE {{base_url}}/api/{{api_version}}/packages/{{package_id}}`
Manager/Admin expected: `204`

- [ ] `PUT {{base_url}}/api/{{api_version}}/packages/{{package_id}}/deactivate`
Manager/Admin expected: `200`

- [ ] Test `package_id` khong ton tai cho 3 request tren
Expected: `404`

## Error checklist nhanh

### 400

- [ ] Body thieu field bat buoc
- [ ] Sai format field
- [ ] Validation no dung nhu ky vong
- [ ] Neu dang ra `400` ma ra `500`, ghi lai payload gay loi

### 401

- [ ] Khong gui token
- [ ] Gui token rac
- [ ] Login sai email/password

### 403

- [ ] Teacher goi API admin
- [ ] Teacher goi API manager
- [ ] Neu teacher van tao/xoa/update user duoc, danh dau `BUG SECURITY`

### 404

- [ ] `bank_id` khong ton tai
- [ ] `package_id` khong ton tai
- [ ] `order_id` khong ton tai
- [ ] `question_id` khong ton tai

### 500

- [ ] Test payload null/sai enum cho orders/packages/save-batch
- [ ] Neu gap `500`, ghi ro:
Endpoint
Role
Payload
Expected
Actual
Log backend

### 502

- [ ] `POST /questions/ai-generate` khi chua co `GEMINI_API_KEY`
Expected: `502 AI_SERVICE_ERROR`

### 503

- [ ] Stop backend container roi goi lai request
- [ ] Stop db container roi restart backend
Note: `503` la bai test ha tang, khong phai business API

## Dinh nghia "pass that su"

- [ ] Dung status code
- [ ] Dung role
- [ ] Dung shape response
- [ ] Khong mo nham quyen cho user role thap
- [ ] Khong day input xau thanh `500`

## Uu tien chay truoc

- [ ] Login teacher
- [ ] `Get Current User`
- [ ] `Get My Question Banks`
- [ ] `Create Question Bank`
- [ ] `Create Order`
- [ ] Login admin
- [ ] `Get All Users (Admin)`
- [ ] `Get All Orders`
- [ ] `GET /packages`
- [ ] Negative tests cho `403`, `400`, `404`, `502`

## Ghi chu cuoi

- Request nao khong khop backend hien tai thi khong tick pass chi vi Postman gui duoc.
- Neu request "pass" nhung mo sai quyen, do la bug nghiem trong hon ca request fail.
- Checklist nay nen duoc dung cung voi `POSTMAN_BACKEND_CHECK.md`.
