package org.agromax.util;

import org.agromax.core.nlp.pipeline.ComparableWord;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Some miscellaneous helper methods, all static unlike you ms/mr dynamic
 *
 * @author The Joker, hahahahaaaaa!
 */
public class Util {
    private static final Logger LOGGER = Logger.getLogger(Util.class.getName());

    static {
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                System.out.println(String.format("%s: %s", record.getLevel().toString(), record.getMessage()));
            }

            @Override
            public void flush() {

            }

            @Override
            public void close() throws SecurityException {

            }
        });
    }

    public static final String SP_MODEL_PATH = "E:\\Coding\\Java Related\\stanford-parser\\stanford-parser-full-2015-04-20\\edu\\stanford\\nlp\\models\\parser\\nndep\\english_UD.gz";

    public static final String SP_TAGGER_PATH = "E:\\Coding\\Java Related\\stanford-parser\\stanford-parser-full-2015-04-20\\edu\\stanford\\nlp\\models\\pos-tagger\\english-left3words\\english-left3words-distsim.tagger";

    public static final String VOCAB_DIR = System.getProperty("user.dir") + "/data/vocab";

    public static final String DATA_DIR = System.getProperty("user.dir");

    public static final String BASE_URL = "http://agro";

    public static String url(String... elements) {
        StringBuilder url = new StringBuilder(BASE_URL);
        for (String e : elements) {
            url.append("/").append(e);
        }
//        url.delete(url.length() - 1, url.length());
        return url.toString();
    }

    public static final class StringWelder {

        private final String prefix;
        private final String delimiter;
        private final String suffix;

        private final StringBuilder value;

        public StringWelder(CharSequence delimiter) {
            if (delimiter == null) throw new NullPointerException("delimiter must not be null");

            this.delimiter = delimiter.toString();
            this.prefix = "";
            this.suffix = "";
            value = new StringBuilder();
        }

        public StringWelder(CharSequence delimiter,
                            CharSequence prefix,
                            CharSequence suffix) {
            if (delimiter == null) throw new NullPointerException("delimiter must not be null");
            if (prefix == null) throw new NullPointerException("prefix must not be null");
            if (suffix == null) throw new NullPointerException("suffix must not be null");

            this.delimiter = delimiter.toString();
            this.prefix = prefix.toString();
            this.suffix = suffix.toString();
            value = new StringBuilder();
            value.append(this.prefix);
        }

        public void add(CharSequence element) {
            value.append(element).append(delimiter);
        }

        public String toString() {
            value.delete(value.length() - delimiter.length(), value.length());
            if (!suffix.equals("")) return value.append(suffix).toString();
            return value.toString();
        }

    }

    public static void log(Level level, Object... tokens) {
        StringWelder welder = new StringWelder(" ");
        for (Object token : tokens) {
            welder.add(String.valueOf(token));
        }
        LOGGER.log(level, welder.toString());
    }

    public static void log(Object... tokens) {
        StringWelder welder = new StringWelder(" ");
        for (Object token : tokens) {
            welder.add(String.valueOf(token));
        }
        LOGGER.info(welder.toString());
    }

    public static String dir(String base, String... parts) {
        return Paths.get(base, parts).toString();
    }

    public static Path dirPath(String... parts) {
        String base = System.getProperty("user.dir");
        return Paths.get(base, parts);
    }

    public static String weld(Collection<ComparableWord> collection, String separator) {
        StringBuilder value = new StringBuilder();
        collection.stream().forEach(e -> {
            value.append(String.valueOf(e.getWord())).append(separator);
        });
        return value.toString().trim();
    }

    public static boolean inOpenRange(int a, int x, int b) {
        return x > a && x < b;
    }

    public static boolean inClosedRange(int a, int x, int b) {
        return x >= a && x <= b;
    }

    public static boolean inOpenRange(long a, long x, long b) {
        return x > a && x < b;
    }

    public static boolean inClosedRange(long a, long x, long b) {
        return x >= a && x <= b;
    }
}
