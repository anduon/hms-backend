
# HMS Backend - Spring Boot Hotel Management System

## Mô tả
Dự án backend quản lý khách sạn xây dựng bằng Spring Boot, kết nối với PostgreSQL.

---

## Yêu cầu
- Java 21
- Maven
- PostgreSQL (đã cài và tạo database)

---

## Cài đặt và chạy dự án

### 1. Clone project
```bash
git clone https://github.com/anduon/hms-backend.git
cd hms-backend
```

### 2. Tạo file `.env` ở thư mục gốc dự án với nội dung:
```env
DB_URL=jdbc:postgresql://localhost:5432/ten_database
DB_USERNAME=postgres
DB_PASSWORD=matkhau
```
> **Lưu ý:** Thêm `.env` vào `.gitignore` để không đẩy file này lên GitHub.

### 3. Tạo database trên PostgreSQL:
```sql
CREATE DATABASE ten_database;
```

### 4. Cấu hình `application.properties` trong `src/main/resources`:
```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### 5. Chạy ứng dụng

#### Cách 1: Qua terminal (Linux/macOS)
```bash
export DB_URL=jdbc:postgresql://localhost:5432/ten_database
export DB_USERNAME=postgres
export DB_PASSWORD=matkhau

mvn clean install
mvn spring-boot:run
```

#### Cách 2: Qua terminal (Windows CMD)
```cmd
set DB_URL=jdbc:postgresql://localhost:5432/ten_database
set DB_USERNAME=postgres
set DB_PASSWORD=matkhau

mvn clean install
mvn spring-boot:run
```

#### Cách 3: Qua IntelliJ IDEA
- Mở project, build Maven nếu cần.
- Chạy class chính `HmsBackendApplication.java`.
- Thêm biến môi trường `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` trong Run Configuration.

### 6. Kiểm tra
- Mở trình duyệt truy cập: [http://localhost:8080](http://localhost:8080)
- Kiểm tra logs để đảm bảo ứng dụng kết nối database thành công.
- Truy cập trang Swagger UI tại: http://localhost:8080/swagger-ui/index.html

---
