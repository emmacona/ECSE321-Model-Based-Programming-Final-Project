package ca.mcgill.ecse321.eventregistration.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

@Entity
public class Organizer extends Person{
	
	private List<Event> events;
	
	@OneToMany(mappedBy="organizer",fetch = FetchType.EAGER)
	public List<Event> getOrganizes() {
		return this.events;
	}

	public void setOrganizes(List<Event> events) {
		this.events = events;
	}
	
	
}
