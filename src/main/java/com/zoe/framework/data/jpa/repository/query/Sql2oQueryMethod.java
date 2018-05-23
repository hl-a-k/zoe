package com.zoe.framework.data.jpa.repository.query;


import com.zoe.framework.data.jpa.repository.Sql2oQuery;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * JPA specific extension of {@link Sql2oQueryMethod}.
 *
 * @author Oliver Gierke
 * @author Thomas Darimont
 * @author Christoph Strobl
 */
public class Sql2oQueryMethod extends QueryMethod {

    /**
     * @see <a href=
     *      "http://download.oracle.com/otn-pub/jcp/persistence-2.0-fr-eval-oth-JSpec/persistence-2_0-final-spec.pdf">JPA
     *      2.0 Specification 2.2 Persistent Fields and Properties Page 23 - Top paragraph.</a>
     */
    private static final Set<Class<?>> NATIVE_ARRAY_TYPES;

    static {

        Set<Class<?>> types = new HashSet<Class<?>>();
        types.add(byte[].class);
        types.add(Byte[].class);
        types.add(char[].class);
        types.add(Character[].class);

        NATIVE_ARRAY_TYPES = Collections.unmodifiableSet(types);
    }

    private final Method method;


    /**
     * Creates a {@link Sql2oQueryMethod}.
     *
     * @param method must not be {@literal null}
     * @param metadata must not be {@literal null}
     */
    public Sql2oQueryMethod(Method method, RepositoryMetadata metadata, ProjectionFactory factory) {

        super(method, metadata, factory);

        Assert.notNull(method, "Method must not be null!");

        this.method = method;

        Assert.isTrue(!(isModifyingQuery() && getParameters().hasSpecialParameter()),
                String.format("Modifying method must not contain %s!", Parameters.TYPES));
        assertParameterNamesInAnnotatedQuery();
    }

    /**
     * Creates a {@link Parameters} instance.
     *
     * @param method
     * @return must not return {@literal null}.
     */
    protected Sql2oParameters createParameters(Method method) {
        return new Sql2oParameters(method);
    }

    /**
     * Returns the {@link Parameters} wrapper to gain additional information about {@link Method} parameters.
     *
     * @return
     */
    public Sql2oParameters getParameters() {
        return (Sql2oParameters) super.getParameters();
    }

    private void assertParameterNamesInAnnotatedQuery() {

        String annotatedQuery = getAnnotatedQuery();

        if (!Sql2oQueryUtils.hasNamedParameter(annotatedQuery)) {
            return;
        }

        for (Parameter parameter : getParameters()) {

            if (!parameter.isNamedParameter()) {
                continue;
            }

            if (!annotatedQuery.contains(String.format(":%s", parameter.getName()))
                    && !annotatedQuery.contains(String.format("#%s", parameter.getName()))) {
                throw new IllegalStateException(
                        String.format("Using named parameters for method %s but parameter '%s' not found in annotated query '%s'!",
                                method, parameter.getName(), annotatedQuery));
            }
        }
    }

    /**
     * Returns whether the finder is a modifying one.
     *
     * @return
     */
    @Override
    public boolean isModifyingQuery() {
        return getAnnotationValue("updateQuery", Boolean.class);
    }

    /**
     * Returns the actual return type of the method.
     *
     * @return
     */
    Class<?> getReturnType() {

        return method.getReturnType();
    }

    /**
     * Returns the query string declared in a {@link Sql2oQuery} annotation or {@literal null} if neither the annotation found
     * nor the attribute was specified.
     *
     * @return
     */
    String getAnnotatedQuery() {

        String query = getAnnotationValue("value", String.class);
        return StringUtils.hasText(query) ? query : null;
    }

    /**
     * Returns the countQuery string declared in a {@link Sql2oQuery} annotation or {@literal null} if neither the annotation
     * found nor the attribute was specified.
     *
     * @return
     */
    String getCountQuery() {

        String countQuery = getAnnotationValue("countQuery", String.class);
        return StringUtils.hasText(countQuery) ? countQuery : null;
    }

    /**
     * Returns the count query projection string declared in a {@link Sql2oQuery} annotation or {@literal null} if neither the
     * annotation found nor the attribute was specified.
     *
     * @return
     * @since 1.6
     */
    String getCountQueryProjection() {

        String countProjection = getAnnotationValue("countProjection", String.class);
        return StringUtils.hasText(countProjection) ? countProjection : null;
    }

    /**
     * Returns whether the backing query is a native one.
     *
     * @return
     */
    boolean isNativeQuery() {
        return getAnnotationValue("nativeQuery", Boolean.class);
    }

    /* 
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.QueryMethod#getNamedQueryName()
     */
    @Override
    public String getNamedQueryName() {

        String annotatedName = getAnnotationValue("name", String.class);
        return StringUtils.hasText(annotatedName) ? annotatedName : null;
    }

    /**
     * Returns the name of the {@link Sql2oQuery} that shall be used for count queries.
     *
     * @return
     */
    String getNamedCountQueryName() {

        String annotatedName = getAnnotationValue("countName", String.class);
        return StringUtils.hasText(annotatedName) ? annotatedName : getNamedQueryName() + ".count";
    }

    /**
     * Returns the {@link Sql2oQuery} annotation's attribute casted to the given type or default value if no annotation
     * available.
     *
     * @param attribute
     * @param type
     * @return
     */
    private <T> T getAnnotationValue(String attribute, Class<T> type) {
        return getMergedOrDefaultAnnotationValue(attribute, Sql2oQuery.class, type);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private <T> T getMergedOrDefaultAnnotationValue(String attribute, Class annotationType, Class<T> targetType) {

        Annotation annotation = AnnotatedElementUtils.findMergedAnnotation(method, annotationType);
        if (annotation == null) {
            return targetType.cast(AnnotationUtils.getDefaultValue(annotationType, attribute));
        }

        return targetType.cast(AnnotationUtils.getValue(annotation, attribute));
    }

    /* 
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.QueryMethod#isCollectionQuery()
     */
    @Override
    public boolean isCollectionQuery() {
        return super.isCollectionQuery() && !NATIVE_ARRAY_TYPES.contains(method.getReturnType());
    }

    /**
     * Return {@literal true} if the method is procedure annotation.
     *
     * @return
     */
    public boolean isProcedureQuery() {
        return getAnnotationValue("procedure", Boolean.class);
    }

    /**
     * Return {@literal true} if the method is checkValid annotation.
     *
     * @return
     */
    public boolean checkValid() {
        return getAnnotationValue("checkValid", Boolean.class);
    }
}
