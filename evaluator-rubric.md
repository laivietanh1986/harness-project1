# Evaluator Rubric — F11 Task Filtering & Sorting

| Dimension | 1 (kém) | 3 (được) | 5 (tốt) |
|---|---|---|---|
| Functional correctness | Filter cơ bản lỗi | Filter đơn lẻ đúng, kết hợp sai | Mọi tổ hợp filter+sort+page đều đúng |
| Edge case handling | Crash khi tham số sai | Trả lỗi nhưng message mơ hồ | 400 rõ ràng, 0 kết quả trả mảng rỗng sạch |
| Architecture compliance | Logic filter nằm trong Controller | Có Service nhưng lẫn logic query thô | Service dùng Specification/Query rõ ràng, Controller mỏng |
| Test coverage | Không có test | Test happy-path only | Test cả edge case (sai tham số, 0 kết quả, kết hợp filter) |
| Code quality | Trùng lặp code, đặt tên tối nghĩa | Chấp nhận được | Rõ ràng, không trùng lặp, dễ mở rộng thêm filter mới |

**Điểm tổng = trung bình cộng 5 dimension (thang 1-5)**

## Defects tìm được
(liệt kê cụ thể, không viết chung chung "có vài lỗi nhỏ")

## Kết luận
Điểm: __/5