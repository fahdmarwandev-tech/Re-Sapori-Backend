package com.resapori.e_commerce.service;

public interface IWhatsAppService {

    /**
     * Dispatches a WhatsApp template message using Meta Cloud API.
     *
     * @param phoneNumber  Recipient phone number (e.g., Egyptian number "01122406458" or international format)
     * @param templateName Name of the approved template on Meta WhatsApp Business
     * @param languageCode Template language code (e.g., "en", "ar")
     * @return Raw JSON response from Meta WhatsApp API containing message ID / wamid
     */
    String sendTemplate(String phoneNumber, String templateName, String languageCode);

    /**
     * Dispatches a default WhatsApp template ("testingtemplate" in English).
     *
     * @param phoneNumber Recipient phone number
     * @return Raw JSON response from Meta WhatsApp API
     */
    String sendTemplate(String phoneNumber);

    /**
     * Formats a phone number for Meta WhatsApp Cloud API (especially Egyptian numbers).
     *
     * @param phoneNumber raw input phone number
     * @return normalized phone number (e.g. 201122406458)
     */
    String formatPhoneNumber(String phoneNumber);
}
