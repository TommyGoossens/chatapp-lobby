package com.tommy.lobby.config.swagger;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@Controller
public class ApiController {
    @RequestMapping(value = "/api")
    public String home() {
        return "redirect:/swagger-ui.html";
    }
}
