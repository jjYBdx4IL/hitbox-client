package com.github.jjYBdx4IL.streaming.clients;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Schedule a delayed playback of some sound file.
 *
 * @author jjYBdx4IL
 */
public class SoundPlaybackTask extends TimerTask {

    private static final Logger LOG = LoggerFactory.getLogger(SoundPlaybackTask.class);

    private final File soundFile;
    private final Timer timer;

    public SoundPlaybackTask(File soundFile) {
        this.soundFile = soundFile;
        this.timer = new Timer(true);
    }

    /**
     * Don't call this method directly, it's intended to be called by the internal timer task.
     */
    @Override
    public void run() {
        play(soundFile);
    }

    public void run(long delayMillis) {
        timer.schedule(new SoundPlaybackTask(soundFile), delayMillis);
    }

    public static void play(File file) {
        try {
            final Clip clip = (Clip) AudioSystem.getLine(new Line.Info(Clip.class));

            clip.addLineListener(new LineListener() {
                @Override
                public void update(LineEvent event) {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                }
            });

            clip.open(AudioSystem.getAudioInputStream(file));
            clip.start();
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException exc) {
            LOG.error("", exc);
        }
    }

}
