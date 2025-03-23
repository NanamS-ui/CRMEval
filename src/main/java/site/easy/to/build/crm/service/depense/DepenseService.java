package site.easy.to.build.crm.service.depense;

import java.util.List;
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
}
