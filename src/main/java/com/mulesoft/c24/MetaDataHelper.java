/**
 *
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package com.mulesoft.c24;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class MetaDataHelper {

    public static List<Class<?>> findImplementationsOf(final Class<?> superClassOrInterface, final Package fromPackage) {

        if (superClassOrInterface == null) {
            return null;
        }

        if (fromPackage == null) {
            return null;
        }

        final List<Class<?>> rVal = new ArrayList<Class<?>>();
        try {
            final Class<?>[] targets = getAllClassesFromPackage(fromPackage.getName());
            if (targets != null) {
                for (Class<?> aTarget : targets) {
                    if (aTarget == null) {
                        continue;
                    }
                    else if (aTarget.equals(superClassOrInterface)) {
                        continue;
                    }
                    else if (!superClassOrInterface.isAssignableFrom(aTarget)) {
                        continue;
                    }
                    else {
                        rVal.add(aTarget);
                    }
                }
            }
        }
        catch (ClassNotFoundException e) {
        }
        catch (IOException e) {
        }

        return rVal;
    }

    /**
     * Load all classes from a package.
     * 
     * @param packageName
     * @return
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static Class<?>[] getAllClassesFromPackage(final String packageName) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    /**
     * Find file in package.
     * 
     * @param directory
     * @param packageName
     * @return
     * @throws ClassNotFoundException
     */
    public static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            }
            else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
}


