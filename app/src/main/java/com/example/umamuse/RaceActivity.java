package com.example.umamuse;

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
import com.example.umamuse.repositories.HorseRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RaceActivity extends AppCompatActivity {

    private ImageView bgImage1, bgImage2;
    private View finishLine;
    private Button btnPrepare, btnStart;
    private FrameLayout lane1, lane2, lane3, lane4;

    private List<Horse> raceHorses;
    private List<ImageView> horseImages;
    private List<SeekBar> horseSeekBars;

    private Handler handler = new Handler();
    private Random random = new Random();
    private int trackWidth;

    private int lapCount = 0;
    private final int TOTAL_LAPS = 5;
    private float totalDistance = 0;
    private float lapDistance = 0;

    private boolean raceRunning = false;
    private Horse winnerHorse = null;
    private float[] horseYOffsets;

    private Runnable raceRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_race);

        bgImage1 = findViewById(R.id.bgImage1);
        bgImage2 = findViewById(R.id.bgImage2);
        finishLine = findViewById(R.id.finishLine);
        btnPrepare = findViewById(R.id.btnNewRace);
        btnStart = findViewById(R.id.btnStart);

        lane1 = findViewById(R.id.lane1);
        lane2 = findViewById(R.id.lane2);
        lane3 = findViewById(R.id.lane3);
        lane4 = findViewById(R.id.lane4);

        horseImages = new ArrayList<>();
        horseSeekBars = new ArrayList<>();

        btnPrepare.setText("Prepare Race");
        btnStart.setText("Start Race");
        btnStart.setEnabled(false);

        btnPrepare.setOnClickListener(v -> prepareRace());
        btnStart.setOnClickListener(v -> startRace());
    }

    private void prepareRace() {
        // --- Chọn ngựa random ---
        List<Horse> allHorses = HorseRepository.getAllHorses();
        Collections.shuffle(allHorses);
        raceHorses = new ArrayList<>(allHorses.subList(0, 4));
        HorseRepository.setCurrentRaceHorses(raceHorses);

        horseImages.clear();
        horseSeekBars.clear();
        lane1.removeAllViews();
        lane2.removeAllViews();
        lane3.removeAllViews();
        lane4.removeAllViews();

        addHorseToLane(raceHorses.get(0), lane1);
        addHorseToLane(raceHorses.get(1), lane2);
        addHorseToLane(raceHorses.get(2), lane3);
        addHorseToLane(raceHorses.get(3), lane4);

        horseYOffsets = new float[raceHorses.size()];

        // Reset background
        bgImage1.setTranslationX(0);
        bgImage2.setTranslationX(bgImage1.getWidth());
        finishLine.setVisibility(View.GONE);

        // Reset trạng thái race
        raceRunning = false;
        lapCount = 0;
        totalDistance = 0;
        winnerHorse = null;

        // Enable Start
        btnStart.setEnabled(true);
        btnPrepare.setEnabled(false);
    }

    private void startRace() {
        raceRunning = true;
        btnStart.setEnabled(false);
        btnPrepare.setEnabled(false);

        // Đảm bảo trackWidth đã đo xong
        lane1.post(() -> {
            trackWidth = lane1.getWidth();
            lapDistance = trackWidth * 2;

            // Khởi tạo speed và SeekBar max
            for (int i = 0; i < raceHorses.size(); i++) {
                Horse h = raceHorses.get(i);
                h.setSpeed(2 + random.nextInt(3));
                h.setFinished(false);
                h.setForward(true);
                h.setLastDirectionChange(System.currentTimeMillis());
                h.setDirectionInterval(2000 + random.nextInt(1000));

                SeekBar sb = horseSeekBars.get(i);
                ImageView iv = horseImages.get(i);
                sb.setMax(trackWidth - iv.getWidth());
                sb.setProgress(0);
                iv.setTranslationX(0);
            }

            startRaceRunnable(); // chạy Runnable sau khi trackWidth có giá trị
        });
    }

    private void startRaceRunnable() {
        raceRunnable = new Runnable() {
            @Override
            public void run() {
                if (!raceRunning) return;

                long now = System.currentTimeMillis();
                boolean allFinished = true;
                boolean showFinishLine = lapCount >= TOTAL_LAPS - 1;

                // Hiển thị finish line khi gần lap cuối
                if (showFinishLine && finishLine.getVisibility() == View.GONE) {
                    finishLine.setVisibility(View.VISIBLE);
                    if (winnerHorse == null)
                        winnerHorse = raceHorses.get(random.nextInt(raceHorses.size()));
                }

                for (int i = 0; i < raceHorses.size(); i++) {
                    Horse horse = raceHorses.get(i);
                    ImageView iv = horseImages.get(i);
                    SeekBar sb = horseSeekBars.get(i);

                    if (!horse.isFinished()) {
                        allFinished = false;

                        int delta = (int) horse.getSpeed() + random.nextInt(3) - 1;
                        int newPos = sb.getProgress();

                        // --- Smooth movement logic ---
                        if (!showFinishLine) {
                            // Lap > 0 thì ngẫu nhiên nhích qua trái/phải nhưng smooth
                            if (lapCount > 0) {
                                int mid = (int)(sb.getMax() * 0.6);
                                int buf = (int)(sb.getMax() * 0.2);

                                // Nếu gần biên trái/phải → giảm tốc và hướng forward
                                if (newPos < mid - buf) horse.setForward(true);
                                else if (newPos > mid + buf) horse.setForward(false);

                                if (newPos >= sb.getMax() - 5) delta = Math.min(delta, 2);
                                if (newPos <= 5) delta = Math.min(delta, 2);

                                newPos += horse.isForward() ? delta : -delta;
                                newPos = Math.max(0, Math.min(sb.getMax(), newPos));
                            } else {
                                // Lap 0 → đi thẳng
                                newPos = Math.min(sb.getMax(), newPos + delta);
                            }
                        } else {
                            // Finish line logic
                            if (horse == winnerHorse) {
                                newPos = Math.min(sb.getMax(), newPos + delta + 3);
                                if (newPos >= sb.getMax()) {
                                    horse.setFinished(true);
                                    if (raceRunning) {
                                        raceRunning = false;
                                        Toast.makeText(RaceActivity.this,
                                                horse.getName() + " đã chiến thắng!", Toast.LENGTH_LONG).show();
                                        btnPrepare.setEnabled(true);
                                    }
                                }
                            } else {
                                // Những ngựa còn lại đi chậm hơn
                                newPos = Math.min((int)(sb.getMax() * 0.9), newPos + delta - 1);
                            }
                        }

                        sb.setProgress(newPos);
                        iv.setTranslationX(newPos);

                        // --- Xóc vó ngựa ---
                        horseYOffsets[i] = (float) (Math.sin(System.currentTimeMillis()*0.01 + i)*5);
                        iv.setTranslationY(horseYOffsets[i]);
                    }
                }

                // --- Update background ---
                float speed = 10f;
                totalDistance += speed;
                if (lapDistance > 0) lapCount = (int)(totalDistance / lapDistance);

                float x1 = bgImage1.getTranslationX() - speed;
                float x2 = bgImage2.getTranslationX() - speed;
                bgImage1.setTranslationX(x1);
                bgImage2.setTranslationX(x2);

                if (x1 <= -bgImage1.getWidth()) bgImage1.setTranslationX(x2 + bgImage1.getWidth());
                if (x2 <= -bgImage2.getWidth()) bgImage2.setTranslationX(x1 + bgImage2.getWidth());

                if (raceRunning) handler.postDelayed(this, 16);
            }
        };

        handler.post(raceRunnable);
    }


    private void addHorseToLane(Horse horse, FrameLayout lane) {
        ImageView iv = new ImageView(this);
        iv.setImageResource(horse.getImageRes());
        iv.setAdjustViewBounds(true);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        iv.setLayoutParams(params);
        lane.addView(iv);
        horseImages.add(iv);

        SeekBar sb = new SeekBar(this);
        sb.setMax(0);
        sb.setProgress(0);
        sb.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,10));
        lane.addView(sb);
        horseSeekBars.add(sb);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
    }
}
