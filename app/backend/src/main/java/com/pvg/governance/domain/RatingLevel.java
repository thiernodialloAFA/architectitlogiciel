package com.pvg.governance.domain;

public enum RatingLevel {
    LOW(1), MEDIUM(2), HIGH(3);

    private final int score;

    RatingLevel(int score) {
        this.score = score;
    }

    public int score() {
        return score;
    }
}
