package site.easy.to.build.crm.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.easy.to.build.crm.entity.Depense;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.Ticket;

import java.util.List;

public interface DepenseRepository extends JpaRepository<Depense, Integer> {
    Depense findByLead(Lead lead);
    Depense findByTicket(Ticket ticket);
    Depense findByDepenseId(int id);
    Depense findByTicketTicketId(int ticketId);
    Depense findByLeadLeadId(int leadId);

    @Query("SELECT COALESCE(SUM(d.valeurDepense), 0) " +
            "FROM Depense d " +
            "LEFT JOIN d.lead l " +
            "LEFT JOIN d.ticket t " +
            "LEFT JOIN l.customer c " +
            "LEFT JOIN t.customer c2 " +
            "WHERE (c.customerId = :customerId OR c2.customerId = :customerId) AND d.etat=1")
    public double getTotalDepenseByCustomerId(@Param("customerId") int customerId);

    @Query("SELECT d FROM Depense d WHERE d.ticket IS NOT NULL")
    List<Depense> findAllDepensesForTickets();

    @Query("SELECT d FROM Depense d WHERE d.lead IS NOT NULL")
    List<Depense> findAllDepensesForLeads();
    @Query("SELECT COALESCE(SUM(d.valeurDepense), 0) FROM Depense d WHERE d.ticket IS NOT NULL AND d.etat =1")
    double getTotalDepenseForTickets();

    @Query("SELECT COALESCE(SUM(d.valeurDepense), 0) FROM Depense d WHERE d.lead IS NOT NULL AND d.etat =1")
    double getTotalDepenseForLeads();

    @Modifying
    @Transactional
    @Query("DELETE FROM Depense d WHERE d.lead.leadId = :leadId AND d.depenseId = :depenseId")
    void deleteByLeadId(@Param("leadId") int leadId, @Param("depenseId") int depenseId);

    @Modifying
    @Transactional
    @Query("UPDATE Depense d SET d.valeurDepense = :valeurDepense WHERE d.depenseId = :depenseId")
    void updateById(@Param("depenseId") int depenseId, @Param("valeurDepense") double valeurDepense);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO depense (valeur_depense, date_depense, etat, lead_id) VALUES (:valeurDepense, :dateDepense, :etat, :leadId)", nativeQuery = true)
    void insertDepense(
            @Param("valeurDepense") Double valeurDepense,
            @Param("dateDepense") String dateDepense,
            @Param("etat") String etat,
            @Param("leadId") int leadId
    );

}
