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

    public boolean isPublicKeyExist(String userId) {
        // Thay bằng truy vấn thực tế của bạn
        String query = "SELECT COUNT(*) FROM users WHERE userId = ?";
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
        writer.println("========== INVOICE ==========");
        writer.println("Customer Name: " + order.getName());
        writer.println("Address: " + order.getAddress());
        writer.println("Phone Number: " + order.getPhone());
        writer.println("----------------------------");
        writer.println("Order Date: " + order.getOrderDate());
        writer.println("Order ID: " + order.getOrderId());
        writer.println("Notes: " + order.getNotes());
        writer.println("----------------------------");
        writer.println("Product List:");
        writer.printf("%-20s %-10s %-10s %-10s\n", "Product Name", "Qty", "Unit Price", "Total Price");
        for (OrderItem item : order.getOrderItems()) {
            writer.printf("%-20s %-10d %-10.2f %-10.2f\n",
                    item.getProductName(),
                    item.getQuantity(),
                    (double) item.getPrice(), // Cast the price to double
                    (double) item.getSubtotal()); // Cast subtotal to double
        }
        writer.println("----------------------------");
        writer.printf("Total Price: %d\n", order.getTotalPrice()); // Use %d for int type
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


    // Kiểm tra xem dữ liệu đã tồn tại hay chưa
    public boolean isInvoiceExists(int orderId, String userId) throws Exception {
        String sql = "SELECT COUNT(*) FROM invoices WHERE user_id = ? AND order_id = ?";
        try {
            conn = new DBContext().getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, userId);
            ps.setInt(2, orderId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Trả về true nếu có dữ liệu
            }
            return false;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
    }


    public void uploadInvoice(String userId, int orderId, String hash, String signature) {
        String checkSql = "SELECT hash, signature FROM invoices WHERE user_id = ? AND order_id = ?";
        String updateSql = "UPDATE invoices SET hash = ?, signature = ?, created_at = ? WHERE user_id = ? AND order_id = ?";
        String insertSql = "INSERT INTO invoices (user_id, order_id, hash, signature, created_at) VALUES (?, ?, ?, ?, ?)";

        try {
            conn = new DBContext().getConnection();

            // Kiểm tra xem bản ghi đã tồn tại chưa
            ps = conn.prepareStatement(checkSql);
            ps.setString(1, userId);
            ps.setInt(2, orderId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Nếu bản ghi tồn tại, kiểm tra giá trị hash và signature
                String existingHash = rs.getString("hash");
                String existingSignature = rs.getString("signature");

                if ((existingHash == null && hash != null) || (existingSignature == null && signature != null)) {
                    // Cập nhật hash hoặc signature nếu một trong hai đang là null
                    ps = conn.prepareStatement(updateSql);
                    ps.setString(1, hash); // Ghi đúng vào cột hash
                    ps.setString(2, signature); // Ghi đúng vào cột signature
                    ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                    ps.setString(4, userId);
                    ps.setInt(5, orderId);
                    ps.executeUpdate();
                } else {
                    System.out.println("Both hash and signature already exist.");
                }
            } else {
                // Nếu bản ghi chưa tồn tại, chèn bản ghi mới
                ps = conn.prepareStatement(insertSql);
                ps.setString(1, userId);
                ps.setInt(2, orderId);
                ps.setString(3, hash); // Ghi đúng vào cột hash
                ps.setString(4, signature); // Ghi đúng vào cột signature
                ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                ps.executeUpdate();
            }
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



    // Lấy public key từ bảng users
    public String getPublicKeyByUserId(String userId) throws Exception {
        String sql = "SELECT publicKey FROM users WHERE userId = ?";
        try {
            conn = new DBContext().getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("publicKey");
            }
            return null;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
    }
    // Lấy thông tin hóa đơn từ bảng invoices
    public Invoice getInvoiceByOrderId(int orderId) throws Exception {
        String sql = "SELECT * FROM invoices WHERE order_id = ?";
        try {
            conn = new DBContext().getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, orderId);
            rs = ps.executeQuery();
            if (rs.next()) {
                Invoice invoice = new Invoice();
                invoice.setUserId(rs.getString("user_id"));
                invoice.setOrderId(rs.getInt("order_id"));
                invoice.setHashFromContent(rs.getString("hash"));
                invoice.setSignature(rs.getString("signature"));
                return invoice;
            }
            return null;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
    }
}
