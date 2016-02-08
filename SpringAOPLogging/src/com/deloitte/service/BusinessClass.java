/**
 * 
 */
package com.deloitte.service;

import java.util.List;

import com.deloitte.domain.Person;

public class BusinessClass implements BusinessInterface {

	public int sayHello(String name) {
		// throwing null pointer exception
		String tst = null;
		return tst.length();
	}

	public int sayHello(List<String> nameList) {
		// throwing null pointer exception
		String tst = null;
		return tst.length();
	}

	public int sayHello(Person person) {
		// throwing null pointer exception
		String tst = null;
		return tst.length();
	}

}
