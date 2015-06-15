import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import java.util.logging.Logger;

/**
 * @author Anurag Gautam
 */
public class Main {
    static Logger logger = Logger.getLogger("AgromaxLogger");

    public static void main(String[] args) {

        logger.info("Creating a default model, using ModelFactory");
        Model model = ModelFactory.createDefaultModel();

        logger.info("Creating a new resource Anurag");
        Resource Anurag = model.createResource();
        logger.info("Resource " + Anurag + " has been created");

        logger.info("Creating a property named \"fullName\"");
        Property fullName = model.createProperty("http://anurag.me/FullName");

        logger.info("Adding property \"fullName\" to the resource \"Anurag\"");
        Anurag.addProperty(fullName, "Anurag Gautam");

        logger.info("Writing model in RDF on the stdout");
        model.write(System.out);


    }
}
