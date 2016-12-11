package com.damonseeley.utils.lighting;

import java.util.List;


public class Test {

	protected String[] tags;
	protected String name;

	public Test(String name, List<String> tags) {
		this.name = name;
		this.tags = new String[tags.size()];
		tags.toArray(this.tags);
	}	
}