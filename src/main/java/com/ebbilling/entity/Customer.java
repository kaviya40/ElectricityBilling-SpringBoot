package com.ebbilling.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity @Table(name="customers") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Customer {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String name;
    @Column(nullable=false,unique=true) private String meterNumber;
    @Column(nullable=false) private String address;
    private String phone;
    private String email;
    @Enumerated(EnumType.STRING) private ConnectionType connectionType;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id") private User user;
    @OneToMany(mappedBy="customer",cascade=CascadeType.ALL,fetch=FetchType.LAZY) private List<Bill> bills;
    @Column(updatable=false) private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum ConnectionType { DOMESTIC, COMMERCIAL, INDUSTRIAL }

    @PrePersist protected void onCreate(){ createdAt=updatedAt=LocalDateTime.now(); }
    @PreUpdate  protected void onUpdate(){ updatedAt=LocalDateTime.now(); }
}
