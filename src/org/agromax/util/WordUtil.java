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
import org.agromax.core.nlp.pipeline.ComparableWord;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * <h1>NEEDS LOADS OF REVISIONS AND REDESIGN</h1>
 *
 * @author Anurag Gautam
 */
public class WordUtil {
    public static Stream<ComparableWord> filterStopwords(Stream<ComparableWord> words) {
        return words.filter(w -> !Stopwords.STOPWORDS.contains(w.getWord()));
    }

    public static Stream<ComparableWord> replaceWord(Stream<ComparableWord> words, String what, String with) {
        return words.map(w -> {
            if (w.getWord().equalsIgnoreCase(what) && !with.isEmpty()) {
                System.out.println("Rep 'it' with " + with);
                w.setWord(with);
            }
            return w;
        });
    }

    @Deprecated
    public static String weld(Stream<ComparableWord> collection, String separator) {
        StringBuilder value = new StringBuilder();
        collection.forEach(e -> {
            value.append(String.valueOf(e.getWord())).append(separator);
        });

        return value.toString().trim();
    }

    @Deprecated
    public static String weld(Collection<ComparableWord> collection, String separator) {
        StringBuilder value = new StringBuilder();
        collection.forEach(e -> {
            value.append(String.valueOf(e.getWord())).append(separator);
        });

        return value.toString().trim();
    }

    public static String join(Stream<ComparableWord> stream, String separator, Function<String, String> decorator) {
        Objects.requireNonNull(separator, "ComparableWord separator cannot be null");
        Objects.requireNonNull(stream, "ComparableWord stream cannot be null");

        StringBuilder value = new StringBuilder();
        if (decorator != null) {
            stream.forEach(e -> {
                value.append(decorator.apply(e.getWord())).append(separator);
            });
        } else {
            stream.forEach(e -> {
                value.append(e.getWord()).append(separator);
            });
        }

        return value.toString().trim();
    }

    public static String join(Stream<ComparableWord> stream, String separator) {
        Objects.requireNonNull(separator, "ComparableWord separator cannot be null");
        Objects.requireNonNull(stream, "ComparableWord stream cannot be null");

        StringBuilder value = new StringBuilder();
        stream.forEach(e -> {
            value.append(e.getWord()).append(separator);
        });

        return value.toString().trim();
    }
}
