package di.dilogin.discord.util;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

/**
 * Utility class for deleting Discord messages with retry mechanism.
 */
public class DiscordMessageDeleter {
    
    private static final int RETRY_DELAY_SECONDS = 30; // Time to wait before retrying
    
    /**
     * Deletes the specified message with retry mechanism.
     * @param initialDelay The initial delay before the first attempt (in seconds).
     * @param message The message to delete.
     */
    public static void deleteMessage(int initialDelay, Message message) {
        deleteMessageWithRetry(initialDelay, message, 3); // Try to delete the message up to 3 times
    }
    
    private static void deleteMessageWithRetry(int delay, Message message, int retries) {
        try {
            message.delete().delay(Duration.ofSeconds(delay)).queue();
        } catch (ErrorResponseException e) {
            if (retries > 0) {
                scheduleRetry(message, retries - 1);
            }
        }
    }
    
    /**
     * Schedules a retry for deleting the message after a certain delay.
     * @param message The message to delete.
     * @param retries The number of retries remaining.
     */
    private static void scheduleRetry(Message message, int retries) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                deleteMessageWithRetry(0, message, retries);
                timer.cancel();
            }
        }, RETRY_DELAY_SECONDS * 1000);
    }
}
