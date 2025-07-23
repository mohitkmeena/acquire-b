package com.acquireindia.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender emailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Async
    public void sendWelcomeEmail(String toEmail, String userName) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to AcquireIndia!");
            
            String htmlContent = getWelcomeEmailTemplate(userName);
            helper.setText(htmlContent, true);
            
            emailSender.send(message);
        } catch (MessagingException e) {
            // Log error
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
    }
    
    @Async
    public void sendOfferNotification(String toEmail, String sellerName, String listingTitle, String buyerName) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("New Offer Received - " + listingTitle);
            
            String htmlContent = getOfferNotificationTemplate(sellerName, listingTitle, buyerName);
            helper.setText(htmlContent, true);
            
            emailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Failed to send offer notification email: " + e.getMessage());
        }
    }
    
    @Async
    public void sendKycStatusEmail(String toEmail, String userName, String status) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("KYC Status Update - AcquireIndia");
            
            String htmlContent = getKycStatusTemplate(userName, status);
            helper.setText(htmlContent, true);
            
            emailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Failed to send KYC status email: " + e.getMessage());
        }
    }
    
    @Async
    public void sendPaymentSuccessEmail(String toEmail, String userName, String transactionId) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Payment Successful - AcquireIndia");
            
            String htmlContent = getPaymentSuccessTemplate(userName, transactionId);
            helper.setText(htmlContent, true);
            
            emailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Failed to send payment success email: " + e.getMessage());
        }
    }
    
    private String getWelcomeEmailTemplate(String userName) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #2563EB; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; }
                    .btn { background: #2563EB; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Welcome to AcquireIndia!</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>Welcome to AcquireIndia - India's premier marketplace for buying and selling digital businesses!</p>
                        <p>Your account has been successfully created. You can now:</p>
                        <ul>
                            <li>Browse business listings</li>
                            <li>Make offers on interesting opportunities</li>
                            <li>List your own business for sale</li>
                            <li>Connect with verified buyers and sellers</li>
                        </ul>
                        <p>Get started by completing your KYC verification for full platform access.</p>
                        <a href="#" class="btn">Complete KYC Verification</a>
                        <p>Thank you for choosing AcquireIndia!</p>
                    </div>
                </div>
            </body>
            </html>
            """, userName);
    }
    
    private String getOfferNotificationTemplate(String sellerName, String listingTitle, String buyerName) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #059669; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; }
                    .btn { background: #059669; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>New Offer Received!</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>Great news! You've received a new offer on your listing:</p>
                        <h3>%s</h3>
                        <p>Offer from: <strong>%s</strong></p>
                        <p>Log in to your dashboard to review the offer details and respond.</p>
                        <a href="#" class="btn">View Offer</a>
                        <p>Best regards,<br>AcquireIndia Team</p>
                    </div>
                </div>
            </body>
            </html>
            """, sellerName, listingTitle, buyerName);
    }
    
    private String getKycStatusTemplate(String userName, String status) {
        String color = status.equals("APPROVED") ? "#059669" : "#DC2626";
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: %s; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>KYC Status Update</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>Your KYC verification status has been updated to: <strong>%s</strong></p>
                        %s
                        <p>Best regards,<br>AcquireIndia Team</p>
                    </div>
                </div>
            </body>
            </html>
            """, color, userName, status,
            status.equals("APPROVED") ? 
                "<p>Congratulations! Your account is now fully verified and you have access to all platform features.</p>" :
                "<p>Please review the feedback and resubmit your documents if needed.</p>");
    }
    
    private String getPaymentSuccessTemplate(String userName, String transactionId) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #059669; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Payment Successful!</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>Your payment has been successfully processed!</p>
                        <p>Transaction ID: <strong>%s</strong></p>
                        <p>You can now access the seller's contact information and proceed with your acquisition.</p>
                        <p>Thank you for using AcquireIndia!</p>
                    </div>
                </div>
            </body>
            </html>
            """, userName, transactionId);
    }
}