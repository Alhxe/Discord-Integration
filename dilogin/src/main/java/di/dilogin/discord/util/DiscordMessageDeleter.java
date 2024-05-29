package di.dilogin.discord.util;

import java.util.Timer;
import java.util.TimerTask;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

/**
 * Utility class for deleting Discord messages with retry mechanism.
 */
public class DiscordMessageDeleter {

    private static final int RETRY_DELAY_SECONDS = 30; // Time to wait before retrying

    /**
     * Deletes the specified message with retry mechanism.
     * 
     * @param initialDelay The initial delay before the first attempt (in seconds).
     * @param message      The message to delete.
     */
    public static void deleteMessage(int initialDelay, Message message) {
        if (message != null) {
            scheduleRetry(message, initialDelay, 3); // Try to delete the message up to 3 times with the initial delay
        }
    }

    /**
     * Schedules a retry for deleting the message after a certain delay.
     * 
     * @param message The message to delete.
     * @param delay   The delay before the retry attempt (in seconds).
     * @param retries The number of retries remaining.
     */
    private static void scheduleRetry(Message message, int delay, int retries) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                	if((message.isFromType(ChannelType.PRIVATE) && message.getAuthor().isBot()) || !message.isFromType(ChannelType.PRIVATE)) {
                    message.delete().queue(
                            success -> timer.cancel(), // Cancel the timer after successful deletion
                            failure -> {
                                if (retries > 0) {
                                    scheduleRetry(message, RETRY_DELAY_SECONDS, retries - 1); // Retry after the delay
                                } else {
                                    timer.cancel(); // Cancel the timer if no more retries are left
                                }
                            });
                	}
                } catch (ErrorResponseException e) {
                    if (retries > 0) {
                        scheduleRetry(message, RETRY_DELAY_SECONDS, retries - 1); // Retry after the delay
                    } else {
                        timer.cancel(); // Cancel the timer if no more retries are left
                    }
                }
            }
        }, delay * 1000); // Convert seconds to milliseconds for Timer.schedule()
    }
}
