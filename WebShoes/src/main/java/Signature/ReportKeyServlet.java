package Signature;

import LoginUser.AccountFF;
import LoginUser.GoogleAccount;
import LoginUser.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.security.*;
import java.util.Base64;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Base64;
@WebServlet("/ReportKeyServlet")
public class ReportKeyServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        String userId = (String) session.getAttribute("userId");

        if (userId == null) {
            resp.sendRedirect("login.jsp?error=notLoggedIn");

@WebServlet("/ReportKeyServlet")
public class ReportKeyServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        String userId = null;

        User user = (User) session.getAttribute("user");
        if (user != null) {
            userId = user.getId();
        }

        if (userId == null) {
            resp.sendRedirect("login.jsp"); // Chuyển hướng nếu chưa đăng nhập

            return;
        }

        try {
            DbSecurity db = new DbSecurity();
            // Kiểm tra nếu Public Key tồn tại
            if (db.isPublicKeyExist(userId)) {
                // Cập nhật endTime của key hiện tại
                db.updateKeyEndTime(userId);

                // Gửi thông báo thành công
                req.setAttribute("message", "Key của bạn đã được báo mất thành công.");
            } else {
                // Không tìm thấy key
                req.setAttribute("errorMessage", "Không tìm thấy key để báo mất.");
            }

            // Chuyển hướng về trang kết quả
            req.getRequestDispatcher("reportResult.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi xử lý yêu cầu.");
            KeyDeletionScheduler scheduler = new KeyDeletionScheduler();

            // Cập nhật thời gian endTime
            db.reportLostKey(userId);

            // Tính toán thời gian xóa key
            long currentTime = System.currentTimeMillis();
            long deletionTime = currentTime + 60000; // 1 phút sau
            Timestamp deletionTimestamp = new Timestamp(deletionTime);

            // Lập lịch xóa key
            scheduler.scheduleKeyDeletion(userId);

            // Gửi thông tin thời gian xóa đến JSP
            req.setAttribute("deletionTime", deletionTimestamp);
            req.getRequestDispatcher("userProfile.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi báo mất khóa.");
        }
    }
}
