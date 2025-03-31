package site.easy.to.build.crm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import site.easy.to.build.crm.service.customer.CustomerService;
@Controller
@RequestMapping("/dupliquer")
public class DupliquerController {
    CustomerService customerService;

    public DupliquerController(CustomerService customerService) {
        this.customerService = customerService;
    }
    @GetMapping()
    public String genererCSV(@RequestParam("id") int id, RedirectAttributes redirectAttributes){
        try{
            customerService.duplicateCustomer(id);
            redirectAttributes.addAttribute("Duplique avec succes");
        }
        catch (Exception e){
            e.printStackTrace();
            redirectAttributes.addAttribute("Erreur de duplication"+e.getMessage());

        }
        return "redirect:/employee/customer/manager/all-customers";
    }

}
