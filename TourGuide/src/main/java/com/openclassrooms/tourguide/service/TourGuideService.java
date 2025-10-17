package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.mapper.NearyAttractionMapper;
import com.openclassrooms.tourguide.model.AttractionProximity;
import com.openclassrooms.tourguide.model.NearbyAttraction;
import com.openclassrooms.tourguide.model.User;
import com.openclassrooms.tourguide.model.UserReward;
import com.openclassrooms.tourguide.model.DTO.NearbyAttractionDTO;
import com.openclassrooms.tourguide.tracker.Tracker;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	private final GpsUtil gpsUtil;
	private final RewardsService rewardsService;
	private final TripPricer tripPricer = new TripPricer();
	public final Tracker tracker;
	private RewardCentral rewardCentral = new RewardCentral();
	boolean testMode = true;
	private NearyAttractionMapper nearyAttractionMapper = new NearyAttractionMapper();
	private final ExecutorService executorService = Executors.newCachedThreadPool(); //Create scalable pool thread

	public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
		this.gpsUtil = gpsUtil;
		this.rewardsService = rewardsService;
		
		Locale.setDefault(Locale.US);

		if (testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}

	public CopyOnWriteArrayList<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}

	public VisitedLocation getUserLocation(User user) {
		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ? user.getLastVisitedLocation()
				: trackUserLocation(user);
		return visitedLocation;
	}

	public User getUser(String userName) {
		return internalUserMap.get(userName);
	}

	public List<User> getAllUsers() {
		return internalUserMap.values().stream().collect(Collectors.toList());
	}

	public void addUser(User user) {
		if (!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}

	public List<Provider> getTripDeals(User user) {
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(),
				user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
				user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}

	public VisitedLocation trackUserLocation(User user) {
		VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
		user.addToVisitedLocations(visitedLocation);
		rewardsService.calculateRewards(user);
		return visitedLocation;
	}

	/**
	 * Gets the five closest tourist attractions for a given user.
	 *
	 * This method finds the user's last known location.
	 * Then, it calculates which five attractions are the nearest.
	 *
	 * @param userName The name of the user to search for.
	 * @return An object that contains the user's location and a list of the five closest attractions.
	 */
	public AttractionProximity getNearByAttractions(String userName) {
		AttractionProximity attractionProximity = new AttractionProximity(); 
		List<NearbyAttractionDTO> listDistanceBetween = getDistanceBetweenUserAndAllAttraction(userName,5);	 //List of distance between user and attractions.
		VisitedLocation userLocation = getUserLocation(getUser(userName));									 //Collect user location
		
		attractionProximity.setUserName(userName);
		attractionProximity.setUserLocation(userLocation.location);
		attractionProximity.setNearestAttractions(listDistanceBetween);
		
		return attractionProximity;
	}

	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				tracker.stopTracking();
			}
		});
	}
	
	/**
	 * Gets the closest attractions for a user.
	 *
	 * This method calculates the distance from the user to every attraction,
	 * sorts them from nearest to farthest, and returns the closest results.
	 *
	 * @param userName The name of the user to locate.
	 * @param limit    The maximum number of attractions to include in the list.
	 * @return         A sorted list of "ClosestAttractions" objects.
	 */
	public List<NearbyAttractionDTO> getDistanceBetweenUserAndAllAttraction(String userName, Integer limit){
		
		//get all attractions.
		List<Attraction> attractions = gpsUtil.getAttractions();
		//get user location
		VisitedLocation userLocation = getUserLocation(getUser(userName));
		//set up distance between list
		List<NearbyAttraction> allAttractionsNearUser = new ArrayList<>();	
		
		//for each attraction, calculate distance from attraction to user 
		for(Attraction a : attractions) {
			NearbyAttraction temp = new NearbyAttraction();		
			
			temp.setAttraction(a); 
			temp.setDistanceMiles(rewardsService.getDistance(a,userLocation.location));
			
			allAttractionsNearUser.add(temp);
		}
		
		//Filter distance by minimum.
		allAttractionsNearUser.sort(Comparator.comparingDouble(NearbyAttraction::getDistanceMiles));
		
		//take 5 first attractions
		List<NearbyAttraction> xFirst = allAttractionsNearUser.stream()
                .limit(limit)
                .collect(Collectors.toList());
		
		//use thread to fetch reward points in same time
		List<CompletableFuture<Void>> futures = xFirst.stream()
		        .map(nearbyAttraction -> CompletableFuture.runAsync(() -> {
		            // each attraction are on there own thread
		            int rewardPoints = rewardCentral.getAttractionRewardPoints(
		                nearbyAttraction.getAttraction().attractionId,
		                getUser(userName).getUserId()
		            );
		            // update object when available
		            nearbyAttraction.setRewardPoint(rewardPoints);
		        }, executorService)) 
		        .collect(Collectors.toList());

		    //wait until finished
		    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		
		//adding attraction into DTO format.
		List<NearbyAttractionDTO> newbyAttractionDTOList = new ArrayList<>();
		    
		for(NearbyAttraction n : xFirst) {
			newbyAttractionDTOList.add(nearyAttractionMapper.toDto(n));
		}
		 
		return newbyAttractionDTOList;
	}

	/**********************************************************************************
	 * 
	 * Methods Below: For Internal Testing
	 * 
	 **********************************************************************************/
	private static final String tripPricerApiKey = "test-server-api-key";
	// Database connection will be used for external users, but for testing purposes
	// internal users are provided and stored in memory
	private final Map<String, User> internalUserMap = new HashMap<>();

	private void initializeInternalUsers() {
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);

			internalUserMap.put(userName, user);
		});
		logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}

	private void generateUserLocationHistory(User user) {
		IntStream.range(0, 3).forEach(i -> {
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(),
					new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}

	private double generateRandomLongitude() {
		double leftLimit = -180;
		double rightLimit = 180;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private double generateRandomLatitude() {
		double leftLimit = -85.05112878;
		double rightLimit = 85.05112878;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private Date getRandomTime() {
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
		return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}

}
