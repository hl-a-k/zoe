package com.zoe.framework.sqlbag;

/*
 *    Copyright 2009-2013 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

import java.io.InputStream;
import java.util.List;

public class XMLMapperBuilder {

    private String currentNamespace;
    private XPathParser parser;
    private String resource;
    private Configuration configuration;

    public XMLMapperBuilder(Configuration configuration) {
        this.configuration = configuration;
    }

    public XMLMapperBuilder(InputStream inputStream, Configuration configuration, String resource, String namespace) {
        this(inputStream, configuration, resource);
        setCurrentNamespace(namespace);
    }

    public XMLMapperBuilder(InputStream inputStream, Configuration configuration, String resource) {
        this(new XPathParser(inputStream, true),
                configuration, resource);
    }

    public XMLMapperBuilder(XPathParser parser, Configuration configuration, String resource) {
        this(configuration);
        this.parser = parser;
        this.resource = resource;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void parse() {
        if (!configuration.isResourceLoaded(resource)) {
            configurationElement(parser.evalNode("/mapper"), false);
            configuration.addLoadedResource(resource);
        }
    }

    public void unload() {
        if (configuration.isResourceLoaded(resource)) {
            configurationElement(parser.evalNode("/mapper"), true);
            configuration.removeLoadedResource(resource);
        }
    }

    public String getCurrentNamespace() {
        return currentNamespace;
    }

    public void setCurrentNamespace(String currentNamespace) {
        if (currentNamespace == null) {
            throw new BuilderException("The mapper element requires a namespace attribute to be specified.");
        }

        if (this.currentNamespace != null && !this.currentNamespace.equals(currentNamespace)) {
            throw new BuilderException("Wrong namespace. Expected '"
                    + this.currentNamespace + "' but found '" + currentNamespace + "'.");
        }

        this.currentNamespace = currentNamespace;
    }

    public String applyCurrentNamespace(String base, boolean isReference) {
        if (base == null) return null;
        if (isReference) {
            // is it qualified with any namespace yet?
            if (base.contains(".")) return base;
        } else {
            // is it qualified with this namespace yet?
            if (base.startsWith(currentNamespace + ".")) return base;
            //if (base.contains("."))
            //    throw new BuilderException("Dots are not allowed in element names, please remove it from " + base);
        }
        return currentNamespace + "." + base;
    }

    private void configurationElement(XNode context, boolean unload) {
        try {
            String namespace = context.getStringAttribute("namespace");
            if (namespace.equals("")) {
                throw new BuilderException("Mapper's namespace cannot be empty");
            }
            setCurrentNamespace(namespace);
            sqlElement(context.evalNodes("/mapper/sql"), unload);
        } catch (Exception e) {
            throw new BuilderException("Error parsing Mapper XML. Cause: " + e, e);
        }
    }

    private void sqlElement(List<XNode> list, boolean unload) throws Exception {
        for (XNode context : list) {
            String id = context.getStringAttribute("id");
            id = applyCurrentNamespace(id, false);
            if (unload) {
                configuration.removeMappedStatement(id);
                configuration.removeMappedComments(id);
            } else {
                configuration.addMappedStatement(id, context.getStringBody());
                String comment = context.getStringAttribute("comment");
                configuration.addMappedComment(id,comment);
            }
        }
    }
}
