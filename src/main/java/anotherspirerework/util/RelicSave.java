package anotherspirerework.util;

import anotherspirerework.AnotherSpireRework;

import java.util.HashMap;
import java.util.Map;

/**
 * The save format of the Strange Spoon removals: how many cards of one identity lost their Exhaust.
 *
 * <p>A card's own flag does not survive a save (the base game stores only ID, upgrade count and
 * {@code misc}), so the relic writes down the counts and re-applies them after loading. The format is
 * "id|upgrades|misc=count" entries separated by semicolons. It lives outside the relic class so it
 * can be exercised without initialising the game's relic base class.
 */
public final class RelicSave {
    private RelicSave() {
    }

    /** Writes the counts; the map order is irrelevant because every entry is self describing. */
    public static String formatCounts(Map<String, Integer> counts) {
        StringBuilder saved = new StringBuilder();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (saved.length() > 0) {
                saved.append(';');
            }
            saved.append(entry.getKey()).append('=').append(entry.getValue());
        }
        return saved.toString();
    }

    /** Reads what {@link #formatCounts} wrote; malformed entries are skipped, not fatal. */
    public static Map<String, Integer> parseCounts(String value) {
        Map<String, Integer> counts = new HashMap<>();
        if (value == null || value.isEmpty()) {
            return counts;
        }
        for (String entry : value.split(";")) {
            int separator = entry.lastIndexOf('=');
            if (separator <= 0) {
                continue;
            }
            try {
                counts.put(entry.substring(0, separator), Integer.parseInt(entry.substring(separator + 1)));
            } catch (NumberFormatException ignored) {
                AnotherSpireRework.logger.warn("Ignoring malformed Strange Spoon entry: " + entry);
            }
        }
        return counts;
    }
}
