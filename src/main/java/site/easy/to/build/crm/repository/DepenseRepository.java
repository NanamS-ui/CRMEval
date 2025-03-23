package site.easy.to.build.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.easy.to.build.crm.entity.Depense;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.Ticket;

public interface DepenseRepository extends JpaRepository<Depense, Integer> {
    Depense findByLead(Lead lead);
    Depense findByTicket(Ticket ticket);
}
