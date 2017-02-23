package works.softwarethat.internet.monitor;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Morten Andersen (mortena@gmail.com)
 */
public class Statistics {
    private static Logger LOGGER = Logger.getLogger(Statistics.class.getName());
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm.ss");

    private long successCount = 0;
    private long errorCount = 0;

    private List<LocalDateTime> errorTimestamps = new ArrayList<>();

    public void regSuccess() {
        synchronized (this) {
            successCount++;
        }
    }

    public void regError() {
        synchronized (this) {
            errorTimestamps.add(LocalDateTime.now());
            errorCount++;
        }
    }

    public void flushDataToFile() {
        List<LocalDateTime> errorTimestamps;
        long successCount;
        long errorCount;
        synchronized (this) {
            errorTimestamps = this.errorTimestamps;
            successCount = this.successCount;
            errorCount = this.errorCount;
            this.errorTimestamps = new ArrayList<>();
            this.successCount = 0;
            this.errorCount = 0;
        }
        ObjectMapper mapper = new ObjectMapper();
        LocalDateTime now = LocalDateTime.now();
        HashMap<String, Object> value = new HashMap<>();
        value.put("successCount", successCount);
        value.put("errorCount", errorCount);
        List<String> timestampStrings = errorTimestamps.stream()
                .map(localDateTime -> formatter.format(localDateTime))
                .collect(Collectors.toList());
        value.put("errorTimestamps", timestampStrings);
        try {
            File resultFile = new File(
                    "./target/report-" + formatter.format(now) + ".json");
            mapper.writerWithDefaultPrettyPrinter().writeValue(resultFile, value);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to write report file", e);
        }
    }
}
