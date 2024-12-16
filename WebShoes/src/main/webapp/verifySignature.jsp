
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<head>
    <title>Verify Invoice Signature</title>
</head>
<body>
<div class="container mt-5">
    <div class="d-flex justify-content-between p-2">
        <h2>Đơn hàng</h2>
    </div>
    <table class="table table-bordered">
        <thead>
        <tr>
            <th>OrderId</th>
            <th>UserId</th>
            <th>TotalPrice</th>
            <th>OrderDate</th>
            <th>Verify Signature</th>
        </tr>
        </thead>
        <tbody>
        <c:if test="${empty orderList_verify}">
            <tr>
                <td colspan="8" style="text-align: center;">No orders found.</td>
            </tr>
        </c:if>
        <c:if test="${not empty orderList_verify}">
            <c:forEach var="o" items="${orderList_verify}">
                <tr>
                    <td>${o.orderId}</td>
                    <td>${o.userId}</td>
                    <td>${o.totalPrice}</td>
                    <td>${o.orderDate}</td>
                    <td>${o.status}</td>
                    <td>
                        <c:if test="${o.status eq 'Chờ xác nhận'}">
                            <form method="post" action="ConfirmOrderServlet">
                                <input type="hidden" name="orderId" value="${o.orderId}" />
                                <button type="submit" class="btn btn-success">Xác nhận</button>
                            </form>
                        </c:if>
                    </td>

                    <td>
                        <!-- Nút kiểm tra chữ ký -->
                        <form action="VerifySignatureServlet" method="post">
                            <input type="hidden" name="orderId" value="${o.orderId}">
                            <button type="submit" class="btn btn-primary">Verify Signature</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
        </c:if>
        </tbody>
    </table>

    <!-- Hiển thị nút phân trang -->
    <nav aria-label="Page navigation example">
        <ul class="pagination justify-content-center">
            <c:forEach var="i" begin="1" end="${totalPages}">
                <li class="page-item ${i == currentPage ? 'active' : ''}">
                    <a class="page-link" href="admin_order?page=${i}">${i}</a>
                </li>
            </c:forEach>
        </ul>
    </nav>

    <!-- Hiển thị kết quả kiểm tra -->
    <c:if test="${not empty verifyResult}">
        <div class="verify-result">
            <h3>Verification Result</h3>
            <c:choose>
                <c:when test="${verifyResult.valid}">
                    <p class="text-success">✔ The invoice is valid.</p>
                </c:when>
                <c:otherwise>
                    <p class="text-danger">✘ The invoice is invalid or has been tampered with.</p>
                </c:otherwise>
            </c:choose>
        </div>
    </c:if>
</div>
</body>
</html>
