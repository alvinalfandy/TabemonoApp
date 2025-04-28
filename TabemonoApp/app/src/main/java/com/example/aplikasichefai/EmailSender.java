package com.example.aplikasichefai;

import android.os.AsyncTask;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {

    private static final String EMAIL_HOST = "mail.tabemono.my.id";
    private static final String EMAIL_USERNAME = "noreply@tabemono.my.id";
    private static final String EMAIL_PASSWORD = "Tabemono123@";
    private static final int EMAIL_PORT = 465;

    public static void sendVerificationEmail(String recipientEmail, String name, String verificationLink) {
        new SendEmailTask(recipientEmail, name, verificationLink, EmailType.VERIFICATION).execute();
    }

    public static void sendPasswordResetEmail(String recipientEmail, String name, String resetLink) {
        new SendEmailTask(recipientEmail, name, resetLink, EmailType.PASSWORD_RESET).execute();
    }

    private enum EmailType {
        VERIFICATION,
        PASSWORD_RESET
    }

    private static class SendEmailTask extends AsyncTask<Void, Void, Boolean> {
        private final String recipientEmail;
        private final String name;
        private final String link;
        private final EmailType emailType;

        public SendEmailTask(String recipientEmail, String name, String link, EmailType emailType) {
            this.recipientEmail = recipientEmail;
            this.name = name;
            this.link = link;
            this.emailType = emailType;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                Properties props = new Properties();
                props.put("mail.smtp.host", EMAIL_HOST);
                props.put("mail.smtp.socketFactory.port", EMAIL_PORT);
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.port", EMAIL_PORT);

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
                    }
                });

                Message message = new MimeMessage(session);
                try {
                    message.setFrom(new InternetAddress(EMAIL_USERNAME, "TabemonoApp"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    message.setFrom(new InternetAddress(EMAIL_USERNAME)); // fallback
                }

                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));

                if (emailType == EmailType.VERIFICATION) {
                    message.setSubject("Email Verification - TabemonoApp");
                    message.setContent(createVerificationEmailContent(name, link), "text/html; charset=utf-8");
                } else if (emailType == EmailType.PASSWORD_RESET) {
                    message.setSubject("Password Reset - TabemonoApp");
                    message.setContent(createPasswordResetEmailContent(name, link), "text/html; charset=utf-8");
                }

                Transport.send(message);
                return true;
            } catch (MessagingException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private static String createVerificationEmailContent(String name, String verificationLink) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Email Verification</title>\n" +
                "</head>\n" +
                "<body style=\"margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;\">\n" +
                "    <table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "        <tr>\n" +
                "            <td style=\"padding: 20px 0;\">\n" +
                "                <table align=\"center\" width=\"600\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);\">\n" +
                "                    <tr>\n" +
                "                        <td align=\"center\" style=\"padding: 30px 0; background-color: #FF6B35; border-radius: 8px 8px 0 0;\">\n" +
                "                            <h1 style=\"color: #ffffff; margin: 0;\">Tabemonoapp</h1>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <tr>\n" +
                "                        <td style=\"padding: 40px 30px;\">\n" +
                "                            <h2 style=\"color: #333333; margin-top: 0;\">Verify Your Email Address</h2>\n" +
                "                            <p style=\"color: #666666; font-size: 16px; line-height: 1.5;\">Hello " + name + ",</p>\n" +
                "                            <p style=\"color: #666666; font-size: 16px; line-height: 1.5;\">Thank you for registering with Tabemonoapp! To complete your registration and start enjoying our services, please verify your email address by clicking the button below:</p>\n" +
                "                            <div style=\"text-align: center; margin: 30px 0;\">\n" +
                "                                <a href=\"" + verificationLink + "\" style=\"background-color: #FF6B35; color: #ffffff; padding: 12px 30px; text-decoration: none; border-radius: 4px; font-weight: bold; display: inline-block;\">Verify Email</a>\n" +
                "                            </div>\n" +
                "                            <p style=\"color: #666666; font-size: 16px; line-height: 1.5;\">If the button doesn't work, you can also click on the link below or copy and paste it into your browser:</p>\n" +
                "                            <p style=\"color: #0066cc; font-size: 14px; line-height: 1.5; word-break: break-all;\">" + verificationLink + "</p>\n" +
                "                            <p style=\"color: #666666; font-size: 16px; line-height: 1.5;\">If you did not create an account, you can safely ignore this email.</p>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <tr>\n" +
                "                        <td style=\"background-color: #f8f8f8; padding: 20px 30px; border-radius: 0 0 8px 8px; font-size: 14px; color: #888888; text-align: center;\">\n" +
                "                            <p style=\"margin: 0 0 10px 0;\">© 2025 Tabemonoapp. All rights reserved.</p>\n" +
                "                            <p style=\"margin: 0;\">Powered by <a href=\"https://tabemono.my.id\" style=\"color: #FF6B35; text-decoration: none;\">tabemono.my.id</a></p>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                </table>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "</body>\n" +
                "</html>";
    }

    private static String createPasswordResetEmailContent(String name, String resetLink) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Password Reset</title>\n" +
                "</head>\n" +
                "<body style=\"margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;\">\n" +
                "    <table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "        <tr>\n" +
                "            <td style=\"padding: 20px 0;\">\n" +
                "                <table align=\"center\" width=\"600\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);\">\n" +
                "                    <tr>\n" +
                "                        <td align=\"center\" style=\"padding: 30px 0; background-color: #FF6B35; border-radius: 8px 8px 0 0;\">\n" +
                "                            <h1 style=\"color: #ffffff; margin: 0;\">Tabemonoapp</h1>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <tr>\n" +
                "                        <td style=\"padding: 40px 30px;\">\n" +
                "                            <h2 style=\"color: #333333; margin-top: 0;\">Reset Your Password</h2>\n" +
                "                            <p style=\"color: #666666; font-size: 16px; line-height: 1.5;\">Hello " + name + ",</p>\n" +
                "                            <p style=\"color: #666666; font-size: 16px; line-height: 1.5;\">We received a request to reset your password for your Tabemonoapp account. Click the button below to create a new password:</p>\n" +
                "                            <div style=\"text-align: center; margin: 30px 0;\">\n" +
                "                                <a href=\"" + resetLink + "\" style=\"background-color: #FF6B35; color: #ffffff; padding: 12px 30px; text-decoration: none; border-radius: 4px; font-weight: bold; display: inline-block;\">Reset Password</a>\n" +
                "                            </div>\n" +
                "                            <p style=\"color: #666666; font-size: 16px; line-height: 1.5;\">If the button doesn't work, you can also click on the link below or copy and paste it into your browser:</p>\n" +
                "                            <p style=\"color: #0066cc; font-size: 14px; line-height: 1.5; word-break: break-all;\">" + resetLink + "</p>\n" +
                "                            <p style=\"color: #666666; font-size: 16px; line-height: 1.5;\">This link will expire in 24 hours. If you did not request a password reset, you can safely ignore this email.</p>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <tr>\n" +
                "                        <td style=\"background-color: #f8f8f8; padding: 20px 30px; border-radius: 0 0 8px 8px; font-size: 14px; color: #888888; text-align: center;\">\n" +
                "                            <p style=\"margin: 0 0 10px 0;\">© 2025 Tabemonoapp. All rights reserved.</p>\n" +
                "                            <p style=\"margin: 0;\">Powered by <a href=\"https://tabemono.my.id\" style=\"color: #FF6B35; text-decoration: none;\">tabemono.my.id</a></p>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                </table>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "</body>\n" +
                "</html>";
    }
}