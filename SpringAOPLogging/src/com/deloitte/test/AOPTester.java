/**
 * 
 */
package com.deloitte.test;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.deloitte.domain.Person;
import com.deloitte.service.BusinessInterface;

public class AOPTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		ApplicationContext contex = new ClassPathXmlApplicationContext(
				"ApplicationContext.xml");
		BusinessInterface tester = (BusinessInterface) contex
				.getBean("businessClassProxy");
		// int result=tester.sayHello("Rave");

		/*
		 * List<String> strList = new ArrayList<String>(); strList.add("ravi");
		 * strList.add("kiran"); int result = tester.sayHello(strList);
		 * System.out.println(result);
		 */
		int result = tester.sayHello(new Person("Ravi", "Kiran", 122354234L));

	}

}
