package com.example.KLTN.Service;

import com.example.KLTN.DTO.ClientSdi;
import com.example.KLTN.DTO.MailDTO;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ClientImpl implements ClientServe {
    @Autowired
    JavaMailSender mailSender;

    @Autowired
    UserService userService;

    @Override
    public boolean creat(ClientSdi sdi) {
        try {
            MailDTO mailDTO = new MailDTO();
            mailDTO.setTo(sdi.getEmail());
            mailDTO.setSubject("Xác nhận đăng ký tài khoản thành công!");
            Map<String, Object> map = new HashMap<>();
            mailDTO.setProps(map);
            userService.sendHtmlMail(mailDTO, "Client");
            return true;
        } catch (MessagingException exp) {
            exp.printStackTrace();
        }
        return false;
    }
}
