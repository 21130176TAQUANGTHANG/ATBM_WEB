package Signature;

import DBConnect.DBContext;
import Order.Order;
import Order.OrderItem;

import java.io.PrintWriter;
import java.sql.*;

public class DbSecurity {
    private Connection conn;
    private PreparedStatement ps;
    private ResultSet rs;

    public boolean hasKey(String userId) {
        String query = "SELECT publicKey FROM users WHERE userId = ?";
        try {
            conn = new DBContext().getConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, userId);
            rs = ps.executeQuery();
            return rs.next() && rs.getString("publicKey") != null;
        } catch (Exception e) {
            throw new RuntimeException("Error checking key in database.", e);
        } finally {
            closeResources();
        }
    }

    public void savePublicKeyToDatabase(String userId, String publicKey, Timestamp createTime, Timestamp endTime) {
        String query = "INSERT INTO users (userId, publicKey, createTime, endTime) VALUES (?, ?, ?, ?)";
        try {
            conn = new DBContext().getConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, userId);
            ps.setString(2, publicKey);
            ps.setTimestamp(3, createTime);
            ps.setTimestamp(4, endTime);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeResources();
        }
    }

    public void savePublicKeyToDatabase(String userId, String publicKey) {
        savePublicKeyToDatabase(userId, publicKey, null, null);
    }

    public boolean isPublicKeyExist(String userId) {
        String query = "SELECT COUNT(*) FROM users WHERE userId = ? AND endTime IS NULL";
        try {
            conn = new DBContext().getConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, userId);
            rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeResources();
        }
    }

    public String getPublicKeyFromDatabase(String userId) {
        String query = "SELECT publicKey FROM users WHERE userId = ? AND endTime IS NULL";
        try {
            conn = new DBContext().getConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, userId);
            rs = ps.executeQuery();
            return rs.next() ? rs.getString("publicKey") : null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeResources();
        }
    }

    public void updateEndTime(String userId, Timestamp endTime) {
        String query = "UPDATE users SET endTime = ? WHERE userId = ? AND endTime IS NULL";
        try {
            conn = new DBContext().getConnection();
            ps = conn.prepareStatement(query);
            ps.setTimestamp(1, endTime);
            ps.setString(2, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeResources();
        }
    }

    public void reportLostKey(String userId) {
        updateEndTime(userId, new Timestamp(System.currentTimeMillis()));
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
        } finally {
            closeResources();
        }
    }

    public void uploadSignature(int orderId, String signature) {
        String query = "UPDATE orders SET signature = ? WHERE order_id = ?";
        try {
            conn = new DBContext().getConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, signature);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeResources();
        }
    }

    public void getOrdersByUserId(int orderId, PrintWriter writer) {
        String query = "SELECT o.order_id, o.order_date, o.notes, o.total_price, o.name, o.address, o.phone, o.status, " +
                "od.quantity, od.size, od.subtotal, p.productName, p.productPrice " +
                "FROM orders o " +
                "JOIN orderdetails od ON o.order_id = od.order_id " +
                "JOIN product p ON od.product_id = p.productid " +
                "WHERE o.order_id = ?";
        try {
            conn = new DBContext().getConnection();
            ps = conn.prepareStatement(query);
            ps.setInt(1, orderId);
            rs = ps.executeQuery();

            Order currentOrder = null;
            while (rs.next()) {
                int currentOrderId = rs.getInt("order_id");
                if (currentOrder == null || currentOrder.getOrderId() != currentOrderId) {
                    if (currentOrder != null) {
                        printOrder(currentOrder, writer);
                    }
                    currentOrder = new Order(
                            currentOrderId,
                            rs.getTimestamp("order_date"),
                            rs.getString("notes"),
                            rs.getInt("total_price"),
                            rs.getString("name"),
                            rs.getString("address"),
                            rs.getString("phone"),
                            rs.getString("status")
                    );
                }
                currentOrder.addOrderItem(new OrderItem(
                        rs.getString("productName"),
                        rs.getInt("quantity"),
                        rs.getInt("productPrice"),
                        rs.getInt("subtotal"),
                        rs.getString("size")
                ));
            }

            if (currentOrder != null) {
                printOrder(currentOrder, writer);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
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
                    (double) item.getPrice(),
                    (double) item.getSubtotal());
        }
        writer.println("----------------------------");
        writer.printf("Tổng tiền: %d\n", order.getTotalPrice());
        writer.println("============================");
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
}
