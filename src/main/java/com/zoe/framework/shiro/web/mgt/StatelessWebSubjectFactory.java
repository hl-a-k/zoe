package com.zoe.framework.shiro.web.mgt;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.web.mgt.DefaultWebSubjectFactory;

/**
 * 无状态 web subject factory
 * Created by caizhicong on 2017/4/10.
 */
public class StatelessWebSubjectFactory extends DefaultWebSubjectFactory {
    @Override
    public Subject createSubject(SubjectContext subjectContext) {
        subjectContext.setSessionCreationEnabled(false);
        return super.createSubject(subjectContext);
    }
}
