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

package org.agromax.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.agromax.Util.inOpenRange;

/**
 * @author The Vision
 */
public class VocabStore {

    public VocabStore() {
        this.root = new KnowledgeNode("_root@" + System.currentTimeMillis());
        graph = new Graph();
    }

    private static boolean isValidCatalog(String catalogue) {
        return catalogue.matches("([0-9]+\\.)+");
    }

    /**
     * Inserts a new label into the tree
     */
    public void insert(String id, String label) {
        if (!isValidCatalog(id)) //
            throw new IllegalArgumentException(String.format("Invalid catalogue id '%s', catalogue id must be of the form ([0-9]+\\.)+", id));

        String[] ids = id.split("\\.");
        KnowledgeNode p = root;

        for (String iid : ids) {
            int ii = Integer.parseInt(iid);
            if (p.hasChild(ii)) {
                p = p.getChild(ii);
            } else {
                throw new RuntimeException("Inconsistent Store state found. Does not contain the id:" + ii);
            }
        }

        int jj = Integer.parseInt(ids[ids.length - 1]);
        p.insert(jj, new KnowledgeNode(label));
        graph.addUndirectedEdge(p.getLabel(), label);
    }

    public Set<String> getRelatedDomains(String term) {
        return graph.relatedTerms(term, true);
    }

    public List<String> getSubDomains(String... hierarchy) {
        final List<String> subDomains = new LinkedList<String>();
        assert hierarchy.length >= 1;
        String master = hierarchy[0];
        KnowledgeNode masterNode = null;
        for (int i = 0; i < root.children.length; i++) {
            if (root.children[i] != null && root.children[i].label.equalsIgnoreCase(master))
                masterNode = root.children[i];
        }
        if (masterNode == null) return subDomains;

        for (int i = 1; i < hierarchy.length; i++)
            for (int j = 0; j < masterNode.children.length; j++) {
                if (masterNode.children[j] != null && masterNode.children[j].label.equalsIgnoreCase(hierarchy[i])) {
                    masterNode = masterNode.children[j];
                    break;
                }
            }

        for (int i = 0; i < masterNode.children.length; i++) {
            if (masterNode.children[i] != null)
                subDomains.add(masterNode.children[i].label);
        }

        return subDomains;
    }

    @Override
    public String toString() {
        return graph.toString();
    }

    /**
     * Represents a domain
     */
    static class KnowledgeNode {

        private static final int MAX_CHILDREN = 50;

        private final String label;
        private final KnowledgeNode[] children;

        public KnowledgeNode(String label) {
            this.label = label;
            children = new KnowledgeNode[MAX_CHILDREN];
        }

        public KnowledgeNode getChild(int index) {
            if (inOpenRange(-1, index, MAX_CHILDREN)) // -1 < id < MAX_CHILDREN
                return children[index];
            else
                throw new RuntimeException("index out of bounds");
        }

        public void insert(int id, KnowledgeNode kn) {
            if (inOpenRange(-1, id, MAX_CHILDREN)) { // -1 < id < MAX_CHILDREN
                if (children[id] != null)
                    System.err.println("Warning: " + id + " all-ready present as an id.");
                children[id] = kn;
            }
        }

        public String getLabel() {
            return label;
        }

        @Override
        public String toString() {
            StringBuilder branches = new StringBuilder();
            for (String line : Arrays.toString(children).split("\n")) {
                branches.append(" ").append(line).append("\n");
            }
            return "{" + "\n" +
                    " label: " + label + "\n" +
                    " children: " + branches + "\n" +
                    "}";
        }

        public boolean hasChild(int index) {
            return children[index] != null;
        }
    }

    private final KnowledgeNode root;

    private final Graph graph;
}
