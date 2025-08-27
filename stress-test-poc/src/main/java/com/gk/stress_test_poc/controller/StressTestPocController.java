package com.example.restapi.controller;

@RestController
@RequestMapping("/api/v1/games")
@Tag(name = "Game Controller", description = "APIs for managing games")
public class StressTestPocController {
    
    private static final Logger logger = LoggerFactory.getLogger(StressTestPocController.class);
    
    private final StressTestPocApplicationService service;    

    public StressTestPocController(StressTestPocApplicationService service) {
        this.service = service;
    }
    
    @PostMapping
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Game created successfully"),
    })
    public ResponseEntity<> create(@Valid @RequestBody StressTestPocGameDTO request) {
        logger.info("Creating new game: {}", );
        
        StressTestPocGameDTO game = service.create(request);
        
        return new ResponseEntity<>(game, HttpStatus.CREATED);
    }
}