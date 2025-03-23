package site.easy.to.build.crm.service.budget;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.repository.BudgetRepository;

import java.util.List;
import java.util.Optional;

@Service
public class BudgetServiceImpl implements BudgetService {

    @Autowired
    private BudgetRepository budgetRepository; // Injection correcte du repository

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
}
