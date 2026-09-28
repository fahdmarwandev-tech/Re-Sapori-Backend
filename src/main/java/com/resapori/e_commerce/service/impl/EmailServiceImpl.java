package com.resapori.e_commerce.service.impl;

import com.resapori.e_commerce.service.IEmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements IEmailService {

    private final JavaMailSender mailSender;

    @Setter
    @Value("${spring.mail.username:campuspulseai@gmail.com}")
    private String fromEmail = "campuspulseai@gmail.com";

    @Override
    public void sendEmail(String to, String subject, String body) {
        log.info("Sending email to: {}, subject: {}", to, subject);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email successfully sent to: {}", to);
        } catch (Exception ex) {
            log.warn("Failed to dispatch email via SMTP: {}. Code: {}", ex.getMessage(), body);
        }
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        log.info("Sending HTML email to: {}, subject: {}", to, subject);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            try {
                helper.setFrom(fromEmail, "Re Sapori");
            } catch (Exception e) {
                helper.setFrom(fromEmail);
            }
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("HTML email successfully sent to: {}", to);
        } catch (Exception ex) {
            log.warn("Failed to dispatch HTML email via SMTP to {}: {}", to, ex.getMessage());
        }
    }
}
