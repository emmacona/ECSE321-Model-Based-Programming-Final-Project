package ca.mcgill.ecse321.eventregistration.dto;

public class ApplePayDto {

	private String deviceID;
	private int amount;

	public ApplePayDto() {

	}

	public ApplePayDto(String deviceID, int amount) {
		this.setDeviceID(deviceID);
		this.setAmount(amount);
	}

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}
}
