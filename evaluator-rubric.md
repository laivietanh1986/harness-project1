# Evaluator Rubric — F11 Task Filtering & Sorting

| Dimension | 1 (kém) | 3 (được) | 5 (tốt) | **Điểm** |
|---|---|---|---|---|
| Functional correctness | Filter cơ bản lỗi | Filter đơn lẻ đúng, kết hợp sai | Mọi tổ hợp filter+sort+page đều đúng | **5** |
| Edge case handling | Crash khi tham số sai | Trả lỗi nhưng message mơ hồ | 400 rõ ràng, 0 kết quả trả mảng rỗng sạch | **3** |
| Architecture compliance | Logic filter nằm trong Controller | Có Service nhưng lẫn logic query thô | Service dùng Specification/Query rõ ràng, Controller mỏng | **5** |
| Test coverage | Không có test | Test happy-path only | Test cả edge case (sai tham số, 0 kết quả, kết hợp filter) | **1** |
| Code quality | Trùng lặp code, đặt tên tối nghĩa | Chấp nhận được | Rõ ràng, không trùng lặp, dễ mở rộng thêm filter mới | **4** |

**Điểm tổng = trung bình cộng 5 dimension (thang 1-5)**

## Defects tìm được

1. **[Nghiêm trọng] 400 response không thực sự "kèm message rõ ràng" như PRODUCT.md yêu cầu.**
   `TaskService.listTasks` set message rõ ràng qua `ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid sortBy '...' allowed values: ...")`
   ([TaskService.java:40-58](src/main/java/com/example/taskapi/service/TaskService.java#L40-L58)), nhưng project
   không có `@ControllerAdvice`/`@ExceptionHandler` nào serialize `reason` đó ra body, và
   `application.properties` không set `server.error.include-message=always` (mặc định Spring Boot là `never`).
   Verify thực tế bằng curl khi tự review:
   ```
   GET /api/tasks?sortBy=bogus  ->  400
   {"timestamp":"...","status":400,"error":"Bad Request","path":"/api/tasks"}
   ```
   Message "invalid sortBy 'bogus', allowed values: title, createdAt, category" **không xuất hiện trong response
   body** mà client nhận được — nó chỉ có trong log JSON (WARN) phía server. Đây là vi phạm trực tiếp acceptance
   criteria của docs/PRODUCT.md: *"Tham số không hợp lệ (sortBy sai tên cột) -> trả 400 kèm message rõ ràng"*.
   feature_list.json F11 evidence chỉ ghi "?sortBy=bogus -> 400" (đúng status code) nhưng không kiểm tra nội dung
   body, nên defect này lọt qua lúc tự verify ban đầu.

2. **[Nghiêm trọng] Không có test tự động nào trong repo.** `src/test` không tồn tại — 0 file `*Test.java`.
   Toàn bộ "verification" cho F11 (và F01-F08) là curl thủ công trong phiên làm việc, ghi lại bằng tay trong
   feature_list.json/claude-progress.md, không phải test có thể chạy lại (JUnit/MockMvc). Không có bảo vệ chống
   regression khi sửa code sau này.

3. **[Nhỏ] Filter category/status so khớp case-sensitive tuyệt đối** (`TaskSpecifications.java` dùng `cb.equal`
   trực tiếp, không `lower()`/`ignoreCase`). PRODUCT.md không yêu cầu rõ case-insensitive nên không tính là lỗi
   sai spec, nhưng là hành vi chưa được ghi chú/test — `?category=Work` sẽ không khớp `work`.

4. **[Nhỏ] Validation không nhất quán giữa createTask và importTasks.** `createTask` dựa vào `@Valid` +
   `@NotBlank` trên `CreateTaskRequest` (khai báo), còn `importTasks` tự check `title == null || isBlank()` bằng
   tay trong service (mệnh lệnh) vì `@Valid` không cascade qua `List<>` mặc định. Hoạt động đúng nhưng là hai cách
   validate khác nhau cho cùng một field trên cùng một DTO — dễ lệch nếu sau này thêm rule mới cho `title`.

## Kết luận
Điểm: **3.6/5** (5 + 3 + 5 + 1 + 4 = 18 / 5)

Điểm mạnh: logic filter/sort/pagination đúng ở mọi tổ hợp đã test (AND-combine, mỗi sortBy asc/desc, 0 kết quả,
phân trang), kiến trúc sạch (Specification pattern, Controller mỏng, check_architecture.sh pass).
Điểm yếu quyết định: message lỗi 400 không thực sự tới được client (defect #1) và hoàn toàn không có test tự
động (defect #2) — hai điểm này là lý do chính điểm không đạt mức "tốt".