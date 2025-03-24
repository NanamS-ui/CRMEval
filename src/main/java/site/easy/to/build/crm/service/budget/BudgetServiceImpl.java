package site.easy.to.build.crm.service.budget;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.Notification;
import site.easy.to.build.crm.repository.BudgetRepository;
import site.easy.to.build.crm.service.customer.CustomerService;
import site.easy.to.build.crm.service.depense.DepenseService;
import site.easy.to.build.crm.service.seuil.SeuilService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BudgetServiceImpl implements BudgetService {

    @Autowired
    private BudgetRepository budgetRepository; // Injection correcte du repository
    private final SeuilService seuilService;
    private final CustomerService customerService;
    private final DepenseService depenseService;

    public BudgetServiceImpl(SeuilService seuilService, CustomerService customerService, DepenseService depenseService) {
        this.seuilService = seuilService;
        this.customerService = customerService;
        this.depenseService = depenseService;
    }

    @Override
    public List<Budget> findAll() {
        return budgetRepository.findAll();
    }

    @Override
    public Optional<Budget> findById(int id) {
        return budgetRepository.findById(id);
    }

    @Override
    public Budget save(Budget budget) {
        return budgetRepository.save(budget);
    }

    @Override
    public void delete(int id) {
        budgetRepository.deleteById(id);
    }

    @Override
    public List<Budget> findByCustomerId(int customerId) {
        return budgetRepository.findByCustomerCustomerId(customerId);
    }

    @Override
    public double getTotalBudgetByCustomerId(int customerId) {
        return budgetRepository.getTotalBudgetByCustomerId(customerId);
    }

    public Notification checkBudget(int customerId,double newDepense){
        double totalDepense = depenseService.getTotalDepenseByCustomerId(customerId)+newDepense;
        System.out.println("total depense"+totalDepense);
        double seuil = seuilService.getSeuilActuel().getTaux();
        double budget = getTotalBudgetByCustomerId(customerId);

        double seuilBudget = budget * (seuil/100);
        LocalDateTime date= LocalDateTime.now();
        Customer cust= customerService.findByCustomerId(customerId);
        if( totalDepense > seuilBudget){
            return  new Notification("Le seuil du budget est dépassé",date,0,cust);
        }else if (totalDepense > budget){
            return new Notification("Le budget est dépassé",date,0,cust);
        }else if (totalDepense==seuilBudget) {
            return new Notification("le seuil du budget est atteint",date,1,cust);
        }else{
            return new Notification("successful", date, 1, cust);
        }
    }
}