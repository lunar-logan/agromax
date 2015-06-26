package org.agromax;

import org.agromax.core.TripleGenerator;
import org.agromax.util.FileUtil;
import org.agromax.util.Util;

import java.util.List;

/**
 * @author Anurag Gautam
 */
public class AgroMax {
    public static void main(String[] args) {
        String text = FileUtil.read(Util.dirPath("data", "test.txt"));
        List<TripleGenerator.Triple<String, String, String>> triples = TripleGenerator.getTriples(text);
        String markup = TripleGenerator.publish(triples);
        FileUtil.write(markup, Util.dirPath("data", "output.html"));
//        Console console = System.console();
    }
}