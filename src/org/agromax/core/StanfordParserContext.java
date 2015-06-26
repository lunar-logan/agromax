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

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Anurag Gautam
 */
public class StanfordParserContext {
    private final MaxentTagger tagger;
    private final DependencyParser dependencyParser;

    public StanfordParserContext(MaxentTagger tagger, DependencyParser dependencyParser) {
        this.tagger = tagger;
        this.dependencyParser = dependencyParser;
    }

    public DependencyParser getDependencyParser() {
        return dependencyParser;
    }

    public MaxentTagger getTagger() {
        return tagger;
    }

    public List<TaggedWord> tag(String text) {
        List<TaggedWord> taggedWordList = new ArrayList<>();

        DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));
        for (List<HasWord> sentence : tokenizer) {
            List<TaggedWord> taggedWords = tagger.tagSentence(sentence);
            taggedWordList.addAll(taggedWords);
        }

        return taggedWordList;
    }
}
