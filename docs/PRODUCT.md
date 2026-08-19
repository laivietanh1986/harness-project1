# Product Spec

6 tính năng, thứ tự triển khai theo feature_list.json (WIP=1 — chỉ 1 active tại một thời điểm):

F01. Health check
F02. List tasks
F03. Create task
F04. Persistent storage
F05. Bulk import tasks
F06. Get task detail

Không refactor, không tối ưu, không thêm tính năng ngoài 6 mục trên.

## F05. Bulk import tasks
- `POST /api/tasks/import` nhận vào một mảng task (JSON array), tạo mới hàng loạt (bulk create).
- Response: 201 Created, trả về danh sách các task đã tạo (DTO, không expose entity).
- Acceptance criteria:
  - Request body là mảng rỗng `[]` → trả về mảng rỗng, không lỗi.
  - Mỗi phần tử trong mảng được validate như F03 (POST /api/tasks); phần tử không hợp lệ → 400.
  - Các task import được lưu vào cùng bảng/kho dữ liệu với task tạo qua F03.
  - Task được import qua endpoint này phải persist qua restart (H2 file mode) — verify tương tự F04: import task, restart app, GET /api/tasks (hoặc GET /api/tasks/{id}) vẫn thấy task đó.

## F06. Get task detail
- `GET /api/tasks/{id}` trả về chi tiết đầy đủ của 1 task theo id.
- Response: 200 + TaskResponse DTO nếu tồn tại; 404 nếu không tìm thấy id.
## F07. Task Filtering & Sorting API
GET /api/tasks?category=&status=&sortBy=&sortDir=&page=&size=

- category, status: lọc kết hợp được (AND), optional
- sortBy: title|createdAt|category, mặc định createdAt
- sortDir: asc|desc, mặc định desc
- page, size: phân trang, mặc định page=0 size=20
- Tham số không hợp lệ (sortBy sai tên cột) -> trả 400 kèm message rõ ràng, KHÔNG crash
- Kết hợp filter cho ra 0 kết quả -> trả mảng rỗng, không lỗi
- Response phải kèm metadata phân trang: {content, totalElements, totalPages, page, size}
