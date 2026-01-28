package com.car_rental_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.car_rental_backend.exception.ActivationEmailException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for sending emails (activation, etc.) via Brevo REST API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private static final String FROM_NAME = "Luxury Car Rental";

    private final RestTemplate restTemplate;

    @Value("${app.base-url:https://car-rental-backend-springboot.onrender.com}")
    private String baseUrl;

    @Value("${app.mail.from:mohdakram6174@gmail.com}")
    private String fromEmail;

    @Value("${spring.mail.brevo.api-key}")
    private String brevoApiKey;

    @Value("${spring.mail.brevo.api-url:https://api.brevo.com/v3/smtp/email}")
    private String brevoApiUrl;

    private static final String ACTIVATION_SUBJECT = "Activate your Car Rental account";
    private static final int TOKEN_VALID_HOURS = 24;

    /**
     * Send email via Brevo REST API.
     */
    private void sendEmailViaApi(String toEmail, String subject, String htmlContent) {
        log.info("Sending email via Brevo API to: {}", toEmail);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey);

        Map<String, Object> body = new HashMap<>();

        Map<String, String> sender = new HashMap<>();
        sender.put("name", FROM_NAME);
        sender.put("email", fromEmail);
        body.put("sender", sender);

        List<Map<String, String>> to = new ArrayList<>();
        Map<String, String> recipient = new HashMap<>();
        recipient.put("email", toEmail);
        to.add(recipient);
        body.put("to", to);

        body.put("subject", subject);
        body.put("htmlContent", htmlContent);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(brevoApiUrl, request, String.class);
            log.info("Email sent successfully via Brevo API to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email via Brevo API to {}: {}", toEmail, e.getMessage(), e);
            throw new ActivationEmailException("Could not send email via Brevo API", e);
        }
    }

    /**
     * Send activation email with link. Link expires in 24 hours.
     */
    public void sendActivationEmail(String toEmail, String firstName, String activationToken) {
        String activationLink = baseUrl + "/api/v1/auth/activate?token=" + activationToken;
        String htmlBody = buildActivationEmailHtml(firstName, activationLink);
        sendEmailViaApi(toEmail, ACTIVATION_SUBJECT, htmlBody);
    }

    private String buildActivationEmailHtml(String firstName, String activationLink) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f5;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color: #f4f4f5; padding: 40px 20px;">
                        <tr>
                            <td align="center">
                                <table role="presentation" width="600" cellspacing="0" cellpadding="0" style="background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.08); overflow: hidden;">
                                     <tr>
                                        <td style="background: linear-gradient(135deg, #1e3a5f 0%%, #2d5a87 100%%); padding: 32px 40px; text-align: center;">
                                            <h1 style="margin: 0; color: #ffffff; font-size: 24px; font-weight: 600;">Luxury Car Rental</h1>
                                            <p style="margin: 8px 0 0 0; color: rgba(255,255,255,0.9); font-size: 14px;">Activate your account</p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 40px;">
                                            <p style="margin: 0 0 16px 0; font-size: 16px; color: #1f2937; line-height: 1.5;">Hi %s,</p>
                                            <p style="margin: 0 0 24px 0; font-size: 16px; color: #4b5563; line-height: 1.6;">Thanks for registering. Please click the button below to activate your account. This link is valid for %d hours.</p>
                                            <table role="presentation" cellspacing="0" cellpadding="0" style="margin: 0 auto;">
                                                <tr>
                                                    <td style="border-radius: 8px; background: linear-gradient(135deg, #1e3a5f 0%%, #2d5a87 100%%);">
                                                        <a href="%s" target="_blank" style="display: inline-block; padding: 14px 28px; font-size: 16px; font-weight: 600; color: #ffffff; text-decoration: none;">Activate account</a>
                                                    </td>
                                                </tr>
                                            </table>
                                            <p style="margin: 24px 0 0 0; font-size: 13px; color: #6b7280;">If the button doesn't work, copy and paste this link into your browser:</p>
                                            <p style="margin: 8px 0 0 0; font-size: 13px; color: #3b82f6; word-break: break-all;">%s</p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 24px 40px; background-color: #f9fafb; border-top: 1px solid #e5e7eb;">
                                            <p style="margin: 0; font-size: 12px; color: #9ca3af;">If you didn't create an account, you can safely ignore this email.</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """
                .formatted(
                        firstName != null && !firstName.isBlank() ? firstName : "there",
                        TOKEN_VALID_HOURS,
                        activationLink,
                        activationLink);
    }

    public static int getActivationTokenValidHours() {
        return TOKEN_VALID_HOURS;
    }

    /**
     * Send booking confirmation email
     */
    public void sendBookingConfirmationEmail(String toEmail, String firstName, String bookingReference,
            String carName, String startDate, String endDate,
            String pickupLocation, String totalPrice) {
        String subject = "Booking Confirmation - " + bookingReference;
        String htmlBody = buildBookingConfirmationEmailHtml(firstName, bookingReference, carName,
                startDate, endDate, pickupLocation, totalPrice);
        sendEmailViaApi(toEmail, subject, htmlBody);
    }

    /**
     * Send booking cancellation email
     */
    public void sendBookingCancellationEmail(String toEmail, String firstName, String bookingReference,
            String carName, String startDate, String endDate,
            String cancellationReason) {
        String subject = "Booking Cancelled - " + bookingReference;
        String htmlBody = buildBookingCancellationEmailHtml(firstName, bookingReference, carName,
                startDate, endDate, cancellationReason);
        sendEmailViaApi(toEmail, subject, htmlBody);
    }

    private String buildBookingConfirmationEmailHtml(String firstName, String bookingReference, String carName,
            String startDate, String endDate, String pickupLocation,
            String totalPrice) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f5;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color: #f4f4f5; padding: 40px 20px;">
                        <tr>
                            <td align="center">
                                <table role="presentation" width="600" cellspacing="0" cellpadding="0" style="background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.08); overflow: hidden;">
                                    <tr>
                                        <td style="background: linear-gradient(135deg, #1e3a5f 0%%, #2d5a87 100%%); padding: 32px 40px; text-align: center;">
                                            <h1 style="margin: 0; color: #ffffff; font-size: 24px; font-weight: 600;">Luxury Car Rental</h1>
                                            <p style="margin: 8px 0 0 0; color: rgba(255,255,255,0.9); font-size: 14px;">Booking Confirmation</p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 40px;">
                                            <p style="margin: 0 0 16px 0; font-size: 16px; color: #1f2937; line-height: 1.5;">Hi %s,</p>
                                            <p style="margin: 0 0 24px 0; font-size: 16px; color: #4b5563; line-height: 1.6;">Your booking has been confirmed! Here are your booking details:</p>

                                            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="margin: 24px 0; border: 1px solid #e5e7eb; border-radius: 8px; overflow: hidden;">
                                                <tr>
                                                    <td style="padding: 12px 16px; background-color: #f9fafb; border-bottom: 1px solid #e5e7eb; font-weight: 600; color: #374151;">Booking Reference</td>
                                                    <td style="padding: 12px 16px; background-color: #ffffff; border-bottom: 1px solid #e5e7eb; color: #1f2937;">%s</td>
                                                </tr>
                                                <tr>
                                                    <td style="padding: 12px 16px; background-color: #f9fafb; border-bottom: 1px solid #e5e7eb; font-weight: 600; color: #374151;">Car</td>
                                                    <td style="padding: 12px 16px; background-color: #ffffff; border-bottom: 1px solid #e5e7eb; color: #1f2937;">%s</td>
                                                </tr>
                                                <tr>
                                                    <td style="padding: 12px 16px; background-color: #f9fafb; border-bottom: 1px solid #e5e7eb; font-weight: 600; color: #374151;">Pickup Date</td>
                                                    <td style="padding: 12px 16px; background-color: #ffffff; border-bottom: 1px solid #e5e7eb; color: #1f2937;">%s</td>
                                                </tr>
                                                <tr>
                                                    <td style="padding: 12px 16px; background-color: #f9fafb; border-bottom: 1px solid #e5e7eb; font-weight: 600; color: #374151;">Return Date</td>
                                                    <td style="padding: 12px 16px; background-color: #ffffff; border-bottom: 1px solid #e5e7eb; color: #1f2937;">%s</td>
                                                </tr>
                                                <tr>
                                                    <td style="padding: 12px 16px; background-color: #f9fafb; border-bottom: 1px solid #e5e7eb; font-weight: 600; color: #374151;">Pickup Location</td>
                                                    <td style="padding: 12px 16px; background-color: #ffffff; border-bottom: 1px solid #e5e7eb; color: #1f2937;">%s</td>
                                                </tr>
                                                <tr>
                                                    <td style="padding: 12px 16px; background-color: #f9fafb; font-weight: 600; color: #374151;">Total Price</td>
                                                    <td style="padding: 12px 16px; background-color: #ffffff; color: #059669; font-weight: 600; font-size: 18px;">₹%s</td>
                                                </tr>
                                            </table>

                                            <p style="margin: 24px 0 0 0; font-size: 14px; color: #6b7280; line-height: 1.6;">Please keep this booking reference for your records. If you have any questions, feel free to contact us.</p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 24px 40px; background-color: #f9fafb; border-top: 1px solid #e5e7eb;">
                                            <p style="margin: 0; font-size: 12px; color: #9ca3af;">Thank you for choosing Luxury Car Rental!</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """
                .formatted(
                        firstName != null && !firstName.isBlank() ? firstName : "there",
                        bookingReference,
                        carName,
                        startDate,
                        endDate,
                        pickupLocation,
                        totalPrice);
    }

    private String buildBookingCancellationEmailHtml(String firstName, String bookingReference, String carName,
            String startDate, String endDate, String cancellationReason) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f5;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color: #f4f4f5; padding: 40px 20px;">
                        <tr>
                            <td align="center">
                                <table role="presentation" width="600" cellspacing="0" cellpadding="0" style="background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.08); overflow: hidden;">
                                    <tr>
                                        <td style="background: linear-gradient(135deg, #991b1b 0%%, #dc2626 100%%); padding: 32px 40px; text-align: center;">
                                            <h1 style="margin: 0; color: #ffffff; font-size: 24px; font-weight: 600;">Luxury Car Rental</h1>
                                            <p style="margin: 8px 0 0 0; color: rgba(255,255,255,0.9); font-size: 14px;">Booking Cancellation</p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 40px;">
                                            <p style="margin: 0 0 16px 0; font-size: 16px; color: #1f2937; line-height: 1.5;">Hi %s,</p>
                                            <p style="margin: 0 0 24px 0; font-size: 16px; color: #4b5563; line-height: 1.6;">Your booking has been cancelled. Here are the details:</p>

                                            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="margin: 24px 0; border: 1px solid #e5e7eb; border-radius: 8px; overflow: hidden;">
                                                <tr>
                                                    <td style="padding: 12px 16px; background-color: #f9fafb; border-bottom: 1px solid #e5e7eb; font-weight: 600; color: #374151;">Booking Reference</td>
                                                    <td style="padding: 12px 16px; background-color: #ffffff; border-bottom: 1px solid #e5e7eb; color: #1f2937;">%s</td>
                                                </tr>
                                                <tr>
                                                    <td style="padding: 12px 16px; background-color: #f9fafb; border-bottom: 1px solid #e5e7eb; font-weight: 600; color: #374151;">Car</td>
                                                    <td style="padding: 12px 16px; background-color: #ffffff; border-bottom: 1px solid #e5e7eb; color: #1f2937;">%s</td>
                                                </tr>
                                                <tr>
                                                    <td style="padding: 12px 16px; background-color: #f9fafb; border-bottom: 1px solid #e5e7eb; font-weight: 600; color: #374151;">Pickup Date</td>
                                                    <td style="padding: 12px 16px; background-color: #ffffff; border-bottom: 1px solid #e5e7eb; color: #1f2937;">%s</td>
                                                </tr>
                                                <tr>
                                                    <td style="padding: 12px 16px; background-color: #f9fafb; border-bottom: 1px solid #e5e7eb; font-weight: 600; color: #374151;">Return Date</td>
                                                    <td style="padding: 12px 16px; background-color: #ffffff; border-bottom: 1px solid #e5e7eb; color: #1f2937;">%s</td>
                                                </tr>
                                                <tr>
                                                    <td style="padding: 12px 16px; background-color: #f9fafb; font-weight: 600; color: #374151;">Cancellation Reason</td>
                                                    <td style="padding: 12px 16px; background-color: #ffffff; color: #dc2626;">%s</td>
                                                </tr>
                                            </table>

                                            <p style="margin: 24px 0 0 0; font-size: 14px; color: #6b7280; line-height: 1.6;">If you have any questions about this cancellation, please contact our support team.</p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 24px 40px; background-color: #f9fafb; border-top: 1px solid #e5e7eb;">
                                            <p style="margin: 0; font-size: 12px; color: #9ca3af;">We hope to serve you again soon!</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """
                .formatted(
                        firstName != null && !firstName.isBlank() ? firstName : "there",
                        bookingReference,
                        carName,
                        startDate,
                        endDate,
                        cancellationReason != null && !cancellationReason.isBlank() ? cancellationReason
                                : "No reason provided");
    }
}
