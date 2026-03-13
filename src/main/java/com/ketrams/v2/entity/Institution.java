package com.ketrams.v2.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "institution")
@Data
@NoArgsConstructor
public class Institution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String category;

    @ManyToOne
    @JoinColumn(name = "sub_county_id")
    private SubCounty subCounty;

    @ManyToOne
    @JoinColumn(name = "ward_id")
    private Ward ward;

    private boolean enabled = true; // NEW: soft delete flag

    @OneToMany(mappedBy = "institution")
    @JsonIgnore
    private List<AppUser> users = new ArrayList<>();
}