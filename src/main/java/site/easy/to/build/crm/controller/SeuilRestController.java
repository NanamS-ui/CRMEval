package site.easy.to.build.crm.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import site.easy.to.build.crm.service.seuil.SeuilService;
import site.easy.to.build.crm.service.user.UserService;
import site.easy.to.build.crm.util.AuthenticationUtils;
import site.easy.to.build.crm.entity.Seuil;
import site.easy.to.build.crm.entity.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping("/api/seuils")
public class SeuilRestController {
    private final SeuilService seuilService;
    private final UserService userService;
    private final AuthenticationUtils authenticationUtils;

    @Autowired
    public SeuilRestController(SeuilService seuilService, UserService userService, AuthenticationUtils authenticationUtils) {
        this.seuilService = seuilService;
        this.userService = userService;
        this.authenticationUtils = authenticationUtils;
    }

    @GetMapping
    public ResponseEntity<List<Seuil>> listSeuils() {
        return ResponseEntity.ok(seuilService.getAllSeuils());
    }

    @PutMapping("/edit2/{id}")
    public ResponseEntity<?> updateSeuil2(@PathVariable int id, @RequestBody Seuil seuil) {
        Seuil updatedSeuil = seuilService.updateSeuil(id, seuil);

        if (updatedSeuil == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Seuil non trouvé"));
        }

        return ResponseEntity.ok(Collections.singletonMap("message", "Seuil mis à jour avec succès"));
    }


    @PutMapping("/{id}")
    public ResponseEntity<Seuil> updateSeuil(@RequestBody Seuil seuilLimite2, @PathVariable("id") int id) {
        try {
            Optional<Seuil> optionalSeuil = seuilService.findById(id);

            if (optionalSeuil.isEmpty()) {
                return ResponseEntity.notFound().build(); // Return 404 if not found
            }

            Seuil seuilLimite = optionalSeuil.get();

            seuilLimite.setDateSeuil(seuilLimite2.getDateSeuil());
            seuilLimite.setTaux(seuilLimite2.getTaux());

            Seuil updatedSeuil = seuilService.update(seuilLimite);

            return ResponseEntity.ok(updatedSeuil);
        } catch (Exception e) {
            // Log the exception for debugging
            e.printStackTrace();
            // Return HTTP 500 Internal Server Error with a generic error message
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/actual")
    public ResponseEntity<Seuil> showSeuilActual() {
        return ResponseEntity.ok(seuilService.getSeuilActuel());
    }

    @PostMapping("/add")
    public ResponseEntity<Seuil> createSeuil(@RequestBody Seuil seuil) {
        try {
            seuil.setDateSeuil(LocalDateTime.now());

            Seuil createdSeuil = seuilService.addSeuil(seuil);

            return ResponseEntity.ok(createdSeuil);
        } catch (Exception e) {
            // Log l'erreur et retourner un code HTTP 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Seuil> getSeuilById(@PathVariable int id) {
        Optional<Seuil> seuil = seuilService.getSeuilById(id);
        return seuil.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

//    @PutMapping("/{id}")
//    public ResponseEntity<Seuil> updateSeuil(@PathVariable int id, @RequestBody Seuil seuil) {
//        seuilService.updateSeuil(id, seuil);
//        return ResponseEntity.ok(seuil);
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSeuil(@PathVariable int id) {
        seuilService.deleteSeuil(id);
        return ResponseEntity.noContent().build();
    }
}
