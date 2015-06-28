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

import java.util.Collection;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This is the core algorithm that identifies Subject-Predicate-Objects from an English sentence and
 * generates RDF triples.
 *
 * @author Anurag Gautam
 * @author Maya Gautam
 * @version 1.0
 * @since 1.2
 */
public class Maya {
    /**
     * Suppress instantiation of this class
     */
    private Maya() {
    }

    /**
     * Generates all the subject-predicate-objects triples possible from a sentence.
     *
     * @param dependencies
     * @param taggedWords
     * @param relationMap  relationship graph
     */
    public static void getTriples(Collection<TypedDependency> dependencies,
                                  Collection<TaggedWord> taggedWords,
                                  TreeMap<ComparableWord, TreeSet<ComparableWord>> relationMap) {

    }
}
