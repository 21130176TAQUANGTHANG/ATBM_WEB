package Signature;

import DBConnect.DBContext;
import Order.Order;
import Order.OrderItem;

import java.io.PrintWriter;
import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbSecurity {
    Connection conn;
    PreparedStatement ps;
    ResultSet rs;

<<<<<<< HEAD
    public boolean hasKey(String userId) {
        String query = "SELECT publicKey FROM users WHERE userId=?";
        try {
            conn = new DBContext().getConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, userId);
            rs = ps.executeQuery();
            // Kiểm tra nếu có public_key
            if (rs.next() && rs.getString("publicKey") != null) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error checking key in database.", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }


=======
    // Lưu public key vào cơ sở dữ liệu
>>>>>>> buu
    public void savePublicKeyToDatabase(String userId, String publicKey, Timestamp createTime, Timestamp endTime) {
        try {
            String query = "INSERT INTO users (userId, publicKey, createTime, endTime) VALUES (?, ?, ?, ?)";
            conn = new DBContext().getConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, userId);
            ps.setString(2, publicKey);
            ps.setTimestamp(3, createTime);
            ps.setTimestamp(4, endTime); // endTime có thể là null nếu là khóa mới
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

<<<<<<< HEAD
    public void savePublicKeyToDatabase(String userId, String publicKey) {
        try {
            String query = "INSERT INTO users (userId, publicKey) VALUES (?, ?)";
            conn = new DBContext().getConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, userId);
            ps.setString(2, publicKey);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

=======
    // Kiểm tra nếu người dùng đã có public key
>>>>>>> buu
    public boolean isPublicKeyExist(String userId) {
        String query = "SELECT COUNT(*) FROM users WHERE userId = ? AND endTime IS NULL";
        try {
            conn = new DBContext().getConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    // Lấy public key từ cơ sở dữ liệu
    public String getPublicKeyFromDatabase(String userId) {
        String query = "SELECT publicKey FROM users WHERE userId = ? AND endTime IS NULL";
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

<<<<<<< HEAD
    public void updateKeyEndTime(String userId) {
        String query = "UPDATE users SET endTime = CURRENT_TIMESTAMP WHERE userId = ?";
=======
    // Cập nhật endTime cho public key khi báo mất key
    public void updateEndTime(String userId, Timestamp endTime) {
        try {
            String query = "UPDATE users SET endTime = ? WHERE userId = ? AND endTime IS NULL";
            conn = new DBContext().getConnection();
            ps = conn.prepareStatement(query);
            ps.setTimestamp(1, endTime); // Thời gian khi báo mất key
            ps.setString(2, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Lấy public key hiện tại từ cơ sở dữ liệu (dùng cho việc báo mất key)
    public String getCurrentPublicKey(String userId) {
        String query = "SELECT publicKey FROM users WHERE userId = ? AND endTime IS NULL";
>>>>>>> buu
        try {
            conn = new DBContext().getConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, userId);
<<<<<<< HEAD
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeResources();
        }
=======
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("publicKey");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    public void reportLostKey(String userId) {
        String query = "UPDATE users SET endTime = ? WHERE userId = ? AND endTime IS NULL";
        try {
            conn = new DBContext().getConnection();
            ps = conn.prepareStatement(query);

            // Thời điểm hiện tại
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            ps.setTimestamp(1, currentTime); // Gán endTime = thời điểm hiện tại
            ps.setString(2, userId);

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void deleteKey(String userId) {
        String query = "DELETE FROM users WHERE userId = ?";
        try {
            conn = new DBContext().getConnection();
            ps = conn.prepareStatement(query);

            ps.setString(1, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        DbSecurity db = new DbSecurity();

        // Kiểm tra nếu public key tồn tại
        boolean exists = db.isPublicKeyExist("1");
        System.out.println("Public key exists: " + exists);

        // Cập nhật endTime khi báo mất key
        Timestamp endTime = new Timestamp(System.currentTimeMillis());
        db.updateEndTime("1", endTime);

        // Lấy public key hiện tại
        String publicKey = db.getPublicKeyFromDatabase("1");
        System.out.println("Current Public Key: " + publicKey);
>>>>>>> buu
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
        writer.println("============================");
        writer.println();
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

    public String getPublicKey(String userId) {
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
            throw new RuntimeException("Lỗi khi lấy Public Key từ CSDL", e);
        }
        return null;
    }

    public void uploadSignature(int orderId, String signature) {
        String query = "UPDATE orders SET signature = ? WHERE order_id = ?";
        try {
            conn = new DBContext().getConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, signature); // Thiết lập giá trị chữ ký
            ps.setInt(2, orderId);      // Thiết lập ID của đơn hàng
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


}
