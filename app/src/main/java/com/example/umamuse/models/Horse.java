package com.example.umamuse.models;

public class Horse {
    private int id;
    private String name;
    private int imageRes;

    private float speed;
    private boolean finished;

    // Mới: hướng chạy random
    private boolean forward = true; // true = chạy xuôi, false = chạy ngược
    private long lastDirectionChange = 0;
    private long directionInterval = 3000 + (long)(Math.random() * 1000); // 3-4s

    public Horse(int id, String name, int imageRes) {
        this.id = id;
        this.name = name;
        this.imageRes = imageRes;
        this.speed = 5f + (float) Math.random() * 4f; // 5-9 px/frame
        this.finished = false;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getImageRes() { return imageRes; }

    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed = speed; }

    public boolean isFinished() { return finished; }
    public void setFinished(boolean finished) { this.finished = finished; }

    public boolean isForward() { return forward; }
    public void setForward(boolean forward) { this.forward = forward; }

    public long getLastDirectionChange() { return lastDirectionChange; }
    public void setLastDirectionChange(long lastDirectionChange) { this.lastDirectionChange = lastDirectionChange; }

    public long getDirectionInterval() { return directionInterval; }
    public void setDirectionInterval(long directionInterval) { this.directionInterval = directionInterval; }
}
