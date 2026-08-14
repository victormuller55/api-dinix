package br.net.convertix.dinix.config;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Profile("dev")
public class HomeController {

    @GetMapping("/")
    public String swagger() {
        return "redirect:/swagger-ui.html";
    }
}
