package com.railway.helloworld;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@Controller
public class HelloworldApplication {

	@GetMapping("/")  // Mapeamos la ruta raíz
	public String index() {
		return "index";  // Thymeleaf buscará la plantilla 'index.html'
	}

	@GetMapping("/registro")
	public String registrar() {
		return "RegisterUser";
	}

}
