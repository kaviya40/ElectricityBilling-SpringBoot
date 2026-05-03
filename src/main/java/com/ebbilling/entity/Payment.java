package com.ebbilling.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name="payments") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,unique=true) private String transactionId;
    @OneToOne(fetch=FetchType.LAZY) @JoinColumn(name="bill_id",nullable=false) private Bill bill;
    @Column(nullable=false,precision=10,scale=2) private BigDecimal amountPaid;
    @Enumerated(EnumType.STRING) private PaymentMode paymentMode;
    private LocalDateTime paymentDateTime;
    private String remarks;
    @Column(updatable=false) private LocalDateTime createdAt;

    public enum PaymentMode { CASH, ONLINE, UPI, CARD }

    @PrePersist protected void onCreate(){ createdAt=LocalDateTime.now(); if(paymentDateTime==null) paymentDateTime=LocalDateTime.now(); }
}
