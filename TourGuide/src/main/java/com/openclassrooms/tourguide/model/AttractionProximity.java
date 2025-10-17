package com.openclassrooms.tourguide.model;

import java.util.List;

import com.openclassrooms.tourguide.model.DTO.NearbyAttractionDTO;

import gpsUtil.location.Location;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttractionProximity {
	
	private String userName;
	private Location userLocation;
	private List<NearbyAttractionDTO> nearestAttractions;
	
}