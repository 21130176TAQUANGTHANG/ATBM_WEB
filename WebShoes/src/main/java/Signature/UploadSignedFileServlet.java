package Signature;

import LoginUser.User;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/UploadSignedFileServlet")
@MultipartConfig(maxFileSize = 10 * 1024 * 1024) // Tối đa 10MB
public class UploadSignedFileServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        String userId = null;

        User user = (User) session.getAttribute("user");
        if (user != null) {
            userId = user.getId();
        }

        String orderIdParam = req.getParameter("orderId");
        if (orderIdParam == null || orderIdParam.isEmpty()) {
            resp.getWriter().println("Order ID is missing.");
            return;
        }

        int orderId;
        try {
            orderId = Integer.parseInt(orderIdParam);
        } catch (NumberFormatException e) {
            resp.getWriter().println("Invalid Order ID format.");
            return;
        }

        String signatureContent;
        try (var inputStream = req.getPart("signatureFile").getInputStream()) {
            String fileContent = new String(inputStream.readAllBytes(), "UTF-8");
            signatureContent = extractSignature(fileContent);

            if (signatureContent == null || signatureContent.isEmpty()) {
                resp.getWriter().println("No valid signature found in the uploaded file.");
                return;
            }
        } catch (Exception e) {
            resp.getWriter().println("Error reading uploaded file: " + e.getMessage());
            return;
        }

        try {
            DbSecurity db = new DbSecurity();
            db.uploadSignature(orderId, signatureContent);
            req.getRequestDispatcher("CustomerOrder.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().println("Error uploading signature: " + e.getMessage());
        }
    }

    private String extractSignature(String fileContent) {
        String signaturePrefix = "signature:";
        int startIndex = fileContent.indexOf(signaturePrefix);
        if (startIndex != -1) {
            startIndex += signaturePrefix.length();
            String signature = fileContent.substring(startIndex).trim();
            if (signature.length() > 0) {
                return signature;
            }
        }
        return null;
    }
}

