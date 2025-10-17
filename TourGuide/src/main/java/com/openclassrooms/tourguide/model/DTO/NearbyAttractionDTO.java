package com.openclassrooms.tourguide.model.DTO;

import lombok.Data;

@Data
public class NearbyAttractionDTO {
	
	private String attractionName;
	private Double attractionLatitude;
	private Double attractionlongitude;
	private Double distanceMiles;
	private Integer rewardPoint;

}
