# Mô tả thành phần – TTHC Report (Spring Boot)

## Tổng quan luồng xử lý

```
Upload .xlsx  →  ExcelParseService (đọc từng file)
              →  danh sách UnitRow (theo đơn vị, có số liệu)
              →  AggregateService (gộp theo unitKey, cộng số)
              →  Xem trước / Xuất Excel / Lưu lịch sử
```

---

## 1. Tầng cấu hình

| Class / file | Chức năng |
|--------------|-----------|
| `TthcReportApplication` | Entry Spring Boot |
| `FirebaseConfig` | Khởi tạo Firebase Admin (credentials JSON) |
| `application.yml` | Port, multipart, `firebase.enabled`, collection |
| `static/index.html` + `app.js` | Giao diện web (upload, tổng hợp, lịch sử) |

---

## 2. Model & DTO

| Class | Chức năng |
|-------|-----------|
| `UnitRow` | Một dòng đơn vị: `unit`, `numbers` (map key→số), `sources` |
| `ParsedFile` | Kết quả parse 1 file: family A/B/C, rows, lỗi |
| `ReportRecord` | Bản ghi lịch sử: `name`, `reportType`, totals, rows |
| `CanonField` | Định nghĩa cột **Theo kỳ** (`full`) và **Theo tuần** (`weekly`) |
| `MergeRequest` | Body API merge/export: rows, reportType, name, save |
| `MergeResponse` | Kết quả gộp: rows, totals, unitCount, savedId |

**Các key số chính:**  
`recv_total`, `recv_direct`, `recv_online_full`, `recv_online_partial`, `recv_postal`, `recv_carry`,  
`res_total`, `res_before`, `res_ontime`, `res_late`,  
`proc_total`, `proc_ontime`, `proc_late`, …

---

## 3. Service – xử lý nghiệp vụ

### `UnitNormalizeService`
- `normalizeUnitName` – trim, bỏ ký tự đầu dòng
- `removeDiacritics` – bỏ dấu tiếng Việt
- **`unitKey(name)`** – tạo khóa gộp đơn vị (port từ HTML):
  - Bỏ UBND / Thành phố Hà Nội
  - Gộp: Sở VHTTDL + Sở Du lịch; Trung tâm HCC; Văn phòng UBND TP; Chi nhánh số N
  - Phường/xã → prefix `ubnd`
  - Fallback rỗng → `ubnd thanh pho` hoặc `van phong ubnd thanh pho`

### `ExcelParseService`
- `parseAll(files)` – đọc nhiều file upload
- `pickBestSheet` / `scoreSheet` – chọn sheet có STT + đơn vị + TTHC
- **`parseTongHopTheoDonVi`** – mẫu A, **cột cố định** (khớp HTML gốc):
  - 2 total, 3 trực tiếp, 4 trực tuyến → online_full, 5 BCCI
  - 6 res_total, 7 **Đúng và trước hạn → res_before**, 8 res_late
  - 9–11 proc total / trong hạn / quá hạn
- **`parseByHeader` + `classify`** – mẫu II.06a / 07a / generic (đọc theo nhãn header)
- `toNum` – hỗ trợ số dạng `1.234` hoặc `1,234`

### `AggregateService`
- **`aggregate(rows)`** – gộp theo `unitKey`, cộng từng key số, gộp `sources`
- Bỏ qua: `cap xa`, `account khong dung`, dòng không có số
- Sắp xếp tên đơn vị theo locale **vi**
- **`totals(rows)`** – tổng toàn bảng

### `ExcelExportService`
- Xuất `.xlsx` theo `CanonField.full()` hoặc `weekly()`
- Header 2 dòng, merge ô, tô vàng, dòng TỔNG CỘNG
- Theo kỳ: `res_ontime` hiển thị = `res_ontime + res_before`

### `ReportHistoryService`
- `save(name, …)` – ghi Firestore
- `listRecent` – danh sách (không kèm rows)
- `get(id)` – chi tiết kèm rows (để Xem)
- `updateName` / `delete`

---

## 4. API (`ReportController`)

| Method | Path | Việc |
|--------|------|------|
| POST | `/api/reports/parse` | Upload multipart → parse |
| POST | `/api/reports/merge` | Gộp; `save=true` thì lưu lịch sử + `name` |
| POST | `/api/reports/export` | Tải Excel |
| GET | `/api/reports/history` | Lịch sử |
| GET | `/api/reports/{id}` | Chi tiết (xem bảng) |
| PATCH | `/api/reports/{id}/name` | Đổi tên |
| DELETE | `/api/reports/{id}` | Xoá |
| GET | `/api/reports/{id}/excel` | Tải lại Excel từ lịch sử |
| GET | `/api/reports/health` | Server + storage OK? |

---

## 5. Frontend (`static/`)

| Hàm / vùng | Chức năng |
|------------|-----------|
| `handleFiles` | Upload → `/parse` |
| `mergeBtn` | Gọi `/merge` (không save) |
| `buildFullTableHtml` | Bảng xem trước đủ cột kỳ/tuần |
| `saveBtn` + form tên | Nhập tên → `/merge` save=true |
| Lịch sử | Tự tải khi mở trang; Xem / Sửa / Tải / Xoá |
| `showConfirm` | Modal xác nhận (không dùng `confirm` trình duyệt) |
| Toast | Thông báo mọi thao tác |

---

## 6. Quy tắc gộp (khớp HTML gốc)

1. Mỗi dòng Excel → 1 `UnitRow` với các field số đã map.
2. **Tổng hợp theo đơn vị**: map **cố định cột** (không dựa header phụ).
3. **Mẫu khác** (II.06a, 07a): map theo `classify(label)`.
4. Gộp: cùng `unitKey` → cộng số; “Đúng và trước hạn” nằm ở `res_before`.
5. Theo tuần: hiện `res_before` = Trước hạn, `res_ontime` = Đúng hạn (tách từ mẫu khác).
6. Theo kỳ: cột “Đúng và trước hạn” = `res_before + res_ontime`.

---

## 7. Chạy & cấu hình

```bash
# Đặt firebase-service-account.json vào src/main/resources/
# Sửa application.yml: storage-bucket (tuỳ chọn), firebase.enabled

mvn spring-boot:run
# http://localhost:8080/
```
