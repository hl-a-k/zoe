package com.zoe.framework.data.jpa.repository.support;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * Sql2oPageImpl
 * Created by caizhicong on 2017/7/24.
 */
@JsonIgnoreProperties({"numberOfElements","sort","first","last"})
public class Sql2oPageImpl<T> extends PageImpl<T> implements java.io.Serializable {

    public Sql2oPageImpl(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    @JsonProperty("rows")
    @JSONField(name = "rows")
    @Override
    public List<T> getContent() {
        return super.getContent();
    }

    @JsonProperty("total")
    @JSONField(name = "total")
    @Override
    public long getTotalElements() {
        return super.getTotalElements();
    }

    @JsonProperty("page")
    @JSONField(name = "page")
    @Override
    public int getNumber() {
        return super.getNumber() + 1;
    }

    @JsonProperty("size")
    @JSONField(name = "size")
    @Override
    public int getSize() {
        return super.getSize();
    }

    @JSONField(serialize = false)
    @Override
    public int getNumberOfElements() {
        return super.getNumberOfElements();
    }

    @JSONField(serialize = false)
    @Override
    public Sort getSort() {
        return super.getSort();
    }

    @JSONField(serialize = false)
    @Override
    public boolean isFirst() {
        return super.isFirst();
    }

    @JSONField(serialize = false)
    @Override
    public boolean isLast() {
        return super.isLast();
    }
}
