package ca.mcgill.ecse321.eventregistration.dao;

import org.springframework.data.repository.CrudRepository;

import ca.mcgill.ecse321.eventregistration.model.Event;
import ca.mcgill.ecse321.eventregistration.model.Organizer;

public interface EventRepository extends CrudRepository<Event, String> {

	Event findByName(String name);
	boolean existsByName(String name);
//	boolean existsByOrganizer(Organizer organizer);

}
