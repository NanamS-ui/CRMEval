package site.easy.to.build.crm.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.easy.to.build.crm.service.customer.CustomerService;

@RestController
@RequestMapping("/api/importRest")
public class ImportRestController {

    private final CustomerService customerService;

    public ImportRestController(CustomerService customerService) {
        this.customerService = customerService;
    }
    @PostMapping
    public ResponseEntity<?> importData(
            @RequestParam("file") MultipartFile file,
            @RequestParam("separateur") String separateur) {
        try {
            customerService.importCustomerFromCSV(file, separateur);
            return ResponseEntity.ok().body("{\"message\": \"Importation réussie\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Une erreur s'est produite lors de l'importation\"}");
        }
    }
}
