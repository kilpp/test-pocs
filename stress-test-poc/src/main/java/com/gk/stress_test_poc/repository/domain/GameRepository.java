package com.gk.stress_test_poc.repository.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends CrudRepository<DungeonGameEntity, String> {


}
