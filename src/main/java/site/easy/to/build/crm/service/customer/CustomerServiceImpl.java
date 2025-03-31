package site.easy.to.build.crm.service.customer;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.easy.to.build.crm.entity.*;
import site.easy.to.build.crm.repository.*;
import site.easy.to.build.crm.service.lead.LeadService;
import site.easy.to.build.crm.service.ticket.TicketService;
import site.easy.to.build.crm.util.CopieUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static site.easy.to.build.crm.util.CopieUtil.parseDate;
import static site.easy.to.build.crm.util.CopieUtil.writeToFile;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final LeadService leadService;
    private final DepenseRepository depenseRepository;
    private final TicketService ticketService;
    private final LeadRepository leadRepository;
    private final TicketRepository ticketRepository;
    private final BudgetRepository budgetRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository, UserRepository userRepository, LeadService leadService, DepenseRepository depenseRepository, TicketService ticketService, LeadRepository leadRepository, TicketRepository ticketRepository, BudgetRepository budgetRepository) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.leadService = leadService;
        this.depenseRepository = depenseRepository;
        this.ticketService = ticketService;
        this.leadRepository = leadRepository;
        this.ticketRepository = ticketRepository;
        this.budgetRepository = budgetRepository;
    }

    @Override
    public Customer findByCustomerId(int customerId) {
        return customerRepository.findByCustomerId(customerId);
    }

    @Override
    public Customer findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    @Override
    public List<Customer> findByUserId(int userId) {
        return customerRepository.findByUserId(userId);
    }

    @Override
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Override
    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    public void delete(Customer customer) {
        customerRepository.delete(customer);
    }

    @Override
    public List<Customer> getRecentCustomers(int userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return customerRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public long countByUserId(int userId) {
        return customerRepository.countByUserId(userId);
    }

    @Override
    public String generateCsv(Customer customer, List<Lead> leads, List<Ticket> tickets,
                              Map<Integer, Depense> depenseParLead,
                              Map<Integer, Depense> depenseParTicket) {
        StringBuilder stringBuilder = new StringBuilder();

        // Informations du Customer
        stringBuilder.append("customerId,name,Email,createdAt,country,idUser").append("\n");
        stringBuilder.append(customer.getCustomerId()).append(",");
        stringBuilder.append(customer.getName()).append(",");
        stringBuilder.append(customer.getEmail()).append(",");
        stringBuilder.append(customer.getCreatedAt()).append(",");
        stringBuilder.append(customer.getCountry()).append(",");
        stringBuilder.append(customer.getUser().getId()).append("\n");
        stringBuilder.append("-#").append("\n");

        // Ajouter les Leads avec leur dépense unique
        stringBuilder.append("leadId,leadName,status,expense").append("\n");
        for (Lead lead : leads) {
            stringBuilder.append(lead.getLeadId()).append(",");
            stringBuilder.append(lead.getName()).append(",");
            stringBuilder.append(lead.getStatus()).append(",");

            // Récupérer la dépense associée au Lead
            Depense depense = depenseParLead.get(lead.getLeadId());
            if (depense != null) {
                stringBuilder.append(depense.getValeurDepense()); // Valeur de la dépense
            } else {
                stringBuilder.append("0"); // Si aucune dépense, mettre "0"
            }
            stringBuilder.append("\n");
        }

        stringBuilder.append("--#").append("\n");

        // Ajouter les Tickets avec leur dépense unique
        stringBuilder.append("ticketId,ticketName,status,expense").append("\n");
        for (Ticket ticket : tickets) {
            stringBuilder.append(ticket.getTicketId()).append(",");
            stringBuilder.append(ticket.getSubject()).append(",");
            stringBuilder.append(ticket.getStatus()).append(",");

            // Récupérer la dépense associée au Ticket
            Depense depense = depenseParTicket.get(ticket.getTicketId());
            if (depense != null) {
                stringBuilder.append(depense.getValeurDepense()); // Valeur de la dépense
            } else {
                stringBuilder.append("0"); // Si aucune dépense, mettre "0"
            }
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }

    @Override
    public Customer saveCustomer(String name, String country, String email, int idUser) {
        // Créer un nouveau client
        Customer customer = new Customer();

        // Définir les propriétés du client à partir des paramètres
        customer.setName(name);
        customer.setCountry(country);
        customer.setEmail(email);

        // Associer l'utilisateur au client (ici, on suppose que tu as une méthode pour récupérer l'utilisateur par id)
        User user = userRepository.findById(idUser);
        customer.setUser(user);

        // Enregistrer le client dans la base de données
        return customerRepository.save(customer);
    }

    @Override
    public String generateCustomerCSV(Customer customer, List<Lead> leads, List<Ticket> tickets,List<Depense>depenses) {
        StringBuilder sb = new StringBuilder();

        sb.append("# CUSTOMER DATA\n");

        sb.append("name,email,createdAt,country,idUser\n");

        sb.append(CopieUtil.escapeCsvField(customer.getName())).append(",");
        sb.append(CopieUtil.escapeCsvField(customer.getEmail())).append(",");
        sb.append(CopieUtil.escapeCsvField(customer.getCreatedAt())).append(",");
        sb.append(CopieUtil.escapeCsvField(customer.getCountry())).append(",");
        sb.append(CopieUtil.escapeCsvField(customer.getUser().getId())).append("\n\n");

        sb.append("# LEAD DATA\n");
        sb.append("leadName,status,customerId\n");
        for (Lead lead : leads) {
            sb.append(CopieUtil.escapeCsvField(lead.getName())).append(",");
            sb.append(CopieUtil.escapeCsvField(lead.getStatus())).append(",");
            sb.append(CopieUtil.escapeCsvField(lead.getCustomer().getCustomerId())).append("\n");
        }
        sb.append("\n");

        sb.append("# TICKET DATA\n");
        sb.append("subject,status,customerId,createdAt\n");
        for (Ticket ticket : tickets) {
            sb.append(CopieUtil.escapeCsvField(ticket.getSubject())).append(",");
            sb.append(CopieUtil.escapeCsvField(ticket.getStatus())).append(",");
            sb.append(CopieUtil.escapeCsvField(ticket.getCustomer().getCustomerId())).append(",");
            sb.append(CopieUtil.escapeCsvField(ticket.getCreatedAt())).append("\n");
        }
        sb.append("\n");

        sb.append("# DEPENSE DATA\n");
        sb.append("montant,dateDepense,ticketSubject,leadName,etat\n");
        for(Depense depense:depenses){
            sb.append(CopieUtil.escapeCsvField(depense.getValeurDepense())).append(",");
            sb.append(CopieUtil.escapeCsvField(depense.getDateDepense())).append(",");
            if (depense.getTicket()!=null) sb.append(CopieUtil.escapeCsvField(depense.getTicket().getSubject())).append(",");
            else sb.append(",");
            if (depense.getLead()!=null) sb.append(CopieUtil.escapeCsvField(depense.getLead().getName())).append(",");
            else sb.append(",");
            sb.append(CopieUtil.escapeCsvField(depense.getEtat())).append("\n");
        }
        return sb.toString();
    }


    @Override
    public void duplicateCustomer(int idCustomer) throws IOException {
        Customer customer = findByCustomerId(idCustomer);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found with id: " + idCustomer);
        }
        Customer copie=new Customer();
        copie.setCreatedAt(customer.getCreatedAt());
        copie.setUser(customer.getUser());
        copie.setEmail(customer.getEmail());
        String copieName= CopieUtil.getCopieName(customer.getName());
        String copieEmail=CopieUtil.getCopieName(customer.getEmail());
        copie.setName(copieName);
        copie.setEmail(copieEmail);
        copie.setCountry(customer.getCountry());
        List<Lead> leads = leadRepository.findByCustomerCustomerId(idCustomer);
        List<Ticket> tickets = ticketRepository.findByCustomerCustomerId(idCustomer);
        List<Depense>depenses=new ArrayList<>();
        for(Lead lead:leads){
            Depense depense= depenseRepository.findByLeadLeadId(lead.getLeadId());
            depenses.add(depense);
        }
        for (Ticket ticket:tickets){
            Depense depense=depenseRepository.findByTicketTicketId(ticket.getTicketId());
            depenses.add(depense);
        }
        String csvContent = generateCustomerCSV(copie, leads, tickets,depenses);
        String filename = "customer_export_" + idCustomer + "_" + System.currentTimeMillis() + ".csv";
        writeToFile(csvContent, filename);
    }
    @Override
    public Customer importCustomerFromCSV(MultipartFile file, String separateur) throws IOException {
        // Lire toutes les lignes du fichier
        List<String> lines = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.toList());

        Customer customer = new Customer();
        List<Lead> leads = new ArrayList<>();
        List<Ticket> tickets = new ArrayList<>();
        List<String[]> depenseTempData = new ArrayList<>();

        String currentSection = null;
        int lineNumber = 0;

        try {
            // Parcourir chaque ligne du fichier
            for (String line : lines) {
                lineNumber++;

                // Ignorer les lignes vides
                if (line.trim().isEmpty()) {
                    continue;
                }

                // Détection des sections
                if (line.startsWith("#")) {
                    currentSection = line.trim();
                    continue;
                }

                try {
                    if ("# CUSTOMER DATA".equals(currentSection)) {
                        if (!line.startsWith("name")) {
                            String[] values = line.split(separateur);
                            customer.setName(values[0]);
                            customer.setEmail(values[1]);
                            customer.setCreatedAt(parseDate(values[2]));
                            customer.setCountry(values[3]);
                            customer.setUser(userRepository.findById(Integer.parseInt(values[4])));

                        }
                    }
                    else if ("# LEAD DATA".equals(currentSection)) {
                        if (!line.startsWith("leadName")) {
                            String[] values = line.split(separateur);

                            Lead lead = new Lead();
                            lead.setName(values[0]);
                            lead.setStatus(values[1]);
                            leads.add(lead);
                        }
                    }
                    else if ("# TICKET DATA".equals(currentSection)) {
                        if (!line.startsWith("subject")) {
                            String[] values = line.split(separateur);

                            Ticket ticket = new Ticket();
                            ticket.setSubject(values[0]);
                            ticket.setStatus(values[1]);
                            ticket.setCreatedAt(parseDate(values[3]));
                            tickets.add(ticket);
                        }
                    }
                    else if ("# DEPENSE DATA".equals(currentSection)) {
                        if (!line.startsWith("montant")) {
                            String[] values = line.split(separateur);
                            depenseTempData.add(values);
                        }
                    }

                } catch (Exception e) {
                    throw new RuntimeException("Erreur ligne " + lineNumber + ": " + line + " - " + e.getMessage(), e);
                }
            }

            // Sauvegarde en base de données
            Customer savedCustomer = customerRepository.save(customer);

            // Traitement des leads
            Map<String, Lead> leadMap = new HashMap<>();
            for (Lead lead : leads) {
                lead.setCustomer(savedCustomer);
                Lead savedLead = leadService.save(lead);
                leadMap.put(savedLead.getName(), savedLead);
            }

            // Traitement des tickets
            Map<String, Ticket> ticketMap = new HashMap<>();
            for (Ticket ticket : tickets) {
                ticket.setCustomer(savedCustomer);
                Ticket savedTicket = ticketService.save(ticket);
                ticketMap.put(savedTicket.getSubject(), savedTicket);
            }

            // Traitement des dépenses
            for (String[] values : depenseTempData) {
                Depense depense = new Depense();
                double valeurDepense = Double.parseDouble(values[0]);
                depense.setValeurDepense(valeurDepense);
                LocalDateTime localDateTime = parseDate(values[1]);
                depense.setDateDepense(localDateTime);
                depense.setEtat(Integer.parseInt(values[4]));

                if (values[2] != null && !values[2].isEmpty()) {

                    depense.setTicket(ticketMap.get(values[2]));
                }

                if (values[3] != null && !values[3].isEmpty()) {
                    depense.setLead(leadMap.get(values[3]));
                }

                depenseRepository.save(depense);
            }

            return savedCustomer;

        } catch (Exception e) {
            throw new IOException("Erreur lors de l'import CSV. Dernière ligne traitée: " + lineNumber, e);
        }
    }


}
