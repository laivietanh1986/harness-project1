# Evaluator Rubric — F11 Task Filtering & Sorting

| Dimension | 1 (kém) | 3 (được) | 5 (tốt) | Điểm |
|---|---|---|---|---|
| Functional correctness | Filter cơ bản lỗi | Filter đơn lẻ đúng, kết hợp sai | Mọi tổ hợp filter+sort+page đều đúng | 5 |
| Edge case handling | Crash khi tham số sai | Trả lỗi nhưng message mơ hồ | 400 rõ ràng, 0 kết quả trả mảng rỗng sạch | 4 |
| Architecture compliance | Logic filter nằm trong Controller | Có Service nhưng lẫn logic query thô | Service dùng Specification/Query rõ ràng, Controller mỏng | 5 |
| Test coverage | Không có test | Test happy-path only | Test cả edge case (sai tham số, 0 kết quả, kết hợp filter) | 4 |
| Code quality | Trùng lặp code, đặt tên tối nghĩa | Chấp nhận được | Rõ ràng, không trùng lặp, dễ mở rộng thêm filter mới | 4 |

**Điểm tổng = trung bình cộng 5 dimension (thang 1-5) = 4.4/5**

*(Cập nhật sau review độc lập đối chiếu với docs/SPRINT_CONTRACT_F11.md — xem "Defects tìm được" bên dưới. Điểm gốc tự chấm khi build là 4.8/5; hạ Edge case handling và Test coverage xuống 4 sau khi phát hiện 1 edge case chưa test/message không rõ và test coverage không thực sự phủ "mọi tổ hợp" như mô tả.)*

## Ghi chú theo dimension

- **Functional correctness**: xác nhận bằng 11 test MockMvc (category alone, status
  alone, category+status AND, sortBy=title/sortDir=asc ordering, phân trang nhiều
  trang) và curl thủ công (category=work&status=open, sortBy=title&sortDir=asc&size=2).
  Tất cả đúng.
- **Edge case handling**: sortBy sai, sortDir sai, page âm, size<=0 đều trả 400 kèm
  message rõ ràng (không crash/500, không stack trace); filter 0 kết quả trả
  `{"content":[],"totalElements":0,"totalPages":0,...}` với status 200. **Cập nhật:**
  `page`/`size` không phải số (vd `page=abc`) cũng trả 400 (không crash) nhưng qua
  default Spring type-conversion handler — body chỉ có
  `{"timestamp":...,"status":400,"error":"Bad Request","path":...}`, KHÔNG có field
  `message` giải thích lý do, khác hẳn message rõ ràng của các case sortBy/sortDir/page
  âm/size<=0 tự viết tay. Case này chưa có test nào phủ.
- **Architecture compliance**: TaskController chỉ khai báo @RequestParam và gọi thẳng
  TaskService, không có logic query. TaskService dùng
  `JpaSpecificationExecutor<Task>` + `Specification<Task>` (TaskSpecifications) và
  `Pageable`/`Sort`, không nối chuỗi JPQL thủ công. `./check_architecture.sh` pass.
- **Test coverage**: TaskFilteringSortingTest bao phủ default params, mỗi filter
  riêng lẻ, filter kết hợp AND, 0-result, invalid sortBy/sortDir, invalid page/size,
  và metadata phân trang nhiều trang. **Cập nhật:** claim "mọi tổ hợp filter+sort+page
  đều đúng" ở band 5 hơi rộng hơn thực tế được test — chỉ có 1 tổ hợp sort
  (`sortBy=title&sortDir=asc`) được assert đúng thứ tự; `sortBy=category`,
  `sortBy=createdAt` (ngoài default), và `sortDir=desc` tường minh không có test assert
  thứ tự riêng. `page`/`size` không phải số cũng chưa có test (xem Edge case handling).
- **Code quality**: tách TaskSpecifications (dễ thêm filter mới bằng cách thêm 1
  method `hasX` + `.and(hasX(x))`), `TaskResponse.from(Task)` tránh trùng lặp mapping
  giữa 4 method của TaskService. Trừ 1 điểm vì `TaskService.listTasks` là một method
  khá dài (validation + query trong cùng 1 method) — chấp nhận được ở quy mô hiện tại
  nhưng nếu thêm nhiều filter/param nữa nên tách phần validate sortBy/sortDir/page/size
  ra khỏi method chính.

## Defects tìm được

Review độc lập (đối chiếu code thực tế + chạy lại `./check_architecture.sh` và
`./mvnw test` + curl thủ công) tìm thấy 2 defect không được ghi nhận trong lần tự chấm
đầu tiên, cộng 1 điểm ghi chú tài liệu đã biết trước:

1. **`feature_list.json` entry F02 bị stale** (mức trung bình) — F11 đổi response của
   `GET /api/tasks` từ mảng JSON thuần sang envelope phân trang
   `{content, totalElements, ...}` (đúng theo §2 của sprint-contract). Nhưng verification
   command của F02 (`jq 'type == "array"'`) giờ sẽ fail, và entry F02 trong
   feature_list.json không được cập nhật/ghi chú lại để phản ánh thay đổi này —
   vi phạm convention "update feature_list.json after finish" của CLAUDE.md.
2. **`page`/`size` không phải số → 400 nhưng message không rõ ràng, chưa test**
   (mức nhẹ-trung bình) — vd `GET /api/tasks?page=abc` trả 400 qua default Spring
   type-conversion handler, body không có field `message`. Không crash (không 500),
   nhưng không đạt mức "message rõ ràng" như PRODUCT.md yêu cầu chung cho mọi tham số
   không hợp lệ, và case này chưa có test nào phủ.
3. Không phải defect, đã ghi trong claude-progress.md và docs/SPRINT_CONTRACT_F11.md §1:
   tiêu đề mục F11 trong docs/PRODUCT.md hiện đang ghi nhầm là "F07", trùng với F07
   (structured logging) đã có trong feature_list.json. Đây là vấn đề tài liệu, không
   phải lỗi code, và nằm ngoài phạm vi F11 theo contract.

**Scope check (đối chiếu docs/SPRINT_CONTRACT_F11.md §7 Exclusions):** không có scope
violation nào. Không có endpoint mới, không auth/rate-limit, không enum enforcement,
không backfill migration, không rewrite tiêu đề PRODUCT.md, không refactor code không
liên quan — mọi thay đổi ngoài phần filter/sort/page đều là additive DTO field theo
đúng §3.

## Kết luận
Điểm: 4.4/5 (hạ từ 4.8/5 tự chấm ban đầu sau review độc lập) — Architecture compliance
vẫn ở band cao nhất (5); Functional correctness ở band 5 cho các case đã verify; Edge
case handling và Test coverage hạ xuống 4 do defect #1/#2 ở trên; Code quality giữ 4 do
`TaskService.listTasks` gộp validate + query trong một method (tự chấm từ đầu, vẫn đúng).
Không có scope violation.