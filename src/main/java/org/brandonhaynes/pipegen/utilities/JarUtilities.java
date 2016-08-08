package org.brandonhaynes.pipegen.utilities;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.logging.Logger;

public class JarUtilities {
    private static final Logger log = Logger.getLogger(JarUtilities.class.getName());

    public static void addClass(final JarFile jar, final CtClass cc, final Version version, final Path backupPath)
            throws CannotCompileException, IOException {
        String temporaryDirectory = System.getProperty("java.io.tmpdir");
        cc.getClassFile().setMajorVersion(version.getMajor());
        cc.writeFile(temporaryDirectory);
        addFile(jar, new File(String.format("%s/%s.class", temporaryDirectory, cc.getSimpleName())),
                cc.toBytecode(), backupPath);
    }

    public static void replaceClass(final URL jarUrl, final CtClass cc, final Path backupPath)
            throws CannotCompileException, IOException {
        replaceClass(jarUrl, cc, new Version(cc.getClassFile2().getMajorVersion(),
                cc.getClassFile2().getMinorVersion()), backupPath);
    }

    public static void replaceClass(final URL jarUrl, final CtClass cc, final Version version, final Path backupPath)
            throws CannotCompileException, IOException {
        replaceClass(new JarFile(((JarURLConnection) jarUrl.openConnection()).getJarFileURL().getFile()),
                     cc, version, backupPath);
    }

    public static void replaceClasses(final URL jarUrl, final ClassPool pool, final Collection<Class> classes,
                                      final Version version, final Path backupPath)
            throws CannotCompileException, IOException, NotFoundException {
        Collection<CompiledClass> compiledClasses = Lists.newArrayList();
        for (Class clazz : classes)
            compiledClasses.add(new CompiledClass(pool.get(clazz.getName()), version));
        replaceFiles(new JarFile(((JarURLConnection) jarUrl.openConnection()).getJarFileURL().getFile()),
                compiledClasses, backupPath);
    }

    public static void replaceClasses(final URL jarUrl, final Version version,
                                      final Path backupPath, final CtClass... ccs)
            throws CannotCompileException, IOException {
        Collection<CompiledClass> classes = Lists.newArrayList();
        for (CtClass cc : ccs)
            classes.add(new CompiledClass(cc, version));
        replaceFiles(new JarFile(((JarURLConnection) jarUrl.openConnection()).getJarFileURL().getFile()),
                classes, backupPath);
    }

    public static void replaceClass(final JarFile jar, final CtClass cc, final Version version, final Path backupPath)
            throws CannotCompileException, IOException {
        replaceFiles(jar, Lists.newArrayList(new CompiledClass(cc, version)), backupPath);
    }

    public static void replaceClass(final JarFile jar, final File classFile, byte[] bytecode, final Path backupPath)
            throws CannotCompileException, IOException {
        replaceFiles(jar, Lists.newArrayList(new CompiledClass(classFile, bytecode)), backupPath);
    }

    public static void addFile(final JarFile jar, final File classFile,
                               final byte[] fileBytecode, final Path backupPath)
            throws IOException {
        Collection<CompiledClass> classes = Lists.newArrayList(new CompiledClass(classFile, fileBytecode));
        if(Iterables.any(getEntryIterator(jar), entry -> isMatchingEntry(entry, classes)))
            throw new IOException("Specified file already exists in the jar.");

        replaceFiles(jar, classes, backupPath);
    }

    public static List<Class> getClasses(final String jarFilename, boolean throwOnResolutionFailure) {
        try {
            return getClasses(new JarFile(jarFilename), throwOnResolutionFailure);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Class> getClasses(final JarFile jar, boolean throwOnResolutionFailure) {
        List<Class> classes = Lists.newArrayList();

        for (JarEntry entry : getEntryIterator(jar))
            if (entry.getName().endsWith(".class"))
                try {
                    classes.add(Class.forName(toQualifiedClass(entry.getName())));
                } catch(ClassNotFoundException|NoClassDefFoundError e) {
                    if(throwOnResolutionFailure)
                        throw new RuntimeException(e);
                    else
                        log.info("Could not resolve class " + entry.getName());
                }

        return classes;
    }

    private static synchronized void replaceFiles(final JarFile jar,
                                                  final Collection<CompiledClass> classes,
                                                  final Path backupPath) throws IOException {
        File jarFile = new File(jar.getName());
        File stagingJarFile = File.createTempFile(jar.getName(), null);
        byte[] buffer = new byte[4096];
        int bytesRead;

        backupJarFile(jarFile.toPath(), backupPath);

        try(FileOutputStream jarStream = new FileOutputStream(stagingJarFile)) {
            try(JarOutputStream stagingJar = new JarOutputStream(jarStream)) {
                for(CompiledClass compiledClass: classes) {
                    stagingJar.putNextEntry(new JarEntry(compiledClass.getClassFile().getPath()));
                    stagingJar.write(compiledClass.getBytecode());
                }

                for (JarEntry entry : getEntryIterator(jar)) {
                    if (!isMatchingEntry(entry, classes))
                        try (InputStream stream = jar.getInputStream(entry)) {
                            entry.setCompressedSize(-1);
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

    private static void backupJarFile(Path jarCurrentFile, Path backupPath) throws IOException {
        Path jarBackup = backupPath != null ? Paths.get(backupPath.toString() + "/" + jarCurrentFile.toString()) : null;
        Path jarBackupDirectory = jarBackup != null ? jarBackup.getParent() : null;

        if(jarBackupDirectory != null && !jarBackupDirectory.toFile().exists())
            jarBackupDirectory.toFile().mkdirs();
        if(jarBackup != null && !jarBackup.toFile().exists())
            Files.copy(jarCurrentFile, jarBackup);
    }

    private static File toClassPath(CtClass cc) {
        return new File(cc.getClassFile2().getName().replace(".", File.separator) + ".class");
    }

    private static String toQualifiedClass(String classPath) {
        return classPath.replace("/", ".").replace(".class", "");
    }

    private static Iterable<JarEntry> getEntryIterator(final JarFile jar) {
        return () -> Iterators.forEnumeration(jar.entries());
    }

    private static boolean isMatchingEntry(final JarEntry entry, final Collection<CompiledClass> classes) {
        return classes.stream().anyMatch(c -> entry.getName().equals(c.getClassFile().getPath()));
        //return entry.getName().equals(classFile.getPath());
    }

    private static void waitForCompletion() {
        try {
            Thread.sleep(750);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static class CompiledClass {
        private final File classFile;
        private final byte[] bytecode;

        CompiledClass(CtClass cc, Version version) throws CannotCompileException, IOException {
            String temporaryDirectory = System.getProperty("java.io.tmpdir");
            cc.defrost();
            cc.getClassFile().setMajorVersion(version.getMajor());
            cc.writeFile(temporaryDirectory);

            this.classFile = toClassPath(cc);
            this.bytecode = cc.toBytecode();
        }

        CompiledClass(File classFile, byte[] bytecode) {
            this.classFile = classFile;
            this.bytecode = bytecode;
        }

        File getClassFile() { return classFile; }
        byte[] getBytecode() { return bytecode; }
    }
}