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

package org.agromax.core.nlp.pipeline;

import edu.stanford.nlp.ling.IndexedWord;
import org.agromax.core.RDFGenerator;

import java.util.Objects;

/**
 * Represents an english word, used within <code>RDFGenerator</code> class only.
 * This class is closely knit with stanford-nlp library and uses (requires) <code>IndexedWord</code> class to run.
 * Compares words based on there {@code index} in the original sentence.
 *
 * @author Deadpool
 * @version $revision$
 * @see RDFGenerator
 * @see IndexedWord
 * @since Agromax 1.0
 */
public class ComparableWord implements Comparable<ComparableWord> {
    private String word;
    private final String posTag;
    private final int index;

    public ComparableWord(IndexedWord indexedWord) {
        Objects.requireNonNull(indexedWord);

        word = indexedWord.word();
        posTag = indexedWord.tag();
        index = indexedWord.index();
    }

    /**
     * @param str   the english word
     * @param index index (1-based) of {@code str} in the original sentence
     */
    public ComparableWord(String str, int index) {
        Objects.requireNonNull(str, "ComparableWord must not be null");

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

    public void setWord(String word) {
        this.word = word;
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
    public int compareTo(ComparableWord o) {
        if (o == null) throw new NullPointerException();
        return index < o.index ? -1 : (index > o.index ? 1 : 0);
    }

    @Override
    public String toString() {
        return String.format("%s/%s/%d", word, posTag, index);
    }

}
