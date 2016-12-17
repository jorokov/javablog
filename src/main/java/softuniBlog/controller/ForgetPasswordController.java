package softuniBlog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import softuniBlog.entity.User;
import softuniBlog.repository.UserRepository;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Controller
public class ForgetPasswordController {

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private UserRepository users;

    @RequestMapping(value = "/forgetpassword", method = RequestMethod.GET)
    public String resetPasswordView(final Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("view", "user/forgetpassword");

//        return "/forgetpassword";
        return "base-layout";
    }

    @RequestMapping(value = "/forgetpassword", method = RequestMethod.POST)
    public String forgetPassword(@ModelAttribute User user, final Model model) throws MessagingException, IOException {
        model.addAttribute("user", user);
        User foundUser = users.findByEmail(user.getEmail());
        if (foundUser != null) {
            String secureToken = UUID.randomUUID().toString();
            foundUser.setResetPasswordToken(secureToken);
            giveTokenOneHourExpirationDelay(foundUser);
            updateUserIntoTheDatabase(foundUser, secureToken);
            String responseMessage = "A mail has been sent to your mail box";
            model.addAttribute("responseMessage", responseMessage);
        } else {
            String responseMessage = "Invalid address mail.This account doesn't exist";
            model.addAttribute("invalidMailAddress",responseMessage);

            return"/forgetpassword";
        }

        return "redirect:/login";
    }

    private void updateUserIntoTheDatabase(User foundUser, String secureToken) throws MessagingException, IOException {
        users.save(foundUser);
        String text = "You are receiving this because you (or someone else) have requested the reset of the password for your account.\n\n"
                + "Please click on the following link, or paste into your browser to complete the reset password process : <a href=\""
                + ServletUriComponentsBuilder.fromCurrentContextPath().path("/resetpassword").queryParam("_key", secureToken).build().toUriString() + "\">click here</a>"
                + " .If you did not request this, please ignore this email and your password will remain unchanged.";
        sendResetPasswordLink(foundUser.getEmail(), text);
    }

    private void giveTokenOneHourExpirationDelay(User foundUser) {
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        Date expirationDate = calendar.getTime();
        foundUser.setResetPasswordExpires(expirationDate);
    }

    private void sendResetPasswordLink(String email, String text) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(email);
        helper.setText(text, true);
        helper.setSubject("Password reset request");
        mailSender.send(message);
    }
}
