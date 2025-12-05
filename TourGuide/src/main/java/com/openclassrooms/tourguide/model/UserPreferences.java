package com.openclassrooms.tourguide.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPreferences {
	
	private int attractionProximity = Integer.MAX_VALUE;
	private int tripDuration = 1;
	private int ticketQuantity = 1;
	private int numberOfAdults = 1;
	private int numberOfChildren = 0;
	
	public UserPreferences() {
	}

}
