package site.easy.to.build.crm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.entity.Depense;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.service.depense.DepenseService;
import site.easy.to.build.crm.service.lead.LeadService;
import site.easy.to.build.crm.service.ticket.TicketService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/expense")
public class DepenseController {
    private final DepenseService depenseService;
    private final LeadService leadService;
    private final TicketService ticketService;

    public DepenseController(DepenseService depenseService, LeadService leadService, TicketService ticketService) {
        this.depenseService = depenseService;
        this.leadService = leadService;
        this.ticketService = ticketService;
    }

    @GetMapping("/form-expense/{id}")
    public String formAddExpense(Model model, @PathVariable("id") int id) {
        Lead lead = leadService.findByLeadId(id);
        Depense depense = depenseService.findByLead(lead);
        if (depense != null){
            return "redirect:/employee/lead/manager/all-leads";
        }
        model.addAttribute("id_lead", id);
        return "lead/form";
    }

    @GetMapping("/form-expense/ticket/{id}")
    public String formAddExpenseTicket(Model model, @PathVariable("id") int id) {
        Ticket lead = ticketService.findByTicketId(id);
        Depense depense = depenseService.findByTicket(lead);
        if (depense != null){
            return "redirect:/employee/ticket/manager/all-tickets";
        }
        model.addAttribute("id_lead", id);
        return "lead/form";
    }

    @PostMapping("/save/lead")
    public String saveExpense(@RequestParam("id-lead") int leadId,
                              @RequestParam("somme") double somme,
                              @RequestParam("date") String dateCreation) {

        Depense depense = new Depense();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse(dateCreation, formatter);

        Lead lead = leadService.findByLeadId(leadId);
        depense.setLead(lead);
        depense.setValeurDepense(somme);
        depense.setDateDepense(dateTime);
        depense.setEtat(0);

        depenseService.saveDepense(depense);

        return "redirect:/employee/lead/manager/all-leads";
    }

    @PostMapping("/save/ticket")
    public String saveExpenseTicket(@RequestParam("id-lead") int leadId,
                              @RequestParam("somme") double somme,
                              @RequestParam("date") String dateCreation) {

        Depense depense = new Depense();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse(dateCreation, formatter);

        Ticket lead = ticketService.findByTicketId(leadId);
        depense.setTicket(lead);
        depense.setValeurDepense(somme);
        depense.setDateDepense(dateTime);
        depense.setEtat(0);

        depenseService.saveDepense(depense);

        return "redirect:/employee/ticket/manager/all-tickets";
    }
}
