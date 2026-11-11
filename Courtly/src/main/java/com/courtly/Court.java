package com.courtly;

import jakarta.persistence.*;

@Entity
@Table(name = "courts")
public class Court {
    @Id
    private int id;
    @Column(nullable = false)
    private String name;

    protected Court() {
    }

    public Court(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
