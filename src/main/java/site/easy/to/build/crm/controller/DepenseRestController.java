package site.easy.to.build.crm.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.entity.Depense;
import site.easy.to.build.crm.repository.DepenseRepository;
import site.easy.to.build.crm.service.depense.DepenseService;

@RestController
@RequestMapping("/api/depenses")
public class DepenseRestController {
    private final DepenseService depenseService;
    private final DepenseRepository depenseRepository;

    public DepenseRestController(DepenseService depenseService, DepenseRepository depenseRepository) {
        this.depenseService = depenseService;
        this.depenseRepository = depenseRepository;
    }

    @PutMapping("/update/{id}/{valeurDepense}")
    public ResponseEntity<Depense> updateDepense(
            @PathVariable Integer id,
            @PathVariable double valeurDepense) {

        // Call the service to update the depense value using the provided id and valeurDepense
        depenseService.updateDepenseValue(id, valeurDepense);

        // Optionally, fetch the updated Depense if needed (depends on your use case)
        Depense depense = depenseRepository.findByDepenseId(id); // You might need a `findById` method in the service

        // Return the updated depense with a 200 OK status
        return ResponseEntity.ok(depense);
    }

}
