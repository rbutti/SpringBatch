/**
 * 
 */
package com.deloitte.service;

import java.util.List;



import com.deloitte.domain.Person;


public interface BusinessInterface {
	
	public int sayHello(String name);
	
	public int sayHello(List<String> nameList);
	
	public int sayHello(Person person);
}
