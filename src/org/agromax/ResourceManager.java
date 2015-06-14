package org.agromax;

import com.mongodb.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Paths;

/**
 * @author Riddler
 */
public class ResourceManager {
    private static final ResourceManager ourInstance = new ResourceManager();

    private static final int BUFFER_SIZE = 1024; // In bytes

    private static final String DATABASE_NAME = "agromax";

    private static final String COLLECTION_NAME = "doc_cache";

    private final DBCollection cache;

    public static ResourceManager getInstance() {
        return ourInstance;
    }

    private ResourceManager() {
        cache = setupDB();
    }

    private DBCollection setupDB() {
        try {
            MongoClient mongoClient = new MongoClient();
            DB db = mongoClient.getDB(DATABASE_NAME);
            return db.getCollection(COLLECTION_NAME);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String get(URI theUri, boolean cacheRefresh) {
        String buffer = null;
        if (theUri.getScheme().equalsIgnoreCase("file"))
            buffer = retrieveLocal(theUri);
        else
            buffer = retrieveRemote(theUri, cacheRefresh);
        return buffer;
    }

    public String get(String path) throws URISyntaxException {
        if (path.matches("^[a-zA-Z]+://(/)?.+")) { // Its a URI
            return get(new URI(path), false);
        } else {
            // Probably a file path, without mentioned scheme "file"
            return get(Paths.get(path).toUri(), false);
        }
    }

    private String retrieveRemote(URI theUri, boolean cacheRefresh) {
        String uri = theUri.toString();

        BasicDBObject query = new BasicDBObject("uri", uri);
        String content = null;
        try {
            DBObject result = (cache != null) ? cache.findOne(query) : null;
            if (result == null || cacheRefresh) {
                Document soup = Jsoup.connect(uri).get();
                content = soup.body().text();

                BasicDBObject doc = new BasicDBObject("uri", uri).append("content", content);
                WriteResult writeResult = null;

                if (result == null && cache != null) writeResult = cache.insert(doc);
                else if (cacheRefresh && cache != null) writeResult = cache.update(result, doc);
                System.out.println("Trying to add: " + doc);
                System.out.println("Result: " + writeResult);

            } else { // Its probably a cache hit
                System.out.println("Cache hit: " + result);
                content = (String) result.get("content");
            }
        } catch (IOException | MongoTimeoutException e) {
            e.printStackTrace();
            try {
                Document soup = Jsoup.connect(uri).get();
                content = soup.body().text();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return content;
    }

    private String retrieveLocal(URI theUri) {
        File file = new File(theUri);
        try {
            Reader reader = new InputStreamReader(new FileInputStream(file));
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[BUFFER_SIZE];
            while (true) {
                int readCount = reader.read(buffer);
                if (readCount < 0) break;
                builder.append(buffer, 0, readCount);
            }
            reader.close();
            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
/*
    public static void main(String[] args) throws URISyntaxException {
        ResourceManager rm = ResourceManager.getInstance();
        System.out.println(rm.get("http://iitk.ac.in/doaa"));
        System.out.println(rm.get(System.getProperty("user.dir") + "\\.gitignore"));
    }
*/
}
