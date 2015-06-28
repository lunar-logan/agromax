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

import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.TypedDependency;
import org.agromax.ResourceManager;
import org.agromax.core.nlp.pipeline.ComparableWord;
import org.agromax.core.nlp.pipeline.SPPipeline;
import org.agromax.core.nlp.pipeline.StanfordParser;
import org.agromax.util.Util;
import org.agromax.util.WordUtil;

import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Generates Subject, Predicate and Object triples
 *
 * @author Anurag Gautam
 */
public class SPOGenerator {

    private static final Logger logger = Logger.getLogger(SPOGenerator.class.getName());

    /*
    Don't allow anyone to instantiate
     */
    private SPOGenerator() {
    }

    private static final TreeSet<ComparableWord> lastSubjectPhrase = new TreeSet<>();


    private static TreeMap<ComparableWord, TreeSet<ComparableWord>> graph;

    public static TreeMap<ComparableWord, TreeSet<ComparableWord>> getRelationshipGraph(SPPipeline pipeline, CharSequence text) {
        pipeline.registerPipelineAction((taggedWords, dependency) -> {
            graph = getRelationshipGraph(dependency);
//            logger.info("Generated relationship graph");
        });

        pipeline.schedule(text);
        return graph;
    }

    public static TreeMap<ComparableWord, TreeSet<ComparableWord>> generate(SPPipeline pipeline, CharSequence text) {
        pipeline.registerPipelineAction((taggedWords, dependency) -> {
            graph = getRelationshipGraph(dependency);
//            logger.info("Generated relationship graph");
//            graph.forEach((k, v) -> System.out.println(k + " => " + v));
//            List<Triple<String, String, String>> triples = getTriples(dependency, taggedWords, graph);
//            logger.info(triples.size() + " triples generated");
//            triples.forEach(System.out::println);
        });

        pipeline.schedule(text);

        return graph;
    }

    /**
     * For each node of relationship graph, we do the following:
     * <ul>
     * <li>Think of that node as a <i>potential predicate</i></li>
     * <li>Traverse its adjacency list</li>
     * <li>Identify any potential subject and object phrase available</li>
     * </ul>
     */
    private static List<Triple<String, String, String>> getTriples(Collection<TypedDependency> dependencies,
                                                                   List<TaggedWord> taggedWords,
                                                                   TreeMap<ComparableWord, TreeSet<ComparableWord>> relationMap) {
        final Queue<ComparableWord> queue = new LinkedList<>();

        // Stores the list of visited nodes of the graph
        final TreeSet<ComparableWord> visited = new TreeSet<>();

        // Stores the probable SPO triples that are generated
        final List<Triple<String, String, String>> triples = new LinkedList<>();

        // For each node of the relationship graph, we try to identify any triple that could possibly be
        // generated
        for (ComparableWord u : relationMap.keySet()) {
            // Will store the subject phrase, that could contain many words
            TreeSet<ComparableWord> subjectPhrase = new TreeSet<>();

            // Will store object phrase
            TreeSet<ComparableWord> objectPhrase = new TreeSet<>();


            relationMap.get(u).stream().forEach(v -> {
                queue.clear();
                visited.clear();

                // Get the subject
                if (v.getIndex() < u.getIndex()) {
                    queue.add(v);
                    while (!queue.isEmpty()) {
                        ComparableWord w = queue.poll();
                        subjectPhrase.add(w);
                        if (!visited.contains(w)) {
                            if (relationMap.containsKey(w)) {
                                for (ComparableWord x : relationMap.get(w)) {
                                    subjectPhrase.add(x);
                                    queue.add(x);
                                }
                            }
                            visited.add(w);
                        }
                    }

                } else {

                    // Get the object
                    queue.add(v);
                    while (!queue.isEmpty()) {
                        ComparableWord w = queue.poll();
                        objectPhrase.add(w);
                        if (!visited.contains(w)) {
                            if (relationMap.containsKey(w)) {
                                for (ComparableWord x : relationMap.get(w)) {
                                    objectPhrase.add(x);
                                    queue.add(x);
                                }
                            }
                            visited.add(w);
                        }
                    }
                }
            });

            // Only if are able to identify at least one object and atleast one subject phrase
            // we consider the triple for output
            if (subjectPhrase.size() > 0 && objectPhrase.size() > 0) {

                /* Co-reference resolution phase
                 Replace pronouns with previous sentence's subject, the approach we use is very naive.
                 System simply replaces the pronouns with previous sentence's NNX words. Since there could be many such
                 words, a single co-reference could generate many *potential* triples.
                 */
//                System.out.println("Last subject phrase " + lastSubjectPhrase);
                List<TreeSet<ComparableWord>> potentialSubjectPhrases = new LinkedList<>();
                potentialSubjectPhrases.add(subjectPhrase);
                for (ComparableWord lastSubj : lastSubjectPhrase) {
                    TreeSet<ComparableWord> subj = new TreeSet<>();
                    for (ComparableWord w : subjectPhrase) {
                        if (w.getTag().equalsIgnoreCase("PRP") && w.getWord().equalsIgnoreCase("it")) {
                            subj.add(lastSubj);
                        } else {
                            subj.add(w);
                        }
                    }
                    potentialSubjectPhrases.add(subj);
                }
//                System.out.println(potentialSubjectPhrases);
                /*Stream<ComparableWord> subStream = subjectPhrase.stream().map(w -> {
                    if (w.getTag().equalsIgnoreCase("PRP") && w.getWord().equalsIgnoreCase("it") && !lastSubjectPhrase.isEmpty()) {
                        w.setWord(lastSubj);
                    }
                    return w;
                });
                */

                // Identify NNP word from subjectPhrase, will be used for co-reference resolution in the next sentence
                // of text
                lastSubjectPhrase.clear();
                subjectPhrase.forEach(w -> {
                    if (w.getTag().startsWith("NN")) {
                        lastSubjectPhrase.add(w);
                    }
                });
                objectPhrase.forEach(w -> {
                    if (w.getTag().startsWith("NN")) {
                        lastSubjectPhrase.add(w);
                    }
                });

                String object = WordUtil.join(objectPhrase.stream(), " ", w -> {
                    if (!Stopwords.STOPWORDS.contains(w.toLowerCase()))
                        return w;
                    return "";
                });
                String predicate = u.getWord();
//                System.out.println(object + " -- " + predicate);

                potentialSubjectPhrases.forEach(subjPhrase -> {
                    String subject = WordUtil.join(subjPhrase.stream(), " ", w -> {
                        if (!Stopwords.STOPWORDS.contains(w.toLowerCase()))
                            return w;
                        return "";
                    });
                    triples.add(new Triple<>(subject, predicate, object));
                });
            }

        }
        return triples;
    }

    /**
     * Generates the relationship graph. Works closely with Stanford Parser. This method generates a very important
     * graph that is the main ingredient of our triple generation algorithm.
     * <br>
     * <p>
     * Stanford dependencies are in general of following type: <br>
     * {@code dependency-name(governor-i, dependent-j)}, here {@code i, j} represents the index(1-based) of the word.
     * This method creates graph by considering {@code governor} and {@code dependent} as a <b>directed</b> edge.
     * </p>
     *
     * @param dependencies <code>Collection&lt;TypedDependency&gt;</code>
     * @return <code>TreeMap&lt;ComparableWord, TreeSet&lt;ComparableWord&gt;&gt;</code>
     */
    private static TreeMap<ComparableWord, TreeSet<ComparableWord>> getRelationshipGraph(Collection<TypedDependency> dependencies) {
        TreeMap<ComparableWord, TreeSet<ComparableWord>> relations = new TreeMap<>();
        for (TypedDependency d : dependencies) {
            ComparableWord gov = new ComparableWord(d.gov());
            ComparableWord dep = new ComparableWord(d.dep());

            if (relations.containsKey(gov)) {
                relations.get(gov).add(dep);
            } else {
                TreeSet<ComparableWord> set = new TreeSet<>();
                set.add(dep);
                relations.put(gov, set);
            }
        }
        return relations;
    }

    public static void main(String[] args) throws URISyntaxException {
        MaxentTagger tagger = new MaxentTagger(Util.SP_TAGGER_PATH);
        DependencyParser parser = DependencyParser.loadFromModelFile(Util.SP_MODEL_PATH);

        SPPipeline pipeline = new SPPipeline(new StanfordParser(tagger, parser));

        generate(pipeline, ResourceManager.getInstance().get(Util.dirPath("data", "test.txt").toString()));
    }
}
