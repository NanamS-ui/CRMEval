package site.easy.to.build.crm.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.entity.Depense;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.Notification;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.service.budget.BudgetService;
import site.easy.to.build.crm.service.depense.DepenseService;
import site.easy.to.build.crm.service.lead.LeadService;
import site.easy.to.build.crm.service.notification.NotificationService;
import site.easy.to.build.crm.service.ticket.TicketService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/expense")
public class DepenseController {
    private final DepenseService depenseService;
    private final LeadService leadService;
    private final TicketService ticketService;
    private final BudgetService budgetService;
    private final NotificationService notificationService;

    public DepenseController(DepenseService depenseService, LeadService leadService, TicketService ticketService, BudgetService budgetService, NotificationService notificationService) {
        this.depenseService = depenseService;
        this.leadService = leadService;
        this.ticketService = ticketService;
        this.budgetService = budgetService;
        this.notificationService = notificationService;
    }

    @GetMapping("/form-expense/{id}/{id-customer}")
    public String formAddExpense(Model model, @PathVariable("id") int id, @PathVariable("id-customer") int id_customer) {
        Lead lead = leadService.findByLeadId(id);
        Depense depense = depenseService.findByLead(lead);
        if (depense != null){
            return "redirect:/employee/lead/manager/all-leads";
        }
        model.addAttribute("id_lead", id);
        model.addAttribute("id_customer", id_customer);
        return "lead/form";
    }

    @GetMapping("/form-expense/ticket/{id}/{id-customer}")
    public String formAddExpenseTicket(Model model, @PathVariable("id") int id, @PathVariable("id-customer") int id_customer) {
        Ticket lead = ticketService.findByTicketId(id);
        Depense depense = depenseService.findByTicket(lead);
        if (depense != null){
            return "redirect:/employee/ticket/manager/all-tickets";
        }
        model.addAttribute("id_lead", id);
        model.addAttribute("id_customer", id_customer);
        return "ticket/form";
    }

    @PostMapping("/save/lead")
    public String saveExpense(@RequestParam("id-lead") int leadId,
                              @RequestParam("somme") double somme,
                              @RequestParam("date") String dateCreation, @RequestParam("id-customer") int customerId) {

        Depense depense = new Depense();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse(dateCreation, formatter);

        Lead lead = leadService.findByLeadId(leadId);
        Notification notif = budgetService.checkBudget(customerId, somme);
        depense.setLead(lead);
        depense.setValeurDepense(somme);
        depense.setDateDepense(dateTime);
        notif.setDateNotification(dateTime);
        depense.setEtat(notif.getEtat());

        Depense depense1 = depenseService.saveDepense(depense);
        notif.setEtat(0);
        notif.setIdDepense(depense1.getDepenseId());
        System.out.println("notif.getMessage()" + notif.getMessage());
        if (!notif.getMessage().equals("successful")) {
            notificationService.save(notif);
        }
        return "redirect:/employee/lead/manager/all-leads";
    }

    @PostMapping("/save/ticket")
    public String saveExpenseTicket(@RequestParam("id-lead") int leadId,
                              @RequestParam("somme") double somme,
                              @RequestParam("date") String dateCreation, @RequestParam("id-customer") int customerId) {

        Depense depense = new Depense();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse(dateCreation, formatter);

        Ticket lead = ticketService.findByTicketId(leadId);
        Notification notif = budgetService.checkBudget(customerId, somme);
        depense.setTicket(lead);
        depense.setValeurDepense(somme);
        depense.setDateDepense(dateTime);
        notif.setDateNotification(dateTime);
        depense.setEtat(notif.getEtat());

        Depense depense1 = depenseService.saveDepense(depense);
        notif.setEtat(0);
        notif.setIdDepense(depense1.getDepenseId());
        System.out.println("notif.getMessage()" + notif.getMessage());
        if (!notif.getMessage().equals("successful")) {
            notificationService.save(notif);
        }

        return "redirect:/employee/ticket/manager/all-tickets";
    }

    @PutMapping("/{id}")
    public ResponseEntity<Depense> updateDepense(
            @PathVariable Integer id,
            @RequestBody Depense updatedDepense) {

        Depense depense = depenseService.updateDepense(id, updatedDepense);
        return ResponseEntity.ok(depense);
    }
}
