package com.railway.helloworld.controller;
import com.railway.helloworld.HelloworldApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class HomeController {


    @GetMapping("/")  // Mapeamos la ruta raíz
    public String index() {
        return "index";  // Thymeleaf buscará la plantilla 'index.html'
    }

    @GetMapping("/registro")
    public String registrar() {
        return "RegisterUser";
    }
}