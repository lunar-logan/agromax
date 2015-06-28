import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import org.agromax.core.nlp.pipeline.SPPipeline;
import org.agromax.core.nlp.pipeline.StanfordParser;
import org.agromax.platform.bootloader.*;
import org.agromax.platform.server.CmdHost;
import org.agromax.util.Util;

import java.net.URISyntaxException;
import java.util.logging.Logger;

/**
 * TODO: ResourceManager class needs attention. Unable to handle path separator character.
 *
 * @author Anurag Gautam
 */
public class Main {
    static SPPipeline pipeline = null;

    static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws URISyntaxException, InterruptedException {

//        logger.info("Creating a default model, using ModelFactory");
//        Model model = ModelFactory.createDefaultModel();
//
//        logger.info("Creating a new resource Anurag");
//        Resource Anurag = model.createResource("http://agromax.org/Person+We+Me");
//        logger.info("Resource " + Anurag + " has been created");
//
//        logger.info("Creating a property named \"fullName\"");
//        Property fullName = model.createProperty("http://anurag.me/FullName");
//
//        logger.info("Adding property \"fullName\" to the resource \"Anurag\"");
//        Anurag.addProperty(fullName, "Anurag Gautam is a good boy");
//
//        logger.info("Writing model in RDF on the stdout");
//        model.write(System.out);
//
//
//        ResourceManager rm = ResourceManager.getInstance();
//        AgroMax.getTriples(rm.get(System.getProperty("user.dir") + "/data/test.txt"));
//            System.out.println(VocabLoader.loadAll());

        Bootloader bootloader = new Bootloader();

        // Add action to load stanford parser
        bootloader.addAction(new BootAction() {
            @Override
            public String getName() {
                return "Stanford parser loader";
            }

            @Override
            public BootActionType getType() {
                return BootActionType.REQUIRED;
            }

            @Override
            public void perform() throws BootActionException {
                MaxentTagger tagger = new MaxentTagger(Util.SP_TAGGER_PATH);
                DependencyParser parser = DependencyParser.loadFromModelFile(Util.SP_MODEL_PATH);
                pipeline = new SPPipeline(new StanfordParser(tagger, parser));
            }
        });

        BootResult result = bootloader.boot();
        logger.info("Booting completed in " + result.getBootTime() + " sec(s).");
        if (pipeline != null) {
            logger.info("Parser context created");
//            EventLoop eventLoop = EventLoop.getInstance(pipeline);
//            Thread serverThread = new Thread(eventLoop);
//            serverThread.start();
//            serverThread.join();
            CmdHost host = new CmdHost(pipeline);
            host.run();
        } else {
            logger.severe("Parser context could not be created");
        }

    }
}
