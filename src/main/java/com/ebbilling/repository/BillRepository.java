package com.ebbilling.repository;

import com.ebbilling.entity.Bill;
import com.ebbilling.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    Optional<Bill> findByBillNumber(String billNumber);
    List<Bill> findByCustomerOrderByBillDateDesc(Customer customer);
    List<Bill> findByStatus(Bill.BillStatus status);
    long countByStatus(Bill.BillStatus status);

    @Query("SELECT COALESCE(SUM(b.totalAmount),0) FROM Bill b WHERE b.status='PAID'")
    BigDecimal getTotalRevenue();

    @Query("SELECT b FROM Bill b WHERE b.customer.id=:cid ORDER BY b.billDate DESC")
    List<Bill> findByCustomerId(@Param("cid") Long cid);

    boolean existsByCustomerAndBillingMonthAndBillingYear(Customer c, String m, Integer y);

    @Query("SELECT b.billingMonth, b.billingYear, SUM(b.totalAmount) FROM Bill b WHERE b.status='PAID' GROUP BY b.billingYear, b.billingMonth ORDER BY b.billingYear DESC, b.billingMonth DESC")
    List<Object[]> getMonthlyRevenue();

    @Query("SELECT b.billingMonth, b.billingYear, SUM(b.unitsConsumed) FROM Bill b WHERE b.customer.id=:cid GROUP BY b.billingYear, b.billingMonth ORDER BY b.billingYear DESC, b.billingMonth DESC")
    List<Object[]> getMonthlyUsage(@Param("cid") Long cid);
}
