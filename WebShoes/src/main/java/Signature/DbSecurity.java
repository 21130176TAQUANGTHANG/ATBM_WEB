package Signature;

import DBConnect.DBContext;

import java.sql.*;

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

    public static void main(String[] args) {
        DbSecurity db = new DbSecurity();
        db.isPublicKeyExist("1");

    }
}
