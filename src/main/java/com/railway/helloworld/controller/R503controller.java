package com.railway.helloworld.controller;


import com.railway.helloworld.AutentRequest;
import com.railway.helloworld.FingerprintRequest;
import com.railway.helloworld.RegistroAlumnoDTO;
import com.railway.helloworld.model.Asistencia;
import com.railway.helloworld.model.Usuario;
import com.railway.helloworld.repository.AsistenciaRepository;
import com.railway.helloworld.repository.UsuarioRepository;
import com.railway.helloworld.service.R503service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;





import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

//@CrossOrigin(origins = "http://localhost:8080")
@CrossOrigin(origins = "*")

@RestController //Indica que el controlador manejara respuestas REST (JSON )
@RequestMapping("/R503") //Esta es la ruta(EndPoint)

public class R503controller {


    public static String ipEsp32 = "192.168.0.207";
    private static String ultimaPlantillaBase64 = null;
    private static String idHuella = null;
    private static String idAutent = null;

    @Autowired
    private UsuarioRepository usuarioRepository; // Inyectar el repositorio

    @Autowired
    private AsistenciaRepository asistenciaRepository;// Inyectar el repositorio

    @Autowired
    private R503service sensorService; //Inyectamos el service.


    private String R503 = "Apagado"; //Esta variable String tiene el estado por defecto del sensor.

    //Este controlador solo sirve para actualizar el estado del sensor prendido o pagado, no interfiere con otras cosas(No eliminar.)
    @GetMapping("/estado")
    public String obtenerEstadoLed() {
        return R503.equalsIgnoreCase("Encendido") ? "Encendido" : "Apagado";
    }


    @PostMapping("/control")
    public String controlarR503(@RequestParam String estado) {
        R503 = estado;

        // URL del ESP32
        String urlESP = "http://" + ipEsp32 + "/R503?estado=" + estado; //192.168.227.205 (celular) o (192.168.1.70)

        // Enviar la petición al ESP32
        RestTemplate restTemplate = new RestTemplate();
        String respuestaESP = restTemplate.getForObject(urlESP, String.class);

        return "Spring Boot: " + (R503.equalsIgnoreCase("Encendido") ? "LED Encendido" : "LED Apagado") +
                " | Respuesta ESP32: " + respuestaESP;
    }

    //El controlador que recibe el POST del esp32 (template,id)
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFingerprint(@RequestBody FingerprintRequest request) {
        // Aquí procesamos directamente la huella y su id
        String base64Template = request.getTemplate();
        String id = request.getIdFinger();

        //Imprimimos en consola para asegurarnos de que lleguen.
        System.out.println(String.format("""
                Template recibido: %s
                Id Recibido: %s
                """,base64Template,id));

        // la guardamos en una variable global
        ultimaPlantillaBase64 = request.getTemplate();
        idHuella = request.getIdFinger();

        // validación, guardado, etc.
        return ResponseEntity.ok("Huella digital recibida correctamente.");
    }

    //El controlador que recibe otro POST del esp32 (template,id) :V
    @PostMapping("/uploadAutent")
    public ResponseEntity<String> uploadFingerprint(@RequestBody AutentRequest request) {
        // Aquí procesamos directamente el id
        String id = request.getId_autent();

        //Imprimimos en consola para asegurarnos de que lleguen.
        System.out.println(String.format("""
                Id recibido: %s
                """,id));

        // la guardamos en una variable global
        idAutent = request.getId_autent();

        registrarAsistencia(idAutent);


        // validación, guardado, etc.
        return ResponseEntity.ok("Autenticacion exitosa!");
    }




    //El controlador que envia datos del sensor al formulario
    @GetMapping("/getTemplate")
    public ResponseEntity<Map<String, String>> getFingerprintTemplate() {
        if (ultimaPlantillaBase64 == null && idHuella == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Map<String, String> response = new HashMap<>();
        response.put("template", ultimaPlantillaBase64);
        response.put("idFinger", idHuella);

        return ResponseEntity.ok(response);
    }

    //El controlador que manda los datos del formulario a DTO.
    @PostMapping("/registrar")
    public ResponseEntity<String> registrarUsuario(@ModelAttribute RegistroAlumnoDTO dto) {
        // Mapear los datos del DTO a la entidad Usuario

        Usuario usuario = new Usuario(
                dto.getTxtNombre(),
                dto.getTxtApellido(),
                dto.getTxtTelefono(),
                dto.getTxtCorreo(),
                dto.getPlantillaHuella(),
                dto.getFingerId());

        // Guardar el usuario en la base de datos
        usuarioRepository.save(usuario); // Aquí es donde realmente guardas el usuario

        // Retornar una respuesta exitosa
        return ResponseEntity.ok("Usuario registrado correctamente");
    }

    //Controlador post para activar la autenticacion del ESP32:
    @GetMapping("/autenticar")
    public ResponseEntity<String> autenticarConR503()   {
        String urlESP = "http://" + ipEsp32 + "/R503?estado=Autenticando";

        RestTemplate restTemplate = new RestTemplate();
        String respuestaESP = restTemplate.getForObject(urlESP, String.class);

        return ResponseEntity.ok("Se envió autenticación al ESP32. Respuesta: " + respuestaESP);
    }

    @GetMapping("/autoAutenticar")
    public ResponseEntity<String> autenticarConR503auto() {
        String respuesta = sensorService.autenticarConR503auto();
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/formatear")
    public ResponseEntity<String> formatear()   {
        String urlESP = "http://" + ipEsp32 + "/R503?estado=Formateando";

        RestTemplate restTemplate = new RestTemplate();
        String respuestaESP = restTemplate.getForObject(urlESP, String.class);

        return ResponseEntity.ok("Se envió Formateando al ESP32. Respuesta: " + respuestaESP);
    }

    @GetMapping("/reiniciar")
    public ResponseEntity<String> reiniciarConR503()   {
        String urlESP = "http://" + ipEsp32 + "/R503?estado=Encendido";

        RestTemplate restTemplate = new RestTemplate();
        String respuestaESP = restTemplate.getForObject(urlESP, String.class);

        return ResponseEntity.ok("Se envió autenticación al ESP32. Respuesta: " + respuestaESP);
    }

    //Metodos Auxiliares:
    public void registrarAsistencia(String idAutent) {
        Long idUsuario = Long.valueOf(idAutent); // convertir si viene como texto

        Optional<Usuario> usuarioOptional = usuarioRepository.findById(idUsuario);
        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();

            Asistencia nuevaAsistencia = new Asistencia();
            nuevaAsistencia.setAsistenciaFecha(LocalDate.now()); // fecha actual
            nuevaAsistencia.setAsistenciaHora(LocalTime.now());//Hora actual
            nuevaAsistencia.setAsistenciaValor(true); // por defecto no asistió
            nuevaAsistencia.setUsuario(usuario); // relación

            asistenciaRepository.save(nuevaAsistencia);
            System.out.println("Asistencia guardada.");
        } else {
            System.out.println("Usuario no encontrado con ID: " + idUsuario);
        }
    }

    //A este Get accede el Esp32 para eliminar algo.
    @GetMapping("/eliminar")
    public ResponseEntity<String> EliminarConR503(Long id)   {
        String urlESP = "http://" + ipEsp32 + "/R503?estado=Eliminando&id=" + id;

        RestTemplate restTemplate = new RestTemplate();
        String respuestaESP = restTemplate.getForObject(urlESP, String.class);

        return ResponseEntity.ok("Se envió autenticación al ESP32. Respuesta: " + respuestaESP);
    }
}








