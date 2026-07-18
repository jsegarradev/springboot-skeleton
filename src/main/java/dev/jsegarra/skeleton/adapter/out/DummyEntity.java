package dev.jsegarra.skeleton.adapter.out;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity for the Liquibase-owned {@code dummy} table. The reserved word {@code value} maps to the
 * {@code dummy_value} column (springboot.md §7).
 */
@Entity
@Table(name = "dummy")
@Getter
@Setter
@NoArgsConstructor
public class DummyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dummy_value", nullable = false)
    private String value;
}
