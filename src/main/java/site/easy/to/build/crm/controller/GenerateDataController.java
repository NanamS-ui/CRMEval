package site.easy.to.build.crm.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import site.easy.to.build.crm.service.evaluation.GenerateDataService;

@Controller
public class GenerateDataController {
    private final GenerateDataService generateDataService;

    public GenerateDataController(GenerateDataService generateDataService) {
        this.generateDataService = generateDataService;
    }

    @GetMapping("/generate/form")
    public String showForm(Model model) {
        return "generate/form";
    }

    @PostMapping("/generate/data")
    public String generateData(@RequestParam("nombreDeLigneCustomer_info_login_et_Customer") int customers,
                               @RequestParam("nmbreLigne_trigger_lead") int leads,
                               @RequestParam("nombreLigne_trigger_ticket") int tickets,
                               @RequestParam("nombredeligne_depense") int expenses,
                               @RequestParam("nombredeligne_budget") int budgets,
                               RedirectAttributes redirectAttributes) {
        try {
            generateDataService.generateRandomData(customers, leads, tickets, expenses, budgets);
            redirectAttributes.addFlashAttribute("success", "Données générées avec succès.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la génération des données: " + e.getMessage());
        }
        return "redirect:/generate/form";
    }
}
