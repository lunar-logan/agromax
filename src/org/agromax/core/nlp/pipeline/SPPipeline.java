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
import edu.stanford.nlp.trees.TypedDependency;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author Anurag Gautam
 * @version revision 2 on: 25/7/2015
 */
public class SPPipeline {
    private final StanfordParser parser;
    private final Queue<SPPipelineAction> actionList = new LinkedList<>();

    public SPPipeline(StanfordParser parser) {
        this.parser = parser;
    }

    @Deprecated
    public void registerPipelineAction(SPPipelineAction action) {
        actionList.add(action);
    }

    public void addPipelineAction(SPPipelineAction action) {
        actionList.add(action);
    }

    public SPPipelineAction removePipelineAction(SPPipelineAction action) {
        if (actionList.remove(action)) {
            return action;
        }
        return null;
    }

    /**
     * Schedule the text on the pipeline
     *
     * @param seq represents the text (sequence of characters) to be scheduled
     */
    public synchronized void schedule(CharSequence seq) {

        // Tokenize the text to sentences
        List<List<HasWord>> sentences = parser.toSentences(seq);

        // For each sentence tag and process
        sentences.forEach(sentence -> {
            Collection<? extends TaggedWord> taggedWords = parser.tagSentence(sentence);
            Collection<TypedDependency> typedDependencies = parser.sentenceDependencies(taggedWords);
            actionList.forEach(a -> {
                a.perform(taggedWords, typedDependencies);
            });
        });
    }
/*
    public static void main(String[] args) {
        MaxentTagger tagger = new MaxentTagger(Util.SP_TAGGER_PATH);
        DependencyParser parser = DependencyParser.loadFromModelFile(Util.SP_MODEL_PATH);

        SPPipeline pipeline = new SPPipeline(new StanfordParser(tagger, parser));

        pipeline.registerPipelineAction(new SPPipelineAction() {
            @Override
            public void perform(List<TaggedWord> taggedWords, Collection<TypedDependency> dependency) {
                System.out.println(dependency);
            }
        });

        pipeline.schedule("I can almost always tell when movies use fake dinosaurs. I was born in Kanpur.");
    }*/
}
