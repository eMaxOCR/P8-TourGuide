package com.openclassrooms.tourguide.mapper;

import java.util.List;
import org.springframework.stereotype.Service;
import com.openclassrooms.tourguide.model.AttractionsProximity;
import com.openclassrooms.tourguide.model.DTO.NearbyAttractionDTO;
import gpsUtil.location.Location;

@Service
public class AttractionsProximityMapper {

	/**
     * Put attractionsProximity's informations
     *
     * @param userName, userLocation, listDistanceBetween
     * @return AttractionsProximity
     */
	public AttractionsProximity toAttractionsProximity (String userName, Location location, List<NearbyAttractionDTO> nearbyAttractionDTO) {
		AttractionsProximity attractionsProximity = new AttractionsProximity();
		attractionsProximity.setUserName(userName);
		attractionsProximity.setUserLocation(location);
		attractionsProximity.setNearestAttractions(nearbyAttractionDTO);
		
		return attractionsProximity;
	}
	
}
