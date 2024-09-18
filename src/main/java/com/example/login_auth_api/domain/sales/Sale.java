package com.example.login_auth_api.domain.sales;

import com.example.login_auth_api.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "sale_date", nullable = false)
    private LocalDateTime saleDate;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "observation")
    private String observation;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "discount")
    private BigDecimal discount;

    @Column(name = "subtotal")
    private BigDecimal subtotal;

    @Column(name = "is_gift")
    private Boolean isGift = false;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleItem> items = new ArrayList<>();

    public void addSaleItem(SaleItem saleItem) {
        items.add(saleItem);
        saleItem.setSale(this);
    }

    @PrePersist
    protected void onCreate() {
        totalAmount = BigDecimal.ZERO;
    }

}
