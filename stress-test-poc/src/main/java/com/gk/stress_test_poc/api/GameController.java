package com.gk.stress_test_poc.api;

import com.gk.stress_test_poc.service.DungeonGame;
import com.gk.stress_test_poc.service.DungeonGameBO;
import com.gk.stress_test_poc.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/games")
public class GameController {

    private final GameService service;

    public GameController(GameService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<DungeonGameResponseDTO>> get() {
        return ResponseEntity
                .status(200)
                .body(service.get().stream().map(bo ->
                        new DungeonGameResponseDTO(
                        bo.id(),
                        bo.minimalHealth()
                )).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<DungeonGameResponseDTO> create(@RequestBody DungeonGameRequestDTO input) {
        DungeonGameBO response = service.create(new DungeonGameBO(null, input.params(), null));
        return ResponseEntity
                .status(201)
                .body(
                        new DungeonGameResponseDTO(
                                response.id(),
                                response.minimalHealth()
                        )
                );
    }
}
