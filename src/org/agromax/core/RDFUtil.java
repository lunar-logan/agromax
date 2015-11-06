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
import org.agromax.util.Util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.List;

/**
 * This is a temporary class, to be removed when the integration completes
 * Created by Anurag Gautam on 25-10-2015.
 */
public class RDFUtil {
    private static final String NAMESPACE = "http://agro.info/";

    private static String asURI(String val) throws UnsupportedEncodingException, URISyntaxException {
        URI uri = new URI(String.format("%s%s", NAMESPACE, URLEncoder.encode(val, "UTF-8")));
//        System.out.println("A fine url : " + uri);
        return uri.toString();
    }

    public static void dumpAsRDF(List<Triple<String, String, String>> triplets) throws IOException {
        final Model agroModel = ModelFactory.createDefaultModel();
        triplets.forEach(triple -> {
            try {
                String subj = asURI(triple.first);
                String pred = asURI(triple.second);
                String obj = triple.third;
                Resource newResource = agroModel.createResource(subj);
                Property property = agroModel.createProperty(pred);
                newResource.addProperty(property, obj);
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        });
        FileOutputStream fout = new FileOutputStream(Util.dirPath("data", "rdfDump.xml").toFile());
        agroModel.write(fout);
        fout.close();
    }
}
