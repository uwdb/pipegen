package org.brandonhaynes.pipegen.utilities;

import javassist.ClassPath;
import javassist.NotFoundException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarClassPath implements ClassPath {
    private final JarFile jarFile;
    private final String jarFileURL;

    public JarClassPath(URL jarUrl) throws NotFoundException, IOException {
        JarURLConnection connection = (JarURLConnection)jarUrl.openConnection();
        //jarFile = new JarFile(pathname.replace("file:", ""));
        //jarFileURL = new File(pathname).getCanonicalFile().toURI().toURL().toString();
        try {
            jarFileURL = connection.getJarFileURL().toString();
            jarFile = new JarFile(new File(connection.getJarFileURL().toURI()));
        } catch(URISyntaxException e) {
            throw new IOException(e);
        }
    }

    public InputStream openClassfile(String className) throws NotFoundException
    {
        try {
            JarEntry entry = getJarEntry(className);
            return entry != null ? jarFile.getInputStream(entry) : null;
        } catch (IOException e) {
            throw new NotFoundException(e.toString());
        }
    }

    public URL find(String className) {
        JarEntry jarEntry = getJarEntry(className);
        try {
            return jarEntry != null
                    ? new URL("jar:" + jarFileURL + "!/" + getJarName(className))
                    : null;
        } catch(MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    public void close() {
        try {
            jarFile.close();
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public String toString() {
        return jarFile == null ? "<null>" : jarFile.toString();
    }

    private JarEntry getJarEntry(String className) {
        return jarFile.getJarEntry(getJarName(className));
    }

    private static String getJarName(String className) {
        return className.replace('.', '/') + ".class";
    }
}
