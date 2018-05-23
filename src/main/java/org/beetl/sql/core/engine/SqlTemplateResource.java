package org.beetl.sql.core.engine;

import org.beetl.core.Resource;
import org.beetl.core.ResourceLoader;

import java.io.Reader;
import java.io.StringReader;

/**
 * Created by caizhicong on 2017/7/7.
 */
public class SqlTemplateResource extends Resource {

    String template = null;
    public SqlTemplateResource(String id, String sqlTemplate,ResourceLoader loader)
    {
        super(id,loader);
        this.template = sqlTemplate ;
    }
    @Override
    public Reader openReader() {
        return new StringReader(template);
    }

    @Override
    public boolean isModified() {
        SqlTemplateResourceLoader loader = (SqlTemplateResourceLoader)this.resourceLoader;
        return loader.isModified(loader.getResource(this.id));
    }
    public String getTemplate() {
        return template;
    }
    public void setTemplate(String template) {
        this.template = template;
    }
}