package site.easy.to.build.crm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.service.customer.CustomerService;
import site.easy.to.build.crm.service.user.UserService;
import site.easy.to.build.crm.util.AuthenticationUtils;
import site.easy.to.build.crm.util.AuthorizationUtil;

import java.util.List;

@RestController
@RequestMapping("/api/customers") // Préfixe des endpoints REST
public class CustomerRestController {

    private final CustomerService customerService;
    private final UserService userService;
    private final AuthenticationUtils authenticationUtils;

    @Autowired
    public CustomerRestController(CustomerService customerService, UserService userService, AuthenticationUtils authenticationUtils) {
        this.customerService = customerService;
        this.userService = userService;
        this.authenticationUtils = authenticationUtils;
    }

    // Récupérer tous les clients (Manager)
    @GetMapping("/manager/all")
    public List<Customer> getAllCustomers() {
        return customerService.findAll();
    }

    // Récupérer les clients assignés à l'utilisateur connecté
    @GetMapping("/my")
    public List<Customer> getEmployeeCustomers(Authentication authentication) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        if (userId == -1) {
            throw new RuntimeException("Utilisateur non trouvé");
        }
        return customerService.findByUserId(userId);
    }

    // Récupérer un client par ID
    @GetMapping("/{id}")
    public Customer getCustomerById(@PathVariable("id") int id, Authentication authentication) {
        Customer customer = customerService.findByCustomerId(id);
        if (customer == null) {
            throw new RuntimeException("Client non trouvé");
        }

        User employee = customer.getUser();
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User loggedInUser = userService.findById(userId);

        if (loggedInUser.isInactiveUser()) {
            throw new RuntimeException("Compte inactif");
        }

        if (!AuthorizationUtil.checkIfUserAuthorized(employee, loggedInUser)) {
            throw new RuntimeException("Accès refusé");
        }

        return customer;
    }

    // Ajouter un nouveau client
    @PostMapping
    public Customer createCustomer(@RequestBody Customer customer) {
        return customerService.save(customer);
    }

}
