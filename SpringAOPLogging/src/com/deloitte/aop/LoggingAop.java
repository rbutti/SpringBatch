package com.deloitte.aop;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.ThrowsAdvice;

public class LoggingAop implements ThrowsAdvice {

	private static Log logger = LogFactory.getLog(LoggingAop.class);

	public void log(Method method, Object[] args, Object object,
			Exception exception) throws Throwable {
		logger.debug("Inside LoggingAop");

		logger.error(" Method : " + method.getName() + " called on "
				+ object.getClass() + "Arguments : " + Arrays.toString(args));
		logger.debug("Exiting LogginAspectAnnotationDriven");
	}
}
