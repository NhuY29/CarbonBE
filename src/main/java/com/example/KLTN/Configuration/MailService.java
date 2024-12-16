package com.example.KLTN.Configuration;

import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendRejectionEmail(String recipientEmail, String rejectionReason, String projectName, String projectCode) throws MessagingException {
        // Tạo đối tượng MimeMessage
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8"); // Đặt mã hóa UTF-8

        // Cấu trúc nội dung email với thông tin dự án
        String content = "<html><body>"
                + "<h1>Đăng Ký Của Bạn Đã Bị Từ Chối</h1>"
                + "<p>Kính gửi bạn,</p>"
                + "<p>Chúng tôi rất tiếc khi thông báo rằng yêu cầu đăng ký của bạn đã bị từ chối. Dưới đây là lý do từ chối:</p>"
                + "<p><strong>Lý do: " + rejectionReason + "</strong></p>"
                + "<p><strong>Tên dự án: " + projectName + "</strong></p>"
                + "<p><strong>Mã dự án: " + projectCode + "</strong></p>"
                + "<p>Chúng tôi hy vọng sẽ có cơ hội làm việc với bạn trong tương lai. Nếu bạn có bất kỳ câu hỏi nào, đừng ngần ngại liên hệ với chúng tôi.</p>"
                + "<p>Trân trọng,<br>Đội ngũ hỗ trợ</p>"
                + "<div class='footer'><p>Địa chỉ công ty: Phường 5, Ấp Bắc, Tp.Mỹ Tho</p>"
                + "<p>Email hỗ trợ: ny22167@gmail.com</p></div>"
                + "</body></html>";

        // Thiết lập các thông tin email
        helper.setTo(recipientEmail);
        helper.setSubject("Thông Báo Từ Chối Đăng Ký");
        helper.setText(content, true); // true cho phép sử dụng HTML trong nội dung

        try {
            // Gửi email
            mailSender.send(message);
        } catch (MailException e) {
            // Xử lý lỗi khi gửi email
            throw new MessagingException("Lỗi khi gửi email: " + e.getMessage(), e);
        }
    }

}
