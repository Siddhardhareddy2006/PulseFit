package com.pulsefit.subscriptionservice.entity;
import jakarta.persistence.*;
@Entity public class Plan { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id; @Column(nullable=false) public String name; public String description; public int durationMonths; public double price; public String facilityAccess; public boolean active=true; public Plan(){} }
