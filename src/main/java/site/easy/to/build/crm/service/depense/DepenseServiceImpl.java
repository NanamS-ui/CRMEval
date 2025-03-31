package site.easy.to.build.crm.service.depense;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import site.easy.to.build.crm.entity.Depense;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.repository.DepenseRepository;

@Service
public class DepenseServiceImpl implements DepenseService {
    private final DepenseRepository depenseRepository;

    @Autowired // Injection de la dépendance
    public DepenseServiceImpl(DepenseRepository depenseRepository) {
        this.depenseRepository = depenseRepository;
    }
    @Override
    public List<Depense> getAllDepenses() {
        return depenseRepository.findAll();
    }

    @Override
    public Optional<Depense> getDepenseById(Integer id) {
        return depenseRepository.findById(id);
    }

    @Override
    public Depense saveDepense(Depense depense) {
        return depenseRepository.save(depense);
    }

    @Override
    public void saveDepense2(Double valeurDepense, String dateDepense, String etat, int leadId) {
        depenseRepository.insertDepense(valeurDepense, dateDepense, etat, leadId);
    }

    @Override
    public void deleteDepense(Integer id) {
        depenseRepository.deleteById(id);
    }

    @Override
    public Depense updateDepense(Integer id, Depense updatedDepense) {
        return depenseRepository.findById(id)
                .map(existingDepense -> {
                    existingDepense.setValeurDepense(updatedDepense.getValeurDepense());
                    existingDepense.setEtat(updatedDepense.getEtat());
                    existingDepense.setLead(updatedDepense.getLead());
                    existingDepense.setTicket(updatedDepense.getTicket());
                    return depenseRepository.save(existingDepense);
                })
                .orElseThrow(() -> new RuntimeException("Dépense non trouvée avec l'ID : " + id));
    }


    @Override
    public Depense findByLead(Lead leadId) {
        return depenseRepository.findByLead(leadId); // Recherche de la dépense par lead_id
    }

    @Override
    public Depense findByTicket(Ticket ticket) {
        return depenseRepository.findByTicket(ticket); // Recherche de la dépense par lead_id
    }

    @Override
    public double getTotalDepenseByCustomerId(int customerId) {
        return depenseRepository.getTotalDepenseByCustomerId(customerId);
    }

    @Override
    public void updateDepenseValue(int depenseId, double valeurDepense) {
        depenseRepository.updateById(depenseId, valeurDepense);
    }

    @Override
    public Map<Integer, Depense> getDepensesParLead(List<Lead> leads) {
        Map<Integer, Depense> depensesParLead = new HashMap<>();

        for (Lead lead : leads) {
            // Récupérer la dépense associée à ce Lead
            Depense depense = depenseRepository.findByLead(lead);
            if (depense != null) {
                // Si une dépense existe, on l'ajoute
                depensesParLead.put(lead.getLeadId(), depense);
            }
        }

        return depensesParLead;
    }

    // Récupère la dépense associée à chaque Ticket
    public Map<Integer, Depense> getDepensesParTicket(List<Ticket> tickets) {
        Map<Integer, Depense> depensesParTicket = new HashMap<>();

        for (Ticket ticket : tickets) {
            // Récupérer la dépense associée à ce Ticket
            Depense depense = depenseRepository.findByTicket(ticket);
            if (depense != null) {
                // Si une dépense existe, on l'ajoute
                depensesParTicket.put(ticket.getTicketId(), depense);
            }
        }

        return depensesParTicket;
    }

    @Override
    public Depense saveDepense(Map<String, Object> depenseData) {
        Double valeurDepense = (Double) depenseData.get("valeur_depense");
        String dateDepense = (String) depenseData.get("date_depense");
        int etat = (int)depenseData.get("etat");
        int leadId = (int)depenseData.get("lead_id");

        Depense depense = new Depense();
        depense.setValeurDepense(valeurDepense);
        depense.setDateDepense(LocalDateTime.parse(dateDepense));
        depense.setEtat(etat);

        Lead lead = new Lead();
        lead.setLeadId(leadId);
        depense.setLead(lead);

        return depenseRepository.save(depense);
    }
}