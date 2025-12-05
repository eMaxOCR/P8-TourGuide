package com.openclassrooms.tourguide;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import gpsUtil.location.VisitedLocation;
import com.openclassrooms.tourguide.model.AttractionsProximity;
import com.openclassrooms.tourguide.model.User;
import com.openclassrooms.tourguide.model.UserReward;
import com.openclassrooms.tourguide.service.TourGuideService;

import tripPricer.Provider;

@RestController
public class TourGuideController {

	@Autowired
	TourGuideService tourGuideService;
	
	/**
	 * Handles HTTP GET requests to the root URL ("/") of the application.
	 * This method returns a simple greeting message indicating that the TourGuide
	 * service is running. 
	 *
	 * @return Json with greeting message
	 */
    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }
    
    /**
     * Handles HTTP GET requests to retrieve the current location of a specified user.
     *
     * @param String userName 
     * @return Json representing the user's current location
     */
    @RequestMapping("/getLocation") 
    public VisitedLocation getLocation(@RequestParam String userName) {
    	return tourGuideService.getUserLocation(tourGuideService.getUser(userName));
    }
    
    /**
     * Handles HTTP GET requests to retrieve nearby attractions for a specified user.
     *
     * @param String userName
     * @return Json object representing attractions near the user
     */
    @RequestMapping("/getNearbyAttractions") 
    public AttractionsProximity getNearbyAttractions(@RequestParam String userName) {
    	return tourGuideService.getNearByAttractions(userName);
    }
    
    /**
     * Handles HTTP GET requests to retrieve all rewards for a specified user.
     *
     * @param String userName 
     * @return Json listing user's rewards
     */
    @RequestMapping("/getRewards") 
    public List<UserReward> getRewards(@RequestParam String userName) {
    	return tourGuideService.getUserRewards(tourGuideService.getUser(userName));
    }
       
    /**
     * Retrieve available trip deals for a specified user.
     *
     * @param String userName 
     * @return Json listing available trip deals
     */
    @RequestMapping("/getTripDeals")
    public List<Provider> getTripDeals(@RequestParam String userName) {
    	return tourGuideService.getTripDeals(tourGuideService.getUser(userName));
    } 

}