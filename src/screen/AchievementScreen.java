package screen;

import engine.Achievement;
import engine.AchievementManager;
import engine.Core;
import engine.FileManager;

import java.awt.*;
import java.awt.event.KeyEvent;
import engine.SoundManager;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AchievementScreen extends Screen {

    private static final int PAGE = 7;
    private FileManager fileManager;
    private AchievementManager achievementManager;
    private List<Achievement> achievements;
    private List<String> completer;
    private List<String> completer1P;
    private List<String> completer2P;
    private int currentIdx = 0;
    private int currentPage = 0;
    private int maxPage;
    private String numOfPages;

    public AchievementScreen(final int width, final int height, final int fps) {
        super(width, height, fps);
        achievementManager = Core.getAchievementManager();
        achievements = achievementManager.getAchievements();
        fileManager = Core.getFileManager();
        pageSetting();
        // Start menu music loop when the achievement screen is created
        this.returnCode = 3;
        SoundManager.playLoop("sound/menu_sound.wav");
    }

    private void pageSetting(){
        this.completer = Core.getFileManager().getAchievementCompleter(achievements.get(currentIdx));

        this.completer1P = completer.stream()
                .filter(s -> s.startsWith("1:"))
                .collect(Collectors.toList());
        this.completer2P = completer.stream()
                .filter(s -> s.startsWith("2:"))
                .collect(Collectors.toList());
        this.maxPage = Math.max(
                (int) Math.ceil((double) completer1P.size() / PAGE),
                (int) Math.ceil((double) completer2P.size() / PAGE)
        ) - 1;

        if (this.maxPage < 0) this.maxPage = 0;

        if (this.currentPage > this.maxPage) this.currentPage = this.maxPage;

        this.numOfPages = (currentPage + 1) + " / " + (maxPage + 1);
    }

    public final int run() {
        super.run();
        // Stop menu music when leaving the achievement screen
        SoundManager.stop();

        return this.returnCode;
    }

    protected final void update() {

        // [2025-10-17] feat: Added key input logic to navigate achievements
        // When the right or left arrow key is pressed, update the current achievement index
        // and reload the completer list for the newly selected achievement.
        if (inputManager.isKeyDown(KeyEvent.VK_RIGHT) && inputDelay.checkFinished()) {
            currentIdx = (currentIdx + 1) % achievements.size();
            this.currentPage = 0;
            pageSetting();
            inputDelay.reset();
        }
        if (inputManager.isKeyDown(KeyEvent.VK_LEFT) && inputDelay.checkFinished()) {
            currentIdx = (currentIdx - 1 + achievements.size()) % achievements.size();
            this.currentPage = 0;
            pageSetting();
            inputDelay.reset();
        }

        // [2025-10-20] feat: Added key input logic to navigate achievement pages
        // When the up or down arrow key is pressed, switch between pages and update page display text.
        if (inputManager.isKeyDown(KeyEvent.VK_UP) && inputDelay.checkFinished()) {
            if (currentPage > 0) currentPage--;
            inputDelay.reset();
        }

        if (inputManager.isKeyDown(KeyEvent.VK_DOWN) && inputDelay.checkFinished()) {
            if (currentPage < maxPage) currentPage++;
            inputDelay.reset();
        }

        this.numOfPages = (currentPage+1) + " / " + (maxPage+1);

        super.update();
        draw();

        if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE) && this.inputDelay.checkFinished()) {
            this.returnCode = 1;
            this.isRunning = false;
        }

        // back button click event
        if (inputManager.isMouseClicked()) {
            int mx = inputManager.getMouseX();
            int my = inputManager.getMouseY();
            Rectangle backBox = drawManager.getBackButtonHitbox(this);
            if (backBox.contains(mx, my)) {
                this.returnCode = 1;
                this.isRunning = false;
            }
        }
    }

    private void draw() {
        drawManager.initDrawing(this);

        int start = currentPage * PAGE;

        int end1 = Math.min(start + PAGE, completer1P.size());
        int end2 = Math.min(start + PAGE, completer2P.size());

        List<String> page1P = (start < completer1P.size()) ?
                completer1P.subList(start, end1) : Collections.emptyList();
        List<String> page2P = (start < completer2P.size()) ?
                completer2P.subList(start, end2) : Collections.emptyList();

        drawManager.drawAchievementMenu(this, achievements.get(currentIdx), page1P, page2P, numOfPages);

        // hover highlight
        int mx = inputManager.getMouseX();
        int my = inputManager.getMouseY();
        Rectangle backBox = drawManager.getBackButtonHitbox(this);

        if (backBox.contains(mx, my)) {
            drawManager.drawBackButton(this, true);
        }

        drawManager.completeDrawing(this);
    }
}
