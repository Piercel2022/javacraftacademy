package com.javacraftacademy.userservice.service;

import com.javacraftacademy.userservice.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.enabled:true}")
    private boolean emailEnabled;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * Envoie un email de bienvenue à l'utilisateur
     */
    public void sendWelcomeEmail(User user) {
        if (!emailEnabled) {
            log.info("Email sending is disabled");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                log.info("Sending welcome email to: {}", user.getEmail());
                
                Context context = new Context();
                context.setVariable("userName", user.getUsername());
                context.setVariable("userEmail", user.getEmail());
                context.setVariable("frontendUrl", frontendUrl);
                context.setVariable("year", java.time.Year.now().getValue());

                String htmlContent = templateEngine.process("email/welcome", context);

                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(fromEmail);
                helper.setTo(user.getEmail());
                helper.setSubject("Bienvenue sur JavaCraft Academy !");
                helper.setText(htmlContent, true);

                mailSender.send(message);
                log.info("Welcome email sent successfully to: {}", user.getEmail());

            } catch (MessagingException | MailException e) {
                log.error("Failed to send welcome email to: {}", user.getEmail(), e);
            }
        });
    }

    /**
     * Envoie un email de réinitialisation de mot de passe
     */
    public void sendPasswordResetEmail(User user, String resetToken) {
        if (!emailEnabled) {
            log.info("Email sending is disabled");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                log.info("Sending password reset email to: {}", user.getEmail());
                
                String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;
                
                Context context = new Context();
                context.setVariable("userName", user.getUsername());
                context.setVariable("resetUrl", resetUrl);
                context.setVariable("frontendUrl", frontendUrl);
                context.setVariable("year", java.time.Year.now().getValue());

                String htmlContent = templateEngine.process("email/password-reset", context);

                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(fromEmail);
                helper.setTo(user.getEmail());
                helper.setSubject("Réinitialisation de votre mot de passe - JavaCraft Academy");
                helper.setText(htmlContent, true);

                mailSender.send(message);
                log.info("Password reset email sent successfully to: {}", user.getEmail());

            } catch (MessagingException | MailException e) {
                log.error("Failed to send password reset email to: {}", user.getEmail(), e);
            }
        });
    }

    /**
     * Envoie un email de confirmation de changement de mot de passe
     */
    public void sendPasswordChangeConfirmationEmail(User user) {
        if (!emailEnabled) {
            log.info("Email sending is disabled");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                log.info("Sending password change confirmation email to: {}", user.getEmail());

                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(user.getEmail());
                message.setSubject("Confirmation de changement de mot de passe - JavaCraft Academy");
                message.setText(String.format(
                    "Bonjour %s,\n\n" +
                    "Votre mot de passe a été modifié avec succès.\n\n" +
                    "Si vous n'êtes pas à l'origine de cette modification, " +
                    "veuillez contacter notre support immédiatement.\n\n" +
                    "Cordialement,\n" +
                    "L'équipe JavaCraft Academy",
                    user.getUsername()
                ));

                mailSender.send(message);
                log.info("Password change confirmation email sent successfully to: {}", user.getEmail());

            } catch (MailException e) {
                log.error("Failed to send password change confirmation email to: {}", user.getEmail(), e);
            }
        });
    }

    /**
     * Envoie un email de notification de nouvelle connexion
     */
    public void sendLoginNotificationEmail(User user, String ipAddress, String userAgent) {
        if (!emailEnabled) {
            log.info("Email sending is disabled");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                log.info("Sending login notification email to: {}", user.getEmail());

                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(user.getEmail());
                message.setSubject("Nouvelle connexion détectée - JavaCraft Academy");
                message.setText(String.format(
                    "Bonjour %s,\n\n" +
                    "Une nouvelle connexion à votre compte a été détectée :\n\n" +
                    "Date et heure : %s\n" +
                    "Adresse IP : %s\n" +
                    "Navigateur : %s\n\n" +
                    "Si cette connexion n'est pas de vous, " +
                    "veuillez changer votre mot de passe immédiatement.\n\n" +
                    "Cordialement,\n" +
                    "L'équipe JavaCraft Academy",
                    user.getUsername(),
                    java.time.LocalDateTime.now().toString(),
                    ipAddress,
                    userAgent
                ));

                mailSender.send(message);
                log.info("Login notification email sent successfully to: {}", user.getEmail());

            } catch (MailException e) {
                log.error("Failed to send login notification email to: {}", user.getEmail(), e);
            }
        });
    }

    /**
     * Envoie un email de vérification d'adresse email
     */
    public void sendEmailVerificationEmail(User user, String verificationToken) {
        if (!emailEnabled) {
            log.info("Email sending is disabled");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                log.info("Sending email verification email to: {}", user.getEmail());

                String verificationUrl = frontendUrl + "/verify-email?token=" + verificationToken;

                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(user.getEmail());
                message.setSubject("Vérification de votre adresse email - JavaCraft Academy");
                message.setText(String.format(
                    "Bonjour %s,\n\n" +
                    "Merci de vous être inscrit sur JavaCraft Academy !\n\n" +
                    "Pour activer votre compte, veuillez cliquer sur le lien suivant :\n" +
                    "%s\n\n" +
                    "Ce lien expirera dans 24 heures.\n\n" +
                    "Si vous n'avez pas créé ce compte, vous pouvez ignorer cet email.\n\n" +
                    "Cordialement,\n" +
                    "L'équipe JavaCraft Academy",
                    user.getUsername(),
                    verificationUrl
                ));

                mailSender.send(message);
                log.info("Email verification email sent successfully to: {}", user.getEmail());

            } catch (MailException e) {
                log.error("Failed to send email verification email to: {}", user.getEmail(), e);
            }
        });
    }

    /**
     * Envoie un email générique
     */
    public void sendEmail(String to, String subject, String content) {
        if (!emailEnabled) {
            log.info("Email sending is disabled");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                log.info("Sending generic email to: {}", to);

                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(to);
                message.setSubject(subject);
                message.setText(content);

                mailSender.send(message);
                log.info("Generic email sent successfully to: {}", to);

            } catch (MailException e) {
                log.error("Failed to send generic email to: {}", to, e);
            }
        });
    }

    /**
     * Teste la configuration email
     */
    public boolean testEmailConfiguration() {
        if (!emailEnabled) {
            log.info("Email sending is disabled");
            return false;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(fromEmail); // Envoie à soi-même pour le test
            message.setSubject("Test Email Configuration - JavaCraft Academy");
            message.setText("Ceci est un email de test pour vérifier la configuration email.");

            mailSender.send(message);
            log.info("Email configuration test successful");
            return true;

        } catch (MailException e) {
            log.error("Email configuration test failed", e);
            return false;
        }
    }
}

