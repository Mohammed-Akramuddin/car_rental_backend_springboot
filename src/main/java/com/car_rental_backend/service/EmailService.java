package com.car_rental_backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.car_rental_backend.exception.ActivationEmailException;

import java.io.UnsupportedEncodingException;

/**
 * Service for sending emails (activation, etc.) via Brevo SMTP.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private static final String FROM_NAME = "Luxury Car Rental";

    private final JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;

    @Value("${app.mail.from:mohdakram6174@gmail.com}")
    private String fromEmail;

    private static final String ACTIVATION_SUBJECT = "Activate your Car Rental account";
    private static final int TOKEN_VALID_HOURS = 24;

    /**
     * Send activation email with link. Link expires in 24 hours.
     */
    public void sendActivationEmail(String toEmail, String firstName, String activationToken) {
        String activationLink = baseUrl + "/api/v1/auth/activate?token=" + activationToken;
        String htmlBody = buildActivationEmailHtml(firstName, activationLink);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            InternetAddress from = new InternetAddress(fromEmail, FROM_NAME, "UTF-8");
            helper.setFrom(from);
            helper.setTo(toEmail);
            helper.setSubject(ACTIVATION_SUBJECT);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Activation email sent from {} to {}", fromEmail, toEmail);
        } catch (UnsupportedEncodingException e) {
            log.error("Invalid from address encoding: {}", e.getMessage());
            throw new ActivationEmailException("Invalid sender configuration for activation email", e);
        } catch (MessagingException e) {
            log.error("Failed to build/send activation email to {}: {}", toEmail, e.getMessage(), e);
            throw new ActivationEmailException("Could not send activation email", e);
        } catch (MailException e) {
            log.error("SMTP error sending activation email to {}: {} - {}", toEmail, e.getMessage(),
                    e.getCause() != null ? e.getCause().getMessage() : "no cause", e);
            throw new ActivationEmailException(
                    "Could not send activation email. Check SMTP credentials and Brevo sender verification (Senders & Domains).", e);
        }
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
            """.formatted(
                firstName != null && !firstName.isBlank() ? firstName : "there",
                TOKEN_VALID_HOURS,
                activationLink,
                activationLink
            );
    }

    public static int getActivationTokenValidHours() {
        return TOKEN_VALID_HOURS;
    }
}
