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

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class Configuration {

    protected final Map<String, String> mappedStatements = new StrictMap<String>("Mapped Statements collection");
    protected final Set<String> loadedResources = new HashSet<String>();
    protected final Map<String, String> mappedComments = new StrictMap<String>("Mapped Comments collection");

    public Configuration() {
    }

    public void addMappedStatement(String sqlId, String ms) {
        mappedStatements.put(sqlId, ms);
    }

    public void addMappedComment(String sqlId, String comment) {
        mappedComments.put(sqlId, comment);
    }

    public String getMappedComment(String sqlId) {
        return mappedComments.get(sqlId);
    }

    public void removeMappedStatement(String sqlId) {
        mappedStatements.remove(sqlId);
    }

    public void removeMappedComments(String sqlId) {
        mappedComments.remove(sqlId);
    }

    public String getMappedStatement(String sqlId) {
        return mappedStatements.get(sqlId);
    }

    public Collection<String> getMappedStatementNames() {
        return mappedStatements.keySet();
    }

    public Collection<String> getMappedStatements() {
        return mappedStatements.values();
    }

    public void addLoadedResource(String resource) {
        loadedResources.add(resource);
    }

    public boolean isResourceLoaded(String resource) {
        return loadedResources.contains(resource);
    }

    public void removeLoadedResource(String resource) {
        loadedResources.remove(resource);
    }

    /*
     * Extracts namespace from fully qualified statement id.
     *
     * @param statementId
     * @return namespace or null when id does not contain period.
     */
    protected String extractNamespace(String statementId) {
        int lastPeriod = statementId.lastIndexOf('.');
        return lastPeriod > 0 ? statementId.substring(0, lastPeriod) : null;
    }

    private static class StrictMap<V> extends HashMap<String, V> {

        private String name;

        public StrictMap(String name, int initialCapacity, float loadFactor) {
            super(initialCapacity, loadFactor);
            this.name = name;
        }

        public StrictMap(String name, int initialCapacity) {
            super(initialCapacity);
            this.name = name;
        }

        public StrictMap(String name) {
            super();
            this.name = name;
        }

        public StrictMap(String name, Map<String, ? extends V> m) {
            super(m);
            this.name = name;
        }

        @SuppressWarnings("unchecked")
        public V put(String key, V value) {
            if (containsKey(key))
                throw new IllegalArgumentException(name + " already contains value for " + key);
            if (key.contains(".")) {
                final String shortKey = getShortName(key);
                if (super.get(shortKey) == null) {
                    super.put(shortKey, value);
                } else {
                    super.put(shortKey, (V) new Ambiguity(shortKey));
                }
            }
            return super.put(key, value);
        }

        public V get(Object key) {
            V value = super.get(key);
            if (value == null) {
                throw new IllegalArgumentException(name + " does not contain value for " + key);
            }
            if (value instanceof Ambiguity) {
                throw new IllegalArgumentException(((Ambiguity) value).getSubject() + " is ambiguous in " + name
                        + " (try using the full name including the namespace, or rename one of the entries)");
            }
            return value;
        }

        private String getShortName(String key) {
            final String[] keyparts = StringUtils.split(key, ".");
            final String shortKey = keyparts[keyparts.length - 1];
            return shortKey;
        }

        protected static class Ambiguity {
            private String subject;

            public Ambiguity(String subject) {
                this.subject = subject;
            }

            public String getSubject() {
                return subject;
            }
        }
    }

}
