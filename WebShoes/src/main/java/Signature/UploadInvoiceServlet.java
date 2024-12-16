package Signature;

import LoginUser.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Base64;

@WebServlet("/UploadInvoiceServlet")
@MultipartConfig(maxFileSize = 10 * 1024 * 1024) // Tối đa 10MB
public class UploadInvoiceServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Lấy thông tin từ form
        String orderIdParam = req.getParameter("orderId");
        int orderId = Integer.parseInt(orderIdParam);
        HttpSession session = req.getSession();
        String userId = null;

        // Kiểm tra nếu người dùng đăng nhập
        User user = (User) session.getAttribute("user");
        if (user != null) {
            userId = user.getId();
        }

        // Lấy các tệp từ form
        Part hashFilePart = req.getPart("signatureFilehash");
        Part signatureFilePart = req.getPart("signatureFile");

        // Đọc và xử lý nội dung của file hash
        InputStream hashFileContent = hashFilePart.getInputStream();
        String hashFileContentStr = new String(hashFileContent.readAllBytes(), StandardCharsets.UTF_8);

        // Tính toán hash (SHA-256)
        String hash = calculateHash(hashFileContentStr);

        // Đọc và xử lý nội dung của file chữ ký
        InputStream signatureFileContent = signatureFilePart.getInputStream();
        String signatureFileContentStr = new String(signatureFileContent.readAllBytes(), StandardCharsets.UTF_8);

        // Tách chữ ký từ nội dung file chữ ký
        String signature = extractSignature(signatureFileContentStr);

        // Kiểm tra và lưu hash vào database
        try {
            DbSecurity dbSecurity = new DbSecurity();
            dbSecurity.uploadInvoice(userId,orderId,hash,signature);
        } catch (Exception e) {
            resp.getWriter().println("Lỗi khi xử lý yêu cầu: " + e.getMessage());
            e.printStackTrace();
        }

        // Đưa ra phản hồi sau khi xử lý thành công
        resp.getWriter().println("Invoice hash and signature uploaded successfully for order ID: " + orderId);
    }

    // Hàm tính hash (SHA-256)
    private String calculateHash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error calculating hash", e);
        }
    }

    // Hàm tách chữ ký từ nội dung file
    private String extractSignature(String fileContent) {
        final String SIGNATURE_PREFIX = "Chữ ký số:";
        int signatureIndex = fileContent.indexOf(SIGNATURE_PREFIX);
        if (signatureIndex != -1) {
            return fileContent.substring(signatureIndex + SIGNATURE_PREFIX.length()).trim();
        }
        return null; // Trả về null nếu không tìm thấy chữ ký
    }
}
