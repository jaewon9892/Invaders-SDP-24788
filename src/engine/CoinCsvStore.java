package engine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public final class CoinCsvStore {
    private static final Path DIR = Paths.get(System.getProperty("user.home"), ".invaders");
    private static final Path CSV = DIR.resolve("coin.csv");
    private static final String COINS_PREFIX = "COINS";

    private CoinCsvStore() {}

    private static void ensureFile() throws IOException {
        if (!Files.exists(DIR)) Files.createDirectories(DIR);
        if (!Files.exists(CSV)) {
            List<String> init = new ArrayList<>();
            init.add(COINS_PREFIX + ",0");
            Files.write(CSV, init, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        }
    }

    public static int loadCoins() {
        try {
            ensureFile();
            List<String> lines = Files.readAllLines(CSV, StandardCharsets.UTF_8);
            if (!lines.isEmpty() && lines.get(0).startsWith(COINS_PREFIX)) {
                String[] parts = lines.get(0).split(",", 2);
                return (parts.length > 1) ? Integer.parseInt(parts[1].trim()) : 0;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public static void saveCoins(int coins) {
        try {
            ensureFile();
            List<String> lines = Files.readAllLines(CSV, StandardCharsets.UTF_8);
            if (lines.isEmpty()) lines.add(COINS_PREFIX + "," + coins);
            else lines.set(0, COINS_PREFIX + "," + coins);
            Files.write(CSV, lines, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
