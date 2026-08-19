# Evaluator Rubric — F11 Task Filtering & Sorting

| Dimension | 1 (kém) | 3 (được) | 5 (tốt) | Điểm |
|---|---|---|---|---|
| Functional correctness | Filter cơ bản lỗi | Filter đơn lẻ đúng, kết hợp sai | Mọi tổ hợp filter+sort+page đều đúng | 5 |
| Edge case handling | Crash khi tham số sai | Trả lỗi nhưng message mơ hồ | 400 rõ ràng, 0 kết quả trả mảng rỗng sạch | 5 |
| Architecture compliance | Logic filter nằm trong Controller | Có Service nhưng lẫn logic query thô | Service dùng Specification/Query rõ ràng, Controller mỏng | 5 |
| Test coverage | Không có test | Test happy-path only | Test cả edge case (sai tham số, 0 kết quả, kết hợp filter) | 1 |
| Code quality | Trùng lặp code, đặt tên tối nghĩa | Chấp nhận được | Rõ ràng, không trùng lặp, dễ mở rộng thêm filter mới | 4 |

**Điểm tổng = trung bình cộng 5 dimension (thang 1-5)**

## Defects tìm được

1. **[Đã tìm thấy khi tự review, đã fix]** Ban đầu, `sortBy`/`sortDir` sai trả về 400 nhưng **body không có field `message` nào cả** — chỉ có `{"timestamp","status","error","path"}`. Nguyên nhân: Spring Boot mặc định `server.error.include-message=never`, nên `ResponseStatusException(..., reason)` chỉ log ở server (WARN), không trả về client. Điều này vi phạm trực tiếp acceptance criterion "sortBy sai tên cột -> trả 400 kèm message rõ ràng". Đã fix bằng cách thêm `server.error.include-message=always` vào `application.properties`, rebuild và verify lại: `?sortBy=bogus` giờ trả `"message":"invalid sortBy 'bogus', allowed values: title, createdAt, category"`; `?sortDir=bogus` tương tự. Đã kiểm tra fix này không phá F06 (404 vẫn sạch, chỉ thêm `"message":"404 NOT_FOUND"`) và không phá validate @Valid hiện có (blank-title POST vẫn 400, giờ có thêm message rõ hơn).

2. **[Chưa fix — gap có chủ đích]** Không có test tự động nào cho F11 (cũng như không có `src/test` cho toàn bộ project). Toàn bộ verification là manual: start app, curl từng case (filter đơn/kết hợp/0-match, cả 3 sortBy × asc/desc đại diện, page>0, page/size âm, sortBy/sortDir sai), đọc log JSON để confirm level/requestId. Điều này nhất quán với cách F01-F08 đã được verify trước đó trong project (không có test framework nào được set up), nhưng vẫn là gap thật theo rubric — điểm test coverage = 1 phản ánh đúng thực tế, không tự nâng lên vì đã test tay kỹ.

3. **[Minor, không fix]** `TaskService.listTasks()` gộp chung validate tham số + build Specification + build Pageable + query + log trong một method ~40 dòng. Không có logic trùng lặp hay bug, nhưng có thể tách nhỏ hơn để dễ đọc hơn. Không tách vì: (a) tách thành private method khác trong `TaskService` sẽ bị `check_architecture.py` coi là "service method không log" (đã gặp vấn đề y hệt với `toResponse` — phải chuyển nó thành static factory trên DTO); (b) chuyển logic ra ngoài class thì lại làm loãng nơi chứa business rule chính.

4. **[Minor, không phải bug]** `category`/`status` là string tự do, không có enum/whitelist, nên filter sai chính tả (vd `?category=wrk`) im lặng trả về 0 kết quả thay vì 400. Đây đúng theo spec (chỉ `sortBy` sai mới bắt buộc 400), không phải defect, nhưng là một tradeoff UX đáng ghi nhận.

## Kết luận
Điểm: 4.0/5 (trung bình 5/5/5/1/4). Điểm thấp gần như hoàn toàn do thiếu test tự động (dimension 4) — logic filter/sort/page đã verify đúng cho mọi tổ hợp đã thử qua HTTP thật, kiến trúc dùng `JpaSpecificationExecutor` + `Specification` rõ ràng với controller mỏng, và một defect thật (missing error message trong response body) đã được tìm ra và fix ngay trong lúc tự review thay vì báo cáo điểm cao mà bỏ qua.
