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

package org.agromax.core;

import java.util.Objects;

/**
 * Represents an english word, used within <code>RDFGenerator</code> class
 *
 * @author Deadpool
 * @see RDFGenerator
 */
public class Word implements Comparable<Word> {
    private final String word;
    private final String posTag;
    private final int index;

    public Word(String str, int index) {
        Objects.requireNonNull(str, "word must not be null");

        int slash = str.indexOf('/');
        if (slash >= 0) {
            word = str.substring(0, slash);
            posTag = str.substring(slash + 1);
            this.index = index;
        } else {
            word = str;
            posTag = "";
            this.index = 0;
        }
    }

    public String getWord() {
        return word;
    }

    public String getTag() {
        return posTag;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public int compareTo(Word o) {
        if (o == null) throw new NullPointerException();
        return index < o.index ? -1 : (index > o.index ? 1 : 0);
    }

    @Override
    public String toString() {
        return String.format("%s/%s/%d", word, posTag, index);
    }

}
