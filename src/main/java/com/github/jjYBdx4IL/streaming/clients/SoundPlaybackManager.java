package com.github.jjYBdx4IL.streaming.clients;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jjYBdx4IL
 */
public class SoundPlaybackManager {

    private static final Logger LOG = LoggerFactory.getLogger(SoundPlaybackManager.class);
    private static final long SND_NOTIF_DEAD_TIME = 30 * 1000L;  // don't play any sound notification more than once every 30 secs
    private final Map<String, Long> lastScheduledPlayedTimes = new HashMap<>();

    public SoundPlaybackManager() {
    }

    public void schedulePlayback(File soundFile) {
        final String key = soundFile.getAbsolutePath();
        final long now = System.currentTimeMillis();
        final long lastScheduledPlayedTime = lastScheduledPlayedTimes.containsKey(key) ? lastScheduledPlayedTimes.get(key) : 0L;
        // is this sound already scheduled for playback?
        if (lastScheduledPlayedTime >= now) {
            // then there is no need to schedule it again
            LOG.debug("sound already scheduled for playback: " + key);
            return;
        }
        final long nextPlayTime = lastScheduledPlayedTime + SND_NOTIF_DEAD_TIME;
        final long delayMillis = nextPlayTime > now ? nextPlayTime - now : 0L;
        lastScheduledPlayedTimes.put(key, now + delayMillis);
        LOG.debug(String.format(Locale.ROOT, "scheduling sound playback with %d ms delay: %s", delayMillis, key));
        new SoundPlaybackTask(soundFile).run(delayMillis);
    }
}
