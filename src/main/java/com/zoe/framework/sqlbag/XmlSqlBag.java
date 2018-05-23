package com.zoe.framework.sqlbag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.springframework.util.ObjectUtils.isEmpty;

/**
 * Created by caizhicong on 2016/7/12.
 */
public class XmlSqlBag extends SqlBag{
    private static final Logger logger = LoggerFactory.getLogger(XmlSqlBag.class);
    private Configuration configuration;
    private Resource configLocation;
    private Resource[] mapperLocations;
    private Resource[] configLocations;

    public void setConfigLocation(Resource configLocation) {
        this.configLocation = configLocation;
    }

    public void setMapperLocations(Resource[] mapperLocations) {
        this.mapperLocations = mapperLocations;
    }

    public void setConfigLocations(Resource[] configLocations) {
        this.configLocations = configLocations;
    }

    private void verifyConfiguration() {
        if (isDebug() || configuration == null) {
            long start = System.currentTimeMillis();
            init();
            if (configuration == null) {
                configuration = new Configuration();
            }
            long end = System.currentTimeMillis();
            logger.info("Parsed configuration file: '" + this.configLocation + "' takes " + (end - start) + " ms");
        }
    }

    private void init() {
        Configuration configuration = null;

        XMLConfigBuilder xmlConfigBuilder = null;
        if (this.configLocation != null) {
            try {
                xmlConfigBuilder = new XMLConfigBuilder(this.configLocation.getInputStream());

                if (xmlConfigBuilder != null) {
                    try {
                        xmlConfigBuilder.parse();

                        if (logger.isDebugEnabled()) {
                            logger.debug("Parsed configuration file: '" + this.configLocation + "'");
                        }
                    } catch (Exception ex) {
                        logger.debug("Failed to parse config resource: " + this.configLocation, ex);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            configuration = xmlConfigBuilder.getConfiguration();
        }

        if (!isEmpty(this.configLocations)) {
            for (Resource resource : this.configLocations) {
                if (resource == null) {
                    continue;
                }

                try {
                    xmlConfigBuilder = new XMLConfigBuilder(configuration, resource.getInputStream());

                    if (xmlConfigBuilder != null) {
                        try {
                            xmlConfigBuilder.parse();

                            if (logger.isDebugEnabled()) {
                                logger.debug("Parsed configuration file: '" + resource + "'");
                            }
                        } catch (Exception ex) {
                            logger.debug("Failed to parse config resource: " + resource, ex);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                configuration = xmlConfigBuilder.getConfiguration();
            }
        }

        if (!isEmpty(this.mapperLocations)) {
            for (Resource mapperLocation : this.mapperLocations) {
                if (mapperLocation == null) {
                    continue;
                }

                try {
                    XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(mapperLocation.getInputStream(),
                            configuration, mapperLocation.toString());
                    xmlMapperBuilder.parse();
                } catch (Exception e) {
                    logger.debug("Failed to parse mapping resource: '" + mapperLocation + "'", e);
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("Parsed mapper file: '" + mapperLocation + "'");
                }
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Property 'mapperLocations' was not specified or no matching resources found");
            }
        }

        this.configuration = configuration;
    }

    /**
     * 加载某个SQL映射文件
     *
     * @param filePath
     */
    public void load(String filePath) {
        loadOneFile(filePath, false);
    }

    /**
     * 卸载某个SQL映射文件
     *
     * @param filePath
     */
    public void unload(String filePath) {
        loadOneFile(filePath, true);
    }

    /**
     * 加载或卸载某个SQL映射文件
     *
     * @param filePath
     */
    public void loadOneFile(String filePath, boolean unload) {
        File file = new File(filePath);
        if (file.exists()) {
            verifyConfiguration();
            try {
                XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(new FileInputStream(file),
                        configuration, filePath);
                if (unload) {
                    xmlMapperBuilder.unload();
                } else {
                    xmlMapperBuilder.parse();
                }
            } catch (Exception e) {
                logger.debug("Failed to parse mapping resource: '" + filePath + "'", e);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Parsed mapper file: '" + filePath + "'");
            }
        }
    }

    public Configuration getConfiguration(){
        return configuration;
    }

    public String get(String sqlId) {
        verifyConfiguration();
        String sql = configuration.getMappedStatement(sqlId);
        return sql;
    }

    public void set(String sqlId, String sql) {
        configuration.addMappedStatement(sqlId, sql);
    }

    @Override
    public void remove(String sqlId) {
        configuration.removeMappedStatement(sqlId);
    }
}
