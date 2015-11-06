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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.BadURIException;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;
import org.agromax.ResourceManager;
import org.agromax.core.nlp.pipeline.ComparableWord;
import org.agromax.util.Util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.net.URLEncoder.encode;
import static org.agromax.util.WordUtil.filterStopwords;
import static org.agromax.util.WordUtil.weld;

/**
 * This class, as named, generates RDF triples from a sentence.
 * For better results <b>electrocute</b> your brain weekly.
 * Update: 25/10/2015 : I'm reworking this class
 *
 * @deprecated
 * @author Harley Quinn
 */
public class RDFGenerator {

    private static final Logger logger = Logger.getLogger(RDFGenerator.class.getName());

    private static String lastSubjectPhrase = "";

    public static Model getTriples(String text) {
        MaxentTagger tagger = new MaxentTagger(Util.SP_TAGGER_PATH);
        DependencyParser parser = DependencyParser.loadFromModelFile(Util.SP_MODEL_PATH);

        logger.info("Beginning to process the text. Text size: " + text.length());
        DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));
        logger.info("Text processing finish");

        Model agroModel = ModelFactory.createDefaultModel();

        for (List<HasWord> sentence : tokenizer) {
            List<TaggedWord> tagged = tagger.tagSentence(sentence);
            GrammaticalStructure gs = parser.predict(tagged);

            // Print typed dependencies
            triples(gs, (ArrayList<TaggedWord>) tagged, agroModel);

            // For debugging purpose
            /*System.out.println("------------------------------------");
            gs.allTypedDependencies().forEach(e -> {
                System.out.println(e.reln() + "[ " + e.gov() + "(" + e.gov().index() + ") ---> " + e.dep() + "(" + e.dep().index() + ") ]");
            });
            System.out.println("-------------------------------------");*/
        }
        return agroModel;
    }

    private static Model triples(GrammaticalStructure gs, ArrayList<TaggedWord> tagged, Model agroModel) {
        final TreeMap<ComparableWord, TreeSet<ComparableWord>> relations = new TreeMap<>();

        for (TypedDependency d : gs.typedDependencies()) {

            ComparableWord gov = new ComparableWord(d.gov().toString(), d.gov().index());
            ComparableWord dep = new ComparableWord(d.dep().toString(), d.dep().index());
//            String dep = d.dep().toString();

            if (relations.containsKey(gov)) {
                relations.get(gov).add(dep);//new ComparableWord(dep, d.dep().index()));
            } else {
                TreeSet<ComparableWord> set = new TreeSet<>();
                set.add(dep);//new ComparableWord(dep, d.dep().index()));
                relations.put(gov, set);
            }
        }

        logger.info(" ===== Relations =====");
        logger.info(relations.toString());

        // Find the triples

        // Stores the last subject phrase, used for co-reference resolution

        final HashSet<ComparableWord> visited = new HashSet<>();

        relations.forEach((p, o) -> {
            if (!visited.contains(p)) {
                final TreeSet<ComparableWord> subjectPhrase = new TreeSet<ComparableWord>();
                final TreeSet<ComparableWord> objectPhrase = new TreeSet<ComparableWord>();
                if (o.size() > 1) {
                    o.forEach(e -> {
                        if (e.getIndex() < p.getIndex()) {
                            subjectPhrase.add(e);
                            if (relations.containsKey(e)) {
                                relations.get(e).forEach(w -> {
                                    if (w.getIndex() < e.getIndex())
                                        subjectPhrase.add(w);
                                });
//                            subjectPhrase.addAll(relations.get(e));
                            }
                        } else {
                            objectPhrase.add(e);

                            Queue<ComparableWord> queue = new LinkedList<ComparableWord>();
                            queue.add(e);
                            while (!queue.isEmpty()) {
                                ComparableWord e0 = queue.poll();
                                if (relations.containsKey(e0)) {
                                    relations.get(e0).forEach(w -> {
//                                    if (w.getIndex() < e.getIndex())
                                        objectPhrase.add(w);
                                        queue.add(w);
                                    });
                                }
//                                objectPhrase.addAll(relations.get(e));
                            }
                        }
                    });
                }
                if (!subjectPhrase.isEmpty() && !objectPhrase.isEmpty()) {
                    // Replace all PRP(It) words with the lastSubjectPhrase
                    Stream<ComparableWord> subStream = subjectPhrase.stream().map(w -> {
                        if (w.getTag().equalsIgnoreCase("PRP") && w.getWord().equalsIgnoreCase("it") && !lastSubjectPhrase.isEmpty()) {
                            w.setWord(lastSubjectPhrase);
                        }
                        return w;
                    });

                    // Identify NNP word from subjectPhrase
                    subjectPhrase.forEach(w -> {
                        if (w.getTag().startsWith("NNP")) {
                            lastSubjectPhrase = w.getWord();
                        }
                    });

                    Resource subj = null;
                    Property predicate = null;
                    try {
                        subj = agroModel.createResource(Util.url(encode(weld(filterStopwords(subStream), " "), "utf-8")));
//                    String propertyName = "http://agromax.org/" + encode(p.getWord(), "utf-8");
//                    logger.log(Level.OFF, "Adding property \"" + propertyName + "\"");
                        predicate = agroModel.createProperty(Util.url(encode(p.getWord(), "utf-8")));
                        subj.addProperty(predicate, weld(objectPhrase.stream(), " "));

                    } catch (UnsupportedEncodingException | BadURIException e) {
                        System.err.println(e.getMessage());
                    }
                }
            }
            visited.add(p);
        });

        return agroModel;

    }

    public static void main(String[] args) throws URISyntaxException, IOException {

        ResourceManager rm = ResourceManager.getInstance();
        Model triples = getTriples(rm.get(Paths.get(Util.DATA_DIR, "data", "test1.txt").toString()));

        FileOutputStream outputFile = new FileOutputStream(Util.dir(Util.DATA_DIR,"data", "output.xml"));
        logger.info("Writing output to " + outputFile);
        triples.write(outputFile);
        outputFile.flush();
        outputFile.close();
    }
}
