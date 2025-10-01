package com.example.umamuse.repositories;

import com.example.umamuse.R;
import com.example.umamuse.models.Horse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class HorseRepository {
    private static List<Horse> allHorses;
    private static List<Horse> currentRaceHorses = new ArrayList<>();
    private static final Random random = new Random();

    // Danh sách full ngựa trong game
    public static List<Horse> getAllHorses() {
        if (allHorses == null) {
            allHorses = new ArrayList<>();
            allHorses.add(new Horse(1, "Smart Falcon", R.drawable.h1));
            allHorses.add(new Horse(2, "Narita Taishin", R.drawable.h2));
            allHorses.add(new Horse(3, "Daiwa Scarlet", R.drawable.h3));
            allHorses.add(new Horse(4, "Tokai Teio", R.drawable.h4));
            allHorses.add(new Horse(5, "Gold Ship", R.drawable.h5));
            allHorses.add(new Horse(6, "Mejiro McQueen", R.drawable.h6));
            allHorses.add(new Horse(7, "Special Week", R.drawable.h7));
            allHorses.add(new Horse(8, "Silence Suzuka", R.drawable.h8));
            allHorses.add(new Horse(9, "T.M. Opera O", R.drawable.h9));
        }
        return allHorses;
    }

    // Chuẩn bị 1 lineup mới (random)
    public static void prepareNewRace(int count) {
        List<Horse> horses = new ArrayList<>(getAllHorses());
        Collections.shuffle(horses, random);

        currentRaceHorses.clear();
        for (int i = 0; i < count && i < horses.size(); i++) {
            currentRaceHorses.add(horses.get(i));
        }
    }

    // Lấy lineup hiện tại (cho Race hoặc Bet)
    public static List<Horse> getCurrentRaceHorses() {
        return new ArrayList<>(currentRaceHorses); // copy để tránh modify ngoài repo
    }

    // Kiểm tra lineup có chưa
    public static boolean hasCurrentRace() {
        return currentRaceHorses != null && !currentRaceHorses.isEmpty();
    }

    public static void setCurrentRaceHorses(List<Horse> horses) {
        if (horses == null) {
            return;
        }
        
        if (currentRaceHorses == null) {
            currentRaceHorses = new ArrayList<>();
        } else {
            currentRaceHorses.clear();
        }
        
        currentRaceHorses.addAll(horses);
    }
}
