package org.sysma.schedulerExecutor;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
public @interface TaskDef {
	public String name();
	public String filePath() default "";
	//public String address() default "";
	//public int port() default 8080;
	//public int mult();
}
