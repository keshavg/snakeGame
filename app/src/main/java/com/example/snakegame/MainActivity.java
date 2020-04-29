// Snake Game
// Jay Bhagat
// May 2018
// With help from tutorial: http://gamecodeschool.com/android/coding-a-snake-game-for-android/

package com.example.snakegame;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import java.util.Random;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class MainActivity extends AppCompatActivity {

    SnakeEngine snakeEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Display display = getWindowManager().getDefaultDisplay(); // getting the screen size

        Point size = new Point(); // make a point object for the display
        display.getSize(size);

        snakeEngine = new SnakeEngine(this, size);

        setContentView(snakeEngine);

    }

    @Override
    protected void onResume() {
        super.onResume();
        snakeEngine.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        snakeEngine.pause();
    }
}

class SnakeEngine extends SurfaceView implements Runnable {
    // Variables for declaration
    private Thread thread = null;

    private Context context;

    private SoundPool soundPool;
    private int eat_bob = -1;
    private int snake_crash = -1;

    public enum Heading {UP, RIGHT, DOWN, LEFT}

    private Heading heading = Heading.RIGHT;

    private int screenX;
    private int screenY;

    private int snakeLength;

    private int snakeX;
    private int snakeY;

    private int blockSize;

    private final int NUM_BLOCKS_WIDE = 40;
    private int numBlocksHigh;

    private long nextFrameTime;

    private final long FPS = 10;

    private final long MILLIS_PER_SECOND = 1000;

    private int score;

    private int[] snakeXs;
    private int[] snakeYs;

    // To draw everything
    private volatile boolean isPlaying;

    private Canvas canvas;

    private SurfaceHolder surfaceHolder;

    private Paint paint;


    public SnakeEngine(Context context, Point size) {
        super(context);

        context = context;

        screenX = size.x;
        screenY = size.y;

        blockSize = screenX / NUM_BLOCKS_WIDE;
        numBlocksHigh = screenY / blockSize;

        surfaceHolder = getHolder(); // objects for drawing
        paint = new Paint();

        snakeXs = new int[200]; // 200 points = achievement
        snakeYs = new int[200];

        newGame(); // begins game
    }

    public void run() {

        while (isPlaying) {
            if(updateRequired()) {
                update();
                draw();
            }

        }
    }

    public void pause() {
        isPlaying = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            // catches error
        }
    }

    public void resume() {
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }

    public void newGame() {
        snakeLength = 1; // only one snake to start
        snakeXs[0] = NUM_BLOCKS_WIDE / 2;
        snakeYs[0] = numBlocksHigh / 2;

        spawnSnake();

        score = 0; // resets score for every new game

        nextFrameTime = System.currentTimeMillis(); // sets up the frame time
    }

    public void spawnSnake() {
        Random random = new Random();
        snakeX = random.nextInt(NUM_BLOCKS_WIDE - 1) + 1;
        snakeY = random.nextInt(numBlocksHigh - 1) + 1;
    }

    private void snakeEat(){
        snakeLength++; // increase length

        spawnSnake();

        score = score + 1; // increase score
    }

    private void moveSnake(){
        for (int i = snakeLength; i > 0; i--) { // moves snake body
            snakeXs[i] = snakeXs[i - 1];
            snakeYs[i] = snakeYs[i - 1];
        }

        switch (heading) { // moves snake head
            case UP:
                snakeYs[0]--;
                break;

            case RIGHT:
                snakeXs[0]++;
                break;

            case DOWN:
                snakeYs[0]++;
                break;

            case LEFT:
                snakeXs[0]--;
                break;
        }
    }

    private boolean detectDeath(){
        boolean dead = false; // the snake is not dead

        // is the snake dead now?
        if (snakeXs[0] == -1) dead = true;
        if (snakeXs[0] >= NUM_BLOCKS_WIDE) dead = true;
        if (snakeYs[0] == -1) dead = true;
        if (snakeYs[0] == numBlocksHigh) dead = true;

        // has the snake gotten stuck in a circle?
        for (int i = snakeLength - 1; i > 0; i--) {
            if ((i > 4) && (snakeXs[0] == snakeXs[i]) && (snakeYs[0] == snakeYs[i])) {
                dead = true;
            }
        }

        return dead;
    }

    public void update() {
        if (snakeXs[0] == snakeX && snakeYs[0] == snakeY) {
            snakeEat();
        }

        moveSnake();

        if (detectDeath()) {
            //start again
            newGame();
        }
    }

    public void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();

            canvas.drawColor(Color.argb(255, 26, 128, 182));

            paint.setColor(Color.argb(255, 255, 255, 255));

            paint.setTextSize(90);
            canvas.drawText("Score:" + score, 10, 70, paint);

            for (int i = 0; i < snakeLength; i++) { // drawing the snake for each block
                canvas.drawRect(snakeXs[i] * blockSize,
                        (snakeYs[i] * blockSize),
                        (snakeXs[i] * blockSize) + blockSize,
                        (snakeYs[i] * blockSize) + blockSize,
                        paint);
            }

            paint.setColor(Color.argb(255, 255, 0, 0));

            canvas.drawRect(snakeX * blockSize, // additional block to get
                    (snakeY * blockSize),
                    (snakeX * blockSize) + blockSize,
                    (snakeY * blockSize) + blockSize,
                    paint);

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public boolean updateRequired() {
        if(nextFrameTime <= System.currentTimeMillis()){ // does the frame need to update

            nextFrameTime =System.currentTimeMillis() + MILLIS_PER_SECOND / FPS;

            return true;
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                if (motionEvent.getX() >= screenX / 2) {
                    switch(heading){
                        case UP:
                            heading = Heading.RIGHT;
                            break;
                        case RIGHT:
                            heading = Heading.DOWN;
                            break;
                        case DOWN:
                            heading = Heading.LEFT;
                            break;
                        case LEFT:
                            heading = Heading.UP;
                            break;
                    }
                } else {
                    switch(heading){
                        case UP:
                            heading = Heading.LEFT;
                            break;
                        case LEFT:
                            heading = Heading.DOWN;
                            break;
                        case DOWN:
                            heading = Heading.RIGHT;
                            break;
                        case RIGHT:
                            heading = Heading.UP;
                            break;
                    }
                }
        }
        return true;
    }
}


