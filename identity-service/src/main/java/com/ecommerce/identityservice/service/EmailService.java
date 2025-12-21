package com.ecommerce.identityservice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    JavaMailSender mailSender;
    public void sendOtpEmail(String toEmail,
                             String userName,
                             String otpCode,
                             int expireMinutes) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper =
                new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject("Xác thực tài khoản - Mã OTP");
        helper.setFrom("your_email@gmail.com");

        String htmlContent = """
                <div style="font-family: Arial, sans-serif; line-height: 1.6;">
                    <h2>Xin chào %s,</h2>

                    <p>Chúng tôi đã nhận được yêu cầu xác thực tài khoản của bạn.</p>

                    <p>Mã xác thực (OTP) của bạn là:</p>

                    <h1 style="color: #2F80ED;">%s</h1>

                    <p>⏳ Mã này sẽ hết hạn sau <b>%d phút</b>.</p>

                    <hr>

                    <p><b>Lưu ý bảo mật:</b></p>
                    <ul>
                        <li>Không chia sẻ mã OTP cho bất kỳ ai</li>
                        <li>Chúng tôi sẽ không bao giờ yêu cầu mã này qua điện thoại hoặc email</li>
                    </ul>

                    <p>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email.</p>

                    <p>Trân trọng,<br>
                    <b>K-SHOP Support Team</b></p>
                </div>
                """.formatted(userName, otpCode, expireMinutes);

        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}
