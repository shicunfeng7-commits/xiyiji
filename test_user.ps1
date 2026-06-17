$token = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjExLCJwaG9uZSI6IjEzODAwMDAwMDAxIiwicm9sZSI6InVzZXIiLCJpYXQiOjE3ODE3MDI4NTIsImV4cCI6MTc4MjMwNzY1Mn0.8HD89kxcoj74STMjakDSMUs83-knBfj4W7Ux46oyN2I"
$header = @{Authorization="Bearer $token"}

Write-Host "===== 1. 获取服务时间段 ====="
Invoke-RestMethod -Uri "http://localhost:8080/api/user/time-slots" -Method Get -Headers $header | ConvertTo-Json -Depth 5

Write-Host "`n===== 2. 获取个人信息 ====="
Invoke-RestMethod -Uri "http://localhost:8080/api/user/profile" -Method Get -Headers $header | ConvertTo-Json -Depth 5

Write-Host "`n===== 3. 更新个人信息（昵称+楼栋） ====="
$body = '{"nickname":"测试用户","buildingName":"学生宿舍·1号楼","roomNo":"301"}'
Invoke-RestMethod -Uri "http://localhost:8080/api/user/profile" -Method Put -Body $body -ContentType "application/json" -Headers $header | ConvertTo-Json -Depth 5

Write-Host "`n===== 4. 创建订单 ====="
$orderBody = '{"buildingCategory":"学生宿舍","buildingName":"学生宿舍·1号楼","roomNo":"301","contactPhone":"13800000001","serviceDate":"2026-06-18","startTime":"09:00","endTime":"11:00","remark":"测试订单"}'
Invoke-RestMethod -Uri "http://localhost:8080/api/user/order/create" -Method Post -Body $orderBody -ContentType "application/json" -Headers $header | ConvertTo-Json -Depth 10

Write-Host "`n===== 5. 查看我的订单列表 ====="
Invoke-RestMethod -Uri "http://localhost:8080/api/user/order/list" -Method Get -Headers $header | ConvertTo-Json -Depth 10

Write-Host "`n===== 6. 申请成为员工 ====="
$applyBody = '{"name":"测试员工","phone":"13800000001","major":"计算机科学","grade":"2023级"}'
Invoke-RestMethod -Uri "http://localhost:8080/api/user/apply-employee" -Method Post -Body $applyBody -ContentType "application/json" -Headers $header | ConvertTo-Json -Depth 5

Write-Host "`n===== 7. 查看申请状态 ====="
Invoke-RestMethod -Uri "http://localhost:8080/api/user/apply-status" -Method Get -Headers $header | ConvertTo-Json -Depth 5

Write-Host "`n===== 8. 查看管理员二维码 ====="
Invoke-RestMethod -Uri "http://localhost:8080/api/user/admin/qrcode" -Method Get -Headers $header | ConvertTo-Json -Depth 5