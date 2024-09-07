package com.tuituidan.openhub.translator;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * DatasourceAnno.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/7
 */
@Documented
@Retention(RUNTIME)
@Target({FIELD, METHOD})
public @interface DatasourceAnno {

}
