package ca.mcgill.ecse321.eventregistration.dto;

import java.util.Collections;
import java.util.List;

public class PersonDto {

	private String name;
	private List<EventDto> eventsAttended;
	private List<CircusDto> circusAttended;
	private List<ApplePayDto> payments;

	public PersonDto() {
	}

	@SuppressWarnings("unchecked")
	public PersonDto(String name) {
		this(name, Collections.EMPTY_LIST);
	}

	public PersonDto(String name, List<EventDto> events) {
		this.name = name;
		this.eventsAttended = events;
	}

	public String getName() {
		return name;
	}

	public List<EventDto> getEventsAttended() {
		return eventsAttended;
	}

	public void setEventsAttended(List<EventDto> events) {
		this.eventsAttended = events;
	}
	
	public List<CircusDto> getCircusAttended() {
		return circusAttended;
	}

	public void setCircusAttended(List<CircusDto> circusAttended) {
		this.circusAttended = circusAttended;
	}
	
//	public List<ApplePayDto> getPayments() {
//		return payments;
//	}
//
//	public void setPayments(List<ApplePayDto> payments) {
//		this.payments = payments;
//	}
	
}
