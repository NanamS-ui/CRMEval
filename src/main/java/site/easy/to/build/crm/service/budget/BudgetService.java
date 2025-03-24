package site.easy.to.build.crm.service.budget;

import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.Notification;

import java.util.List;
import java.util.Optional;

public interface BudgetService {
    List<Budget> findAll();
    Optional<Budget> findById(int id);
    Budget save(Budget budget);
    void delete(int id);
    List<Budget> findByCustomerId(int customerId);
    double getTotalBudgetByCustomerId(int customerId);
    public Notification checkBudget(int customerId, double newDepense);

}
