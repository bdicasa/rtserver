
package rtserver.internal;

import java.io.File;
import java.io.FileInputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Brian DiCasa
 */
public class ClassFinder {
	
    private static final Logger logger = LoggerFactory.getLogger(ClassFinder.class);
    
    /**
     * Finds all classes in the given package with have the specified annotation.
     * @param packageName The name of the package that contains the classes.
     * @param annotation The class annotation that you are trying to find classes for.
     * @return A list of classes that are in the given package and marked with the specified annotation.
     */
    public static List<Class> find(String packageName, Class annotation) {
    	
    	List<Class> classes = find(packageName);
    	List<Class> annotated = new ArrayList<Class>();
    	
    	for (Class c : classes) {
    		Annotation ann = c.getAnnotation(annotation);
			if (ann.annotationType().equals(annotation)) {
				annotated.add(c);
			}
    	}
    	
    	classes = null;
    	return annotated;
    }
    
    public static List<Class> find(String packageName) {

        List<Class> classes = new ArrayList<Class>();
        try {

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = packageName.replace('.', '/');
            Enumeration<URL> resources = classLoader.getResources(path);

            List<File> directories = new ArrayList<File>();

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String fileName = resource.getFile();
                logger.trace("Found file '" + fileName + "' to search.");
                if (fileName.contains(".jar")) {
                    // Class definitions are in a jar file, load classes from jar
                    String jarPath = getJarPathFromResource(fileName);
                    addClasses(classes, loadClassesFromJar(jarPath, path));

                } else {
                    String fileNameDecoded = URLDecoder.decode(fileName, "UTF-8");
                    directories.add(new File(fileNameDecoded));
                }
            }

            for (File directory : directories) {
                addClasses(classes, findClasses(directory, packageName));
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error find classes with base package: " + packageName, ex);
        }

        return classes;
    }

    private static List<Class> findClasses(File directory, String packageName) throws Exception {

        List<Class> classes = new ArrayList<Class>();

        if (!directory.exists()) return classes;

        File[] files = directory.listFiles();

        for (File file : files) {
            String fileName = file.getName();

            if (file.isDirectory()) {
                if (fileName.contains(".")) {
                    // Unprocessable directory skip to next
                    continue;
                }
                addClasses(classes, findClasses(file, packageName + "." + fileName));

            } else if (fileName.endsWith(".class") && !fileName.contains("$")) {
                // If we have a class that is not inner or anonymous (they contain $ in class name)
                // then retrieve it and add it to our found classes

                // The -6 is to remove .class
                String className = packageName + '.' + fileName.substring(0, fileName.length() - 6);

                addClass(classes, getClass(className));
            }
        }

        return classes;
    }

    private static List<Class> loadClassesFromJar(String jarFileName, String baseResourcePath) throws Exception {

        List<Class> classes = new ArrayList<Class>();

        JarInputStream jarStream = new JarInputStream(new FileInputStream(jarFileName));
        JarEntry jarEntry = null;

        while (true) {
            jarEntry = jarStream.getNextJarEntry();

            if (jarEntry == null) break;

            if (jarEntry.getName().contains(baseResourcePath) && jarEntry.getName().endsWith(".class")) {
                // If we have a class in our base resource path

                String className = jarEntry.getName().replaceAll("/", "\\.");
                className = className.replace(".class", "");

                addClass(classes, getClass(className));
            }
        }

        return classes;
    }

    private static Class getClass(String className) throws ClassNotFoundException {

        Class c;
        try {
            c = Class.forName(className);
        } catch (ExceptionInInitializerError ex) {
            c = Class.forName(className, false,
                              Thread.currentThread().getContextClassLoader());
        }

        return c;
    }

    /**
     * Retrieves a jar file name from a resource found in the class path.
     * The resource will look something like this:
     *     file:/C:/path/to/jar/jarName.jar!/some/base/package
     * @param resourceName The name of the resource found the class path.
     * @return The jar's file path, e.g. C:/path/to/jar/jarName.jar
     */
    private static String getJarPathFromResource(String resourcePath) {

        // Remove package information from jar (!/some/base/package)
        resourcePath = resourcePath.split("!")[0];

        if (resourcePath.endsWith("!")) {
            resourcePath = resourcePath.substring(0, resourcePath.length() - 1);
        }

        if (resourcePath.startsWith("file:")) {
            resourcePath = resourcePath.substring(5, resourcePath.length());
        }

        try {
            resourcePath = URLDecoder.decode(resourcePath, "UTF-8");
        } catch (Exception ex) {
            throw new RuntimeException("Error decoding jar file path.", ex);
        }

        return resourcePath;
    }

    /**
     * Adds a class to a class list if it doesn't already exist in the list
     */
    private static void addClass(List<Class> existingClasses, Class classToAdd) {

        boolean exists = false;
        for (Class c : existingClasses) {
            if (c.getName().equals(classToAdd.getName())) {
                exists = true;
                break;
            }
        }

        if (!exists) existingClasses.add(classToAdd);
    }

    /**
     * Adds classes to an existing class list as long as they don't already
     * exist.
     */
    private static void addClasses(List<Class> existingClasses, List<Class> classesToAdd) {

        for (Class classToAdd : classesToAdd) {
            addClass(existingClasses, classToAdd);
        }
    }
}

