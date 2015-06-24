/*
 * Copyright 2015 Anurag Gautam
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.agromax.util;

import org.agromax.core.Stopwords;
import org.agromax.core.Word;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * <h1>NEEDS LOADS OF REVISIONS AND REDESIGN</h1>
 *
 * @author Anurag Gautam
 */
public class WordUtil {
    public static Stream<Word> filterStopwords(Stream<Word> words) {
        return words.filter(w -> !Stopwords.STOPWORDS.contains(w.getWord()));
    }

    public static Stream<Word> replaceWord(Stream<Word> words, String what, String with) {
        return words.map(w -> {
            if (w.getWord().equalsIgnoreCase(what) && !with.isEmpty()) {
                System.out.println("Rep 'it' with " + with);
                w.setWord(with);
            }
            return w;
        });
    }

    @Deprecated
    public static String weld(Stream<Word> collection, String separator) {
        StringBuilder value = new StringBuilder();
        collection.forEach(e -> {
            value.append(String.valueOf(e.getWord())).append(separator);
        });

        return value.toString().trim();
    }

    @Deprecated
    public static String weld(Collection<Word> collection, String separator) {
        StringBuilder value = new StringBuilder();
        collection.forEach(e -> {
            value.append(String.valueOf(e.getWord())).append(separator);
        });

        return value.toString().trim();
    }

    public static String join(Stream<Word> stream, String separator) {
        Objects.requireNonNull(separator, "Word separator cannot be null");
        Objects.requireNonNull(stream, "Word stream cannot be null");

        StringBuilder value = new StringBuilder();
        stream.forEach(e -> {
            value.append(e.getWord()).append(separator);
        });

        return value.toString().trim();
    }
}
