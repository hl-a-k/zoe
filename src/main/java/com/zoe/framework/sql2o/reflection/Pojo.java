package com.zoe.framework.sql2o.reflection;

import com.zoe.framework.sql2o.Sql2oException;
import com.zoe.framework.sql2o.converters.Converter;
import com.zoe.framework.sql2o.converters.ConverterException;
import com.zoe.framework.sql2o.quirks.Quirks;

import static com.zoe.framework.sql2o.converters.Convert.throwIfNull;

/**
 * Used internally to represent a plain old java object.
 */
public class Pojo {

    private PojoMetadata metadata;
    private boolean caseSensitive;
    private Object object;
    
    public Pojo(PojoMetadata metadata, boolean caseSensitive, Object object){
        this.caseSensitive = caseSensitive;
        this.metadata = metadata;
        this.object = object;
    }
    
    public Pojo(PojoMetadata metadata, boolean caseSensitive){
        this.caseSensitive = caseSensitive;
        this.metadata = metadata;
        ObjectConstructor objectConstructor = metadata.getObjectConstructor();
        object = objectConstructor.newInstance();
    }

    @SuppressWarnings("unchecked")
    public Object getProperty(String propertyPath, Quirks quirks){
        // String.split uses RegularExpression
        // this is overkill for every column for every row
        int index = propertyPath.indexOf('.');

        IMember member;

        if (index > 0) {
            final String substring = propertyPath.substring(0, index);

            member = metadata.getPropertyMember(substring);

            String newPath = propertyPath.substring(index+1);

            Object subValue = this.metadata.getValueOfProperty(substring, this.object);

            if (subValue == null){
                try {
                    subValue = member.getType().newInstance();
                } catch (InstantiationException e) {
                    throw new Sql2oException("Could not instantiate a new instance of class "+ member.getType().toString(), e);
                } catch (IllegalAccessException e) {
                    throw new Sql2oException("Could not instantiate a new instance of class "+ member.getType().toString(), e);
                }

                return member.getProperty(this.object);
            }

            PojoMetadata subMetadata = new PojoMetadata(member.getType(), this.caseSensitive, this.metadata.isAutoDeriveColumnNames(), this.metadata.getColumnMappings(), this.metadata.throwOnMappingFailure);
            Pojo subPojo = new Pojo(subMetadata, this.caseSensitive, subValue);

            return subPojo.getProperty(newPath, quirks);
        }
        else{
            member = metadata.getPropertyMember(propertyPath);

            Converter converter;

            try {
                converter = throwIfNull(member.getType(), quirks.converterOf(member.getType()));
            } catch (ConverterException e) {
                throw new Sql2oException("Cannot convert column " + propertyPath + " to type " + member.getType(), e);
            }

            try {
                return converter.convert(member.getProperty(this.object));
            } catch (ConverterException e) {
                throw new Sql2oException("Error trying to convert column " + propertyPath + " to type " + member.getType(), e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void setProperty(String propertyPath, Object value, Quirks quirks){
        // String.split uses RegularExpression
        // this is overkill for every column for every row
        int index = propertyPath.indexOf('.');
        IMember member;
        if (index > 0){
            final String substring = propertyPath.substring(0, index);
            member = metadata.getPropertyMember(substring);
            String newPath = propertyPath.substring(index+1);
            
            Object subValue = this.metadata.getValueOfProperty(substring, this.object);
            if (subValue == null){
                try {
                    subValue = member.getType().newInstance();
                } catch (InstantiationException e) {
                    throw new Sql2oException("Could not instantiate a new instance of class "+ member.getType().toString(), e);
                } catch (IllegalAccessException e) {
                    throw new Sql2oException("Could not instantiate a new instance of class "+ member.getType().toString(), e);
                }
                member.setProperty(this.object, subValue);
            }
            
            PojoMetadata subMetadata = new PojoMetadata(member.getType(), this.caseSensitive, this.metadata.isAutoDeriveColumnNames(), this.metadata.getColumnMappings(), this.metadata.throwOnMappingFailure);
            Pojo subPojo = new Pojo(subMetadata, this.caseSensitive, subValue);
            subPojo.setProperty(newPath, value, quirks);
        }
        else{
            member = metadata.getPropertyMember(propertyPath);
            Converter converter;
            try {
                converter = throwIfNull(member.getType(), quirks.converterOf(member.getType()));
            } catch (ConverterException e) {
                throw new Sql2oException("Cannot convert column " + propertyPath + " to type " + member.getType(), e);
            }

            try {
                member.setProperty(this.object, converter.convert( value ));
            } catch (ConverterException e) {
                throw new Sql2oException("Error trying to convert column " + propertyPath + " to type " + member.getType(), e);
            }
        }
        
        
    }



    public Object getObject(){
        return this.object;
    }
    
}
