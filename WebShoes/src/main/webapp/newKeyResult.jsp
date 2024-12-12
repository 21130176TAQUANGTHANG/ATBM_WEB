<%--
  Created by IntelliJ IDEA.
  User: PC
  Date: 12/12/2024
  Time: 10:20 CH
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Thông báo báo mất khóa</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/mdb-ui-kit/7.3.2/mdb.min.css" rel="stylesheet" />
</head>
<body>
<div class="container mt-5">
    <h2 class="text-center mb-4">Kết quả tạo khóa mới</h2>
    <div class="alert alert-success text-center" role="alert">
        <strong>Khóa mới đã được tạo thành công!</strong>
    </div>
    <div class="card mt-4">
        <div class="card-body">
            <h5>Public Key mới:</h5>
            <textarea rows="5" class="form-control mb-3" readonly>${publicKey}</textarea>
            <h5>Private Key:</h5>
            <textarea rows="5" class="form-control mb-3" readonly>${privateKey}</textarea>
        </div>
        <div class="card-footer text-center">
            <a href="home.jsp" class="btn btn-primary">Quay lại trang chủ</a>
            <a href="KeyGenerationServlet" class="btn btn-secondary">Tạo lại khóa mới</a>
        </div>
    </div>
</div>
</body>
</html>
