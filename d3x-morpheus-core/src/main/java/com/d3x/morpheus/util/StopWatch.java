package com.d3x.morpheus.util;

import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * A class that provides a useful timing / collector api
 *
 * @author Xavier Witdouck
 */
@lombok.AllArgsConstructor()
@lombok.extern.slf4j.Slf4j()
public class StopWatch {

    private long start;


    /**
     * Captures current time in a thread local variable
     * @return  the current time
     */
    public static StopWatch start() {
        return new StopWatch(System.currentTimeMillis());
    }


    /**
     * Returns the elapsed millis since start to now()
     * @return      the elapsed millis from start to now()
     */
    public long elapsedMillis() {
        return System.currentTimeMillis() - start;
    }


    /**
     * Resets the start time for this stop watch to now
     * @return      the current time in epoch millis
     */
    public long reset() {
        this.start = System.currentTimeMillis();
        return start;
    }


    /**
     * Logs an INFO message with the elapsed time between start() and now()
     * @param message   the message to prefix to timing
     * @return          the elapsed time from start() to now()
     */
    public long info(String message) {
        var time = System.currentTimeMillis() - start;
        log.info(String.format("%s: Timing: %s ", message, toString(time)));
        return time;
    }


    /**
     * Logs an WARM message with the elapsed time between start() and now()
     * @param message   the message to prefix to timing
     * @return          the elapsed time from start() to now()
     */
    public long warn(String message) {
        var time = System.currentTimeMillis() - start;
        log.warn(String.format("%s: Timing: %s ", message, toString(time)));
        return time;
    }


    /**
     * Logs an ERROR message with the elapsed time between start() and now()
     * @param message   the message to prefix to timing
     * @return          the elapsed time from start() to now()
     */
    public long error(String message) {
        var time = System.currentTimeMillis() - start;
        log.error(String.format("%s: Timing: %s ", message, toString(time)));
        return time;
    }


    /**
     * Returns a time string appropriate to magnitude
     * @param millis    the time in millis
     * @return          the time string
     */
    private static String toString(long millis) {
        if (millis < 1000) {
            return millis + " millis";
        } else if (millis < 60000) {
            return String.format("%1$.3f seconds", millis / 1000d);
        } else if (millis < 3600000) {
            return String.format("%1$.3f minutes", millis / (1000d * 60d));
        } else {
            return String.format("%1$.3f hours", millis / (1000d * 60d * 60d));
        }
    }


    /**
     * Times a callable and returns the result with timing
     * @param callable  the callable to run
     * @param <T>       the type for callable
     * @return          the result of callable
     */
    public static <T> Result<T> time(Callable<T> callable) {
        try {
            var t1 = System.currentTimeMillis();
            var value = callable.call();
            var t2 = System.currentTimeMillis();
            return new Result<T>(t1, t2, value);
        } catch (Exception ex) {
            throw new RuntimeException("Timing operation failed: " + ex.getMessage(), ex);
        }
    }


    /**
     * Times a runnable and returns the time in millis
     * @param runnable  the runnable to run
     * @return          the time in millis
     */
    public static long time(Runnable runnable) {
        try {
            var t1 = System.currentTimeMillis();
            runnable.run();
            var t2 = System.currentTimeMillis();
            return t2-t1;
        } catch (Exception ex) {
            throw new RuntimeException("Timing operation failed: " + ex.getMessage(), ex);
        }
    }


    /**
     * A class that represents a timing case
     */
    @lombok.Data
    @lombok.AllArgsConstructor()
    public static class Result<T> {

        @lombok.Getter()
        private final long start;
        @lombok.Getter()
        private final long end;
        private final T value;


        /**
         * Returns the value for this ressult
         * @return  value for result
         */
        public Optional<T> getValue() {
            return Optional.ofNullable(value);
        }

        /**
         * Returns the time in millis for result
         * @return      the time in millis
         */
        public long getMillis() {
            return end - start;
        }

        @Override
        public String toString() {
            return "StopWatch Result: " + value + " in " + getMillis() + " millis";
        }
    }


    public static void main(String[] args) {
        IO.println(StopWatch.toString(123));
        IO.println(StopWatch.toString(1423));
        IO.println(StopWatch.toString(24500));
        IO.println(StopWatch.toString(65000));
        IO.println(StopWatch.toString(10 * 60 * 1000));
        IO.println(StopWatch.toString(58 * 60 * 1000));
        IO.println(StopWatch.toString(200 * 60 * 1000));
    }
}
