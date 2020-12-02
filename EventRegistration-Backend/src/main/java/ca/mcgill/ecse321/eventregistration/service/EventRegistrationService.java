package ca.mcgill.ecse321.eventregistration.service;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.mcgill.ecse321.eventregistration.dao.*;
import ca.mcgill.ecse321.eventregistration.model.*;

@Service
public class EventRegistrationService {

	@Autowired
	private EventRepository eventRepository;
	@Autowired
	private PersonRepository personRepository;
	@Autowired
	private RegistrationRepository registrationRepository;
	@Autowired
	private OrganizerRepository organizerRepository;
	@Autowired
	private CircusRepository circusRepository;
	@Autowired
	private ApplePayRepository applePayRepository;

	//////////////////////////////////////////////////////////////
	// PERSON
	//////////////////////////////////////////////////////////////

	@Transactional
	public Person createPerson(String name) {
		if (name == null || name.trim().length() == 0) {
			throw new IllegalArgumentException("Person name cannot be empty!");
		} else if (personRepository.existsById(name)) {
			throw new IllegalArgumentException("Person has already been created!");
		}
		Person person = new Person();
		person.setName(name);
		personRepository.save(person);
		return person;
	}


	@Transactional
	public Person getPerson(String name) {
		if (name == null || name.trim().length() == 0) {
			throw new IllegalArgumentException("Person name cannot be empty!");
		}
		Person person = personRepository.findByName(name);
		return person;
	}

	@Transactional
	public List<Person> getAllPersons() {
		return toList(personRepository.findAll());
	}

	//////////////////////////////////////////////////////////////
	// EVENT
	//////////////////////////////////////////////////////////////

	@Transactional
	public Event buildEvent(Event event, String name, Date date, Time startTime, Time endTime) {
		// Input validation
		String error = "";
		if (name == null || name.trim().length() == 0) {
			error = error + "Event name cannot be empty! ";
		} else if (eventRepository.existsById(name)) {
			throw new IllegalArgumentException("Event has already been created!");
		}
		if (date == null) {
			error = error + "Event date cannot be empty! ";
		}
		if (startTime == null) {
			error = error + "Event start time cannot be empty! ";
		}
		if (endTime == null) {
			error = error + "Event end time cannot be empty! ";
		}
		if (endTime != null && startTime != null && endTime.before(startTime)) {
			error = error + "Event end time cannot be before event start time!";
		}
		error = error.trim();
		if (error.length() > 0) {
			throw new IllegalArgumentException(error);
		}
		event.setName(name);
		event.setDate(date);
		event.setStartTime(startTime);
		event.setEndTime(endTime);
		return event;
	}

	@Transactional
	public Event createEvent(String name, Date date, Time startTime, Time endTime) {
		String error = "";
		if(name == null || !checkName(name)) {
			error = error + "Event name cannot be empty!";
		}
		if(eventRepository.existsByName(name)) {
			error = "Event has already been created!";
		}
		if(date == null) {
			error = error + "Event date cannot be empty!";
		}
		if(startTime == null) {
			error = error + "Event start time cannot be empty!";
		}
		if(endTime == null) {
			error = error + "Event end time cannot be empty!";
		}
		if(endTime != null && startTime != null && endTime.before(startTime)) {
			error = error + "Event end time cannot be before event start time!";
		}
		error = error.trim();
		if (error.length() > 0) {
			throw new IllegalArgumentException(error);
		}
		Event event = new Event();
		buildEvent(event, name, date, startTime, endTime);
		eventRepository.save(event);
		return event;
	}

	@Transactional
	public Event getEvent(String name) {
		if (name == null || name.trim().length() == 0) {
			throw new IllegalArgumentException("Event name cannot be empty!");
		}
		Event event = eventRepository.findByName(name);
		return event;
	}

	// This returns all objects of instance "Event" (Subclasses are filtered out)
	// Edit : now returns all events (+ circuses)
	@Transactional
	public List<Event> getAllEvents() {
		return toList(eventRepository.findAll()).stream().collect(Collectors.toList());
	}

	//////////////////////////////////////////////////////////////
	// REGISTRATION
	//////////////////////////////////////////////////////////////

	@Transactional
	public Registration register(Person person, Event event) {
		String error = "";
		if (person == null) {
			error = error + "Person needs to be selected for registration! ";
		} else if (!personRepository.existsById(person.getName())) {
			error = error + "Person does not exist! ";
		}
		if (event == null) {
			error = error + "Event needs to be selected for registration!";
		} else if (!eventRepository.existsById(event.getName())) {
			error = error + "Event does not exist!";
		}
		if (registrationRepository.existsByPersonAndEvent(person, event)) {
			error = error + "Person is already registered to this event!";
		}

		error = error.trim();

		if (error.length() > 0) {
			throw new IllegalArgumentException(error);
		}

		Registration registration = new Registration();
		registration.setId(person.getName().hashCode() * event.getName().hashCode());
		registration.setPerson(person);
		registration.setEvent(event);

		registrationRepository.save(registration);

		return registration;
	}

	@Transactional
	public List<Registration> getAllRegistrations() {
		return toList(registrationRepository.findAll());
	}

	@Transactional
	public Registration getRegistrationByPersonAndEvent(Person person, Event event) {
		if (person == null || event == null) {
			throw new IllegalArgumentException("Person or Event cannot be null!");
		}

		return registrationRepository.findByPersonAndEvent(person, event);
	}
	@Transactional
	public List<Registration> getRegistrationsForPerson(Person person){
		if(person == null) {
			throw new IllegalArgumentException("Person cannot be null!");
		}
		return registrationRepository.findByPerson(person);
	}

	@Transactional
	public List<Registration> getRegistrationsByPerson(Person person) {
		return toList(registrationRepository.findByPerson(person));
	}

	@Transactional
	public List<Event> getEventsAttendedByPerson(Person person) {
		if (person == null) {
			throw new IllegalArgumentException("Person cannot be null!");
		}
		List<Event> eventsAttendedByPerson = new ArrayList<>();
		for (Registration r : registrationRepository.findByPerson(person)) {
			eventsAttendedByPerson.add(r.getEvent());
		}
		return eventsAttendedByPerson;
	}

	private <T> List<T> toList(Iterable<T> iterable) {
		List<T> resultList = new ArrayList<T>();
		for (T t : iterable) {
			resultList.add(t);
		}
		return resultList;
	}

	//////////////////////////////////////////////////////////////
	// ORGANIZER
	//////////////////////////////////////////////////////////////
	@Transactional
	public Organizer createOrganizer(String name) {
		if (name == null || name.trim().length() == 0) {
			throw new IllegalArgumentException("Organizer name cannot be empty!");
		} else if (organizerRepository.existsById(name)) {
			throw new IllegalArgumentException("Organizer has already been created!");
		}
		Organizer organizer = new Organizer();
		organizer.setName(name);
		organizerRepository.save(organizer);
		return organizer;
	}

	@Transactional
	public Organizer getOrganizer(String name) {
		if (name == null || name.trim().length() == 0) {
			throw new IllegalArgumentException("Person name cannot be empty!");
		}
		Organizer person = organizerRepository.findByName(name);
		return person;
	}

	@Transactional
	public List<Organizer> getAllOrganizers() {
		return toList(organizerRepository.findAll());
	}


	@Transactional
	public void organizesEvent(Organizer organizer, Event event) {
		String error = "";
		if (organizer == null) {
			error = error + "Organizer needs to be selected for organizes!";
		}
		if(organizer != null && !organizerRepository.existsByName(organizer.getName())) {
			error = error + "Organizer needs to be selected for organizes!";
		}
		if(event == null) {
			error = error + "Event does not exist!";
		} 
		if(event != null && !eventRepository.existsByName(event.getName())) {
			error = error + "Event does not exist!";
		}
		error = error.trim();
		if (error.length() > 0) {
			throw new IllegalArgumentException(error);
		}
		List<Event> organizes = new ArrayList<Event>();
		List<Event> newEvent = new ArrayList<Event>();
		newEvent.add(event);
		organizes = organizer.getOrganizes();
		
		if(organizes == null) {
			organizer.setOrganizes(newEvent);
		} else {
			organizes.add(event);
			organizer.setOrganizes(organizes);
		}
		organizerRepository.save(organizer);
	}
	
	@Transactional
	public List<Event> getEventsOrganizedByOrganizer(Organizer organizer) {
		if (organizer == null) {
			throw new IllegalArgumentException("Person cannot be null!");
		}
		List<Event> eventsOrganizedByOrganizer = new ArrayList<>();
		eventsOrganizedByOrganizer = organizer.getOrganizes();
		
//		if(eventsOrganizedByOrganizer == null) {
//			throw new IllegalArgumentException("Organizer didn't organize an event yet!");
//		} else {
			return eventsOrganizedByOrganizer;
//		}
	}



	//////////////////////////////////////////////////////////////
	// APPLE PAY
	//////////////////////////////////////////////////////////////


	@Transactional
	public ApplePay createApplePay(String deviceID, int amount) {
		String error = "";
		if(amount < 0 ) {
			error = "Payment amount cannot be negative!";
		}
		if(deviceID == null) {
			error = "Device id is null or has wrong format!";
		} 
		if(!isValidDeviceID(deviceID)) {
			error = "Device id is null or has wrong format!";
		}
		if (error.length() > 0) {
			throw new IllegalArgumentException(error);
		}
		if(applePayRepository.existsByDeviceID(deviceID)) {
			ApplePay ap = applePayRepository.findByDeviceID(deviceID);
			ap.setDeviceID(deviceID);
			ap.setAmount(amount);
			applePayRepository.save(ap);
			return ap;
		} else {
			ApplePay ap = new ApplePay();
			ap.setDeviceID(deviceID);
			ap.setAmount(amount);
			applePayRepository.save(ap);
			return ap;
		}

	}
	
	@Transactional
	public ApplePay getApplePay(String deviceID) {
		String error = "";
		if(deviceID == null) {
			error = "DeviceID cannot be empty!";
			throw new IllegalArgumentException(error);
		}
		if(!applePayRepository.existsByDeviceID(deviceID)) {
			error = "ApplePay with deviceID: " + deviceID + " does not exist!";
			throw new IllegalArgumentException(error);
		}
		ApplePay ap = applePayRepository.findByDeviceID(deviceID);
		return ap;
	}
	
	@Transactional
	public List<ApplePay> getAllApplePay() {
		return toList(applePayRepository.findAll());
	}


	@Transactional
	public void pay(Registration r, ApplePay ap) {
		if(ap == null) {
			throw new IllegalArgumentException("Registration and payment cannot be null!");
		}
		if(ap.getAmount() < 0 ) {
			throw new IllegalArgumentException("Payment amount cannot be negative!");
		}
		r.setApplePay(ap);
		registrationRepository.save(r);
	}



	//////////////////////////////////////////////////////////////
	// CIRCUS
	//////////////////////////////////////////////////////////////

	@Transactional
	public List<Circus> getAllCircuses() {
		return toList(circusRepository.findAll());
	}
	
	public Circus getCircus(String name) {
		if (name == null || name.trim().length() == 0) {
			throw new IllegalArgumentException("Event name cannot be empty!");
		}
		return circusRepository.findByName(name);
	}
	
	@Transactional
	public Circus createCircus(String name, Date circusDate, Time startTime, Time endTime, String company) {
		String error = "";
		if(company == null || !checkName(company)) {
			error = error + "Circus company cannot be empty!";
		}
		if(name == null || !checkName(name)) {
			error = error + "Event name cannot be empty!";
		}
		if(circusRepository.existsByName(name)) {
			error = "Event has already been created!";
		}
		if(circusDate == null) {
			error = error + "Event date cannot be empty!";
		}
		if(startTime == null) {
			error = error + "Event start time cannot be empty!";
		}
		if(endTime == null) {
			error = error + "Event end time cannot be empty!";
		}
		if(endTime != null && startTime != null && endTime.before(startTime)) {
			error = error + "Event end time cannot be before event start time!";
		}
		error = error.trim();
		if (error.length() > 0) {
			throw new IllegalArgumentException(error);
		}
		Circus c = new Circus();
		c.setCompany(company);
		c.setName(name);
		c.setDate(circusDate);
		c.setStartTime(startTime);
		c.setEndTime(endTime);
		circusRepository.save(c);
		return c;
	}
	

	
	//////////////////////////////////////////////////////////////
	// HELPER
	//////////////////////////////////////////////////////////////

	public boolean isValidDeviceID(String deviceID) {
		if (deviceID == null || deviceID.length() != 9) {
			return false;
		}
		int digits = 0;
		int letters = 0;
		for (int i = 0; i < 4; i++) {
			if ((Character.isDigit(deviceID.charAt(i)) == true)) {
				digits++;
			}
		}
		for (int i = 5; i < 9; i++) {
			if ((Character.isLetter(deviceID.charAt(i)) == true)) {
				letters++;
			}
		}
		if(digits == 4 && letters == 4) {
			return true;
		} else {
			return false;
		}	
	}
	
	public boolean checkName(String companyName) {
		if(companyName.trim().length() <= 0) {
			return false;
		} else {
			return true;
		}
	}







}
