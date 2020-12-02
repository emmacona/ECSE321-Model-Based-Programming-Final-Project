package ca.mcgill.ecse321.eventregistration.controller;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ca.mcgill.ecse321.eventregistration.model.*;
import ca.mcgill.ecse321.eventregistration.dto.*;
import ca.mcgill.ecse321.eventregistration.service.EventRegistrationService;

@CrossOrigin(origins = "*")
@RestController
public class EventRegistrationRestController {

	@Autowired
	private EventRegistrationService service;

	////////////////////////////////////////////////////////
	// POST MAPPINGS
	///////////////////////////////////////////////////////

	// @formatter:off
	// Turning off formatter here to ease comprehension of the sample code by
	// keeping the linebreaks
	// Example REST call:
	// http://localhost:8088/persons/John
	@PostMapping(value = { "/persons/{name}", "/persons/{name}/" })
	public PersonDto createPerson(@PathVariable("name") String name) throws IllegalArgumentException {
		// @formatter:on
		Person person = service.createPerson(name);
		return convertToDto(person);
	}

	// @formatter:off
	// Example REST call:
	// http://localhost:8080/events/testevent?date=2013-10-23&startTime=00:00&endTime=23:59
	@PostMapping(value = { "/events/{name}", "/events/{name}/" })
	public EventDto createEvent(@PathVariable("name") String name, @RequestParam Date date,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME, pattern = "HH:mm") LocalTime startTime,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME, pattern = "HH:mm") LocalTime endTime, @RequestParam String company)
			throws IllegalArgumentException {
		// @formatter:on
		if(company == null || company.isEmpty() || company == "--") {
			Event event = service.createEvent(name, date, Time.valueOf(startTime), Time.valueOf(endTime));
			return convertToDto(event);
		} else {
			Circus circus = service.createCircus(name, date, Time.valueOf(startTime), Time.valueOf(endTime), company);
			return convertToDto(circus);
		}
		
	}

	// @formatter:off
	@PostMapping(value = { "/register", "/register/" })
	public RegistrationDto registerPersonForEvent(@RequestParam(name = "person") PersonDto pDto,
			@RequestParam(name = "event") EventDto eDto) throws IllegalArgumentException {
		// @formatter:on

		// Both the person and the event are identified by their names
		Person p = service.getPerson(pDto.getName());
		Event e = service.getEvent(eDto.getName());

		Registration r = service.register(p, e);
		return convertToDtoWithoutApplePay(r);
	}
	
	// Example REST call:
	// http://localhost:8088/organizer/Emma
	@PostMapping(value = { "/organizer/{name}", "/organizer/{name}/" })
	public OrganizerDto createOrganizer(@PathVariable("name") String name) throws IllegalArgumentException {
		Organizer organizer = service.createOrganizer(name);
		return convertToDtoWithoutEvents(organizer);
	}
	
	// Example REST call:
	// http://localhost:8088/circus/alegria
	@PostMapping(value = { "/circus/{name}", "/circus/{name}/" })
	public CircusDto createCircus(@PathVariable("name") String name,  @RequestParam Date date,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME, pattern = "HH:mm") LocalTime startTime,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME, pattern = "HH:mm") LocalTime endTime, @RequestParam String company)
			throws IllegalArgumentException {
		// @formatter:on
		Circus circus = service.createCircus(name, date, Time.valueOf(startTime), Time.valueOf(endTime), company);
		return convertToDto(circus);
	}
	
//	// Example REST call:
//	// http://localhost:8088/applePay/0000-AAAA
//	@PostMapping(value = { "/applePay/{device}", "/applePay/{device}/" })
//	public ApplePayDto createApplePay(@PathVariable("device") String deviceID, @RequestParam int amount)
//			throws IllegalArgumentException {
//		ApplePay ap = service.createApplePay(deviceID, amount);
//		return convertToDto(ap);
//	}
	
	@PostMapping(value = { "/pay", "/pay/" })
	public ApplePayDto payForRegistration(@RequestParam(name = "person") PersonDto pDto, 
			@RequestParam(name = "event") EventDto eDto, @RequestParam(name = "deviceID") String deviceID, @RequestParam(name="paymentAmount") int paymentAmount ) throws IllegalArgumentException {

		// ApplePay is referred to by deviceID
		ApplePay ap = service.createApplePay(deviceID, paymentAmount);
		// Registration is referred to by id
		Person person = service.getPerson(pDto.getName());
		Event event = service.getEvent(eDto.getName());
		Registration r = service.getRegistrationByPersonAndEvent(person, event);
		service.pay(r, ap);
		return convertToDto(ap);
	}
	
	@PostMapping(value = { "/organize", "/organize/" })
	public OrganizerDto organizeEvent(@RequestParam(name = "organizer") OrganizerDto oDto, 
			@RequestParam(name = "event") EventDto eDto) throws IllegalArgumentException {

		Organizer organizer = service.getOrganizer(oDto.getName());
		Event event = service.getEvent(eDto.getName());
		service.organizesEvent(organizer, event);
		return(convertToDto(organizer));

	}

	////////////////////////////////////////////////////////
	// GET MAPPINGS
	///////////////////////////////////////////////////////

	@GetMapping(value = { "/events", "/events/" })
	public List<EventDto> getAllEvents() {
		List<EventDto> eventDtos = new ArrayList<>();
		for (Event event : service.getAllEvents()) {
			eventDtos.add(convertToDto(event));
		}
		return eventDtos;
	}

	// Example REST call:
	// http://localhost:8088/events/person/JohnDoe
	@GetMapping(value = { "/events/person/{name}", "/events/person/{name}/" })
	public List<EventDto> getEventsOfPerson(@PathVariable("name") PersonDto pDto) {
		Person p = convertToDomainObject(pDto);
		return createAttendedEventDtosForPerson(p);
	}

	@GetMapping(value = { "/persons/{name}", "/persons/{name}/" })
	public PersonDto getPersonByName(@PathVariable("name") String name) throws IllegalArgumentException {
		return convertToDto(service.getPerson(name));
	}

	@GetMapping(value = { "/registrations", "/registrations/" })
	public RegistrationDto getRegistration(@RequestParam(name = "person") PersonDto pDto,
			@RequestParam(name = "event") EventDto eDto) throws IllegalArgumentException {
		// Both the person and the event are identified by their names
		Person p = service.getPerson(pDto.getName());
		Event e = service.getEvent(eDto.getName());

		Registration r = service.getRegistrationByPersonAndEvent(p, e);
		return convertToDto(r, p, e);
	}
	

	@GetMapping(value = { "/registrations/person/{name}", "/registrations/person/{name}/" })
	public List<RegistrationDto> getRegistrationsForPerson(@PathVariable("name") PersonDto pDto)
			throws IllegalArgumentException {
		// Both the person and the event are identified by their names
		Person p = service.getPerson(pDto.getName());

		return createRegistrationDtosForPerson(p);
	}

	@GetMapping(value = { "/persons", "/persons/" })
	public List<PersonDto> getAllPersons() {
		List<PersonDto> persons = new ArrayList<>();
		for (Person person : service.getAllPersons()) {
			persons.add(convertToDto(person));
		}
		return persons;
	}

	@GetMapping(value = { "/events/{name}", "/events/{name}/" })
	public EventDto getEventByName(@PathVariable("name") String name) throws IllegalArgumentException {
		return convertToDto(service.getEvent(name));
	}

	@GetMapping(value = { "/organizer/{name}", "/organizer/{name}/" })
	public OrganizerDto getOrganizerByName(@PathVariable("name") String name) throws IllegalArgumentException {
		return convertToDto(service.getOrganizer(name));
	}
	
	@GetMapping(value = { "/organizers", "/organizers/" })
	public List<OrganizerDto> getAllOrganizers() {
		List<OrganizerDto> o = new ArrayList<>();
		for (Organizer or : service.getAllOrganizers()) {
			o.add(convertToDto(or));
		}
		return o;
	}
	
	@GetMapping(value = { "/circuses/{name}", "/circus/{name}/" })
	public CircusDto getCircusByName(@PathVariable("name") String name) throws IllegalArgumentException {
		return convertToDto(service.getCircus(name));
	}
	
	@GetMapping(value = { "/circuses", "/circuses/" })
	public List<CircusDto> getAllCircuses() {
		List<CircusDto> c = new ArrayList<>();
		for (Circus cur : service.getAllCircuses()) {
			c.add(convertToDto(cur));
		}
		return c;
	}
	
	@GetMapping(value = { "/applePay/{deviceID}", "/applePay/{deviceID}/" })
	public ApplePayDto getApplePayByDeviceID(@PathVariable("deviceID") String deviceID) throws IllegalArgumentException {
		return convertToDto(service.getApplePay(deviceID));
	}
	
	
	@GetMapping(value = { "/applePays", "/applePays/" })
	public List<ApplePayDto> getAllApplePay() throws IllegalArgumentException {
		List<ApplePayDto> aps = new ArrayList<>();
		for (ApplePay a : service.getAllApplePay()) {
			aps.add(convertToDto(a));
		}
		return aps;
	}
	
	// Model - DTO conversion methods (not part of the API)
	////////////////////////////////////////////////////////
	// CONVERT TO DTO
	///////////////////////////////////////////////////////

	private EventDto convertToDto(Event e) {
		if (e == null) {
			throw new IllegalArgumentException("There is no such Event!");
		}
		if (e instanceof Circus) {
			CircusDto cDto = new CircusDto(e.getName(), ((Circus) e).getCompany(), e.getDate(), e.getStartTime(), e.getEndTime());
			return cDto;
		} else {
			EventDto eventDto = new EventDto(e.getName(), e.getDate(), e.getStartTime(), e.getEndTime());
			eventDto.setCompany("--");
			return eventDto;
		}
		
	}

	private PersonDto convertToDto(Person p) {
		if (p == null) {
			throw new IllegalArgumentException("There is no such Person!");
		}
		PersonDto personDto = new PersonDto(p.getName());
		personDto.setEventsAttended(createAttendedEventDtosForPerson(p));
//		personDto.setPayments(createPaymentDtosForPerson(p));
		return personDto;
	}

	// DTOs for registrations
	private RegistrationDto convertToDto(Registration r, Person p, Event e) {
		EventDto eDto = convertToDto(e);
		PersonDto pDto = convertToDto(p);
		RegistrationDto rDto = new RegistrationDto(pDto, eDto);
		rDto.setPayment(convertToDto(r.getApplePay()));
		return rDto;
	}

	private RegistrationDto convertToDtoWithoutApplePay(Registration r) {
		EventDto eDto = convertToDto(r.getEvent());
		PersonDto pDto = convertToDto(r.getPerson());
		RegistrationDto rDto = new RegistrationDto(pDto, eDto);
		return rDto;
	}

	// return registration dto without peron object so that we are not repeating
	// data
	private RegistrationDto convertToDtoWithoutPerson(Registration r) {
		PersonDto eDto = convertToDto(r.getPerson());
		EventDto pDto = convertToDto(r.getEvent());
		RegistrationDto rDto = new RegistrationDto(eDto, pDto);
		rDto.setPerson(null);
		return rDto;
	}

	private Person convertToDomainObject(PersonDto pDto) {
		List<Person> allPersons = service.getAllPersons();
		for (Person person : allPersons) {
			if (person.getName().equals(pDto.getName())) {
				return person;
			}
		}
		return null;
	}
	
	private OrganizerDto convertToDto(Organizer o) {
		if (o == null) {
			throw new IllegalArgumentException("There is no such Person!");
		}
		OrganizerDto organizerDto = new OrganizerDto(o.getName());
		organizerDto.setEvents(createOrganizedEventDtosForOrganizer(o));
		return organizerDto;
	}
	
	private OrganizerDto convertToDtoWithoutEvents(Organizer o) {
		if (o == null) {
			throw new IllegalArgumentException("There is no such Person!");
		}
		OrganizerDto organizerDto = new OrganizerDto(o.getName());
		return organizerDto;
	}
	
	private CircusDto convertToDto(Circus c) {
		if(c == null) {
			throw new IllegalArgumentException("There is no such Event!");
		}
		CircusDto cDto = new CircusDto(c.getName(), c.getCompany(), c.getDate(), c.getStartTime(), c.getEndTime());
		return cDto;
	}
	
	private ApplePayDto convertToDto(ApplePay ap) {
		if(ap == null) {
			throw new IllegalArgumentException("There is no such ApplePay!");
		}
		ApplePayDto apDto = new ApplePayDto(ap.getDeviceID(), ap.getAmount());
		return apDto;
	}

	// Other extracted methods (not part of the API)
	////////////////////////////////////////////////////////
	// HELPER
	///////////////////////////////////////////////////////

	private List<EventDto> createAttendedEventDtosForPerson(Person p) {
		List<Event> eventsForPerson = service.getEventsAttendedByPerson(p);
		List<EventDto> events = new ArrayList<>();
		for (Event event : eventsForPerson) {
			events.add(convertToDto(event));
		}
		return events;
	}

	private List<RegistrationDto> createRegistrationDtosForPerson(Person p) {
		List<Registration> registrationsForPerson = service.getRegistrationsForPerson(p);
		List<RegistrationDto> registrations = new ArrayList<RegistrationDto>();
		for (Registration r : registrationsForPerson) {
			registrations.add(convertToDtoWithoutPerson(r));
		}
		return registrations;
	}
	
	private List<EventDto> createOrganizedEventDtosForOrganizer(Organizer o) {
		List<Event> eventsForOrganizer = service.getEventsOrganizedByOrganizer(o);
		List<EventDto> events = new ArrayList<>();
		for (Event event : eventsForOrganizer) {
			events.add(convertToDto(event));
		}
		return events;
	}
	
	private List<ApplePayDto> createPaymentDtosForPerson(Person p) {
		List<ApplePayDto> paymentsForPerson = new ArrayList<>();
		List<Registration> registrationForPerson = service.getRegistrationsByPerson(p);
		for(Registration r : registrationForPerson) {
			paymentsForPerson.add(convertToDto(r.getApplePay()));	
		}
		return paymentsForPerson;
	}
	
}
