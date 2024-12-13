package Signature;

public class SignatureService {
    private DbSecurity dbSecurity;
    private ElectronicSignature electronicSignature;

    public SignatureService(DbSecurity dbSecurity, ElectronicSignature electronicSignature) {
        this.dbSecurity = dbSecurity;
        this.electronicSignature = electronicSignature;
    }

    public void signOrder(String userId, int orderId) {
        try {
            // Lấy thông tin đơn hàng (giả định nội dung đơn hàng là orderId dạng chuỗi)
            String orderContent = "OrderId: " + orderId;

            // Tạo chữ ký
            String signature = electronicSignature.sign(orderContent);

            // Lưu chữ ký vào cơ sở dữ liệu
            dbSecurity.saveSignatureToDatabase(userId, orderId, signature);

            System.out.println("Chữ ký đã được lưu thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi tạo chữ ký cho đơn hàng.", e);
        }
    }
}

