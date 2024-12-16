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

    public void sendRejectionEmail(String recipientEmail, String rejectionReason, String projectName, String projectCode,
                                   String projectDescription, String projectStartDate, String projectEndDate, String type,
                                   String standard, String field, String commune, String district, String conscious) throws MessagingException {
        String address = "Xã " + commune + ", huyện " + district + ", tỉnh " + conscious;

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        String content = "<html><body>"
                + "<h1>Đăng Ký Của Bạn Đã Bị Từ Chối</h1>"
                + "<p>Kính gửi bạn,</p>"
                + "<p>Chúng tôi rất tiếc khi thông báo rằng yêu cầu đăng ký của bạn đã bị từ chối. Dưới đây là lý do từ chối:</p>"
                + "<p><strong>Lý do: " + rejectionReason + "</strong></p>"
                + "<p>Tên dự án: " + projectName + "</p>"
                + "<p>Mã dự án: " + projectCode + "</p>"
                + "<p>Mô tả dự án: " + projectDescription + "</p>"
                + "<p>Thời gian bắt đầu: " + projectStartDate + "</p>"
                + "<p>Thời gian kết thúc: " + projectEndDate + "</p>"
                + "<p>Loại hình: " + type + "</p>"
                + "<p>Tiêu chuẩn: " + standard + "</p>"
                + "<p>Lĩnh vực: " + field + "</p>"
                + "<p>Địa chỉ: " + address + "</p>"
                + "<p>Chúng tôi hy vọng sẽ có cơ hội làm việc với bạn trong tương lai. Nếu bạn có bất kỳ câu hỏi nào, đừng ngần ngại liên hệ với chúng tôi.</p>"
                + "<p>Trân trọng,<br>Đội ngũ hỗ trợ</p>"
                + "<div class='footer'><p>Địa chỉ công ty: Phường 5, Ấp Bắc, Tp.Mỹ Tho</p>"
                + "<p>Email hỗ trợ: ny22167@gmail.com</p></div>"
                + "</body></html>";

        helper.setTo(recipientEmail);
        helper.setSubject("Thông Báo Từ Chối Đăng Ký");
        helper.setText(content, true);

        try {
            mailSender.send(message);
        } catch (MailException e) {
            throw new MessagingException("Lỗi khi gửi email: " + e.getMessage(), e);
        }
    }
}
