package com.openclassrooms.tourguide.model;

import gpsUtil.location.Attraction;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NearbyAttraction {

	private Attraction attraction;
	private Double distanceMiles;
	private Integer rewardPoint;
	
}