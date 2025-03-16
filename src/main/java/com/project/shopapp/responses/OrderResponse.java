package com.project.shopapp.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@AllArgsConstructor

@Builder
@NoArgsConstructor
public class OrderResponse extends BaseResponse{
    private Long id;
    @JsonProperty("user_id")
    private Long userId;
    @JsonProperty("fullname")
    private String fullName;
    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("address")
    private String address;


    private String note;

    @JsonProperty("order_date")
    private LocalDateTime orderDate;


    private String status;

    @Column(name = "total_money")
    private Float totalMoney;

    @Column(name = "shipping_method")
    private String shippingMethod;

    @Column(name = "shipping_address")
    private String shippingAddress;

    @Column(name = "shipping_date")
    private Date shippingDate;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "active")
    private Boolean active;
}
