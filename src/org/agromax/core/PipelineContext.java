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

import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.TypedDependency;
import org.agromax.util.Util;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * @author Anurag Gautam
 */
public class PipelineContext implements Callable<Integer> {
    private final String text;
    private final StanfordParserContext parserContext;

    public PipelineContext(String text, StanfordParserContext ctx) {
        this.text = text;
        this.parserContext = ctx;
    }

    @Override
    public Integer call() throws Exception {
        Queue<Collection<TypedDependency>> dependencies = computeDependencies();
        Queue<TreeMap<Word, TreeSet<Word>>> relationshipGraphs = getRelationshipGraphs(dependencies);
        return 0;
    }

    private Queue<TreeMap<Word, TreeSet<Word>>> getRelationshipGraphs(Queue<Collection<TypedDependency>> dependencyQueue) {

        Queue<TreeMap<Word, TreeSet<Word>>> graphs = new LinkedList<>();
        while (!dependencyQueue.isEmpty()) {
            Collection<TypedDependency> sentDependencies = dependencyQueue.poll();

            // Computes the relationship graph for each sentence
            TreeMap<Word, TreeSet<Word>> relations = new TreeMap<>();
            for (TypedDependency d : sentDependencies) {
                Word gov = new Word(d.gov());
                Word dep = new Word(d.dep());

                if (relations.containsKey(gov)) {
                    relations.get(gov).add(dep);
                } else {
                    TreeSet<Word> set = new TreeSet<>();
                    set.add(dep);
                    relations.put(gov, set);
                }
            }

            graphs.add(relations);
        }

        return graphs;
    }


    private Queue<Collection<TypedDependency>> computeDependencies() {
        Queue<Collection<TypedDependency>> dependencies = parserContext.getDependencies(text);
        return dependencies;
    }


    public static void main(String[] args) throws Exception {
        MaxentTagger tagger = new MaxentTagger(Util.SP_TAGGER_PATH);
        DependencyParser parser = DependencyParser.loadFromModelFile(Util.SP_MODEL_PATH);
        PipelineContext pipeline = new PipelineContext("I can almost always tell when movies use fake dinosaurs. I was born in Kanpur. I went to IIT Kanpur to complete my Masters in Computer Science.", new StanfordParserContext(tagger, parser));
        pipeline.call();
    }

}

