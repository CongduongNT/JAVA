# Bao Cao Backend Tasks

## [BE] OCRService: goi Gemini Vision API de doc bai lam hoc sinh

### Muc tieu
- Them service OCR doc `answer_sheets.file_url` bang Gemini Vision.
- Xu ly theo owner teacher, cap nhat `ocr_status`, luu `ocr_raw_data` ve DB.
- Giu boundary ro rang de task endpoint process noi vao ngay.

### Thiet ke va pham vi
- Them `OCRService` xu ly luong nghiep vu:
  - load `AnswerSheet`
  - check teacher ownership
  - bo qua reprocess neu da `COMPLETED` va co `ocr_raw_data`
  - danh dau `PROCESSING`
  - tai file tu `file_url`
  - goi Gemini Vision
  - parse JSON OCR
  - cap nhat `student_name`, `student_code`, `ocr_raw_data`, `ocr_status`
  - neu loi thi chuyen `FAILED`
- Tach 2 collaborator de giu tach biet trach nhiem:
  - `AnswerSheetFileLoader`
  - `GeminiVisionClient`
- Them implementation runtime:
  - `HttpAnswerSheetFileLoader` de download file tu public Supabase URL
  - `GeminiVisionClientAdapter` de goi SDK `google-genai`
- Tach them `AnswerSheetMapper` de service upload va service OCR dung chung mapping DTO, tranh lap lai logic.

### File chinh da them
- `backend/src/main/java/com/planbookai/backend/mapper/AnswerSheetMapper.java`
- `backend/src/main/java/com/planbookai/backend/service/AnswerSheetFileLoader.java`
- `backend/src/main/java/com/planbookai/backend/service/HttpAnswerSheetFileLoader.java`
- `backend/src/main/java/com/planbookai/backend/service/GeminiVisionClient.java`
- `backend/src/main/java/com/planbookai/backend/service/GeminiVisionClientAdapter.java`
- `backend/src/main/java/com/planbookai/backend/service/OCRService.java`
- `backend/src/test/java/com/planbookai/backend/service/OCRServiceTest.java`

### Rule nghiep vu da khoa
- Chi `TEACHER` duoc process answer sheet.
- Chi owner cua answer sheet moi duoc process.
- Neu status dang `PROCESSING` thi reject.
- Neu da `COMPLETED` va da co `ocr_raw_data` thi tra ket qua hien tai, khong goi AI lai.
- Prompt ep Gemini tra ve JSON thuan.
- JSON OCR se duoc normalize va luu vao `ocr_raw_data`.
- `student_name` va `student_code` duoc tach ra tu JSON neu co.
- Loi OCR se mark `FAILED` va luu error payload dang JSON.

### Kiem tra
- `OCRServiceTest`: pass `6/6` bang verify co lap tren class da bien dich.
- Da cover:
  - success path
  - idempotent path khi da OCR xong
  - reject other teacher
  - reject processing in progress
  - mark failed khi Gemini tra JSON loi
  - not found
- Full Maven compile/test van bi chan boi baseline Lombok/toolchain cua repo, khong phai do `OCRService`.

### Rui ro con lai
- `HttpAnswerSheetFileLoader` dang download file qua public URL, nen phu thuoc storage object con public va truy cap duoc tu backend.
- `GeminiVisionClientAdapter` chua co integration test that voi API that do moi truong sandbox khong cho phep network.
- Prompt OCR hien tai uu tien JSON tong quat; khi co requirement grading chi tiet hon, nen tach prompt rieng cho OCR va prompt rieng cho grading.

## [BE] API POST /api/v1/answer-sheets/{id}/process
## [BE] API GET /api/v1/answer-sheets
## [BE] API GET /api/v1/answer-sheets/{id}

### Muc tieu
- Them endpoint trigger OCR cho answer sheet cua teacher hien tai.
- Them endpoint list answer sheets co filter theo `exam_id`.
- Them endpoint detail de xem ket qua OCR raw.
- Giu request contract de test bang Postman it loi nhat.

### Thiet ke va pham vi
- Mo rong `AnswerSheetService` cho 2 nhu cau query:
  - `getMyAnswerSheets(user, page, size, examId)`
  - `getAnswerSheet(id, user)`
- Giu `OCRService` la boundary rieng cho OCR processing.
- Controller `AnswerSheetController` duoc noi 3 route moi:
  - `GET /api/v1/answer-sheets`
  - `GET /api/v1/answer-sheets/{id}`
  - `POST /api/v1/answer-sheets/{id}/process`
- List API ho tro ca `exam_id` va `examId` de tranh loi query key khi test Postman.
- List API co `page` va `size` mac dinh `0` va `10`, sort `uploadedAt desc, id desc`.
- Neu filter `exam_id` duoc gui len:
  - exam khong ton tai -> `404`
  - exam khong thuoc teacher hien tai -> `403`
- Process API khong nhan request body. Chi can `Authorization` header.

### File chinh da cap nhat
- `backend/src/main/java/com/planbookai/backend/repository/AnswerSheetRepository.java`
- `backend/src/main/java/com/planbookai/backend/service/AnswerSheetService.java`
- `backend/src/main/java/com/planbookai/backend/controller/AnswerSheetController.java`
- `backend/src/test/java/com/planbookai/backend/service/AnswerSheetServiceTest.java`
- `backend/src/test/java/com/planbookai/backend/controller/AnswerSheetControllerTest.java`

### Rule nghiep vu da khoa
- Chi `TEACHER` duoc goi ca 3 endpoint.
- Teacher chi nhin thay answer sheets cua chinh minh.
- Teacher chi trigger OCR cho answer sheet cua chinh minh.
- Neu gui dong thoi `exam_id` va `examId` khac nhau -> `400`.
- `GET /answer-sheets/{id}` tra ve ca `ocrRawData` neu da co.
- `POST /answer-sheets/{id}/process` goi lai `OCRService`, nen tu dong ke thua cac rule `PROCESSING`, `COMPLETED`, `FAILED`.

### Kiem tra
- Da verify co lap `30/30` test pass:
  - `AnswerSheetServiceTest`
  - `AnswerSheetControllerTest`
  - `OCRServiceTest`
- Da cover:
  - list filter theo exam
  - sort answer sheets cua teacher hien tai
  - detail owner check
  - process delegate dung `OCRService`
  - query alias `exam_id` va `examId`
  - reject ambiguous query params

### Luu y Postman
- `POST /api/v1/answer-sheets/{id}/process`
  - Method: `POST`
  - Body: de trong
  - Header can co: `Authorization: Bearer <token>`
- `GET /api/v1/answer-sheets`
  - Query khuyen nghi: `exam_id=12&page=0&size=10`
  - Ho tro alias: `examId=12`
- `GET /api/v1/answer-sheets/{id}`
  - Khong can body
  - Chi can token teacher dung owner

### Tac dong toi task sau
- Neu them grading endpoint, chi can doc answer sheet detail hoac goi lai `ocrRawData`.
- Neu can dashboard answer sheets, list API da co page/size va filter `exam_id` de dung lai cho UI.
