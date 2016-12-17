package softuniBlog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import softuniBlog.entity.User;
import softuniBlog.repository.UserRepository;

import java.util.Date;

@Controller
public class ResetPasswordController {

    @Autowired
    private UserRepository users;

    @RequestMapping(value = "/resetpassword", method = RequestMethod.GET)
    public String resetpasswordView(@RequestParam(value = "_key") String resetPasswordToken, final Model model) {
        User user = users.findByResetPasswordToken(resetPasswordToken);
        Date expirationDate;
        if (user != null) {
            expirationDate = user.getResetPasswordExpires();
            if (expirationDate.after(new Date())) {
                model.addAttribute("user", user);
                model.addAttribute("resetPasswordToken", resetPasswordToken);

                model.addAttribute("view", "user/resetpassword");

                return "base-layout";
//                return "/user/resetpassword";
            }
        }
        return "/error/usererror";
    }

    @RequestMapping(value = "/resetpassword", method = RequestMethod.POST)
    public String resetPassword( @RequestParam(value = "_key") String resetPasswordToken, @ModelAttribute User user,
                                 final Model model) {
        User userToUpdate = users.findByResetPasswordToken(resetPasswordToken);
        String uptadedPassword = user.getPassword();
        userToUpdate.setPassword(encryptPassword(uptadedPassword));
        userToUpdate.setResetPasswordToken(null);
        userToUpdate.setResetPasswordExpires(null);
        users.save(userToUpdate);

        boolean passwordChanged = true;
        String redirectionUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path("/home").build().toUriString();
        String responseMessage = "Your password was successfully updated";
        model.addAttribute("passwordChanged", passwordChanged);
        model.addAttribute("redirectionUrl", redirectionUrl);
        model.addAttribute("responseMessage", responseMessage);
        return  "redirect:/login";
    }
    private String encryptPassword(String password){
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(password);
        return hashedPassword;
    }
}