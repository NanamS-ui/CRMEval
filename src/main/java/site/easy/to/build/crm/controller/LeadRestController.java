package site.easy.to.build.crm.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.entity.*;
import site.easy.to.build.crm.entity.settings.LeadEmailSettings;
import site.easy.to.build.crm.google.model.calendar.EventDisplay;
import site.easy.to.build.crm.google.model.drive.GoogleDriveFolder;
import site.easy.to.build.crm.google.model.gmail.Attachment;
import site.easy.to.build.crm.google.service.acess.GoogleAccessService;
import site.easy.to.build.crm.google.service.calendar.GoogleCalendarApiService;
import site.easy.to.build.crm.google.service.drive.GoogleDriveApiService;
import site.easy.to.build.crm.google.service.gmail.GoogleGmailApiService;
import site.easy.to.build.crm.repository.DepenseRepository;
import site.easy.to.build.crm.repository.LeadRepository;
import site.easy.to.build.crm.service.customer.CustomerService;
import site.easy.to.build.crm.service.depense.DepenseService;
import site.easy.to.build.crm.service.drive.GoogleDriveFileService;
import site.easy.to.build.crm.service.file.FileService;
import site.easy.to.build.crm.service.lead.LeadActionService;
import site.easy.to.build.crm.service.lead.LeadService;
import site.easy.to.build.crm.service.settings.LeadEmailSettingsService;
import site.easy.to.build.crm.service.user.UserService;
import site.easy.to.build.crm.util.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/employee/lead")
public class LeadRestController {

    private final LeadService leadService;
    private final AuthenticationUtils authenticationUtils;
    private final UserService userService;
    private final CustomerService customerService;
    private final LeadActionService leadActionService;
    private final GoogleCalendarApiService googleCalendarApiService;
    private final FileService fileService;
    private final GoogleDriveApiService googleDriveApiService;
    private final GoogleDriveFileService googleDriveFileService;
    private final FileUtil fileUtil;
    private final LeadEmailSettingsService leadEmailSettingsService;
    private final GoogleGmailApiService googleGmailApiService;
    private final EntityManager entityManager;
    private final DepenseService depenseService;
    private final LeadRepository leadRepository;
    private final DepenseRepository depenseRepository;
    @Autowired
    public LeadRestController(LeadService leadService, AuthenticationUtils authenticationUtils, UserService userService, CustomerService customerService,
                              LeadActionService leadActionService, GoogleCalendarApiService googleCalendarApiService, FileService fileService,
                              GoogleDriveApiService googleDriveApiService, GoogleDriveFileService googleDriveFileService, FileUtil fileUtil,
                              LeadEmailSettingsService leadEmailSettingsService, GoogleGmailApiService googleGmailApiService, EntityManager entityManager, DepenseService depenseService, LeadRepository leadRepository, DepenseRepository depenseRepository) {
        this.leadService = leadService;
        this.authenticationUtils = authenticationUtils;
        this.userService = userService;
        this.customerService = customerService;
        this.leadActionService = leadActionService;
        this.googleCalendarApiService = googleCalendarApiService;
        this.fileService = fileService;
        this.googleDriveApiService = googleDriveApiService;
        this.googleDriveFileService = googleDriveFileService;
        this.fileUtil = fileUtil;
        this.leadEmailSettingsService = leadEmailSettingsService;
        this.googleGmailApiService = googleGmailApiService;
        this.entityManager = entityManager;
        this.depenseService = depenseService;
        this.leadRepository = leadRepository;
        this.depenseRepository = depenseRepository;
    }

    @GetMapping("/status/count")
    public Map<String, Long> getLeadCountByStatus() {
        return leadService.getLeadCountByStatus();
    }

    @GetMapping("/manager/sumDepense")
    public ResponseEntity<Double> sumDepense() {
        double depenses = depenseRepository.getTotalDepenseForLeads();

        return ResponseEntity.ok(depenses);
    }
    @GetMapping("/list-status")
    public List<String> list(){
        return leadRepository.findDistinctStatus();
    }
    @GetMapping("/show/{id}")
    public ResponseEntity<?> showDetails(@PathVariable("id") int id, Authentication authentication) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User loggedInUser = userService.findById(userId);
        if (loggedInUser.isInactiveUser()) {
            return ResponseEntity.status(403).body("Account is inactive");
        }

        Lead lead = leadService.findByLeadId(id);
        Depense depense = depenseService.findByLead(lead);

        if (lead == null) {
            return ResponseEntity.status(404).body("Lead not found");
        }

        User employee = lead.getEmployee();
        if (!AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER") && !AuthorizationUtil.checkIfUserAuthorized(employee, loggedInUser)) {
            return ResponseEntity.status(403).body("Access denied");
        }

        EventDisplay eventDisplay = null;
        String eventId = lead.getMeetingId();
        List<File> files = fileService.findByLeadId(id);
        List<Attachment> attachments = new ArrayList<>();
        for (File file : files) {
            String base64Data = Base64.getEncoder().encodeToString(file.getFileData());
            Attachment attachment = new Attachment(file.getFileName(), base64Data, file.getFileType());
            attachments.add(attachment);
        }
        if (!(authentication instanceof UsernamePasswordAuthenticationToken) && eventId != null && !eventId.isEmpty() && googleCalendarApiService != null) {
            OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
            try {
                eventDisplay = googleCalendarApiService.getEvent("primary", oAuthUser, eventId);
            } catch (IOException | GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("lead", lead);
        response.put("event", eventDisplay);
        response.put("attachments", attachments);
        response.put("depense", depense);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/assigned-leads")
    public ResponseEntity<List<Lead>> showAssignedEmployeeLeads(Authentication authentication) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        List<Lead> leads = leadService.findAssignedLeads(userId);
        return ResponseEntity.ok(leads);
    }

    @GetMapping("/created-leads")
    public ResponseEntity<List<Lead>> showCreatedEmployeeLeads(Authentication authentication) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        List<Lead> leads = leadService.findCreatedLeads(userId);
        return ResponseEntity.ok(leads);
    }

    @GetMapping("/manager/all-leads")
    public ResponseEntity<List<Lead>> showAllLeads() {
        List<Lead> leads = leadService.findAll();
        return ResponseEntity.ok(leads);
    }

    @GetMapping("/manager/all-leads-details")
    public ResponseEntity<List<Depense>> showAllTicketsDetails() {
        List<Depense> depenses = depenseRepository.findAllDepensesForLeads();
        return ResponseEntity.ok(depenses);
    }

    @DeleteMapping("/delete/{id}/{idDepense}")
    public ResponseEntity<?> deleteLead(@PathVariable("id") int id, @PathVariable("idDepense") int idDepense,Authentication authentication) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User loggedInUser = userService.findById(userId);
        if (loggedInUser.isInactiveUser()) {
            return ResponseEntity.status(403).body("Account is inactive");
        }

        Lead lead = leadService.findByLeadId(id);
        if (lead == null) {
            return ResponseEntity.status(404).body("Lead not found");
        }

        // Delete associated expenses
        depenseRepository.deleteById(idDepense);

        // Delete the lead
        leadService.delete(lead);

        return ResponseEntity.ok("Lead deleted successfully");
    }

}