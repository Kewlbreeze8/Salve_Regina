package io.github.some_example_name.Menu.Others;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SlotUtil {

    public static void updateTimestamp(String slotId) {
        long now = System.currentTimeMillis();
        Preferences prefs = Gdx.app.getPreferences("VN_SAVE_SLOT_" + slotId);
        prefs.putLong("timestamp", now);
        prefs.flush();
    }

    public static String indexToSlotId(int index) {
        if (index < 1 || index > 5) throw new IllegalArgumentException("Invalid manual save slot index: " + index);
        return "slot" + index;
    }

    public static String getTimestampForSlot(String slotId) {
        Preferences prefs = Gdx.app.getPreferences("VN_SAVE_SLOT_" + slotId);
        long millis = prefs.getLong("timestamp", -1);

        if (millis == -1) return "Never Saved";
        return formatTimestamp(millis);
    }

    public static String formatTimestamp(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault());
        return sdf.format(new Date(millis));
    }

    public static String getDateForSlot(String slotId) {
        long millis = SaveManager.loadLong(slotId, "timestamp", 0L);
        if (millis == 0) return "No Date";
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");
        return formatter.format(new Date(millis));
    }


}
