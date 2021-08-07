package com.github.jonasrutishauser.cdi.maven.plugin;

class EmptyClassLoader extends ClassLoader {
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        throw new ClassNotFoundException("empty ClassLoader");
    }
}
