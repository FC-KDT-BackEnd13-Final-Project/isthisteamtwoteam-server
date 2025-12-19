package org.etmetmy.bn_server.global.aop;

import org.etmetmy.bn_server.domain.history.entity.ChangeType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HistoryLogger {
    ChangeType changeType();  // CREATE, UPDATE, DELETE
    String targetType() default "Post";  // Post, Comment, File, Link
}
