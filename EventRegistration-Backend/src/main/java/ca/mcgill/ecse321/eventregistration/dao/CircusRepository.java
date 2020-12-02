package ca.mcgill.ecse321.eventregistration.dao;

import org.springframework.data.repository.CrudRepository;

import ca.mcgill.ecse321.eventregistration.model.Circus;

public interface CircusRepository extends CrudRepository<Circus, String>{

	Circus findByName(String name);
	boolean existsByName(String name);
}
