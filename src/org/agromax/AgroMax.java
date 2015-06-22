package org.agromax;

import org.agromax.core.TripleGenerator;
import org.agromax.core.Word;
import org.agromax.util.FileUtil;
import org.agromax.util.Util;

import java.io.Console;
import java.util.List;
import java.util.TreeSet;

/**
 * @author Bruce Wayne
 */
public class AgroMax {
    public static void main(String[] args) {
        String text = FileUtil.read(Util.dirPath("data", "test.txt"));
        List<TripleGenerator.Triple<TreeSet<Word>, Word, TreeSet<Word>>> triples = TripleGenerator.getTriples(text);
        String markup = TripleGenerator.publish(triples);
        FileUtil.write(markup, Util.dirPath("data", "output.html"));
        Console console = System.console();
    }
}