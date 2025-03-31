package site.easy.to.build.crm.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.Lead;

import java.util.List;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Integer> {

    public Lead findByLeadId(int id);
    @Query("SELECT DISTINCT l.status FROM Lead l")
    List<String> findDistinctStatus();
    List<Lead> findByStatus(String status);
    public List<Lead> findByCustomerCustomerId(int customerId);
    public List<Lead> findByManagerId(int userId);

    public List<Lead> findByEmployeeId(int userId);

    Lead findByMeetingId(String meetingId);

    public List<Lead> findByEmployeeIdOrderByCreatedAtDesc(int employeeId, Pageable pageable);

    public List<Lead> findByManagerIdOrderByCreatedAtDesc(int managerId, Pageable pageable);

    public List<Lead> findByCustomerCustomerIdOrderByCreatedAtDesc(int customerId, Pageable pageable);

    long countByEmployeeId(int employeeId);

    long countByManagerId(int managerId);
    long countByCustomerCustomerId(int customerId);

    void deleteAllByCustomer(Customer customer);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO trigger_lead (name, status, user_id, customer_id, employee_id, created_at) " +
            "VALUES (:name, :status, :userId, :customerId, :employeeId, NOW())", nativeQuery = true)
    void saveLead(@Param("name") String name,
                  @Param("status") String status,
                  @Param("userId") int userId,
                  @Param("customerId") int customerId,
                  @Param("employeeId") int employeeId);
}
