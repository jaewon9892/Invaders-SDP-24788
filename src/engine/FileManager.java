package engine;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import engine.DrawManager.SpriteType;

/**
 * Manages files used in the application.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public final class FileManager {
    /**
     * Singleton instance of the class.
     */
    private static FileManager instance;
    /**
     * Application logger.
     */
    private static Logger logger;
    /**
     * Max number of high scores.
     */
    private static final int MAX_SCORES = 7;

    // coins.csv file to save changes about coin content
    private static final String COIN_FILENAME = "coins.csv";

    /**
     * private constructor.
     */
    private FileManager() {
        logger = Core.getLogger();
    }

    /**
     * Returns shared instance of FileManager.
     *
     * @return Shared instance of FileManager.
     */
    protected static FileManager getInstance() {
        if (instance == null)
            instance = new FileManager();
        return instance;
    }

    /**
     * Loads sprites from disk.
     *
     * @param spriteMap
     *            Mapping of sprite type and empty boolean matrix that will
     *            contain the image.
     * @throws IOException
     *             In case of loading problems.
     */
    public void loadSprite(final Map<SpriteType, boolean[][]> spriteMap)
            throws IOException {
        InputStream inputStream = null;

        try {
            inputStream = DrawManager.class.getClassLoader()
                    .getResourceAsStream("graphics");
            char c;

            // Sprite loading.
            for (Map.Entry<SpriteType, boolean[][]> sprite : spriteMap
                    .entrySet()) {
                for (int i = 0; i < sprite.getValue().length; i++)
                    for (int j = 0; j < sprite.getValue()[i].length; j++) {
                        do
                            c = (char) inputStream.read();
                        while (c != '0' && c != '1');

                        if (c == '1')
                            sprite.getValue()[i][j] = true;
                        else
                            sprite.getValue()[i][j] = false;
                    }
                logger.fine("Sprite " + sprite.getKey() + " loaded.");
            }
            if (inputStream != null)
                inputStream.close();
        } finally {
            if (inputStream != null)
                inputStream.close();
        }
    }

    /**
     * Loads a font of a given size.
     *
     * @param size
     *            Point size of the font.
     * @return New font.
     * @throws IOException
     *             In case of loading problems.
     * @throws FontFormatException
     *             In case of incorrect font format.
     */
    public Font loadFont(final float size) throws IOException,
            FontFormatException {
        InputStream inputStream = null;
        Font font;

        try {
            // Font loading.
            inputStream = FileManager.class.getClassLoader()
                    .getResourceAsStream("font.ttf");
            font = Font.createFont(Font.TRUETYPE_FONT, inputStream).deriveFont(
                    size);
        } finally {
            if (inputStream != null)
                inputStream.close();
        }

        return font;
    }

    /**
     * Returns the application default scores if there is no user high scores
     * file.
     *
     * @return Default high scores.
     * @throws IOException
     *             In case of loading problems.
     */
    private List<Score> loadDefaultHighScores() throws IOException {
        List<Score> highScores = new ArrayList<Score>();
        InputStream inputStream = null;
        BufferedReader reader = null;

        try {
            inputStream = FileManager.class.getClassLoader()
                    .getResourceAsStream("scores.csv");
            reader = new BufferedReader(new InputStreamReader(inputStream));

            // except first line
            reader.readLine();
            String input;
            while ((input = reader.readLine()) != null) {
                String[] pair = input.split(",");
                String name = pair[0], score = pair[1];
                String mode = pair[2];
                Score highScore = new Score(name, Integer.parseInt(score), mode);
                highScores.add(highScore);
            }
        } finally {
            if (inputStream != null)
                inputStream.close();
        }

        return highScores;
    }

    /**
     * Loads high scores from file, and returns a sorted list of pairs score -
     * value.
     *
     * @return Sorted list of scores - players.
     * @throws IOException
     *             In case of loading problems.
     */
    public List<Score> loadHighScores() throws IOException {

        List<Score> highScores = new ArrayList<Score>();
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;

        try {
            String jarPath = FileManager.class.getProtectionDomain()
                    .getCodeSource().getLocation().getPath();
            jarPath = URLDecoder.decode(jarPath, "UTF-8");

            String scoresPath = new File(jarPath).getParent();
            scoresPath += File.separator;
            scoresPath += "scores.csv";

            File scoresFile = new File(scoresPath);
            inputStream = new FileInputStream(scoresFile);
            bufferedReader = new BufferedReader(new InputStreamReader(
                    inputStream, Charset.forName("UTF-8")));

            logger.info("Loading user high scores.");
            // except first line
            //bufferedReader.readLine();

            String line = bufferedReader.readLine();
            if (line != null && !line.contains(",")) {
                // skip header
                // do nothing;
            } else if (line != null) {
                // First line is a valid score, process it
                String[] pair = line.split(",");
                String name = pair[0];
                String score = pair[1];
                String mode = pair[2];
                highScores.add(new Score(name, Integer.parseInt(score), mode));
            }
            String input;
            while ((input = bufferedReader.readLine()) != null) {
                String[] pair = input.split(",");
                String name = pair[0], score = pair[1];
                String mode = pair[2];
                Score highScore = new Score(name, Integer.parseInt(score), mode);
                highScores.add(highScore);
            }
        } catch (FileNotFoundException e) {
            // loads default if there's no user scores.
            logger.info("Loading default high scores.");
            highScores = loadDefaultHighScores();
        } finally {
            if (bufferedReader != null)
                bufferedReader.close();
        }

        Collections.sort(highScores);
        return highScores;
    }

    /**
     * Saves user high scores to disk.
     *
     * @param highScores
     *            High scores to save.
     * @throws IOException
     *             In case of loading problems.
     */
    public void saveHighScores(final List<Score> highScores)
            throws IOException {
        OutputStream outputStream = null;
        BufferedWriter bufferedWriter = null;

        try {
            String jarPath = FileManager.class.getProtectionDomain()
                    .getCodeSource().getLocation().getPath();
            jarPath = URLDecoder.decode(jarPath, "UTF-8");

            String scoresPath = new File(jarPath).getParent();
            scoresPath += File.separator;
            scoresPath += "scores.csv";

            File scoresFile = new File(scoresPath);

            if (!scoresFile.exists())
                scoresFile.createNewFile();

            outputStream = new FileOutputStream(scoresFile);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(
                    outputStream, Charset.forName("UTF-8")));

            logger.info("Saving user high scores.");

            // Before this PR, we can save only 7 scores, but now we can more.
            for (Score score : highScores) {
                bufferedWriter.write(score.getName() + "," + score.getScore() + "," + score.getMode()); // add score.getMode()
                bufferedWriter.newLine();
            }

        } finally {
            if (bufferedWriter != null)
                bufferedWriter.close();
        }
    }

    /**
     * Loads the coin count from the coins.csv file.
     *
     * @return The saved coin count, or 0 if the file is not found or empty.
     * @throws IOException In case of loading problems.
     */
    public int loadCoins() throws IOException {
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;

        try {
            String jarPath = FileManager.class.getProtectionDomain()
                    .getCodeSource().getLocation().getPath();
            jarPath = URLDecoder.decode(jarPath, "UTF-8");

            String coinsPath = new File(jarPath).getParent();
            coinsPath += File.separator;
            coinsPath += COIN_FILENAME; // coins.csv 사용

            File coinsFile = new File(coinsPath);
            inputStream = new FileInputStream(coinsFile);
            bufferedReader = new BufferedReader(new InputStreamReader(
                    inputStream, Charset.forName("UTF-8")));

            logger.info("Loading coin count from " + COIN_FILENAME + ".");

            // coins.csv Read the first line of the file to get the number of coins
            String line = bufferedReader.readLine();

            if (line != null && !line.trim().isEmpty()) {
                try {
                    // Attempt to convert by extracting only numbers
                    String rawCoin = line.trim().replaceAll("[^0-9]", "");
                    if (!rawCoin.isEmpty()) {
                        return Integer.parseInt(rawCoin);
                    }
                } catch (NumberFormatException e) {
                    logger.warning("Coin count line is not a valid number. Returning 0.");
                    return 0;
                }
            }

        } catch (FileNotFoundException e) {
            logger.info(COIN_FILENAME + " not found. Returning 0 coins.");
            return 0;
        } finally {
            if (bufferedReader != null)
                bufferedReader.close();
        }

        return 0;
    }

    /**
     * Saves the current coin count to the coins.csv file.
     *
     * @param coins The total number of coins acquired.
     * @throws IOException In case of saving problems.
     */
    public void saveCoins(final int coins) throws IOException {
        OutputStream outputStream = null;
        BufferedWriter bufferedWriter = null;

        try {
            String jarPath = FileManager.class.getProtectionDomain()
                    .getCodeSource().getLocation().getPath();
            jarPath = URLDecoder.decode(jarPath, "UTF-8");

            String coinsPath = new File(jarPath).getParent();
            coinsPath += File.separator;
            coinsPath += COIN_FILENAME; // Use coins.csv

            File coinsFile = new File(coinsPath);

            if (!coinsFile.exists())
                coinsFile.createNewFile();

            outputStream = new FileOutputStream(coinsFile);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(
                    outputStream, Charset.forName("UTF-8")));

            logger.info("Saving new coin count (" + coins + ") to " + COIN_FILENAME + ".");

            // Save only one line of coin count to the coins.csv file.
            bufferedWriter.write(Integer.toString(coins));
            bufferedWriter.newLine();

        } finally {
            if (bufferedWriter != null)
                bufferedWriter.close();
        }
    }

    /**
     * Search Achievement list of user
     *
     * @param userName user's name to search.
     * @throws IOException In case of loading problems.
     */
    public List<Boolean> searchAchievementsByName(String userName)
            throws IOException {
        List<Boolean> achievementList = new ArrayList<Boolean>();

        try {
            String jarPath = FileManager.class.getProtectionDomain()
                    .getCodeSource().getLocation().getPath();
            jarPath = URLDecoder.decode(jarPath, "UTF-8");

            String achievementPath = String.valueOf(new File(jarPath));
            achievementPath += File.separator;
            achievementPath += "achievement.csv";

            InputStream iStream = new FileInputStream(achievementPath);
            BufferedReader bReader = new BufferedReader(
                    new InputStreamReader(iStream, Charset.forName("UTF-8")));

            bReader.readLine(); // Dump header
            String line;
            boolean flag = false;
            while ((line = bReader.readLine()) != null) {
                String[] playRecord = line.split(",");
                if (playRecord[0].equals(userName)) {
                    flag = true;
                    logger.info("Loading user achievements.");
                    for (int i = 1; i < playRecord.length; i++) {
                        achievementList.add(playRecord[i].equals("1") ? true : false);
                    }
                    break;
                }
            }
            if (!flag)
                for (int i = 0; i < 5; i++) {
                    logger.info("Loading default achievement.");
                    achievementList.add(false);
                }
        } catch (FileNotFoundException e) {
            logger.info("Loading default achievement.");
            for (int i = 0; i < 5; i++) {
                achievementList.add(false);
            }
        }
        return achievementList;
    }

    /**
     * Unlocks an achievement for the given user.
     *
     * @param userName user's name to search.
     * @param unlockedAchievement A list of booleans representing which achievements
     * @throws IOException In case of loading problems.
     */
    public void unlockAchievement(String userName, List<Boolean> unlockedAchievement)
            throws IOException {
        List<String[]> records = new ArrayList<>();
        try {
            String jarPath = FileManager.class.getProtectionDomain()
                    .getCodeSource().getLocation().getPath();
            jarPath = URLDecoder.decode(jarPath, "UTF-8");

            String achievementPath = String.valueOf(new File(jarPath));
            achievementPath += File.separator;
            achievementPath += "achievement.csv";

            InputStream iStream = new FileInputStream(achievementPath);
            BufferedReader bReader = new BufferedReader(
                    new InputStreamReader(iStream, Charset.forName("UTF-8")));

            String line;
            boolean flag = false;
            List<String[]> recorder = new ArrayList<>();
            while ((line = bReader.readLine()) != null) {
                String[] playRecord = line.split(",");
                if (playRecord[0].equals(userName)) {
                    flag = true;
                    logger.info("Achievement has been updated");
                    for (int i = 1; i < playRecord.length; i++) {
                        if (playRecord[i].equals("0") && unlockedAchievement.get(i))
                            playRecord[i] = "1";
                    }
                }
                recorder.add(playRecord);
            }
            if (!flag){
                logger.info("User not found, creating new record.");
                String[] newRecord = new String[unlockedAchievement.size() + 1];
                newRecord[0] = userName;
                for (int i = 0; i < unlockedAchievement.size(); i++)
                    newRecord[i+1] = unlockedAchievement.get(i) ? "1" : "0";
                recorder.add(newRecord);
            }


            OutputStream outStream = new FileOutputStream(achievementPath);
            BufferedWriter bWriter = new BufferedWriter(
                    new OutputStreamWriter(outStream, Charset.forName("UTF-8")));

            for (String[] record : recorder) {
                bWriter.write(String.join(",", record));
                bWriter.newLine();
            }

            bWriter.close();

        } catch (FileNotFoundException e) {
            logger.info("No achievements to save");
        }
    }
}