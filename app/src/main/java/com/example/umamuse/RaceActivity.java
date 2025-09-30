package com.example.umamuse;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.umamuse.models.Horse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RaceActivity extends AppCompatActivity {

    private ImageView bgImage1, bgImage2;
    private View finishLine;
    private Button btnStart;
    private FrameLayout lane1, lane2, lane3;

    private List<Horse> allHorses;
    private List<Horse> raceHorses;
    private List<ImageView> horseImages;
    private List<SeekBar> horseSeekBars;

    private Handler handler = new Handler();
    private Random random = new Random();
    private int trackWidth;
    private boolean raceFinished = false;
    private boolean finishLineVisible = false;

    private int lapCount = 0;
    private final int TOTAL_LAPS = 5;
    private float totalDistance = 0;
    private float lapDistance = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_race);

        bgImage1 = findViewById(R.id.bgImage1);
        bgImage2 = findViewById(R.id.bgImage2);
        finishLine = findViewById(R.id.finishLine);
        btnStart = findViewById(R.id.btnStart);

        lane1 = findViewById(R.id.lane1);
        lane2 = findViewById(R.id.lane2);
        lane3 = findViewById(R.id.lane3);

        horseImages = new ArrayList<>();
        horseSeekBars = new ArrayList<>();

        initHorseList();

        btnStart.setOnClickListener(v -> {
            prepareRace();
            startRace();
        });
    }

    private void initHorseList() {
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

    private void prepareRace() {
        raceFinished = false;
        finishLineVisible = false;
        finishLine.setVisibility(View.GONE);
        lapCount = 0;
        totalDistance = 0;

        lane1.removeAllViews();
        lane2.removeAllViews();
        lane3.removeAllViews();
        horseImages.clear();
        horseSeekBars.clear();

        Collections.shuffle(allHorses);
        raceHorses = new ArrayList<>(allHorses.subList(0, 3));

        addHorseToLane(raceHorses.get(0), lane1);
        addHorseToLane(raceHorses.get(1), lane2);
        addHorseToLane(raceHorses.get(2), lane3);

        lane1.post(() -> {
            trackWidth = lane1.getWidth();
            lapDistance = trackWidth * 2;

            for (int i = 0; i < horseSeekBars.size(); i++) {
                SeekBar sb = horseSeekBars.get(i);
                ImageView iv = horseImages.get(i);
                sb.setMax(trackWidth - iv.getWidth());
                iv.setTranslationX(0);
                Horse horse = raceHorses.get(i);
                horse.setSpeed(5 + random.nextInt(5));
                horse.setFinished(false);
                horse.setForward(true);
                horse.setLastDirectionChange(System.currentTimeMillis());
                horse.setDirectionInterval(3000 + random.nextInt(1000));
            }
        });
    }

    private void addHorseToLane(Horse horse, FrameLayout lane) {
        ImageView iv = new ImageView(this);
        iv.setImageResource(horse.getImageRes());
        iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        iv.setLayoutParams(params);
        lane.addView(iv);
        horseImages.add(iv);

        SeekBar sb = new SeekBar(this);
        sb.setMax(0);
        sb.setProgress(0);
        sb.setThumb(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        sb.setProgressDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        sb.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                0));
        lane.addView(sb);
        horseSeekBars.add(sb);
    }

    private void startRace() {
        btnStart.setEnabled(false);

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (raceFinished) {
                    btnStart.setEnabled(true);
                    return;
                }

                boolean allFinished = true;
                boolean showFinishLine = lapCount >= TOTAL_LAPS - 1;
                if (showFinishLine && !finishLineVisible) {
                    finishLineVisible = true;
                    finishLine.setVisibility(View.VISIBLE);
                }

                Horse winner = null;
                long now = System.currentTimeMillis();

                for (int i = 0; i < horseImages.size(); i++) {
                    Horse horse = raceHorses.get(i);
                    ImageView iv = horseImages.get(i);
                    SeekBar sb = horseSeekBars.get(i);

                    if (!horse.isFinished()) {
                        allFinished = false;

                        // Đổi hướng random mỗi 3-4s
                        if (now - horse.getLastDirectionChange() >= horse.getDirectionInterval()) {
                            horse.setForward(random.nextBoolean());
                            horse.setLastDirectionChange(now);
                            horse.setDirectionInterval(3000 + random.nextInt(1000));
                        }

                        int speedFluctuation = random.nextInt(3) - 1; // -1..1
                        int delta = (int)(horse.getSpeed() + speedFluctuation);
                        int newPos;
                        if (horse.isForward()) {
                            newPos = Math.min(sb.getMax(), sb.getProgress() + delta);
                        } else {
                            newPos = Math.max(0, sb.getProgress() - delta);
                        }

                        sb.setProgress(newPos);
                        iv.setTranslationX(newPos);

                        // Kiểm tra finish line
                        if (finishLineVisible && newPos >= sb.getMax()) {
                            horse.setFinished(true);
                            if (!raceFinished) {
                                raceFinished = true;
                                winner = horse;
                            }
                        }
                    }
                }

                if (winner != null) {
                    Toast.makeText(RaceActivity.this,
                            winner.getName() + " đã chiến thắng!", Toast.LENGTH_LONG).show();
                }

                if (!allFinished) {
                    handler.postDelayed(this, 16);
                } else if (lapCount < TOTAL_LAPS - 1) {
                    lapCount++;
                    for (int i = 0; i < horseImages.size(); i++) {
                        horseSeekBars.get(i).setProgress(0);
                        horseImages.get(i).setTranslationX(0);
                        Horse horse = raceHorses.get(i);
                        horse.setSpeed(5 + random.nextInt(5));
                        horse.setFinished(false);
                        horse.setForward(true);
                        horse.setLastDirectionChange(System.currentTimeMillis());
                        horse.setDirectionInterval(3000 + random.nextInt(1000));
                    }
                    raceFinished = false;
                    handler.postDelayed(this, 16);
                } else {
                    btnStart.setEnabled(true);
                }
            }
        });

        startBackground();
    }

    private void startBackground() {
        lane1.post(() -> {
            final int bgWidth = bgImage1.getWidth();
            bgImage2.setTranslationX(bgWidth);

            Runnable bgRunnable = new Runnable() {
                @Override
                public void run() {
                    if (raceFinished && lapCount >= TOTAL_LAPS - 1) return;

                    float speed = 5f;
                    totalDistance += speed;
                    if (lapDistance > 0) {
                        lapCount = (int)(totalDistance / lapDistance);
                    }

                    float x1 = bgImage1.getTranslationX() - speed;
                    float x2 = bgImage2.getTranslationX() - speed;

                    bgImage1.setTranslationX(x1);
                    bgImage2.setTranslationX(x2);

                    if (x1 <= -bgWidth) {
                        bgImage1.setTranslationX(x2 + bgWidth);
                    }
                    if (x2 <= -bgWidth) {
                        bgImage2.setTranslationX(x1 + bgWidth);
                    }

                    if (lapCount < TOTAL_LAPS) {
                        handler.postDelayed(this, 16);
                    } else if (!finishLineVisible) {
                        finishLineVisible = true;
                        finishLine.setVisibility(View.VISIBLE);
                        handler.postDelayed(this, 16);
                    }
                }
            };
            handler.post(bgRunnable);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
    }
}
