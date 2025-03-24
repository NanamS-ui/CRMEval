package site.easy.to.build.crm.dto;

import site.easy.to.build.crm.entity.Depense;
import site.easy.to.build.crm.entity.Ticket;

public class TicketWithDepenseDTO {
    private Ticket ticket;
    private Depense depense;

    public TicketWithDepenseDTO(Ticket ticket, Depense depense) {
        this.ticket = ticket;
        this.depense = depense;
    }

    // Getters et Setters
    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public Depense getDepense() {
        return depense;
    }

    public void setDepense(Depense depense) {
        this.depense = depense;
    }
}
