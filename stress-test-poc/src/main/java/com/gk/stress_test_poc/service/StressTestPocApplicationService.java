package com.example.restapi.service;


@Service
public class StressTestPocApplicationService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    private final StressTestPocApplicationRepository repository;    


    public StressTestPocApplicationService(UserStressTestPocApplicationRepositoryService repository) {
        this.repository = repository;
    }
    
    public StressTestPocGameBO create(StressTestPocGameBO bo) {
        logger.info("Creating new game: {}", );
        
        User user = repository.createUser(request);
        UserDto.Response response = new UserDto.Response(user);
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}