package com.example.semtechorg.backend;

import java.io.Serializable;

import org.apache.commons.beanutils.BeanUtils;

public class OWLConcept implements Serializable, Cloneable {
	public OWLConcept(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}
	
	public OWLConcept(String name, String value, String type) {
		super();
		this.name = name;
		this.value = value;
		this.type = type;
	}

	private String name = "";
	private String value = "";
	private String type = "";



	@Override
	public OWLConcept clone() throws CloneNotSupportedException {
		try {
			//return (OWLConcept) BeanUtils.cloneBean(this);
			return this;
		} catch (Exception ex) {
			throw new CloneNotSupportedException();
		}
	}



	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}



	public String getValue() {
		return value;
	}



	public void setValue(String value) {
		this.value = value;
	}



	public String getType() {
		return type;
	}



	public void setType(String type) {
		this.type = type;
	}



}
