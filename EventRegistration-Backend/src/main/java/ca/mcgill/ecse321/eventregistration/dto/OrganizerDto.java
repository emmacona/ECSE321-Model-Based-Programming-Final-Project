package ca.mcgill.ecse321.eventregistration.dto;

import java.util.List;

public class OrganizerDto {

	private String name;
	private List<EventDto> eventsAttended;

	public OrganizerDto() {
	}
	
	public OrganizerDto(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public OrganizerDto(String name, List<EventDto> eventsAttended) {
		this.eventsAttended = eventsAttended;
		this.name = name;
	}

	public List<EventDto> getEvents() {
		return eventsAttended;
	}

	public void setEvents(List<EventDto> eventsAttended) {
		this.eventsAttended = eventsAttended;
	}

	
}
