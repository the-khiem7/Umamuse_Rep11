package com.example.umamuse.models;

public class HorseBet {
    private Horse horse;
    private float odds;
    private int betAmount;

    public HorseBet(Horse horse, float odds) {
        this.horse = horse;
        this.odds = odds;
        this.betAmount = 0;
    }

    public Horse getHorse() {
        return horse;
    }

    public float getOdds() {
        return odds;
    }

    public void setOdds(float odds) {
        this.odds = odds;
    }

    public int getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(int betAmount) {
        this.betAmount = betAmount;
    }
    
    public float calculatePotentialWinnings() {
        return betAmount * odds;
    }
}