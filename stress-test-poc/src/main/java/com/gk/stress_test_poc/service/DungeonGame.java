package com.gk.stress_test_poc.service;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DungeonGame {


    public Integer play(List<List<Integer>> params) {

        if (null == params || params.isEmpty() || params.getFirst().isEmpty()) {
            return 1;
        }

        int m = params.size();
        int n = params.getFirst().size();

        // dp[i][j] represents the minimum health needed to reach the princess from cell (i,j)
        int[][] dp = new int[m][n];

        // Start from the princess' cell (bottom-right)
        dp[m - 1][n - 1] = Math.max(1, 1 - params.get(m - 1).get(n - 1));

        // Fill the last column
        for (int i = m - 2; i >= 0; i--) {
            dp[i][n - 1] = Math.max(1, dp[i + 1][n - 1] - params.get(i).get(n - 1));
        }

        // Fill the last row
        for (int j = n - 2; j >= 0; j--) {
            dp[m - 1][j] = Math.max(1, dp[m - 1][j + 1] - params.get(m - 1).get(j));
        }

        // Fill the rest of the dp table
        for (int i = m - 2; i >= 0; i--) {
            for (int j = n - 2; j >= 0; j--) {
                int minHealthOnExit = Math.min(dp[i + 1][j], dp[i][j + 1]);
                dp[i][j] = Math.max(1, minHealthOnExit - params.get(i).get(j));
            }
        }

        return dp[0][0];
    }
}