package com.ebbilling.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name="bills") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Bill {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,unique=true) private String billNumber;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="customer_id",nullable=false) private Customer customer;
    @Column(nullable=false) private Integer unitsConsumed;
    @Column(nullable=false,precision=10,scale=2) private BigDecimal unitCharges;
    @Column(nullable=false,precision=10,scale=2) private BigDecimal fixedCharges;
    @Column(precision=10,scale=2) private BigDecimal penaltyCharges;
    @Column(nullable=false,precision=10,scale=2) private BigDecimal totalAmount;
    @Column(nullable=false) private LocalDate billDate;
    @Column(nullable=false) private LocalDate dueDate;
    private LocalDate paymentDate;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private BillStatus status;
    private String billingMonth;
    private Integer billingYear;
    @Column(updatable=false) private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum BillStatus { PENDING, PAID, OVERDUE }

    @PrePersist protected void onCreate(){ createdAt=updatedAt=LocalDateTime.now(); }
    @PreUpdate  protected void onUpdate(){ updatedAt=LocalDateTime.now(); }
}
