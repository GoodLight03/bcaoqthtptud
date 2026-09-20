# TTHC Report – Spring Boot + Firebase

**Bản: `2026-09-20-v3`**

Tổng hợp báo cáo TTHC theo đơn vị (port từ tool HTML).  
UI nằm trong `src/main/resources/static/index.html`.

Mở app: chân trang hiện `QT&PTNTS · bản 2026-09-20-FINAL-07a` — nếu không thấy dòng này là đang chạy zip cũ.

### Sửa 07a/BTP
File `.xls` mẫu 07a có **cột trống đầu**. STT ở cột 1, tên đơn vị ở cột 2.
Parser tìm chữ `STT` trên header rồi lấy tên đơn vị ở cột kế bên — không hard-code cột 0/1.

---

## Chỗ CẦN ĐIỀN thông số

### 1. Firebase Service Account (bắt buộc nếu muốn lưu lịch sử)

1. Vào [Firebase Console](https://console.firebase.google.com/) → chọn project  
2. **Project settings** (biểu tượng bánh răng) → tab **Service accounts**  
3. **Generate new private key** → tải file JSON  
4. Đổi tên / copy file thành:

```
src/main/resources/firebase-service-account.json
```

⚠️ **Không commit file này lên Git** (đã gợi ý trong `.gitignore`).

### 2. `application.yml`

Mở `src/main/resources/application.yml`:

```yaml
firebase:
  enabled: true
  credentials-path: firebase-service-account.json
  storage-bucket: YOUR_PROJECT_ID.appspot.com   # ← thay YOUR_PROJECT_ID
  collection: tthc_reports
```

| Trường | Cách lấy |
|--------|----------|
| `storage-bucket` | Firebase Console → Storage → tên dạng `xxx.appspot.com` (có thể để trống nếu chưa dùng Storage) |
| `collection` | Tên collection Firestore (mặc định `tthc_reports` – tạo tự động khi ghi) |
| `enabled: false` | Tắt Firebase nếu chỉ muốn parse/export local |

### 3. Firestore – bật API

Firebase Console → **Build → Firestore Database** → Create database (mode production hoặc test).  
Rule tạm thời (dev):

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;  // CHỈ DÙNG DEV – siết lại khi production
    }
  }
}
```

---

## Chạy project

Yêu cầu: **JDK 17+**, **Maven 3.8+**

```bash
cd tthc-report
mvn spring-boot:run
```

Mở trình duyệt: **http://localhost:8080/**

Kiểm tra Firebase:

```
GET http://localhost:8080/api/reports/health
→ { "ok": true, "firebase": true/false }
```

---

## API

| Method | Path | Mô tả |
|--------|------|--------|
| POST | `/api/reports/parse` | Upload multipart `files` → parse Excel |
| POST | `/api/reports/merge` | Body JSON: rows + reportType + save | Gộp đơn vị |
| POST | `/api/reports/export` | Xuất file .xlsx |
| GET | `/api/reports/history` | Lịch sử Firestore |
| GET | `/api/reports/{id}` | Chi tiết 1 lần |
| GET | `/api/reports/{id}/excel` | Tải lại Excel từ lịch sử |
| GET | `/api/reports/health` | Trạng thái + Firebase |

---

## Logo

Đặt file `hcc.png` vào:

```
src/main/resources/static/hcc.png
```

---

## Cấu trúc thư mục

```
tthc-report/
├── pom.xml
├── README.md
├── src/main/java/com/hanoi/tthc/
│   ├── TthcReportApplication.java
│   ├── config/FirebaseConfig.java
│   ├── controller/ReportController.java
│   ├── service/
│   │   ├── ExcelParseService.java
│   │   ├── UnitNormalizeService.java
│   │   ├── AggregateService.java
│   │   ├── ExcelExportService.java
│   │   └── ReportHistoryService.java
│   ├── model/...
│   └── dto/...
└── src/main/resources/
    ├── application.yml
    ├── firebase-service-account.json   ← BẠN THÊM FILE NÀY
    └── static/index.html
```

---

## Ghi chú

- Logic `unitKey`, classify cột, gộp đơn vị port từ bản HTML chuẩn.
- Mẫu **Tổng hợp theo đơn vị** parse theo **header** (không hard-code lệch cột).
- Lưu Firestore: cả metadata + rows. Nếu rows quá lớn có thể tách sang Storage sau.
