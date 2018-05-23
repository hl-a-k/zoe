package org.beetl.sql.core.engine;

import com.zoe.framework.sqlbag.SqlBag;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Resource;
import org.beetl.core.ResourceLoader;

/**
 * SqlTemplateResourceLoader
 * Created by caizhicong on 2017/7/7.
 */
public class SqlTemplateResourceLoader implements ResourceLoader {

    private SqlBag sqlBag;

    public SqlTemplateResourceLoader(SqlBag sqlBag) {
        this.sqlBag = sqlBag;
    }

    @Override
    public Resource getResource(String key) {
        String sql = sqlBag.get(key);
        if (sql != null) sql = sql.trim();
        return new SqlTemplateResource(key, sql, this);
    }

    @Override
    public boolean isModified(Resource resource) {
        return sqlBag.isDebug();
    }

    //never use
    @Override
    public boolean exist(String key) {
        return true;
    }

    //never use
    @Override
    public void close() {
    }

    //never use
    @Override
    public void init(GroupTemplate groupTemplate) {
    }

    @Override
    public String getResourceId(Resource resource, String id) {
        return id;
    }

    @Override
    public String getInfo() {
        return "SqlTemplateResourceLoader";
    }
}
