package site.easy.to.build.crm.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import site.easy.to.build.crm.service.csv.CsvService;
import site.easy.to.build.crm.service.csv.EntityScannerService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

@Controller
@RequestMapping("/csv")
public class CsvController {

    private final CsvService csvService;
    private final EntityScannerService entityScannerService;

    public CsvController(CsvService csvService,EntityScannerService entityScannerService, DataSource dataSource) {
        this.csvService = csvService;
        this.entityScannerService = entityScannerService;
    }
    @GetMapping("/form")
    public String showForm(Model model){
        Set<Class<?>> entities = entityScannerService.getAllEntities();
        char[] separators = {',', ';'};
        model.addAttribute("entities",entities);
        model.addAttribute("separators",separators);
        return "csv/import";
    }

    @PostMapping("/upload")
    public String uploadCsv(@RequestParam("file") MultipartFile file,
                            @RequestParam("entityName") String entityName, RedirectAttributes redirectAttributes,
                            @RequestParam("separator")char separator) throws Exception{
        try {
            Class<?> entityClass = getEntityClass(entityName);
            csvService.insertData(file, entityClass,separator);
            redirectAttributes.addAttribute("message","Fichier CSV importé avec succès !");
            return "redirect:/csv/form";
        } catch (Exception e) {
            e.printStackTrace();
            return "error/500";
        }
    }

    private Class<?> getEntityClass(String entityName) throws ClassNotFoundException {
        String packageName = "site.easy.to.build.crm.entity";
        return Class.forName(packageName + "." + entityName);
    }
}