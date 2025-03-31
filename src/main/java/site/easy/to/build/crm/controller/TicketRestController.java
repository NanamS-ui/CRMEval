package site.easy.to.build.crm.controller;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.dto.TicketWithDepenseDTO;
import site.easy.to.build.crm.entity.*;
import site.easy.to.build.crm.entity.settings.TicketEmailSettings;
import site.easy.to.build.crm.google.service.gmail.GoogleGmailApiService;
import site.easy.to.build.crm.repository.DepenseRepository;
import site.easy.to.build.crm.service.customer.CustomerService;
import site.easy.to.build.crm.service.depense.DepenseService;
import site.easy.to.build.crm.service.settings.TicketEmailSettingsService;
import site.easy.to.build.crm.service.ticket.TicketService;
import site.easy.to.build.crm.service.user.UserService;
import site.easy.to.build.crm.util.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employee/ticket")
public class TicketRestController {

    private final TicketService ticketService;
    private final AuthenticationUtils authenticationUtils;
    private final UserService userService;
    private final CustomerService customerService;
    private final TicketEmailSettingsService ticketEmailSettingsService;
    private final GoogleGmailApiService googleGmailApiService;
    private final EntityManager entityManager;
    private final DepenseService depenseService;
    private final DepenseRepository depenseRepository;

    @Autowired
    public TicketRestController(TicketService ticketService, AuthenticationUtils authenticationUtils, UserService userService, CustomerService customerService,
                                TicketEmailSettingsService ticketEmailSettingsService, GoogleGmailApiService googleGmailApiService, EntityManager entityManager, DepenseService depenseService, DepenseRepository depenseRepository) {
        this.ticketService = ticketService;
        this.authenticationUtils = authenticationUtils;
        this.userService = userService;
        this.customerService = customerService;
        this.ticketEmailSettingsService = ticketEmailSettingsService;
        this.googleGmailApiService = googleGmailApiService;
        this.entityManager = entityManager;
        this.depenseService = depenseService;
        this.depenseRepository = depenseRepository;
    }

    @GetMapping("/show-ticket/{id}")
    public ResponseEntity<?> showTicketDetails(@PathVariable("id") int id, Authentication authentication) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User loggedInUser = userService.findById(userId);
        if (loggedInUser.isInactiveUser()) {
            return ResponseEntity.status(403).body("Account is inactive");
        }

        Ticket ticket = ticketService.findByTicketId(id);
        Depense depense = depenseService.findByTicket(ticket);
        if (ticket == null) {
            return ResponseEntity.status(404).body("Ticket not found");
        }

        User employee = ticket.getEmployee();
        if (!AuthorizationUtil.checkIfUserAuthorized(employee, loggedInUser) && !AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER")) {
            return ResponseEntity.status(403).body("Access denied");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("ticket", ticket);
        response.put("depense", depense);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/manager/all-tickets")
    public ResponseEntity<List<Ticket>> showAllTickets() {
        List<Ticket> tickets = ticketService.findAll();

        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/manager/all-tickets-details")
    public ResponseEntity<List<Depense>> showAllTicketsDetails() {
        List<Depense> depenses = depenseRepository.findAllDepensesForTickets();
        return ResponseEntity.ok(depenses);
    }

    @GetMapping("/manager/sumDepense")
    public ResponseEntity<Double> sumDepense() {
        double depenses = depenseRepository.getTotalDepenseForTickets();

        return ResponseEntity.ok(depenses);
    }

    @GetMapping("/status/count")
    public Map<String, Long> getTicketCountByStatus() {
        return ticketService.getTicketCountByStatus();
    }

    @DeleteMapping("/delete/{id}/{idDepense}")
    public ResponseEntity<?> deleteLead(@PathVariable("id") int id, @PathVariable("idDepense") int idDepense) {

        Ticket lead = ticketService.findByTicketId(id);
        if (lead == null) {
            return ResponseEntity.status(404).body("Lead not found");
        }

        // Delete associated expenses
        depenseRepository.deleteById(idDepense);

        // Delete the lead
        ticketService.delete(lead);

        return ResponseEntity.ok("Lead deleted successfully");
    }

    @PostMapping
    public Ticket saveTicket(@RequestBody Ticket ticket){
        return ticketService.save(ticket);
    }
}