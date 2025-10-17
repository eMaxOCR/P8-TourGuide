package com.openclassrooms.tourguide.mapper;

import com.openclassrooms.tourguide.model.NearbyAttraction;
import com.openclassrooms.tourguide.model.DTO.NearbyAttractionDTO;

public class NearyAttractionMapper {

	/**
     * Convert NearbyAttraction to NearbyAttractionDTO
     *
     * @param NearbyAttraction
     * @return NearbyAttractionDTO
     */
	public NearbyAttractionDTO toDto (NearbyAttraction nearbyAttraction) {
		NearbyAttractionDTO nearbyAttractionDTO = new NearbyAttractionDTO();
		nearbyAttractionDTO.setAttractionName(nearbyAttraction.getAttraction().attractionName);
		nearbyAttractionDTO.setAttractionLatitude(nearbyAttraction.getAttraction().latitude);
		nearbyAttractionDTO.setAttractionlongitude(nearbyAttraction.getAttraction().longitude);
		nearbyAttractionDTO.setDistanceMiles(nearbyAttraction.getDistanceMiles());
		nearbyAttractionDTO.setRewardPoint(nearbyAttraction.getRewardPoint());
		
		return nearbyAttractionDTO;
	}
	
}
