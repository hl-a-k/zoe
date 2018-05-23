package com.zoe.framework.sqlbag;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Created by caizhicong on 2015/6/23.
 */
public class XMLConfigBuilder {
    private boolean parsed;
    private XPathParser parser;
    private Configuration configuration;

    public XMLConfigBuilder(Configuration configuration) {
        this.configuration = configuration;
    }

    public XMLConfigBuilder(Configuration configuration,InputStream inputStream) {
        this.configuration = configuration != null ? configuration : new Configuration();
        this.parsed = false;
        this.parser = new XPathParser(inputStream, true);
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public XMLConfigBuilder(Reader reader) {
        this(new XPathParser(reader, true));
    }

    public XMLConfigBuilder(InputStream inputStream) {
        this(new XPathParser(inputStream, true));
    }

    private XMLConfigBuilder(XPathParser parser) {
        this(new Configuration());
        this.parsed = false;
        this.parser = parser;
    }

    public Configuration parse() {
        if (parsed) {
            throw new BuilderException("Each XMLConfigBuilder can only be used once.");
        }
        parsed = true;
        parseConfiguration(parser.evalNode("/configuration"));
        return configuration;
    }

    private void parseConfiguration(XNode root) {
        try {
            mapperElement(root.evalNode("mappers"));
        } catch (Exception e) {
            throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
        }
    }

    private void mapperElement(XNode parent) throws Exception {
        if (parent != null) {
            for (XNode child : parent.getChildren()) {
                String namespace = child.getStringAttribute("namespace");
                if (namespace != null) {
                    boolean ignore = child.getBooleanAttribute("ignore",false);
                    if(ignore) continue;
                    //内嵌的mapper使用namespace当做resource
                    XMLMapperBuilder mapperParser = new XMLMapperBuilder(new XPathParser(child.toString(true),true), configuration, namespace);
                    mapperParser.parse();
                    continue;
                }

                String resource = child.getStringAttribute("resource");
                if (resource != null) {
                    InputStream inputStream = getResourceAsStream(resource);
                    XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, resource);
                    mapperParser.parse();
                }else {
                    throw new BuilderException("A mapper element may only specify a url, resource or class, but not more than one.");
                }
            }
        }
    }

    public InputStream getResourceAsStream(String resource) throws IOException {
        return getResourceAsStream(null, resource);
    }

    public InputStream getResourceAsStream(ClassLoader loader, String resource) throws IOException {
        InputStream in = getResourceAsStream(resource, loader);
        if (in == null) throw new IOException("Could not find resource " + resource);
        return in;
    }

    public InputStream getResourceAsStream(String resource, ClassLoader classLoader) {
        return getResourceAsStream(resource, getClassLoaders(classLoader));
    }

    InputStream getResourceAsStream(String resource, ClassLoader[] classLoader) {
        for (ClassLoader cl : classLoader) {
            if (null != cl) {

                // try to find the resource as passed
                InputStream returnValue = cl.getResourceAsStream(resource);

                // now, some class loaders want this leading "/", so we'll add it and try again if we didn't find the resource
                if (null == returnValue) returnValue = cl.getResourceAsStream("/" + resource);

                if (null != returnValue) return returnValue;
            }
        }
        return null;
    }

    ClassLoader systemClassLoader;

    XMLConfigBuilder() {
        try {
            systemClassLoader = ClassLoader.getSystemClassLoader();
        } catch (SecurityException ignored) {
            // AccessControlException on Google App Engine
        }
    }

    ClassLoader[] getClassLoaders(ClassLoader classLoader) {
        return new ClassLoader[]{
                classLoader,
                Thread.currentThread().getContextClassLoader(),
                getClass().getClassLoader(),
                systemClassLoader};
    }
}
