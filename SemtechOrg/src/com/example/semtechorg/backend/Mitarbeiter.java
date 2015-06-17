package com.example.semtechorg.backend;

import org.apache.commons.beanutils.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * A simple DTO for the address book example.
 *
 * Serializable and cloneable Java Object that are typically persisted
 * in the database and can also be easily converted to different formats like JSON.
 */
// Backend DTO class. This is just a typical Java backend implementation
// class and nothing Vaadin specific.
public class Mitarbeiter implements Serializable, Cloneable {

    @Override
	public String toString() {
		return "Mitarbeiter [id=" + id + ", Name=" + Name + ", Beschreibung=" + Beschreibung
				+ ", Gehalt=" + Gehalt + ", Erfahrungsjahre=" + Erfahrungsjahre + ", Email="
				+ Email + ", Abteilung=" + Abteilung + ", Bereich=" + Bereich + ", Sparte="
				+ Sparte + "]";
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -5186107535815311412L;

	private Long id;

    private String Name = "";
    private String Beschreibung= "";
    private String Gehalt = "";
    private String Erfahrungsjahre = "";
    private String Email = "";
    private String Abteilung = "";
    private String Bereich = "";
    private String Sparte = "";

    
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    
    @Override
    public Mitarbeiter clone() throws CloneNotSupportedException {
        try {
            return (Mitarbeiter) BeanUtils.cloneBean(this);
        } catch (Exception ex) {
            throw new CloneNotSupportedException();
        }
    }

 

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public String getGehalt() {
		return Gehalt;
	}

	public void setGehalt(String gehalt) {
		Gehalt = gehalt;
	}

	

	public String getBeschreibung() {
		return Beschreibung;
	}

	public void setBeschreibung(String beschreibung) {
		Beschreibung = beschreibung;
	}

	public String getEmail() {
		return Email;
	}

	public void setEmail(String email) {
		Email = email;
	}

	public String getErfahrungsjahre() {
		return Erfahrungsjahre;
	}

	public void setErfahrungsjahre(String erfahrungsjahre) {
		Erfahrungsjahre = erfahrungsjahre;
	}

	public String getAbteilung() {
		return Abteilung;
	}

	public void setAbteilung(String abteilung) {
		Abteilung = abteilung;
	}

}
