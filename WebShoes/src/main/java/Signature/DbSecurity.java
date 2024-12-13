package Signature;

import DBConnect.DBContext;
import Order.Order;
import Order.OrderItem;

import java.io.PrintWriter;
import java.sql.*;

import static java.sql.DriverManager.getConnection;

public class DbSecurity {
    Connection conn;
    PreparedStatement ps;
    ResultSet rs;

    public void savePublicKeyToDatabase(String userId, String publicKey,  Timestamp createTime, Timestamp endTime) {
        try {
            String query = "INSERT INTO users (userId, publicKey, createTime, endTime) VALUES (?, ?, ?, ?)";
            conn = new DBContext().getConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, userId);
            ps.setString(2, publicKey);
            ps.setTimestamp(3, createTime);
            ps.setTimestamp(4, endTime); // ban đầu là null
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isPublicKeyExist(String userId) {
        // Thay bằng truy vấn thực tế của bạn
        String query = "SELECT COUNT(*) FROM users WHERE userId = ?";
        try {
            conn = new DBContext().getConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getInt(1) > 0) {
                    return true;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    public String getPublicKeyFromDatabase(String userId) {
        String query = "SELECT publicKey FROM users WHERE userId = ?";
        try {
            conn = new DBContext().getConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("publicKey");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    return null;
    }

    // Phương thức in hóa đơn chứa thông tin sản phẩm
    public void getOrdersByUserId(int orderid, PrintWriter writer) {
        String query = "SELECT o.order_id, o.order_date, o.notes, o.total_price, o.name, o.address, o.phone, o.status, " +
                "od.quantity, od.size, od.subtotal, p.productName, p.productPrice " +
                "FROM orders o " +
                "JOIN orderdetails od ON o.order_id = od.order_id " +
                "JOIN product p ON od.product_id = p.productid " +
                "WHERE o.order_id = ?";

        try {
            conn = new DBContext().getConnection();
            ps = conn.prepareStatement(query);
            ps.setInt(1, orderid);
            rs = ps.executeQuery();

            Order currentOrder = null;
            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                if (currentOrder == null || currentOrder.getOrderId() != orderId) {
                    // Nếu chuyển sang hóa đơn mới, in hóa đơn cũ
                    if (currentOrder != null) {
                        printOrder(currentOrder, writer);
                    }

                    // Khởi tạo hóa đơn mới
                    currentOrder = new Order(
                            orderId,
                            rs.getTimestamp("order_date"),
                            rs.getString("notes"),
                            rs.getInt("total_price"),
                            rs.getString("name"),
                            rs.getString("address"),
                            rs.getString("phone"),
                            rs.getString("status")
                    );
                }

                // Thêm sản phẩm vào hóa đơn
                currentOrder.addOrderItem(new OrderItem(
                        rs.getString("productName"),  // Tên sản phẩm
                        rs.getInt("quantity"),       // Số lượng
                        rs.getInt("productPrice"),// Giá sản phẩm
                        rs.getInt("subtotal"),    // Tổng tiền (subtotal)
                        rs.getString("size")         // Kích thước (size)
                ));
            }

            // In hóa đơn cuối cùng
            if (currentOrder != null) {
                printOrder(currentOrder, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }
    }
    private void printOrder(Order order, PrintWriter writer) {
        String signature = getSignatureFromDatabase(order.getUserId(), order.getOrderId());
        writer.println("========== HÓA ĐƠN ==========");
        writer.println("Tên khách hàng: " + order.getName());
        writer.println("Địa chỉ: " + order.getAddress());
        writer.println("Số điện thoại: " + order.getPhone());
        writer.println("----------------------------");
        writer.println("Ngày đặt hàng: " + order.getOrderDate());
        writer.println("Mã đơn hàng: " + order.getOrderId());
        writer.println("Ghi chú: " + order.getNotes());
        writer.println("----------------------------");
        writer.println("Danh sách sản phẩm:");
        writer.printf("%-20s %-10s %-10s %-10s\n", "Tên sản phẩm", "SL", "Đơn giá", "Thành tiền");
        for (OrderItem item : order.getOrderItems()) {
            writer.printf("%-20s %-10d %-10.2f %-10.2f\n",
                    item.getProductName(),
                    item.getQuantity(),
                    (double) item.getPrice(), // Ép kiểu giá thành sang double
                    (double) item.getSubtotal()); // Ép kiểu subtotal sang double

        }
        writer.println("----------------------------");
        writer.printf("Tổng tiền: %d\n", order.getTotalPrice()); // Dùng %d cho kiểu int
        writer.println("Chữ ký điện tử: " + signature);
        writer.println("============================");
        writer.println();

    }
    private String getSignatureFromDatabase(String userId, int orderId) {
        String query = "SELECT signature FROM order_signatures WHERE userId = ? AND orderId = ?";
        try {
            conn = new DBContext().getConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, userId);
            ps.setInt(2, orderId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("signature");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lấy chữ ký từ cơ sở dữ liệu.", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeResources();
        }
        return "Không có chữ ký";
    }
    public void saveSignatureToDatabase(String userId, int orderId, String signature) {
        try {
            String query = "INSERT INTO order_signatures (userId, orderId, signature) VALUES (?, ?, ?)";
            try {
                conn = new DBContext().getConnection();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            ps = conn.prepareStatement(query);
            ps.setString(1, userId);
            ps.setInt(2, orderId);
            ps.setString(3, signature);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lưu chữ ký vào cơ sở dữ liệu.", e);
        } finally {
            closeResources();
        }
    }

    private void closeResources() {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            throw new RuntimeException("Error closing database resources", e);
        }
    }
    public static void main(String[] args) {
//        DbSecurity db = new DbSecurity();
//        int orderid = 27;
//        PrintWriter writer = new PrintWriter(System.out);
//        db.getOrdersByUserId(orderid, writer);
        try {
            DbSecurity dbSecurity = new DbSecurity();
            ElectronicSignature electronicSignature = new ElectronicSignature("DSA", "SHA1PRNG", "SUN");
            SignatureService signatureService = new SignatureService(dbSecurity, electronicSignature);

            // Tạo chữ ký cho đơn hàng
            String userId = "1";
            int orderId = 1001;
            signatureService.signOrder(userId, orderId);

            // In hóa đơn có chữ ký
            PrintWriter writer = new PrintWriter(System.out);
            dbSecurity.getOrdersByUserId(orderId, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



}
