package ca.mcgill.ecse321.eventregistration.model;

import java.sql.Date;

import javax.persistence.Entity;

@Entity
public class Circus extends Event{
	
	private String company;

	public void setCompany(String company) {
		this.company = company;
	}

	public String getCompany() {
		return this.company;
	}
	
}
