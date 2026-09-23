package com.resapori.e_commerce.service;

import com.resapori.e_commerce.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        emailService.setFromEmail("campuspulseai@gmail.com");
    }

    @Test
    @DisplayName("Should create and send SimpleMailMessage with expected parameters")
    void shouldSendEmailSuccessfully() {
        String to = "recipient@example.com";
        String subject = "Reset Your Password";
        String body = "Your reset OTP code is 123456";

        emailService.sendEmail(to, subject, body);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertEquals("campuspulseai@gmail.com", capturedMessage.getFrom());
        assertArrayEquals(new String[]{to}, Objects.requireNonNull(capturedMessage.getTo()));
        assertEquals(subject, capturedMessage.getSubject());
        assertEquals(body, capturedMessage.getText());
    }
}
