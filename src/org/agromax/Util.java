package org.agromax;

import java.util.logging.Logger;

/**
 * Some miscellaneous helper methods, all static unlike you ms/mr dynamic
 *
 * @author The Joker, hahahahaaaaa!
 */
public class Util {
    private static final Logger LOGGER = Logger.getLogger("AgroMaxLogger");

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

    public static void log(Object... tokens) {
        StringWelder welder = new StringWelder(" ");
        for (Object token : tokens) {
            welder.add(String.valueOf(token));
        }
        LOGGER.info(welder.toString());
    }
}
