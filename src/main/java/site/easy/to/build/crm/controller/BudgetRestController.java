package site.easy.to.build.crm.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.service.budget.BudgetService;
import site.easy.to.build.crm.service.customer.CustomerService;
import site.easy.to.build.crm.util.AuthenticationUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class BudgetRestController {

    private final BudgetService budgetService;
    private final AuthenticationUtils authenticationUtils;
    private final CustomerService customerService;

    public BudgetRestController(BudgetService budgetService, AuthenticationUtils authenticationUtils, CustomerService customerService) {
        this.budgetService = budgetService;
        this.authenticationUtils = authenticationUtils;
        this.customerService = customerService;
    }

    @GetMapping("/customerBudget")
    public List<Budget> showCreatedCustomerBudget(Authentication authentication) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);

        List<Budget> budgets = budgetService.findByCustomerId(userId);

        return budgets;
    }

    @GetMapping("/budgetSumByCustomer")
    public Map<String, Double> getBudgetSumByCustomer() {
        // Récupérer tous les budgets
        List<Budget> budgets = budgetService.findAll();

        // Calculer la somme des budgets par client
        Map<String, Double> budgetSumByCustomer = new HashMap<>();
        for (Budget budget : budgets) {
            String customerName = budget.getCustomer().getName();
            double valeur = budget.getValeur();
            budgetSumByCustomer.put(customerName, budgetSumByCustomer.getOrDefault(customerName, 0.0) + valeur);
        }

        return budgetSumByCustomer;
    }
}