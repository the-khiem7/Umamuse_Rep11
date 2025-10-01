package com.example.umamuse.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPreferences {
    private static final String PREF_NAME = "user_preferences";
    private static final String KEY_USER_BALANCE = "user_balance";
    private static final int DEFAULT_BALANCE = 1000; // Default starting balance: $1000

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static int getUserBalance(Context context) {
        return getPreferences(context).getInt(KEY_USER_BALANCE, DEFAULT_BALANCE);
    }

    public static void setUserBalance(Context context, int balance) {
        getPreferences(context).edit().putInt(KEY_USER_BALANCE, balance).apply();
    }

    public static void addToUserBalance(Context context, int amount) {
        int currentBalance = getUserBalance(context);
        setUserBalance(context, currentBalance + amount);
    }

    public static void subtractFromUserBalance(Context context, int amount) {
        int currentBalance = getUserBalance(context);
        setUserBalance(context, Math.max(0, currentBalance - amount)); // Prevent negative balance
    }

    // Reset user balance to default value
    public static void resetUserBalance(Context context) {
        setUserBalance(context, DEFAULT_BALANCE);
    }
}