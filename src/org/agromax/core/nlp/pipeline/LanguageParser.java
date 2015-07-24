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

import java.util.Collection;

/**
 * Represents an abstract language parser that supports basic Natural Language Processing abilities
 * like <b>tokenizer</b>, <b>Part of Speech tagger</b> and <b>Dependency analyzer</b>.
 *
 * @author Anurag Gautam
 * @since 1.2
 */
@Deprecated
public interface LanguageParser {
    Collection<?> tagSentence(Collection<?> words);

    /**
     * Tokenize the paragraph into sentences
     *
     * @param text chunk to be tokenize into sentences
     * @return Collection of sentences
     */
    Collection<?> sentenceTokenize(CharSequence text);

    /**
     * Tokenize the sentence into words
     *
     * @param sent Sentence to be tokenized
     * @return Collection of tokenized words
     */
    Collection<?> wordTokenize(CharSequence sent);

    Collection<?> sentenceDependencies(CharSequence sent);

    Collection<?> sentenceDependencies(Collection<?> words);
}
