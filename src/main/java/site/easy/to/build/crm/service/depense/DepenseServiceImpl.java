package site.easy.to.build.crm.service.depense;

import java.util.List;
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
    public void deleteDepense(Integer id) {
        depenseRepository.deleteById(id);
    }


    @Override
    public Depense findByLead(Lead leadId) {
        return depenseRepository.findByLead(leadId); // Recherche de la dépense par lead_id
    }

    @Override
    public Depense findByTicket(Ticket ticket) {
        return depenseRepository.findByTicket(ticket); // Recherche de la dépense par lead_id
    }

}