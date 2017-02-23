package works.softwarethat.internet.monitor;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Morten Andersen (mortena@gmail.com)
 */
public class Client {
    private static final int MAX_THREADS = 5;
    private static Logger LOGGER = Logger.getLogger(Client.class.getName());
    private final List<Long> timerIds = new ArrayList<>();


    public Client(String[] args) {
        LOGGER.log(Level.INFO, "Starting CLIENT");

        Vertx vertx = Vertx.vertx();

        Counter counter = new Counter();
        Statistics statistics = new Statistics();

        startThread(args, vertx, counter, statistics);

        timerIds.add(vertx.setPeriodic(30 * 60000, a -> flustStats(vertx, statistics)));

        vertx.setTimer(5 * 60000, event -> {
            LOGGER.info("Shutting down");
            timerIds.forEach(timerId -> vertx.cancelTimer(timerId));
            flustStats(vertx, statistics);
            System.exit(0);
        });
    }

    private void flustStats(Vertx vertx, Statistics statistics) {
        vertx.executeBlocking(future -> {
            statistics.flushDataToFile();
            future.complete();
        }, res -> {

        });
    }

    private void startThread(String[] args, Vertx vertx, Counter counter, Statistics statistics) {
        HttpClient httpClient = vertx.createHttpClient(new HttpClientOptions().setProtocolVersion(HttpVersion.HTTP_2));
        int delay = counter.counter * 1000;
        timerIds.add(vertx.setTimer(delay == 0 ? 100 : delay, ev1 -> {
            timerIds.add(vertx.setPeriodic(5000, ev2 ->{
                doInvoke(args[1], Integer.parseInt(args[2]), args[3], httpClient, statistics);
            }));
        }));
        counter.inc();
        if (counter.counter < MAX_THREADS) {
            startThread(args, vertx, counter, statistics);
        }
    }

    private void doInvoke(String host, int port, String path, HttpClient httpClient, Statistics statistics) {
        LOGGER.finest("Invoking service: " + path);
        httpClient.get(port, host, path, event -> {
            LOGGER.log(Level.FINEST, "Invoke end - check response");
            if (event.statusCode() == 200) {
                LOGGER.log(Level.FINE, "Invocation succeeded");
                statistics.regSuccess();
            } else {
                LOGGER.log(Level.SEVERE, "No access");
                statistics.regError();
            }
        }).end();
    }

    private class Counter {
        int counter = 0;
        void inc() {
            counter++;
        }
    }
}
