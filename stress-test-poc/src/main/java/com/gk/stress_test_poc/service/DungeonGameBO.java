package com.gk.stress_test_poc.service;

import java.util.List;

public record DungeonGameBO(String id, List<List<Integer>> params, Integer minimalHealth) {
}
