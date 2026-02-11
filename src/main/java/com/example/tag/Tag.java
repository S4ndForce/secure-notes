package com.example.tag;

import jakarta.persistence.*;

@Entity
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    protected Tag() {}
    public Tag(String name) { this.name = name.trim().toLowerCase(); ; }

    public String getName() { return name; }
    public Long getId() { return id; }
}