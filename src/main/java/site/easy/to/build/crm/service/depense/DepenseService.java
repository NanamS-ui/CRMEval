package site.easy.to.build.crm.service.depense;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import site.easy.to.build.crm.entity.Depense;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.Ticket;

public interface DepenseService {
    List<Depense> getAllDepenses();
    Optional<Depense> getDepenseById(Integer id);
    Depense saveDepense(Depense depense);
    void deleteDepense(Integer id);
    Depense findByLead(Lead id);
    Depense findByTicket(Ticket ticket);
    public Depense updateDepense(Integer id, Depense updatedDepense);
    double getTotalDepenseByCustomerId(int customerId);
    public void updateDepenseValue(int depenseId, double valeurDepense);
    public Map<Integer, Depense> getDepensesParLead(List<Lead> leads);
    public Map<Integer, Depense> getDepensesParTicket(List<Ticket> tickets);
    public void saveDepense2(Double valeurDepense, String dateDepense, String etat, int leadId);
    public Depense saveDepense(Map<String, Object> depenseData);
}
