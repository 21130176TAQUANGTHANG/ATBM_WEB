package Signature;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.security.spec.PKCS8EncodedKeySpec;


/**
 *
 * @author Admin
 */

public class ElectronicSignature {
    private KeyPair keyPair;
    private Signature signature;
    private SecureRandom secureRandom;
    private PublicKey publicKey;
    private PrivateKey privateKey;

    public ElectronicSignature(String alg, String algRandom, String prov) throws NoSuchAlgorithmException, NoSuchProviderException {
        // Tạo bộ sinh cặp khóa
        KeyPairGenerator generator = KeyPairGenerator.getInstance(alg, prov);
        secureRandom = SecureRandom.getInstance(algRandom, prov);
        generator.initialize(2048, secureRandom); // Key size should be 2048 bits for security
        keyPair = generator.generateKeyPair();
        signature = Signature.getInstance("SHA256withDSA", prov);
        genKey();
    }
    // Phương thức tạo khóa, trả về true nếu thành công
    public boolean genKey() {
        if (keyPair == null) return false;
        publicKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();
        System.out.println("Khóa đã được tạo thành công!");
        return true;
    }
    // Phương thức tải khóa công khai từ chuỗi Base64
    public void loadPublic(String base64Key) throws GeneralSecurityException {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        KeyFactory keyFactory = KeyFactory.getInstance("DSA");
        publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
    }
//    // Phương thức ký thông điệp và trả về chữ ký dưới dạng chuỗi Base64
//    public String sign(String mes) throws InvalidKeyException, SignatureException {
//        if (privateKey == null) {
//            System.err.println("Khóa bí mật chưa được tạo hoặc tải!");
//            genKey();  // Gọi genKey() để tạo khóa nếu chưa có
//        }
//        byte[] data = mes.getBytes();
//        signature.initSign(privateKey);
//        signature.update(data);
//        byte[] sign = signature.sign();
//        return Base64.getEncoder().encodeToString(sign);
//    }
    // Phương thức ký một tệp và trả về chữ ký dưới dạng chuỗi Base64
    public String signFile(String src) throws InvalidKeyException, IOException, SignatureException {
        if (privateKey == null) {
            throw new IllegalStateException("Khóa bí mật chưa được tạo hoặc tải!");
        }
        signature.initSign(privateKey);
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(src))) {
            byte[] buff = new byte[1024];
            int read;
            while ((read = bis.read(buff)) != -1) {
                signature.update(buff, 0, read);
            }
        }
        byte[] sign = signature.sign();
        return Base64.getEncoder().encodeToString(sign);
    }
    // Phương thức xác thực chữ ký từ thông điệp và chữ ký dưới dạng Base64
    public boolean verify(String mes, String sign) throws InvalidKeyException, SignatureException {
        if (publicKey == null) {
            throw new IllegalStateException("Khóa công khai chưa được tạo hoặc tải!");
        }
        signature.initVerify(publicKey);
        byte[] data = mes.getBytes();
        byte[] signValue = Base64.getDecoder().decode(sign);
        signature.update(data);
        return signature.verify(signValue);
    }
    // Phương thức xác thực chữ ký từ một tệp
    public boolean verifyFile(String src, String sign) throws SignatureException, IOException, InvalidKeyException {
        if (publicKey == null) {
            throw new IllegalStateException("Khóa công khai chưa được tạo hoặc tải!");
        }
        signature.initVerify(publicKey);
        byte[] signValue = Base64.getDecoder().decode(sign);
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(src))) {
            byte[] buff = new byte[1024];
            int read;
            while ((read = bis.read(buff)) != -1) {
                signature.update(buff, 0, read);
            }
        }
        return signature.verify(signValue);
    }
    // Phương thức trả về khóa công khai dưới dạng chuỗi Base64
    public String getPublicKeyAsString() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    // Phương thức để lấy chuỗi Base64 của khóa bí mật
    public String getPrivateKeyAsString() {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }
    public void loadPrivateKey(String base64Key) throws GeneralSecurityException {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        KeyFactory keyFactory = KeyFactory.getInstance("DSA");
        privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));  // Sử dụng PKCS8EncodedKeySpec thay cho X509EncodedKeySpec
    }

    // Ký thông điệp (đơn hàng)
    public String sign(String message) throws InvalidKeyException, SignatureException {
        if (privateKey == null) {
            genKey();  // Tạo khóa nếu chưa có
        }
        byte[] data = message.getBytes();
        signature.initSign(privateKey);
        signature.update(data);
        byte[] signedData = signature.sign();
        return Base64.getEncoder().encodeToString(signedData);
    }
}
