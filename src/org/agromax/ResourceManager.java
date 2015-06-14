package org.agromax;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.URI;
import java.nio.file.Paths;

/**
 * @author Riddler
 */
public class ResourceManager {
    private static final ResourceManager ourInstance = new ResourceManager();

    private boolean cacheRefresh = false;

    private static final int BUFFER_SIZE = 1024; // In bytes

    public static ResourceManager getInstance() {
        return ourInstance;
    }

    private ResourceManager() {
    }

    public String get(URI theUri) {
        String buffer = null;
        if (theUri.getScheme().equalsIgnoreCase("file"))
            buffer = retrieveLocal(theUri);
        else
            buffer = retrieveRemote(theUri);
        return buffer;
    }

    private String retrieveRemote(URI theUri) {
        try {
            Document soup = Jsoup.connect(theUri.toString()).get();
            return soup.body().text();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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

    public static void main(String[] args) {
        ResourceManager rm = ResourceManager.getInstance();
        System.out.println(System.getProperty("user.dir"));
        System.out.println(rm.get(Paths.get(System.getProperty("user.dir") + "\\.gitignore").toUri()));
        System.out.println(rm.get(URI.create("http://iitk.ac.in")));
    }
}
