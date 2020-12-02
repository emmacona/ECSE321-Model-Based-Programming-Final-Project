package ca.mcgill.ecse321.eventregistration.dao;

import org.springframework.data.repository.CrudRepository;

import ca.mcgill.ecse321.eventregistration.model.Organizer;
import ca.mcgill.ecse321.eventregistration.model.Event;

public interface OrganizerRepository extends CrudRepository<Organizer, String>{
	
	Organizer findByName(String name);
	boolean existsByName(String name);
//	boolean existsByEvent(Event event);
}
