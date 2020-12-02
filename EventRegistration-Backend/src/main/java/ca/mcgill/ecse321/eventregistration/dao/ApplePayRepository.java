package ca.mcgill.ecse321.eventregistration.dao;

import org.springframework.data.repository.CrudRepository;

import ca.mcgill.ecse321.eventregistration.model.ApplePay;
import ca.mcgill.ecse321.eventregistration.model.Registration;

public interface ApplePayRepository extends CrudRepository<ApplePay, String>{

	ApplePay findByDeviceID(String deviceID);
	boolean existsByDeviceID(String deviceID);
	boolean existsByRegistration(Registration registration);
	ApplePay findByRegistration(Registration registration);
}
