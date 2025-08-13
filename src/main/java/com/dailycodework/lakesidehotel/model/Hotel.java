package com.dailycodework.lakesidehotel.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Basic Info
    private String name;
    private String address;

    // Contact & Online Presence
    private String contact;
    private String email;
    private String website;
    private Integer roomsCount;

    @Column(length = 2000)
    private String description;
    private String imageUrl;

    // Coordinates for K-D Tree
    private Double latitude;
    private Double longitude;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Room> rooms;

}