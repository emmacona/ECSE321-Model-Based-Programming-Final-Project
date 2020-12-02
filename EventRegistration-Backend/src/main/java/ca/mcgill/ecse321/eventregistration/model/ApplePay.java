package ca.mcgill.ecse321.eventregistration.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class ApplePay {
	
    private String deviceID;

    public void setDeviceID(String appleDeviceID) {
        this.deviceID = appleDeviceID;
    }
    
    @Id
    public String getDeviceID() {
        return this.deviceID;
    }
	
    private int amount;
    
    public void setAmount(int amount) {
    	this.amount = amount;
    }
    
    public int getAmount() {
    	return this.amount;
    }
    
    
    private Registration registration;
 
    
    @OneToOne(optional = true)
    public Registration getRegistration() {
    	return this.registration;
    }
    
    
    public void setRegistration(Registration registration) {
    	this.registration = registration;
    }
    
}
