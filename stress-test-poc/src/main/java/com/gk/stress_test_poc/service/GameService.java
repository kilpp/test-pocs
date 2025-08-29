package com.gk.stress_test_poc.service;

import com.gk.stress_test_poc.repository.domain.DungeonGameEntity;
import com.gk.stress_test_poc.repository.domain.GameRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class GameService {

    private final DungeonGame dungeonGame;
    private final GameRepository repository;

    public GameService(DungeonGame dungeonGame, GameRepository repository) {
        this.dungeonGame = dungeonGame;
        this.repository = repository;
    }

    public List<DungeonGameBO> get() {
        return StreamSupport.stream(repository.findAll().spliterator(), false)
                .map(entity -> new DungeonGameBO(
                        entity.id(),
                        null,
                        entity.minimalHealth()
                ))
                .collect(Collectors.toList());
    }

    public DungeonGameBO create(DungeonGameBO bo) {
        Integer minimalHealth = dungeonGame.play(bo.params());
        DungeonGameEntity entity = repository.save(
                new DungeonGameEntity(
                        null,
                        minimalHealth,
                        OffsetDateTime.now())
        );
        return new DungeonGameBO(entity.id(), bo.params(), entity.minimalHealth());
    }
}
