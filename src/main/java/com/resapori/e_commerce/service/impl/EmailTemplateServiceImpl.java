package com.resapori.e_commerce.service.impl;

import com.resapori.e_commerce.northbound.dto.order.OrderItemResponse;
import com.resapori.e_commerce.northbound.dto.order.OrderResponse;
import com.resapori.e_commerce.service.IEmailTemplateService;
import com.resapori.e_commerce.southbound.enums.OrderType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmailTemplateServiceImpl implements IEmailTemplateService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy - hh:mm a", Locale.US);

    public static String formatOrderNumber(UUID orderId) {
        if (orderId == null) return "#RS-000000";
        String clean = orderId.toString().replace("-", "").toUpperCase();
        return "#RS-" + (clean.length() >= 6 ? clean.substring(0, 6) : clean);
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    private static String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0.00";
        return String.format(Locale.US, "%,.2f", amount);
    }

    @Override
    public String buildOtpEmail(String otp, int expirationMinutes) {
        String safeOtp = escapeHtml(otp);
        String template = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>Re Sapori - Verification Code</title>
            </head>
            <body style="margin: 0; padding: 0; background-color: #0c0c0c; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; -webkit-font-smoothing: antialiased;">
              <table width="100%" border="0" cellspacing="0" cellpadding="0" style="background-color: #0c0c0c; padding: 40px 15px;">
                <tr>
                  <td align="center">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0" style="max-width: 560px; background-color: #171614; border: 1px solid #2e281e; border-radius: 14px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.6);">
                      <!-- Header -->
                      <tr>
                        <td align="center" style="padding: 35px 30px 20px 30px; border-bottom: 1px solid rgba(223, 153, 38, 0.2);">
                          <div style="font-size: 24px; font-weight: 800; letter-spacing: 3px; color: #df9926; text-transform: uppercase;">RE SAPORI</div>
                          <div style="font-size: 11px; letter-spacing: 2.5px; color: #9e9382; text-transform: uppercase; margin-top: 5px;">Fine Dining &amp; Artisanal Pizzeria</div>
                        </td>
                      </tr>
                      
                      <!-- Content Body -->
                      <tr>
                        <td style="padding: 35px 35px 25px 35px; color: #e6e2dd;">
                          <div style="text-align: center; margin-bottom: 25px;">
                            <span style="display: inline-block; background: rgba(223, 153, 38, 0.12); border: 1px solid #df9926; color: #ffb952; padding: 6px 18px; border-radius: 20px; font-size: 11px; font-weight: 700; letter-spacing: 1.5px; text-transform: uppercase;">
                              Password Reset Request
                            </span>
                          </div>

                          <h2 style="font-size: 20px; font-weight: 600; color: #ffffff; margin: 0 0 14px 0; text-align: center;">
                            Reset Your Password
                          </h2>
                          <p style="font-size: 14px; line-height: 1.6; color: #b0a79a; margin: 0 0 24px 0; text-align: center;">
                            Ciao, we received a request to reset your password. Use the single-use verification code below to authorize this request:
                          </p>

                          <!-- OTP Block -->
                          <div style="background: #0f0e0c; border: 1px dashed #df9926; border-radius: 10px; padding: 22px 10px; text-align: center; margin: 0 0 24px 0;">
                            <div style="font-family: 'Courier New', Courier, monospace; font-size: 38px; font-weight: 800; letter-spacing: 12px; color: #ffb952; padding-left: 12px;">
                              {{SAFE_OTP}}
                            </div>
                          </div>

                          <!-- Timer notice -->
                          <p style="font-size: 13px; line-height: 1.5; color: #d4a359; margin: 0 0 20px 0; text-align: center; font-weight: 500;">
                            &#9203; This code is strictly valid for <strong>{{EXPIRATION_MINUTES}} minutes</strong>.
                          </p>
                          <p style="font-size: 12px; line-height: 1.5; color: #787063; margin: 0; text-align: center;">
                            If you did not initiate this request, you can safely disregard this email. Your Re Sapori account remains secure.
                          </p>
                        </td>
                      </tr>

                      <!-- Footer -->
                      <tr>
                        <td align="center" style="padding: 22px 30px; background-color: #12110f; border-top: 1px solid #231f18; font-size: 11px; color: #6b6459; line-height: 1.6;">
                          <div style="color: #9e9382; font-weight: 600; margin-bottom: 4px;">Re Sapori • Fine Dining</div>
                          <div>6th of October City, Giza, Egypt</div>
                          <div style="margin-top: 6px; font-size: 10px; color: #524d45;">&copy; 2026 Re Sapori. All rights reserved.</div>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """;
        return template
                .replace("{{SAFE_OTP}}", safeOtp)
                .replace("{{EXPIRATION_MINUTES}}", String.valueOf(expirationMinutes));
    }

    @Override
    public String buildOrderConfirmedEmail(OrderResponse order) {
        String orderNumber = formatOrderNumber(order != null ? order.getId() : null);
        String customerName = escapeHtml(order != null && order.getCustomerName() != null && !order.getCustomerName().isBlank()
                ? order.getCustomerName() : "Valued Guest");
        String formattedDate = (order != null && order.getCreatedAt() != null)
                ? order.getCreatedAt().format(DATE_TIME_FORMATTER)
                : "Just now";
        String orderTypeStr = (order != null && order.getOrderType() != null)
                ? order.getOrderType().name().replace("_", " ")
                : "DINE-IN";
        String destination = (order != null && order.getOrderType() == OrderType.DELIVERY)
                ? (order.getDeliveryAddress() != null ? escapeHtml(order.getDeliveryAddress()) : "Address on file")
                : (order != null && order.getBranchName() != null ? escapeHtml(order.getBranchName()) : "Re Sapori Hub");
        String branchName = (order != null && order.getBranchName() != null && !order.getBranchName().isBlank())
                ? escapeHtml(order.getBranchName())
                : "Re Sapori Kitchen Hub";
        String paymentMethodStr = (order != null && order.getPaymentMethod() != null)
                ? order.getPaymentMethod().name().replace("_", " ")
                : "Cash";
        String totalAmountStr = formatCurrency(order != null ? order.getTotalAmount() : BigDecimal.ZERO);
        String currency = escapeHtml(order != null && order.getCurrency() != null ? order.getCurrency() : "EGP");

        StringBuilder itemsRows = new StringBuilder();
        List<OrderItemResponse> items = order != null ? order.getItems() : null;
        BigDecimal subtotal = BigDecimal.ZERO;

        if (items != null && !items.isEmpty()) {
            for (OrderItemResponse item : items) {
                String itemName = escapeHtml(item.getNameEn() != null ? item.getNameEn() : "Dish");
                String itemSize = (item.getSize() != null && !item.getSize().name().equalsIgnoreCase("REGULAR"))
                        ? " (" + escapeHtml(item.getSize().name()) + ")" : "";

                BigDecimal lineTotalVal = item.getLineTotal();
                if (lineTotalVal == null) {
                    BigDecimal unitPrice = item.getUnitPriceAtPurchase() != null ? item.getUnitPriceAtPurchase() : BigDecimal.ZERO;
                    lineTotalVal = unitPrice.multiply(BigDecimal.valueOf(Math.max(1, item.getQuantity())));
                }
                subtotal = subtotal.add(lineTotalVal);

                String priceDisplay;
                if (item.isFree()) {
                    priceDisplay = "<span style=\"color: #81c784;\">FREE</span>";
                } else if (lineTotalVal.compareTo(BigDecimal.ZERO) == 0 && item.getOfferName() != null && !item.getOfferName().isBlank()) {
                    priceDisplay = "<span style=\"color: #df9926; font-size: 11px;\">Included in Offer</span>";
                } else {
                    priceDisplay = formatCurrency(lineTotalVal) + " " + currency;
                }

                StringBuilder itemDetails = new StringBuilder();
                if (item.isFree()) {
                    itemDetails.append("<br><span style=\"display: inline-block; margin-top: 3px; font-size: 11px; color: #81c784; font-weight: 600;\">&#10004; Promo Gift</span>");
                } else if (item.getOfferName() != null && !item.getOfferName().isBlank()) {
                    itemDetails.append("<br><span style=\"display: inline-block; margin-top: 3px; font-size: 11px; color: #df9926; font-weight: 600;\">&#127991; Offer: ").append(escapeHtml(item.getOfferName())).append("</span>");
                }
                if (item.getAddOns() != null && !item.getAddOns().isEmpty()) {
                    String addOnsStr = item.getAddOns().stream()
                            .map(a -> escapeHtml(a.getNameEn()) + (a.getPrice() != null && a.getPrice().compareTo(BigDecimal.ZERO) > 0 ? " (+" + formatCurrency(a.getPrice()) + ")" : ""))
                            .collect(Collectors.joining(", "));
                    itemDetails.append("<br><span style=\"display: inline-block; margin-top: 2px; font-size: 11px; color: #9e9382;\">+ ").append(addOnsStr).append("</span>");
                }
                if (item.getNotes() != null && !item.getNotes().isBlank()) {
                    itemDetails.append("<br><span style=\"display: inline-block; margin-top: 2px; font-size: 11px; color: #9e9382; font-style: italic;\">Note: ").append(escapeHtml(item.getNotes().trim())).append("</span>");
                }

                itemsRows.append("""
                    <tr>
                      <td style="padding: 12px 10px; border-bottom: 1px solid #28241d; color: #ffffff; font-size: 13px;">
                        <strong>""").append(itemName).append("</strong>").append(itemSize).append(itemDetails).append("""
                      </td>
                      <td align="center" style="padding: 12px 10px; border-bottom: 1px solid #28241d; color: #d4cdc5; font-size: 13px;">
                        x""").append(item.getQuantity()).append("""
                      </td>
                      <td align="right" style="padding: 12px 10px; border-bottom: 1px solid #28241d; color: #ffb952; font-size: 13px; font-weight: 600; white-space: nowrap;">
                        """).append(priceDisplay).append("""
                      </td>
                    </tr>
                    """);
            }

            // Items Subtotal row
            itemsRows.append("""
                <tr>
                  <td colspan="2" align="left" style="padding: 12px 10px; border-top: 1px solid #383025; border-bottom: 1px solid #28241d; color: #b5ac9f; font-size: 13px;">
                    Items Subtotal
                  </td>
                  <td align="right" style="padding: 12px 10px; border-top: 1px solid #383025; border-bottom: 1px solid #28241d; color: #d4cdc5; font-size: 13px; font-weight: 600; white-space: nowrap;">
                    """).append(formatCurrency(subtotal)).append(" ").append(currency).append("""
                  </td>
                </tr>
                """);
        } else {
            itemsRows.append("""
                <tr>
                  <td colspan="3" align="center" style="padding: 15px; color: #8a8275; font-size: 13px;">
                    Order items received
                  </td>
                </tr>
                """);
        }

        // Promo Code Discount row (Single Source of Truth)
        BigDecimal discount = null;
        if (order != null) {
            if (order.getDiscountAmount() != null && order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                discount = order.getDiscountAmount();
            } else if (order.getPromoDiscountAmount() != null && order.getPromoDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                discount = order.getPromoDiscountAmount();
            }
        }
        if (discount != null) {
            StringBuilder promoLabel = new StringBuilder("&#127991; Promo Code Discount");
            if (order.getPromoCode() != null && !order.getPromoCode().isBlank()) {
                promoLabel = new StringBuilder("&#127991; Promo (").append(escapeHtml(order.getPromoCode().trim()));
                if (order.getPromoDiscountPercentage() != null && order.getPromoDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
                    promoLabel.append(" - ").append(order.getPromoDiscountPercentage().stripTrailingZeros().toPlainString()).append("%");
                }
                promoLabel.append(")");
            }
            itemsRows.append("""
                <tr>
                  <td colspan="2" align="left" style="padding: 12px 10px; border-bottom: 1px solid #28241d; color: #81c784; font-size: 13px;">
                    """).append(promoLabel).append("""
                  </td>
                  <td align="right" style="padding: 12px 10px; border-bottom: 1px solid #28241d; color: #81c784; font-size: 13px; font-weight: 600; white-space: nowrap;">
                    -""").append(formatCurrency(discount)).append(" ").append(currency).append("""
                  </td>
                </tr>
                """);
        }

        // Delivery Fee row
        if (order != null && order.getOrderType() == OrderType.DELIVERY) {
            String feeFormatted = (order.getDeliveryFee() != null && order.getDeliveryFee().compareTo(BigDecimal.ZERO) > 0)
                    ? "+" + formatCurrency(order.getDeliveryFee()) + " " + currency
                    : "FREE";
            itemsRows.append("""
                <tr>
                  <td colspan="2" align="left" style="padding: 12px 10px; border-bottom: 1px solid #28241d; color: #b5ac9f; font-size: 13px;">
                    &#128757; <em>Delivery &amp; Service Fee</em>
                  </td>
                  <td align="right" style="padding: 12px 10px; border-bottom: 1px solid #28241d; color: #ffb952; font-size: 13px; font-weight: 600; white-space: nowrap;">
                    """).append(feeFormatted).append("""
                  </td>
                </tr>
                """);
        } else if (order != null && order.getDeliveryFee() != null && order.getDeliveryFee().compareTo(BigDecimal.ZERO) > 0) {
            itemsRows.append("""
                <tr>
                  <td colspan="2" align="left" style="padding: 12px 10px; border-bottom: 1px solid #28241d; color: #b5ac9f; font-size: 13px;">
                    &#128757; <em>Delivery &amp; Service Fee</em>
                  </td>
                  <td align="right" style="padding: 12px 10px; border-bottom: 1px solid #28241d; color: #ffb952; font-size: 13px; font-weight: 600; white-space: nowrap;">
                    +""").append(formatCurrency(order.getDeliveryFee())).append(" ").append(currency).append("""
                  </td>
                </tr>
                """);
        }

        String template = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>Order Confirmed - Re Sapori</title>
            </head>
            <body style="margin: 0; padding: 0; background-color: #0c0c0c; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; -webkit-font-smoothing: antialiased;">
              <table width="100%" border="0" cellspacing="0" cellpadding="0" style="background-color: #0c0c0c; padding: 40px 15px;">
                <tr>
                  <td align="center">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0" style="max-width: 600px; background-color: #171614; border: 1px solid #2e281e; border-radius: 14px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.6);">
                      <!-- Header -->
                      <tr>
                        <td align="center" style="padding: 35px 30px 20px 30px; border-bottom: 1px solid rgba(223, 153, 38, 0.2);">
                          <div style="font-size: 24px; font-weight: 800; letter-spacing: 3px; color: #df9926; text-transform: uppercase;">RE SAPORI</div>
                          <div style="font-size: 11px; letter-spacing: 2.5px; color: #9e9382; text-transform: uppercase; margin-top: 5px;">Fine Dining &amp; Artisanal Pizzeria</div>
                        </td>
                      </tr>

                      <!-- Banner / Hero -->
                      <tr>
                        <td style="padding: 30px 35px 20px 35px; color: #e6e2dd;">
                          <div style="text-align: center; margin-bottom: 18px;">
                            <span style="display: inline-block; background: rgba(76, 175, 80, 0.15); border: 1px solid #4caf50; color: #81c784; padding: 6px 18px; border-radius: 20px; font-size: 11px; font-weight: 700; letter-spacing: 1.5px; text-transform: uppercase;">
                              &#9679; Confirmed &amp; Being Prepared
                            </span>
                          </div>
                          
                          <h1 style="font-size: 22px; font-weight: 700; color: #ffffff; margin: 0 0 12px 0; text-align: center;">
                            Our Chefs Are Preparing Your Order!
                          </h1>
                          <p style="font-size: 14px; line-height: 1.6; color: #b5ac9f; margin: 0 0 25px 0; text-align: center;">
                            Ciao <strong>{{CUSTOMER_NAME}}</strong>, eccellente news! Your order <strong>{{ORDER_NUMBER}}</strong> has been accepted by our kitchen. Our team is now crafting your dishes with the finest ingredients.
                          </p>

                          <!-- Order Details Metadata Grid -->
                          <div style="background-color: #11100e; border: 1px solid #28241d; border-radius: 10px; padding: 18px 20px; margin-bottom: 25px;">
                            <table width="100%" border="0" cellspacing="0" cellpadding="4" style="font-size: 13px;">
                              <tr>
                                <td style="color: #8c8374; width: 40%;">Order Reference:</td>
                                <td style="color: #ffb952; font-weight: 700; font-family: monospace;">{{ORDER_NUMBER}}</td>
                              </tr>
                              <tr>
                                <td style="color: #8c8374;">Order Date:</td>
                                <td style="color: #e6e2dd;">{{FORMATTED_DATE}}</td>
                              </tr>
                              <tr>
                                <td style="color: #8c8374;">Service Mode:</td>
                                <td style="color: #e6e2dd; font-weight: 600;">{{ORDER_TYPE}}</td>
                              </tr>
                              <tr>
                                <td style="color: #8c8374;">Fulfilling Branch:</td>
                                <td style="color: #ffb952; font-weight: 600;">{{BRANCH_NAME}}</td>
                              </tr>
                              <tr>
                                <td style="color: #8c8374;">Destination:</td>
                                <td style="color: #e6e2dd;">{{DESTINATION}}</td>
                              </tr>
                              <tr>
                                <td style="color: #8c8374;">Payment:</td>
                                <td style="color: #e6e2dd;">{{PAYMENT_METHOD}}</td>
                              </tr>
                            </table>
                          </div>

                          <!-- Items Table -->
                          <div style="margin-bottom: 25px;">
                            <div style="font-size: 13px; font-weight: 700; text-transform: uppercase; letter-spacing: 1px; color: #df9926; margin-bottom: 10px;">
                              Order Summary
                            </div>
                            <table width="100%" border="0" cellspacing="0" cellpadding="0" style="border-collapse: collapse; background-color: #11100e; border: 1px solid #28241d; border-radius: 8px;">
                              <thead>
                                <tr style="background-color: #1b1915; border-bottom: 1px solid #2e281e;">
                                  <th align="left" style="padding: 10px; font-size: 11px; text-transform: uppercase; letter-spacing: 1px; color: #9e9382;">Dish</th>
                                  <th align="center" style="padding: 10px; font-size: 11px; text-transform: uppercase; letter-spacing: 1px; color: #9e9382;">Qty</th>
                                  <th align="right" style="padding: 10px; font-size: 11px; text-transform: uppercase; letter-spacing: 1px; color: #9e9382;">Total</th>
                                </tr>
                              </thead>
                              <tbody>
                                {{ITEMS_ROWS}}
                              </tbody>
                            </table>
                          </div>

                          <!-- Total Amount Highlight -->
                          <div style="background: linear-gradient(135deg, #241e15, #191611); border: 1px solid #df9926; border-radius: 10px; padding: 18px 22px; margin-bottom: 25px;">
                            <table width="100%" border="0" cellspacing="0" cellpadding="0">
                              <tr>
                                <td style="color: #e6e2dd; font-size: 15px; font-weight: 600;">Total Amount Due</td>
                                <td align="right" style="color: #ffb952; font-size: 24px; font-weight: 800; font-family: -apple-system, sans-serif;">
                                  {{TOTAL_AMOUNT}} <span style="font-size: 14px; font-weight: normal; color: #df9926;">{{CURRENCY}}</span>
                                </td>
                              </tr>
                            </table>
                          </div>

                          <p style="font-size: 13px; line-height: 1.6; color: #9e9382; text-align: center; margin: 0;">
                            We will notify you as soon as your culinary selection is ready. If you have any inquiries, feel free to reply directly to this email.<br><br>
                            <span style="color: #df9926; font-size: 15px; font-weight: 600;">Buon Appetito! &#127829;&#127837;</span>
                          </p>
                        </td>
                      </tr>

                      <!-- Footer -->
                      <tr>
                        <td align="center" style="padding: 22px 30px; background-color: #12110f; border-top: 1px solid #231f18; font-size: 11px; color: #6b6459; line-height: 1.6;">
                          <div style="color: #9e9382; font-weight: 600; margin-bottom: 4px;">Re Sapori • Fine Dining</div>
                          <div>6th of October City, Giza, Egypt</div>
                          <div style="margin-top: 6px; font-size: 10px; color: #524d45;">&copy; 2026 Re Sapori. All rights reserved.</div>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """;

        return template
                .replace("{{CUSTOMER_NAME}}", customerName)
                .replace("{{ORDER_NUMBER}}", orderNumber)
                .replace("{{FORMATTED_DATE}}", formattedDate)
                .replace("{{ORDER_TYPE}}", orderTypeStr)
                .replace("{{BRANCH_NAME}}", branchName)
                .replace("{{DESTINATION}}", destination)
                .replace("{{PAYMENT_METHOD}}", paymentMethodStr)
                .replace("{{ITEMS_ROWS}}", itemsRows.toString())
                .replace("{{TOTAL_AMOUNT}}", totalAmountStr)
                .replace("{{CURRENCY}}", currency);
    }

    @Override
    public String buildOrderCancelledEmail(OrderResponse order) {
        String orderNumber = formatOrderNumber(order != null ? order.getId() : null);
        String customerName = escapeHtml(order != null && order.getCustomerName() != null && !order.getCustomerName().isBlank()
                ? order.getCustomerName() : "Valued Guest");
        String totalAmountStr = formatCurrency(order != null ? order.getTotalAmount() : BigDecimal.ZERO);
        String currency = escapeHtml(order != null && order.getCurrency() != null ? order.getCurrency() : "EGP");

        String template = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>Order Cancelled - Re Sapori</title>
            </head>
            <body style="margin: 0; padding: 0; background-color: #0c0c0c; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; -webkit-font-smoothing: antialiased;">
              <table width="100%" border="0" cellspacing="0" cellpadding="0" style="background-color: #0c0c0c; padding: 40px 15px;">
                <tr>
                  <td align="center">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0" style="max-width: 560px; background-color: #171614; border: 1px solid #2e281e; border-radius: 14px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.6);">
                      <!-- Header -->
                      <tr>
                        <td align="center" style="padding: 35px 30px 20px 30px; border-bottom: 1px solid rgba(223, 153, 38, 0.2);">
                          <div style="font-size: 24px; font-weight: 800; letter-spacing: 3px; color: #df9926; text-transform: uppercase;">RE SAPORI</div>
                          <div style="font-size: 11px; letter-spacing: 2.5px; color: #9e9382; text-transform: uppercase; margin-top: 5px;">Fine Dining &amp; Artisanal Pizzeria</div>
                        </td>
                      </tr>

                      <!-- Cancellation Content -->
                      <tr>
                        <td style="padding: 35px 35px 25px 35px; color: #e6e2dd;">
                          <div style="text-align: center; margin-bottom: 20px;">
                            <span style="display: inline-block; background: rgba(220, 53, 69, 0.15); border: 1px solid #e53935; color: #ef5350; padding: 6px 18px; border-radius: 20px; font-size: 11px; font-weight: 700; letter-spacing: 1.5px; text-transform: uppercase;">
                              &#10005; Order Cancelled
                            </span>
                          </div>

                          <h2 style="font-size: 20px; font-weight: 600; color: #ffffff; margin: 0 0 14px 0; text-align: center;">
                            Order Cancellation Notice
                          </h2>
                          <p style="font-size: 14px; line-height: 1.6; color: #b5ac9f; margin: 0 0 20px 0; text-align: center;">
                            Ciao <strong>{{CUSTOMER_NAME}}</strong>, we regret to inform you that order <strong>{{ORDER_NUMBER}}</strong> has been cancelled.
                          </p>

                          <!-- Order Cancellation Summary Box -->
                          <div style="background-color: #12100e; border: 1px solid #2f2520; border-radius: 10px; padding: 18px 20px; margin-bottom: 22px;">
                            <table width="100%" border="0" cellspacing="0" cellpadding="4" style="font-size: 13px;">
                              <tr>
                                <td style="color: #8c8374; width: 45%;">Order Reference:</td>
                                <td style="color: #ffb952; font-weight: 700; font-family: monospace;">{{ORDER_NUMBER}}</td>
                              </tr>
                              <tr>
                                <td style="color: #8c8374;">Cancelled Amount:</td>
                                <td style="color: #ffffff; font-weight: 600;">{{TOTAL_AMOUNT}} {{CURRENCY}}</td>
                              </tr>
                              <tr>
                                <td style="color: #8c8374;">Status:</td>
                                <td style="color: #ef5350; font-weight: 600;">CANCELLED</td>
                              </tr>
                            </table>
                          </div>

                          <p style="font-size: 13px; line-height: 1.6; color: #9e9382; margin: 0 0 16px 0;">
                            If you requested this cancellation or if our team was unable to fulfill it at this moment, please accept our sincere apologies for any inconvenience.
                          </p>
                          <p style="font-size: 13px; line-height: 1.6; color: #9e9382; margin: 0 0 20px 0;">
                            If payment was already completed online via Card or Paymob, a full refund has been initiated and will reflect according to your bank&#39;s standard processing timeframe.
                          </p>
                          <p style="font-size: 12px; line-height: 1.5; color: #787063; margin: 0; text-align: center;">
                            Need help with your order? Reply directly to this email or contact our customer support.
                          </p>
                        </td>
                      </tr>

                      <!-- Footer -->
                      <tr>
                        <td align="center" style="padding: 22px 30px; background-color: #12110f; border-top: 1px solid #231f18; font-size: 11px; color: #6b6459; line-height: 1.6;">
                          <div style="color: #9e9382; font-weight: 600; margin-bottom: 4px;">Re Sapori • Fine Dining</div>
                          <div>6th of October City, Giza, Egypt</div>
                          <div style="margin-top: 6px; font-size: 10px; color: #524d45;">&copy; 2026 Re Sapori. All rights reserved.</div>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """;

        return template
                .replace("{{CUSTOMER_NAME}}", customerName)
                .replace("{{ORDER_NUMBER}}", orderNumber)
                .replace("{{TOTAL_AMOUNT}}", totalAmountStr)
                .replace("{{CURRENCY}}", currency);
    }
}
