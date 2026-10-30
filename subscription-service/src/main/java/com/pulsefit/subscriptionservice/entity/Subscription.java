package com.pulsefit.subscriptionservice.entity;
import jakarta.persistence.*; import java.time.LocalDate;
@Entity public class Subscription { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; public Long memberId; public Long planId; public LocalDate startDate; public LocalDate endDate; public String status; public boolean autoRenew; public Subscription(){} }
