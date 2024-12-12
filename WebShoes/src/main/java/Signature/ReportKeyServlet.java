package Signature;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Base64;

@WebServlet("/ReportKeyServlet")
public class ReportKeyServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Điều hướng sang phương thức xử lý chính nếu cần
        doPost(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().write("Key has been reported successfully.");
        HttpSession session = req.getSession();
        String userId = (String) session.getAttribute("userId"); // Lấy userId từ session

        if (userId == null) {
            resp.sendRedirect("newKeyResult.jsp");
            return;
        }

        try {
            DbSecurity db = new DbSecurity();
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());

            // Cập nhật endTime cho public key hiện tại khi báo mất key
            db.updateEndTime(userId, currentTime);

            // Tạo cặp key mới
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair pair = keyGen.generateKeyPair();

            // Chuyển đổi Public Key và Private Key sang dạng Base64
            String newPublicKey = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
            String newPrivateKey = Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded());

            // Lưu public key mới vào database (createTime = currentTime, endTime = null)
            db.savePublicKeyToDatabase(userId, newPublicKey, currentTime, null);

            // Lưu private key vào session hoặc trả về cho người dùng
            session.setAttribute("privateKey", newPrivateKey);

            // Chuyển hướng đến trang hiển thị kết quả
            req.setAttribute("publicKey", newPublicKey);
            req.setAttribute("privateKey", newPrivateKey);
            req.getRequestDispatcher("newKeyResult.jsp").forward(req, resp);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi tạo cặp khóa.");
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi xử lý báo mất key.");
        }
    }
}
