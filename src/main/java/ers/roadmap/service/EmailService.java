package ers.roadmap.service;

import ers.roadmap.exceptions.VerifyEmailException;
import ers.roadmap.security.model.AppUser;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender emailSender;

    @Value("${email}")
    private String appEmail;

    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    private void sendMessage(String to, String subject, String text) throws MessagingException {

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(appEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text);

        emailSender.send(message);
    }

    public void sendMessage(AppUser notVerifiedUser) throws VerifyEmailException {

        if(notVerifiedUser.isEnabled()) {
            throw new VerifyEmailException("Email Already Verified!");
        }

        String subject = "Email verification";
        String verificationCode = notVerifiedUser.getVerificationCode();

        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            sendMessage(notVerifiedUser.getEmail(), subject, htmlMessage);
        }catch (MessagingException e) {
            e.printStackTrace();
        }

    }


}
