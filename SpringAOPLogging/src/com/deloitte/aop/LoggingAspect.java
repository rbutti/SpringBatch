/**
 * 
 */
package com.deloitte.aop;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;

public class LoggingAspect {

	private static Log logger = LogFactory.getLog(LoggingAspect.class);

	public void log(JoinPoint jp, Throwable exception) throws Throwable {

		logger.debug("Inside LogginAspectAnnotationDriven");

		logger.error("\n Join point kind : " + jp.getKind()
				+ "\n Signature declaring type : "
				+ jp.getSignature().getDeclaringTypeName()
				+ "\n Signature name : " + jp.getSignature().getName()
				+ "\n Arguments : " + Arrays.toString(jp.getArgs())
				+ "\n Target class : " + jp.getTarget().getClass().getName()
				+ "\n This class : " + jp.getThis().getClass().getName()
				+ "\n An exception has been thrown in "
				+ jp.getSignature().getName() + "() \n Cause :"
				+ exception.getCause());

		logger.debug("Exiting LogginAspectAnnotationDriven");
	}
}
