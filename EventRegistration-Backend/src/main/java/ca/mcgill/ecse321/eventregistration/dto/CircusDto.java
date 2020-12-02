package ca.mcgill.ecse321.eventregistration.dto;

import java.sql.Date;
import java.sql.Time;

public class CircusDto extends EventDto{
	
	private String name;
	private String company;
	private Date date;
	private Time startTime;
	private Time endTime;
	
	public CircusDto() {
		
	}
	
	public CircusDto(String name, String company) {
		this(name, company, Date.valueOf("1971-01-01"), Time.valueOf("00:00:00"), Time.valueOf("23:59:59"));
	}

	public CircusDto(String name, String company, Date date, Time startTime, Time endTime) {
		this.setName(name);
		this.setCompany(company);
		this.setDate(date);
		this.setStartTime(startTime);
		this.setEndTime(endTime);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Time getStartTime() {
		return startTime;
	}

	public void setStartTime(Time startTime) {
		this.startTime = startTime;
	}

	public Time getEndTime() {
		return endTime;
	}

	public void setEndTime(Time endTime) {
		this.endTime = endTime;
	}
	
}
