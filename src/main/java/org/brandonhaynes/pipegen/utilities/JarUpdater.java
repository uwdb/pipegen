package org.brandonhaynes.pipegen.utilities;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.brandonhaynes.pipegen.configuration.Version;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class JarUpdater {
    public static void addClass(final JarFile jar, final CtClass cc, final Version version)
            throws CannotCompileException, IOException {
        String temporaryDirectory = System.getProperty("java.io.tmpdir");
        cc.getClassFile().setMajorVersion(version.getMajor());
        cc.writeFile(temporaryDirectory);
        addFile(jar, new File(String.format("%s/%s.class", temporaryDirectory, cc.getSimpleName())), cc.toBytecode());
    }

    public static void replaceClass(final URL jarUrl, final CtClass cc)
            throws CannotCompileException, IOException {
        replaceClass(jarUrl, cc, new Version(cc.getClassFile2().getMajorVersion(),
                                             cc.getClassFile2().getMinorVersion()));
    }

    public static void replaceClass(final URL jarUrl, final CtClass cc, final Version version)
            throws CannotCompileException, IOException {
        replaceClass(new JarFile(((JarURLConnection)jarUrl.openConnection()).getJarFileURL().getFile()), cc, version);
    }

    public static void replaceClasses(final URL jarUrl, final ClassPool pool, final Collection<Class> classes,
                                         final Version version)
            throws CannotCompileException, IOException, NotFoundException {
        for(Class clazz: classes)
            replaceClass(new JarFile(((JarURLConnection)jarUrl.openConnection()).getJarFileURL().getFile()),
                         pool.get(clazz.getName()), version);
    }

    public static void replaceClasses(final URL jarUrl, final Version version, final CtClass... ccs)
            throws CannotCompileException, IOException {
        for(CtClass cc: ccs)
            replaceClass(new JarFile(((JarURLConnection)jarUrl.openConnection()).getJarFileURL().getFile()),
                         cc, version);
    }

    public static void replaceClass(final JarFile jar, final CtClass cc, final Version version)
            throws CannotCompileException, IOException {
        String temporaryDirectory = System.getProperty("java.io.tmpdir");
        cc.defrost();
        cc.getClassFile().setMajorVersion(version.getMajor());
        cc.writeFile(temporaryDirectory);
        replaceFile(jar, toClassPath(cc), cc.toBytecode());
    }

    public static void addFile(final JarFile jar, final File classFile, final byte[] fileBytecode) throws IOException {
        if(Iterables.any(getEntryIterator(jar), entry -> isMatchingEntry(entry, classFile)))
            throw new IOException("Specified file already exists in the jar.");

        replaceFile(jar, classFile, fileBytecode);
    }

    public static void replaceFile(final JarFile jar, final File classFile, final byte[] fileBytecode) throws IOException {
        File jarFile = new File(jar.getName());
        File stagingJarFile = File.createTempFile(jar.getName(), null);
        byte[] buffer = new byte[4096];
        int bytesRead;

        try(FileOutputStream jarStream = new FileOutputStream(stagingJarFile)) {
            try(JarOutputStream stagingJar = new JarOutputStream(jarStream)) {
                stagingJar.putNextEntry(new JarEntry(classFile.getPath()));
                stagingJar.write(fileBytecode);

                for (JarEntry entry : getEntryIterator(jar)) {
                    if (!isMatchingEntry(entry, classFile))
                        try (InputStream stream = jar.getInputStream(entry)) {
                            stagingJar.putNextEntry(entry);

                            while ((bytesRead = stream.read(buffer)) != -1)
                                stagingJar.write(buffer, 0, bytesRead);
                        }
                }
            }

            Files.move(stagingJarFile.toPath(), jarFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
            waitForCompletion();
        }
    }

    private static File toClassPath(CtClass cc) {
        return new File(cc.getClassFile2().getName().replace(".", File.separator) + ".class");
    }

    private static Iterable<JarEntry> getEntryIterator(final JarFile jar) {
        return () -> Iterators.forEnumeration(jar.entries());
    }

    private static boolean isMatchingEntry(final JarEntry entry, final File classFile) {
        return entry.getName().equals(classFile.getPath());
    }

    private static void waitForCompletion() {
        try {
            Thread.sleep(750);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}