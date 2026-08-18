import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LogGenerator {

    private static final String LOG_DIR = "/var/log/myapp";
    private static final String LOG_FILE = "/var/log/myapp/app.log";

    private static final String[] USERS = {"admin", "johndoe", "alice", "bob", "eva"};
    private static final String[] OPERATIONS = {"LOGIN", "LOGOUT", "PURCHASE", "UPDATE_PROFILE", "PASSWORD_RESET"};
    private static final String[] RANDOM_TEXTS = {
        "User performed an action",
        "System state updated successfully",
        "Request processed without errors",
        "Data synchronization in progress",
        "Session token refreshed"
    };
    private static final String[] DETAILS = {
        "status=success code=200",
        "status=info code=201",
        "status=notice code=200",
        "status=ok code=200"
    };

    // Range start for 'created' field: 01-01-2026 00:00:00
    private static final ZonedDateTime START_DATE = ZonedDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());
    private static final long START_EPOCH_MILLI = START_DATE.toInstant().toEpochMilli();

    public static void main(String[] args) {
        // Ensure destination directory exists
        File dir = new File(LOG_DIR);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("Created directory: " + LOG_DIR);
            }
        }

        // Allow configurable log generation rate (default 10 ms per log)
        long sleepIntervalMs = 10;
        String envInterval = System.getenv("LOG_INTERVAL_MS");
        if (envInterval != null && !envInterval.trim().isEmpty()) {
            try {
                sleepIntervalMs = Long.parseLong(envInterval.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        Random random = new Random();
        List<Integer> generatedIds = new ArrayList<>();

        System.out.println("Starting continuous log generation (interval: " + sleepIntervalMs + " ms)...");

        while (true) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
                long nowEpochMilli = System.currentTimeMillis();
                long diff = nowEpochMilli - START_EPOCH_MILLI;
                long randomCreatedMilli = diff > 0 ? START_EPOCH_MILLI + (long) (random.nextDouble() * diff) : nowEpochMilli;
                String randomCreatedDate = Instant.ofEpochMilli(randomCreatedMilli).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                String importTimeDate = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                int randomId;
                if (random.nextInt(100) == 0 && !generatedIds.isEmpty()) {
                    randomId = generatedIds.get(random.nextInt(generatedIds.size()));
                } else {
                    randomId = 100000 + random.nextInt(900000);
                    generatedIds.add(randomId);
                    if (generatedIds.size() > 1000) {
                        generatedIds.remove(0);
                    }
                }

                String randomUser = USERS[random.nextInt(USERS.length)];
                String randomOperation = OPERATIONS[random.nextInt(OPERATIONS.length)];
                String randomText = RANDOM_TEXTS[random.nextInt(RANDOM_TEXTS.length)];
                String randomDetail = DETAILS[random.nextInt(DETAILS.length)];

                // Format log message with 'created' first, 'import_time' second
                String logLine = String.format(
                    "created=\"%s\" import_time=\"%s\" host=\"aabb.com\" id=\"%d\" user=\"%s\" operation=\"%s\" text=\"%s\" details=\"%s\"",
                    randomCreatedDate,
                    importTimeDate,
                    randomId,
                    randomUser,
                    randomOperation,
                    randomText,
                    randomDetail
                );

                writer.println(logLine);
                writer.flush();
                System.out.println("Logged: " + logLine);
            } catch (IOException e) {
                System.err.println("Error writing to log file: " + e.getMessage());
            }

            try {
                Thread.sleep(sleepIntervalMs);
            } catch (InterruptedException e) {
                System.err.println("Log generation thread interrupted.");
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}

