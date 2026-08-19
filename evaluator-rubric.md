# Evaluator Rubric — F11 Task Filtering & Sorting

| Dimension | 1 (kém) | 3 (được) | 5 (tốt) | Điểm |
|---|---|---|---|---|
| Functional correctness | Filter cơ bản lỗi | Filter đơn lẻ đúng, kết hợp sai | Mọi tổ hợp filter+sort+page đều đúng | 5 |
| Edge case handling | Crash khi tham số sai | Trả lỗi nhưng message mơ hồ | 400 rõ ràng, 0 kết quả trả mảng rỗng sạch | 5 |
| Architecture compliance | Logic filter nằm trong Controller | Có Service nhưng lẫn logic query thô | Service dùng Specification/Query rõ ràng, Controller mỏng | 5 |
| Test coverage | Không có test | Test happy-path only | Test cả edge case (sai tham số, 0 kết quả, kết hợp filter) | 5 |
| Code quality | Trùng lặp code, đặt tên tối nghĩa | Chấp nhận được | Rõ ràng, không trùng lặp, dễ mở rộng thêm filter mới | 4 |

**Điểm tổng = trung bình cộng 5 dimension (thang 1-5) = 4.8/5**

## Ghi chú theo dimension

- **Functional correctness**: xác nhận bằng 11 test MockMvc (category alone, status
  alone, category+status AND, sortBy=title/sortDir=asc ordering, phân trang nhiều
  trang) và curl thủ công (category=work&status=open, sortBy=title&sortDir=asc&size=2).
  Tất cả đúng.
- **Edge case handling**: sortBy sai, sortDir sai, page âm, size<=0 đều trả 400 kèm
  message rõ ràng (không crash/500, không stack trace); filter 0 kết quả trả
  `{"content":[],"totalElements":0,"totalPages":0,...}` với status 200.
- **Architecture compliance**: TaskController chỉ khai báo @RequestParam và gọi thẳng
  TaskService, không có logic query. TaskService dùng
  `JpaSpecificationExecutor<Task>` + `Specification<Task>` (TaskSpecifications) và
  `Pageable`/`Sort`, không nối chuỗi JPQL thủ công. `./check_architecture.sh` pass.
- **Test coverage**: TaskFilteringSortingTest bao phủ default params, mỗi filter
  riêng lẻ, filter kết hợp AND, 0-result, invalid sortBy/sortDir, invalid page/size,
  và metadata phân trang nhiều trang — đúng như rubric yêu cầu ở band 5.
- **Code quality**: tách TaskSpecifications (dễ thêm filter mới bằng cách thêm 1
  method `hasX` + `.and(hasX(x))`), `TaskResponse.from(Task)` tránh trùng lặp mapping
  giữa 4 method của TaskService. Trừ 1 điểm vì `TaskService.listTasks` là một method
  khá dài (validation + query trong cùng 1 method) — chấp nhận được ở quy mô hiện tại
  nhưng nếu thêm nhiều filter/param nữa nên tách phần validate sortBy/sortDir/page/size
  ra khỏi method chính.

## Defects tìm được
Không có defect chức năng nào được tìm thấy trong quá trình build + test + verify thủ
công. Một điểm cần lưu ý (không phải defect, đã ghi trong claude-progress.md và
docs/SPRINT_CONTRACT_F11.md §1): tiêu đề mục F11 trong docs/PRODUCT.md hiện đang ghi
nhầm là "F07", trùng với F07 (structured logging) đã có trong feature_list.json. Đây là
vấn đề tài liệu, không phải lỗi code, và nằm ngoài phạm vi F11 theo contract.

## Kết luận
Điểm: 4.8/5 — đạt band cao nhất (5) ở 4/5 dimension, band 4 ở code quality do
`TaskService.listTasks` gộp validate + query trong một method.