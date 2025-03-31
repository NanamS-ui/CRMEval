package site.easy.to.build.crm.service.customer;

import org.checkerframework.checker.units.qual.C;
import org.springframework.web.multipart.MultipartFile;
import site.easy.to.build.crm.entity.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface CustomerService {

    public Customer findByCustomerId(int customerId);

    public List<Customer> findByUserId(int userId);

    public Customer findByEmail(String email);

    public List<Customer> findAll();

    public Customer save(Customer customer);

    public void delete(Customer customer);

    public List<Customer> getRecentCustomers(int userId, int limit);

    long countByUserId(int userId);

    public String generateCsv(Customer customer, List<Lead> leads, List<Ticket> tickets,
                              Map<Integer, Depense> depenseParLead,
                              Map<Integer, Depense> depenseParTicket);

    public Customer saveCustomer(String name, String country, String email, int idUser);

    public String generateCustomerCSV(Customer customer, List<Lead>leads, List<Ticket>tickets, List<Depense> depenses);
    public void duplicateCustomer(int idCustomer) throws IOException;
    public Customer importCustomerFromCSV(MultipartFile file, String separateur) throws IOException;

}
