package com.resapori.e_commerce.service;

public interface IEmailService {
    void sendEmail(String to, String subject, String body);
}
