package org.agromax;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Riddler
 */
public class ResourceManager {
    private static final ResourceManager ourInstance = new ResourceManager();

    private boolean cacheRefresh = false;

    public static ResourceManager getInstance() {
        return ourInstance;
    }

    private ResourceManager() {
    }

    public StringBuffer get(String uri) {
        StringBuffer buffer = null;
        try {
            URI theUri = new URI(uri);
            if (theUri.getScheme().equalsIgnoreCase("file"))
                buffer = retrieveLocal(theUri);
            else
                buffer = retrieveRemote(theUri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    private StringBuffer retrieveRemote(URI theUri) {
        return new StringBuffer();
    }

    private StringBuffer retrieveLocal(URI theUri) {
        File file = new File(theUri);
        try {
            Reader reader = new InputStreamReader(new FileInputStream(file));
            StringBuffer builder = new StringBuffer();
            char[] buffer = new char[1024];
            while (true) {
                int readCount = reader.read(buffer);
                if (readCount < 0) break;
                builder.append(buffer, 0, readCount);
            }
            reader.close();
            return builder;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        ResourceManager rm = ResourceManager.getInstance();
        rm.get("file:///E:/google.co.in");
    }
}
