package com.gk.stress_test_poc.repository.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;


@Table(name = "dungeon_game")
public record DungeonGameEntity(
        @Id
        @Column(value = "id")
        String id,
        @Column(value = "minimal_health")
        Integer minimalHealth,
        @Column(value = "execution_time")
        OffsetDateTime executionTime) {
}