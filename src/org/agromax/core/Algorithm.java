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
import edu.stanford.nlp.trees.TypedDependency;
import org.agromax.core.nlp.pipeline.ComparableWord;
import org.agromax.util.Util;
import org.agromax.util.WordUtil;

import java.util.*;

/**
 * This is the core algorithm that identifies Subject-Predicate-Objects from an English sentence. This class
 * <b>does not</b> generates RDF triples. Instead it simply returns a list of SPO triples. Use {@code RDFGenerator} class to
 * generate actual RDF triples.
 *
 * @author Anurag Gautam
 * @author Maya Gautam
 * @version 1.2 revision on: 25/7/2015
 * @see RDFGenerator
 * @since 1.2
 */
public class Algorithm {
    /**
     * Suppress instantiation of this class
     */
    private Algorithm() {
    }

    private static final TreeSet<ComparableWord> lastSubjectPhrase = new TreeSet<>();

    public static List<Triple<String, String, String>> getTriples(Collection<TypedDependency> dependencies,
                                                                  Collection<? extends TaggedWord> taggedWords,
                                                                  TreeMap<ComparableWord, TreeSet<ComparableWord>> relationMap,
                                                                  boolean doAnaphora) {
        System.out.println(taggedWords);
        if (doAnaphora) {
            return getTriples(dependencies, taggedWords, relationMap);
        }
        return getTriplesWithoutAnaphora(dependencies, taggedWords, relationMap);

    }

    /**
     * Tries to identify the SPO triples without considering anaphora resolution
     *
     * @param dependencies
     * @param taggedWords
     * @param relationMap  the relationship graph of the sentence
     * @return
     */
    private static List<Triple<String, String, String>>
    getTriplesWithoutAnaphora(Collection<TypedDependency> dependencies,
                              Collection<? extends TaggedWord> taggedWords,
                              TreeMap<ComparableWord, TreeSet<ComparableWord>> relationMap) {

        // TODO explain its use case?
        final Queue<ComparableWord> queue = new LinkedList<>();

        // Stores the list of visited nodes of the graph
        final TreeSet<ComparableWord> visited = new TreeSet<>();

        // Stores the SPO triples that gets generated
        final List<Triple<String, String, String>> triples = new LinkedList<>();

        Util.log("Got following relationship graph:\n", relationMap);

        // For each node of the relationship graph, we try to identify any triple(S-P-O triple) that could possibly be
        // generated
        for (ComparableWord u : relationMap.keySet()) {
            // Will store the subject phrase, that could contain many words
            TreeSet<ComparableWord> subjectPhrase = new TreeSet<>();

            // Will store object phrase, this could also contain many words, will weld them
            // in the end
            TreeSet<ComparableWord> objectPhrase = new TreeSet<>();

            // List of potential predicate words
            TreeSet<ComparableWord> predicatePhrase = new TreeSet<>();
            predicatePhrase.add(u);

            boolean verbFound = false;

            // Step #1: Traverse the adjacency list of node "u"
            for (ComparableWord v : relationMap.get(u)) {
                queue.clear();
                visited.clear();

                /*
                Get the subject:
                    #1: Find all the words in the adj. list whose index < index of "u" in the original sentence
                    #2: Think of them as words. But it has a catch. Node "u" might not be a predicate. In fact it could
                        be an NNX type word.
                        In this case, we keep a track on the tag of the word. And won't allow and VBX type words to
                        get into the subject phrase. Such words will become the predicate and following words become
                        the object of the sentence.
                 */
                if (v.getIndex() < u.getIndex() && !verbFound) {
                    if (v.getTag().startsWith("VB")) {
                        predicatePhrase.add(v);
                        verbFound = true;
                    } else {
                        queue.add(v);
                        // Expand the vertex to get any amod, ajcom relations dependent
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
                    }

                } else if (verbFound && v.getTag().startsWith("VB")) {
                    predicatePhrase.add(v);
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
            }


            // Only if are able to identify at least one object and atleast one subject phrase
            // we consider the triple for output
            if (subjectPhrase.size() > 0 && objectPhrase.size() > 0) {

                // Test
                // TODO explain this piece of code?
                if (!predicatePhrase.isEmpty()) {
                    if (u.getIndex() > subjectPhrase.last().getIndex()) {
                        objectPhrase.add(u);
                    } else {
                        subjectPhrase.add(u);
                    }
                } else {
                    predicatePhrase.add(u);
                }


                String object = WordUtil.join(objectPhrase.stream(), " ", w -> {
                    if (!Stopwords.STOPWORDS.contains(w.toLowerCase()))
                        return w;
                    return w;
                });
                String predicate = Util.weld(predicatePhrase, " ");//u.getWord();
                String subject = Util.weld(subjectPhrase, " ");
                triples.add(new Triple<>(subject, predicate, object));
            }
        }
        return triples;
    }

    /**
     * Generates all the subject-predicate-objects triples possible from a sentence.
     *
     * @param dependencies Stanford dependency structure
     * @param taggedWords  List of POS tagged words returned by Stanford parser
     * @param relationMap  relationship graph
     */
    public static List<Triple<String, String, String>> getTriples(Collection<TypedDependency> dependencies,
                                                                  Collection<? extends TaggedWord> taggedWords,
                                                                  TreeMap<ComparableWord, TreeSet<ComparableWord>> relationMap) {
        final Queue<ComparableWord> queue = new LinkedList<>();

        // Stores the list of visited nodes of the graph
        final TreeSet<ComparableWord> visited = new TreeSet<>();

        // Stores the probable SPO triples that are generated
        final List<Triple<String, String, String>> triples = new LinkedList<>();

        Util.log("Got following relationship graph:\n", relationMap);

        // For each node of the relationship graph, we try to identify any triple(S-P-O triple) that could possibly be
        // generated
        for (ComparableWord u : relationMap.keySet()) {
            // Will store the subject phrase, that could contain many words
            TreeSet<ComparableWord> subjectPhrase = new TreeSet<>();

            // Will store object phrase, this could also contain many words, will weld them
            // in the end
            TreeSet<ComparableWord> objectPhrase = new TreeSet<>();

            // List of potential predicate words
            TreeSet<ComparableWord> predicatePhrase = new TreeSet<>();

            boolean verbFound = false;

            // Step #1: Traverse the adjacency list of node "u"
            for (ComparableWord v : relationMap.get(u)) {
                queue.clear();
                visited.clear();

                /*
                Get the subject:
                    #1: Find all the words in the adj. list whose index < index of "u" in the original sentence
                    #2: Think of them as words. But it has a catch. Node "u" might not be a predicate. In fact it could
                        be an NNX type word.
                        In this case, we keep a track on the tag of the word. And won't allow and VBX type words to
                        get into the subject phrase. Such words will become the predicate and following words become
                        the object of the sentence.
                 */
                if (v.getIndex() < u.getIndex() && !verbFound) {
                    if (v.getTag().startsWith("VB")) {
                        predicatePhrase.add(v);
                        verbFound = true;
                    } else {
                        queue.add(v);
                        // Expand the vertex to get any amod, ajcom relations dependent
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
                    }

                } else if (verbFound && v.getTag().startsWith("VB")) {
                    predicatePhrase.add(v);
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
            }


            // Only if are able to identify at least one object and atleast one subject phrase
            // we consider the triple for output
            if (subjectPhrase.size() > 0 && objectPhrase.size() > 0) {

                // Test
                if (!predicatePhrase.isEmpty()) {
                    if (u.getIndex() > subjectPhrase.last().getIndex()) {
                        objectPhrase.add(u);
                    } else {
                        subjectPhrase.add(u);
                    }
                } else {
                    predicatePhrase.add(u);
                }

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
                    return w;
                });
                String predicate = Util.weld(predicatePhrase, " ");//u.getWord();
//                System.out.println(object + " -- " + predicate);

                potentialSubjectPhrases.forEach(subjPhrase -> {
                    String subject = WordUtil.join(subjPhrase.stream(), " ", w -> {
                        if (!Stopwords.STOPWORDS.contains(w.toLowerCase()))
                            return w;
                        return w;
                    });
                    triples.add(new Triple<>(subject, predicate, object));
                });
            }

        }

        return triples;
    }
}
