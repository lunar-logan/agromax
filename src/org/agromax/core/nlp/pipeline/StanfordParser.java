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

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a stanford parser context, basically a wrapper around
 * stanford parser API. All SP methods must be invoked using this class.
 * This class is thread safe.
 *
 * @author Anurag Gautam
 */
public class StanfordParser extends AbstractParser {
    private final MaxentTagger tagger;
    private final DependencyParser dependencyParser;
    private final Object mutexLock = new Object();

    public StanfordParser(MaxentTagger tagger, DependencyParser dependencyParser) {
        Objects.requireNonNull(tagger);
        Objects.requireNonNull(dependencyParser);

        this.tagger = tagger;
        this.dependencyParser = dependencyParser;
    }

    @Override
    public List<TaggedWord> tagSentence(Collection<?> words) {
        Objects.requireNonNull(words);

        synchronized (mutexLock) {
            List<TaggedWord> taggedWords = tagger.tagSentence((List<HasWord>) words);
            mutexLock.notify();
            return taggedWords;
        }
    }

    @Override
    public List<List<HasWord>> sentenceTokenize(CharSequence text) {
        List<List<HasWord>> sentences = new ArrayList<>();
        DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text.toString()));
        synchronized (mutexLock) {
            for (List<HasWord> sentence : tokenizer) {
                sentences.add(sentence);
            }
            mutexLock.notify();
        }
        return sentences;
    }

    @Override
    public Collection<TypedDependency> sentenceDependencies(CharSequence sent) {
        GrammaticalStructure gs = null;
        synchronized (mutexLock) {
            List<TaggedWord> taggedWords = tagSentence(wordTokenize(sent));
            gs = dependencyParser.predict(taggedWords);
            mutexLock.notify();
        }
        assert gs != null;
        return gs.typedDependencies();
    }

    @Override
    public Collection<TypedDependency> sentenceDependencies(Collection<?> words) {
        GrammaticalStructure gs = null;
        synchronized (mutexLock) {
//            List<TaggedWord> taggedWords = tagSentence(wordTokenize(sent));
            gs = dependencyParser.predict((List<? extends TaggedWord>) words);
            mutexLock.notify();
        }
        assert gs != null;
        return gs.typedDependencies();
    }

    @Override
    public Collection<?> wordTokenize(CharSequence sent) {
        throw new NotImplementedException();
    }

}