package com.example.spring_boot_project_api.config;

import com.example.spring_boot_project_api.enums.GenderEnum;
import com.example.spring_boot_project_api.enums.UserEnum;
import com.example.spring_boot_project_api.model.BusinesssOwnerProfiles;
import com.example.spring_boot_project_api.model.CartItems;
import com.example.spring_boot_project_api.model.Carts;
import com.example.spring_boot_project_api.model.Districts;
import com.example.spring_boot_project_api.model.Favorites;
import com.example.spring_boot_project_api.model.FoodCategories;
import com.example.spring_boot_project_api.model.FoodOrderItems;
import com.example.spring_boot_project_api.model.FoodOrders;
import com.example.spring_boot_project_api.model.Foods;
import com.example.spring_boot_project_api.model.HotelRooms;
import com.example.spring_boot_project_api.model.Hotels;
import com.example.spring_boot_project_api.model.Notifications;
import com.example.spring_boot_project_api.model.Payments;
import com.example.spring_boot_project_api.model.PlaceCategoties;
import com.example.spring_boot_project_api.model.PlaceImages;
import com.example.spring_boot_project_api.model.Promotions;
import com.example.spring_boot_project_api.model.Provinces;
import com.example.spring_boot_project_api.model.Restaurants;
import com.example.spring_boot_project_api.model.Reviews;
import com.example.spring_boot_project_api.model.Roles;
import com.example.spring_boot_project_api.model.RoomBookings;
import com.example.spring_boot_project_api.model.Rooms;
import com.example.spring_boot_project_api.model.RoomTypes;
import com.example.spring_boot_project_api.model.TicketBookings;
import com.example.spring_boot_project_api.model.Tickets;
import com.example.spring_boot_project_api.model.TourBookings;
import com.example.spring_boot_project_api.model.TourGuides;
import com.example.spring_boot_project_api.model.TourPackageStops;
import com.example.spring_boot_project_api.model.TourPackages;
import com.example.spring_boot_project_api.model.TourismPlaces;
import com.example.spring_boot_project_api.model.UserRoles;
import com.example.spring_boot_project_api.model.Users;
import com.example.spring_boot_project_api.repository.BusinessOwnerProfileRepository;
import com.example.spring_boot_project_api.repository.CartItemRepository;
import com.example.spring_boot_project_api.repository.CartRepository;
import com.example.spring_boot_project_api.repository.DistrictRepository;
import com.example.spring_boot_project_api.repository.FavoriteRepository;
import com.example.spring_boot_project_api.repository.FoodCategoryRepository;
import com.example.spring_boot_project_api.repository.FoodOrderItemRepository;
import com.example.spring_boot_project_api.repository.FoodOrderRepository;
import com.example.spring_boot_project_api.repository.FoodRepository;
import com.example.spring_boot_project_api.repository.HotelRepository;
import com.example.spring_boot_project_api.repository.HotelRoomRepository;
import com.example.spring_boot_project_api.repository.NotificationRepository;
import com.example.spring_boot_project_api.repository.PaymentRepository;
import com.example.spring_boot_project_api.repository.PlaceCategoryRepository;
import com.example.spring_boot_project_api.repository.PlaceImageRepository;
import com.example.spring_boot_project_api.repository.PromotionRepository;
import com.example.spring_boot_project_api.repository.ProvinceRepository;
import com.example.spring_boot_project_api.repository.RestaurantRepository;
import com.example.spring_boot_project_api.repository.ReviewRepository;
import com.example.spring_boot_project_api.repository.RoleRepository;
import com.example.spring_boot_project_api.repository.RoomBookingRepository;
import com.example.spring_boot_project_api.repository.RoomRepository;
import com.example.spring_boot_project_api.repository.RoomTypeRepository;
import com.example.spring_boot_project_api.repository.TicketBookingRepository;
import com.example.spring_boot_project_api.repository.TicketRepository;
import com.example.spring_boot_project_api.repository.TourBookingRepository;
import com.example.spring_boot_project_api.repository.TourGuideRepository;
import com.example.spring_boot_project_api.repository.TourPackageRepository;
import com.example.spring_boot_project_api.repository.TourPackageStopRepository;
import com.example.spring_boot_project_api.repository.TourismPlaceRepository;
import com.example.spring_boot_project_api.repository.UserRepository;
import com.example.spring_boot_project_api.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Seeds the empty database with realistic Khmer tourism data on first boot.
 * Skipped automatically when provinces already exist.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final PlaceCategoryRepository placeCategoryRepository;
    private final TourismPlaceRepository tourismPlaceRepository;
    private final PlaceImageRepository placeImageRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final BusinessOwnerProfileRepository businessOwnerProfileRepository;
    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final HotelRoomRepository hotelRoomRepository;
    private final RoomRepository roomRepository;
    private final RestaurantRepository restaurantRepository;
    private final FoodCategoryRepository foodCategoryRepository;
    private final FoodRepository foodRepository;
    private final FoodOrderRepository foodOrderRepository;
    private final FoodOrderItemRepository foodOrderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final TourGuideRepository tourGuideRepository;
    private final TourPackageRepository tourPackageRepository;
    private final TourPackageStopRepository tourPackageStopRepository;
    private final TicketRepository ticketRepository;
    private final TicketBookingRepository ticketBookingRepository;
    private final TourBookingRepository tourBookingRepository;
    private final RoomBookingRepository roomBookingRepository;
    private final ReviewRepository reviewRepository;
    private final PromotionRepository promotionRepository;
    private final FavoriteRepository favoriteRepository;
    private final NotificationRepository notificationRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (provinceRepository.count() > 0) {
            log.info("DataSeeder skipped: database already contains data.");
            return;
        }

        log.info("DataSeeder: starting Khmer tourism data seeding...");

        Map<String, Roles> roles = seedRoles();
        Map<String, Users> users = seedUsers();
        assignRoles(users, roles);
        Map<String, Provinces> provinces = seedProvinces();
        Map<String, Districts> districts = seedDistricts(provinces);
        Map<String, PlaceCategoties> categories = seedPlaceCategories();
        Map<String, TourismPlaces> places = seedTourismPlaces(categories, districts, users.get("admin"));
        seedPlaceImages(places);
        seedBusinessProfiles(users.get("sopheak"), users.get("leakhana"));
        Map<String, Hotels> hotels = seedHotels(districts, users);
        Map<String, RoomTypes> roomTypes = seedRoomTypes();
        seedHotelRooms(hotels, roomTypes);
        Map<Long, Rooms> rooms = seedRooms(hotels, roomTypes);
        Map<String, Restaurants> restaurants = seedRestaurants(places);
        Map<String, FoodCategories> foodCategories = seedFoodCategories();
        Map<String, Foods> foods = seedFoods(restaurants, foodCategories);
        Map<String, TourGuides> guides = seedTourGuides(users);
        Map<String, TourPackages> packages = seedTourPackages(provinces, guides);
        seedTourPackageStops(packages, places);
        Map<String, Tickets> tickets = seedTickets(places);
        seedReviews(users, places, hotels, restaurants, packages);
        seedPromotions(hotels, restaurants, packages);
        seedNotifications(users);
        seedCart(users.get("dara"), restaurants.get("redPiano"), foods);
        seedOrderActivity(users, restaurants, foods, packages, tickets, rooms, hotels);
        seedFavorites(users, places, hotels, restaurants, packages);

        log.info("DataSeeder: completed.");
    }

    private Map<String, Roles> seedRoles() {
        String[] names = {"ADMIN", "OWNER", "TOUR_GUIDE", "CUSTOMER"};
        Map<String, Roles> map = new LinkedHashMap<>();
        for (String n : names) {
            Roles r = new Roles();
            r.setName(n);
            map.put(n, roleRepository.save(r));
        }
        return map;
    }

    private Map<String, Users> seedUsers() {
        Object[][] rows = {
            {"admin", "Admin Manager", "admin@khmerstay.com", GenderEnum.Male, "Phnom Penh, Doun Penh", "1990-01-01"},
            {"sopheak", "Sopheak Nara", "sopheak@khmerstay.com", GenderEnum.Male, "Siem Reap, Mondul 2", "1985-06-15"},
            {"leakhana", "Leakhana Vann", "leakhana@khmerstay.com", GenderEnum.Female, "Preah Sihanouk, Mittakpheap", "1990-11-02"},
            {"ratha", "Ratha Keo", "ratha@khmerstay.com", GenderEnum.Male, "Siem Reap, Kralanh", "1994-03-20"},
            {"channary", "Channary Lim", "channary@khmerstay.com", GenderEnum.Female, "Mondulkiri, Sen Monorom", "1995-08-25"},
            {"piseth", "Piseth Uy", "piseth@khmerstay.com", GenderEnum.Male, "Preah Sihanouk, Prey Nob", "1993-12-05"},
            {"dara", "Dara Sok", "dara@khmerstay.com", GenderEnum.Male, "Phnom Penh, Toul Kork", "1997-05-30"},
            {"sokha", "Sokha Thida", "sokha@khmerstay.com", GenderEnum.Female, "Phnom Penh, Boeung Keng Kang", "1999-09-12"},
        };
        Map<String, Users> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Users u = new Users();
            u.setUsername((String) row[0]);
            u.setFullname((String) row[1]);
            u.setEmail((String) row[2]);
            u.setPassword(encoder.encode("khmer@123"));
            u.setGender((GenderEnum) row[3]);
            u.setAddress((String) row[4]);
            u.setDateOfBirth(LocalDate.parse((String) row[5]));
            u.setStatus(UserEnum.Online);
            u.setAvatarUrl("https://i.pravatar.cc/150?u=" + row[0]);
            map.put((String) row[0], userRepository.save(u));
        }
        return map;
    }

    private void assignRoles(Map<String, Users> users, Map<String, Roles> roles) {
        List<Object[]> rows = List.of(
            new Object[]{"admin", "ADMIN"},
            new Object[]{"sopheak", "OWNER"},
            new Object[]{"leakhana", "OWNER"},
            new Object[]{"ratha", "TOUR_GUIDE"},
            new Object[]{"channary", "TOUR_GUIDE"},
            new Object[]{"piseth", "TOUR_GUIDE"},
            new Object[]{"dara", "CUSTOMER"},
            new Object[]{"sokha", "CUSTOMER"}
        );
        for (Object[] row : rows) {
            UserRoles ur = new UserRoles();
            ur.setUser(users.get((String) row[0]));
            ur.setRole(roles.get((String) row[1]));
            userRoleRepository.save(ur);
        }
    }

    private Map<String, Provinces> seedProvinces() {
        String[] names = {
            "Banteay Meanchey", "Battambang", "Kampong Cham", "Kampong Chhnang", "Kampong Speu",
            "Kampong Thom", "Kampot", "Kandal", "Kep", "Koh Kong", "Kratie", "Mondulkiri",
            "Oddar Meanchey", "Pailin", "Phnom Penh", "Preah Vihear", "Prey Veng", "Pursat",
            "Ratanakiri", "Siem Reap", "Preah Sihanouk", "Stung Treng", "Svay Rieng", "Takeo",
            "Tboung Khmum"
        };
        Map<String, Provinces> map = new LinkedHashMap<>();
        for (String n : names) {
            Provinces p = new Provinces();
            p.setName(n);
            p.setImage("https://images.unsplash.com/photo-1508159441828-3d031a33e1e3?w=600&h=400&fit=crop");
            map.put(n, provinceRepository.save(p));
        }
        return map;
    }

    private Map<String, Districts> seedDistricts(Map<String, Provinces> provinces) {
        Object[][] rows = {
            {provinces.get("Phnom Penh"), "Doun Penh"},
            {provinces.get("Phnom Penh"), "Boeung Keng Kang"},
            {provinces.get("Siem Reap"), "Siem Reap"},
            {provinces.get("Siem Reap"), "Banteay Srei"},
            {provinces.get("Siem Reap"), "Prasat Bakong"},
            {provinces.get("Siem Reap"), "Kulen"},
            {provinces.get("Battambang"), "Battambang"},
            {provinces.get("Battambang"), "Sangkae"},
            {provinces.get("Kampot"), "Kampot"},
            {provinces.get("Kampot"), "Tuek Chhou"},
            {provinces.get("Preah Sihanouk"), "Mittakpheap"},
            {provinces.get("Preah Sihanouk"), "Prey Nob"},
            {provinces.get("Kandal"), "Ta Khmau"},
            {provinces.get("Kandal"), "Khsach Kandal"},
            {provinces.get("Koh Kong"), "Khemarak Phoumin"},
            {provinces.get("Kep"), "Damnak Chang'aeur"},
            {provinces.get("Kratie"), "Kratie"},
            {provinces.get("Kratie"), "Sambor"},
            {provinces.get("Mondulkiri"), "Sen Monorom"},
            {provinces.get("Ratanakiri"), "Banlung"},
            {provinces.get("Preah Vihear"), "Choam Khsant"},
            {provinces.get("Preah Vihear"), "Sangkom Thmei"},
            {provinces.get("Kampong Thom"), "Stung Saen"},
            {provinces.get("Pursat"), "Sampov Meas"},
            {provinces.get("Stung Treng"), "Stung Treng"},
            {provinces.get("Oddar Meanchey"), "Samraong"},
            {provinces.get("Kampong Cham"), "Kampong Cham"},
            {provinces.get("Takeo"), "Doun Kaev"},
            {provinces.get("Kampong Speu"), "Chbar Mon"},
            {provinces.get("Banteay Meanchey"), "Serei Saophoan"},
            {provinces.get("Svay Rieng"), "Svay Rieng"},
            {provinces.get("Pailin"), "Pailin"},
            {provinces.get("Prey Veng"), "Prey Veng"},
            {provinces.get("Tboung Khmum"), "Suong"},
            {provinces.get("Kampong Chhnang"), "Kampong Chhnang"}
        };
        Map<String, Districts> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Districts d = new Districts();
            d.setName((String) row[1]);
            d.setProvinces((Provinces) row[0]);
            map.put((String) row[1], districtRepository.save(d));
        }
        return map;
    }

    private Map<String, PlaceCategoties> seedPlaceCategories() {
        Object[][] rows = {
            {"Temple", "https://images.unsplash.com/photo-1508159441828-3d031a33e1e3?w=600&h=400&fit=crop"},
            {"Heritage", "https://images.unsplash.com/photo-1569949381669-ecf31ae866fd?w=600&h=400&fit=crop"},
            {"Nature", "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=600&h=400&fit=crop"},
            {"Beach", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600&h=400&fit=crop"},
            {"City", "https://images.unsplash.com/photo-1553877522-43269d4ea984?w=600&h=400&fit=crop"},
            {"Adventure", "https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=600&h=400&fit=crop"}
        };
        Map<String, PlaceCategoties> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            PlaceCategoties c = new PlaceCategoties();
            c.setName((String) row[0]);
            c.setImage((String) row[1]);
            map.put((String) row[0], placeCategoryRepository.save(c));
        }
        return map;
    }

    private Map<String, TourismPlaces> seedTourismPlaces(Map<String, PlaceCategoties> categories,
                                                         Map<String, Districts> districts, Users admin) {
        Object[][] rows = {
            {"angkorWat", "Angkor Wat", "The largest religious monument in the world and the heart of the Khmer Empire. Famous for its five lotus towers and sunrise views over the reflecting pools.", "Krong Siem Reap", "13.4125", "103.8670", "Temple", "Siem Reap", "4.8"},
            {"bayon", "Bayon Temple", "The state temple of Jayavarman VII, featuring dozens of towering stone faces gazing in every direction.", "Angkor Thom, Siem Reap", "13.4412", "103.8590", "Temple", "Siem Reap", "4.7"},
            {"taProhm", "Ta Prohm", "A temple left in its natural state, embraced by giant strangler fig roots, made famous by Lara Croft: Tomb Raider.", "Angkor, Siem Reap", "13.4347", "103.8892", "Temple", "Siem Reap", "4.7"},
            {"banteaySrei", "Banteay Srei", "The Citadel of Women, a jewel of Khmer art carved from pink sandstone with intricate, finely detailed reliefs.", "Banteay Srei District", "13.5989", "103.9628", "Temple", "Banteay Srei", "4.8"},
            {"angkorThom", "Angkor Thom", "The last great capital of the Khmer Empire, enclosed by a moat and walls, with the Bayon at its center.", "Angkor Thom, Siem Reap", "13.4404", "103.8596", "Heritage", "Siem Reap", "4.6"},
            {"tonleSap", "Tonle Sap Floating Village", "A living floating community on the largest freshwater lake in Southeast Asia, best explored by boat.", "Kampong Phluk, Siem Reap", "13.2507", "103.9210", "Nature", "Prasat Bakong", "4.5"},
            {"kohKer", "Koh Ker", "A remote 10th-century temple complex with the seven-tiered Prasat Thom pyramid hidden in the forest.", "Kulen District", "13.7833", "104.5333", "Heritage", "Kulen", "4.6"},
            {"royalPalace", "Royal Palace Phnom Penh", "The official royal residence, home of the Silver Pagoda, golden throne hall and beautifully kept gardens.", "Sothearos Blvd, Phnom Penh", "11.5629", "104.9292", "Heritage", "Doun Penh", "4.6"},
            {"watPhnom", "Wat Phnom", "The oldest temple in Phnom Penh, atop a hill of the same name, surrounded by shady parkland.", "Norodom Blvd, Phnom Penh", "11.5762", "104.9223", "Temple", "Doun Penh", "4.3"},
            {"centralMarket", "Central Market (Phsar Thmei)", "An iconic Art Deco market in a domed building selling everything from jewelry to street food.", "Phnom Penh", "11.5696", "104.9210", "City", "Doun Penh", "4.4"},
            {"tuolSleng", "Tuol Sleng Genocide Museum", "A former high school turned prison (S-21) that now serves as a sobering memorial to the Khmer Rouge era.", "Phnom Penh", "11.5498", "104.9170", "Heritage", "Doun Penh", "4.6"},
            {"kohRong", "Koh Rong", "A tropical island paradise with powder-white beaches, bioluminescent plankton and laid-back beach bars.", "Preah Sihanouk", "10.7262", "103.2471", "Beach", "Mittakpheap", "4.7"},
            {"kohRongSamloem", "Koh Rong Samloem", "The quieter sister island, ringed by turquoise water, jungle trails and boutique beach camps.", "Preah Sihanouk", "10.5881", "103.3022", "Beach", "Mittakpheap", "4.6"},
            {"otres", "Otres Beach", "A relaxed stretch of white sand south of Sihanoukville, perfect for long swims and sunset cocktails.", "Preah Sihanouk", "10.5693", "103.5577", "Beach", "Mittakpheap", "4.4"},
            {"bokor", "Bokor Hill Station", "A misty 1920s French hill station above Kampot with a 1920s hotel, church and spectacular ocean views.", "Bokor National Park", "10.6300", "104.0500", "Adventure", "Tuek Chhou", "4.5"},
            {"kepCrab", "Kep Crab Market", "Fresh blue crabs grilled right on the waterfront, the seafood soul of Cambodia's smallest province.", "Kep", "10.4829", "104.3166", "City", "Damnak Chang'aeur", "4.3"},
            {"bouSra", "Bou Sra Waterfall", "Two tiered waterfalls in Mondulkiri plunging through dense forest, the largest in Cambodia.", "Sen Monorom", "12.5445", "107.1393", "Nature", "Sen Monorom", "4.7"},
            {"yeakLaom", "Yeak Laom Lake", "A perfectly round crater lake in Ratanakiri, ringed by protected rainforest with a 4km walking trail.", "Banlung", "13.7297", "107.0106", "Nature", "Banlung", "4.6"},
            {"dolphin", "Irrawaddy Dolphin Pool", "Watch rare Irrawaddy dolphins surfacing in the Mekong at Kampi, near Kratie.", "Sambor District", "12.4881", "106.0189", "Nature", "Sambor", "4.5"},
            {"preahVihear", "Preah Vihear Temple", "A cliff-top temple on the Dangrek Mountains with sweeping views over the Cambodian plain.", "Choam Khsant", "14.3900", "104.6800", "Temple", "Choam Khsant", "4.7"},
            {"bambooTrain", "Bamboo Train (Norry)", "Ride a creaking bamboo platform railcar through rural rice paddies in one of Cambodia's quirkiest experiences.", "O Dambang, Battambang", "13.0270", "103.0610", "Adventure", "Sangkae", "4.2"},
            {"phnomSampov", "Phnom Sampov", "A limestone hill with caves, a reclining Buddha and thousands of fruit bats swirling out at sunset.", "Battambang", "13.0548", "103.0987", "Nature", "Battambang", "4.4"}
        };
        Map<String, TourismPlaces> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            TourismPlaces p = new TourismPlaces();
            p.setName((String) row[1]);
            p.setDescription((String) row[2]);
            p.setAddress((String) row[3]);
            p.setLatitude(new BigDecimal((String) row[4]));
            p.setLongitude(new BigDecimal((String) row[5]));
            p.setPlaceCategoty(categories.get((String) row[6]));
            p.setDistrict(districts.get((String) row[7]));
            p.setStaus("ACTIVE");
            p.setRating(new BigDecimal((String) row[8]));
            p.setUser(admin);
            map.put((String) row[0], tourismPlaceRepository.save(p));
        }
        return map;
    }

    private void seedPlaceImages(Map<String, TourismPlaces> places) {
        String[] urls = {
            "https://images.unsplash.com/photo-1508159441828-3d031a33e1e3?w=800&h=500&fit=crop",
            "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=800&h=500&fit=crop",
            "https://images.unsplash.com/photo-1569949381669-ecf31ae866fd?w=800&h=500&fit=crop",
            "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=800&h=500&fit=crop",
            "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&h=500&fit=crop",
            "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=800&h=500&fit=crop"
        };
        for (TourismPlaces place : places.values()) {
            PlaceImages primary = new PlaceImages();
            primary.setImageUrl(urls[0]);
            primary.setIsPrimary(true);
            primary.setTourismPlace(place);
            placeImageRepository.save(primary);

            PlaceImages secondary = new PlaceImages();
            secondary.setImageUrl(urls[place.getName().length() % urls.length]);
            secondary.setIsPrimary(false);
            secondary.setTourismPlace(place);
            placeImageRepository.save(secondary);
        }
    }

    private void seedBusinessProfiles(Users sopheak, Users leakhana) {
        Object[][] rows = {
            {sopheak, "Angkor Hospitality Group", "KH-BUS-0001", "VERIFIED"},
            {leakhana, "Coastal Stay Co., Ltd.", "KH-BUS-0002", "VERIFIED"}
        };
        for (Object[] row : rows) {
            BusinesssOwnerProfiles b = new BusinesssOwnerProfiles();
            b.setUsers((Users) row[0]);
            b.setBusinessName((String) row[1]);
            b.setBusinessLicenseNo((String) row[2]);
            b.setVerificationStatus((String) row[3]);
            b.setVerifiedAt(LocalDate.now().minusDays(30));
            businessOwnerProfileRepository.save(b);
        }
    }

    private Map<String, Hotels> seedHotels(Map<String, Districts> districts, Map<String, Users> users) {
        Object[][] rows = {
            {"sokhaAngkor", "Sokha Angkor Resort", "sopheak", "phone +855 63 969 999", "reservation@sokhaangkor.com", "Siem Reap"},
            {"shintaMani", "Shinta Mani Angkor", "sopheak", "phone +855 63 761 998", "stay@shintamani.com", "Siem Reap"},
            {"raffles", "Raffles Hotel Le Royal", "sopheak", "phone +855 23 981 888", "reservations.royal@raffles.com", "Doun Penh"},
            {"rosewood", "Rosewood Phnom Penh", "leakhana", "phone +855 23 936 888", "phnompenh@rosewoodhotels.com", "Boeung Keng Kang"},
            {"sokhaBeach", "Sokha Beach Resort", "leakhana", "phone +855 34 935 111", "reservation@sokhaha.com", "Mittakpheap"},
            {"vineRetreat", "The Vine Retreat Kampot", "sopheak", "phone +855 92 345 678", "stay@vineretreat.com", "Tuek Chhou"},
            {"diamondBt", "Diamond Hotel Battambang", "leakhana", "phone +855 53 952 833", "info@diamondhotel.com", "Battambang"}
        };
        Map<String, Hotels> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Hotels h = new Hotels();
            h.setHotelName((String) row[1]);
            h.setOwner(users.get((String) row[2]));
            h.setPhoneContact((String) row[3]);
            h.setEmailContact((String) row[4]);
            h.setLocation(districts.get((String) row[5]));
            map.put((String) row[0], hotelRepository.save(h));
        }
        return map;
    }

    private Map<String, RoomTypes> seedRoomTypes() {
        Object[][] rows = {
            {"Single", 1}, {"Double", 2}, {"Twin", 2}, {"Suite", 4}, {"Family", 5}
        };
        Map<String, RoomTypes> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            RoomTypes t = new RoomTypes();
            t.setRoomType((String) row[0]);
            t.setCapacity((Integer) row[1]);
            map.put((String) row[0], roomTypeRepository.save(t));
        }
        return map;
    }

    private void seedHotelRooms(Map<String, Hotels> hotels, Map<String, RoomTypes> roomTypes) {
        Object[][] sets = {
            {"sokhaAngkor", "Double", 60, 2, "120"}, {"sokhaAngkor", "Suite", 20, 4, "250"}, {"sokhaAngkor", "Family", 15, 5, "300"},
            {"shintaMani", "Double", 40, 2, "90"}, {"shintaMani", "Suite", 10, 4, "180"},
            {"raffles", "Single", 20, 1, "140"}, {"raffles", "Double", 80, 2, "185"}, {"raffles", "Suite", 25, 4, "350"},
            {"rosewood", "Double", 50, 2, "260"}, {"rosewood", "Suite", 40, 4, "420"},
            {"sokhaBeach", "Double", 70, 2, "110"}, {"sokhaBeach", "Suite", 15, 4, "240"}, {"sokhaBeach", "Family", 20, 5, "260"},
            {"vineRetreat", "Double", 15, 2, "70"}, {"vineRetreat", "Family", 8, 5, "120"},
            {"diamondBt", "Single", 20, 1, "35"}, {"diamondBt", "Double", 30, 2, "50"}, {"diamondBt", "Family", 10, 5, "80"}
        };
        for (Object[] row : sets) {
            HotelRooms hr = new HotelRooms();
            hr.setHotels(hotels.get((String) row[0]));
            hr.setRoomTypes(roomTypes.get((String) row[1]));
            hr.setTotalRoom((Integer) row[2]);
            hr.setCapacity((Integer) row[3]);
            hr.setPricePerNight(new BigDecimal((String) row[4]));
            hotelRoomRepository.save(hr);
        }
    }

    private Map<Long, Rooms> seedRooms(Map<String, Hotels> hotels, Map<String, RoomTypes> roomTypes) {
        Map<Long, Rooms> rooms = new LinkedHashMap<>();
        String[][] perHotel = {
            {"sokhaAngkor", "Double", "Suite"},
            {"raffles", "Single", "Double"},
            {"rosewood", "Double"},
            {"sokhaBeach", "Double", "Family"},
            {"vineRetreat", "Double"},
            {"diamondBt", "Single", "Double"}
        };
        for (String[] hotelRow : perHotel) {
            Hotels hotel = hotels.get(hotelRow[0]);
            for (int i = 1; i < hotelRow.length; i++) {
                for (int n = 0; n < 2; n++) {
                    Rooms r = new Rooms();
                    r.setHotels(hotel);
                    r.setRoomTypes(roomTypes.get(hotelRow[i]));
                    Rooms saved = roomRepository.save(r);
                    rooms.put(saved.getId(), saved);
                }
            }
        }
        return rooms;
    }

    private Map<String, Restaurants> seedRestaurants(Map<String, TourismPlaces> places) {
        Object[][] rows = {
            {"redPiano", "Red Piano Pub & Restaurant", "Famous for its Old Market terrace, tall Khmer-style cocktails and a special Tomb Raider breakfast.", "10:00", "23:00", "angkorWat"},
            {"malis", "Malis Restaurant", "Fine Khmer dining with a modern twist in a traditional pavilion setting.", "11:00", "22:00", "royalPalace"},
            {"friends", "Friends the Restaurant", "A social enterprise serving delicious tapas-style dishes, training disadvantaged youth.", "11:00", "21:30", "tuolSleng"},
            {"local", "The Local Restaurant", "Khmer family recipes cooked in clay pots, tucked behind the night market.", "08:00", "22:00", "angkorThom"},
            {"coconutBeach", "Coconut Beach Restaurant", "Beach shack serving fresh grilled seafood and fruity smoothies over the sand.", "07:00", "21:00", "kohRong"},
            {"riverside", "Kampot Riverside Cafe", "Indochine-era riverside cafe with Kampot pepper dishes and sunset cocktails.", "07:00", "22:00", "bokor"}
        };
        Map<String, Restaurants> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Restaurants r = new Restaurants();
            r.setName((String) row[1]);
            r.setDescription((String) row[2]);
            r.setOpenTime(LocalTime.parse((String) row[3]));
            r.setClossTime(LocalTime.parse((String) row[4]));
            r.setTourismPlaces(places.get((String) row[5]));
            map.put((String) row[0], restaurantRepository.save(r));
        }
        return map;
    }

    private Map<String, FoodCategories> seedFoodCategories() {
        String[] names = {"Local Khmer", "Seafood", "Desserts", "Beverages", "Street Food", "Vegetarian"};
        Map<String, FoodCategories> map = new LinkedHashMap<>();
        for (String n : names) {
            FoodCategories c = new FoodCategories();
            c.setName(n);
            map.put(n, foodCategoryRepository.save(c));
        }
        return map;
    }

    private Map<String, Foods> seedFoods(Map<String, Restaurants> restaurants, Map<String, FoodCategories> categories) {
        Object[][] rows = {
            {"amok", "Amok Trey (Steamed Fish Curry)", "7.5", "Local Khmer", "redPiano", "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=600&h=400&fit=crop"},
            {"lokLak", "Beef Lok Lak", "6.5", "Local Khmer", "local", "https://images.unsplash.com/photo-1544025162-d76694265947?w=600&h=400&fit=crop"},
            {"redCurry", "Khmer Red Curry Chicken", "6.0", "Local Khmer", "malis", "https://images.unsplash.com/photo-1547592180-85f173990554?w=600&h=400&fit=crop"},
            {"numBanhChok", "Num Banh Chok (Khmer Noodles)", "2.5", "Street Food", "local", "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=600&h=400&fit=crop"},
            {"mangoSalad", "Green Mango Salad", "3.5", "Vegetarian", "friends", "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=600&h=400&fit=crop"},
            {"grilledSquid", "Grilled Squid with Kampot Pepper", "8.0", "Seafood", "coconutBeach", "https://images.unsplash.com/photo-1559742811-822873691df8?w=600&h=400&fit=crop"},
            {"grilledFish", "Whole Grilled Fish with Herbs", "10.0", "Seafood", "coconutBeach", "https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?w=600&h=400&fit=crop"},
            {"steamedPrawns", "Steamed Prawns", "9.5", "Seafood", "malis", "https://images.unsplash.com/photo-1559737558-2f5a35f4523b?w=600&h=400&fit=crop"},
            {"coconutCake", "Coconut Ice Cream", "2.0", "Desserts", "coconutBeach", "https://images.unsplash.com/photo-1573804633927-bfcbcd909acd?w=600&h=400&fit=crop"},
            {"palmFruit", "Palm Fruit Dessert", "1.5", "Desserts", "redPiano", "https://images.unsplash.com/photo-1478144592103-25e218a04891?w=600&h=400&fit=crop"},
            {"bananaFritters", "Banana Fritters with Honey", "2.5", "Desserts", "riverside", "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=600&h=400&fit=crop"},
            {"coconutJuice", "Fresh Coconut Juice", "1.5", "Beverages", "coconutBeach", "https://images.unsplash.com/photo-1513041093234-1d3521f19a3e?w=600&h=400&fit=crop"},
            {"angkorBeer", "Angkor Draft Beer", "2.0", "Beverages", "redPiano", "https://images.unsplash.com/photo-1600271886742-f049cd451bba?w=600&h=400&fit=crop"},
            {"icedCoffee", "Cambodian Iced Coffee", "2.5", "Beverages", "riverside", "https://images.unsplash.com/photo-1461023058943-07fcbe16d735?w=600&h=400&fit=crop"},
            {"kuyTeav", "Kuy Teav (Fried Noodles)", "3.0", "Street Food", "local", "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=600&h=400&fit=crop"},
            {"grilledCorn", "Grilled Corn (Klang Kontreit)", "1.0", "Street Food", "friends", "https://images.unsplash.com/photo-1551754655-cd27e38d2076?w=600&h=400&fit=crop"},
            {"springRolls", "Fried Fresh Spring Rolls", "3.0", "Vegetarian", "friends", "https://images.unsplash.com/photo-1547949003-9792a18a2601?w=600&h=400&fit=crop"},
            {"morningGlory", "Stir-Fried Morning Glory", "2.5", "Vegetarian", "local", "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=600&h=400&fit=crop"},
            {"greenCurry", "Khmer Green Curry (Veggie)", "4.5", "Vegetarian", "malis", "https://images.unsplash.com/photo-1455619452474-d2be8b1e70cd?w=600&h=400&fit=crop"}
        };
        Map<String, Foods> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Foods f = new Foods();
            f.setName((String) row[1]);
            f.setPrice(new BigDecimal((String) row[2]));
            f.setFoodCategories(categories.get((String) row[3]));
            f.setRestaurants(restaurants.get((String) row[4]));
            f.setImage((String) row[5]);
            f.setIsAvailable(true);
            map.put((String) row[0], foodRepository.save(f));
        }
        return map;
    }

    private Map<String, TourGuides> seedTourGuides(Map<String, Users> users) {
        Object[][] rows = {
            {"ratha", "Khmer, English", 8, "80"},
            {"channary", "Khmer, English, French", 5, "70"},
            {"piseth", "Khmer, English, Mandarin", 6, "75"}
        };
        Map<String, TourGuides> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            TourGuides g = new TourGuides();
            g.setUser(users.get((String) row[0]));
            g.setLanguageSpoken((String) row[1]);
            g.setExperienceYear((Integer) row[2]);
            g.setRatePerDay(new BigDecimal((String) row[3]));
            map.put((String) row[0], tourGuideRepository.save(g));
        }
        return map;
    }

    private Map<String, TourPackages> seedTourPackages(Map<String, Provinces> provinces, Map<String, TourGuides> guides) {
        Object[][] rows = {
            {"angkorHeritage", "Angkor Heritage Explorer", "Three days of sunrise temple hops across the greatest monuments of the Khmer Empire.", 3, 8, "185", "Siem Reap", "ratha"},
            {"islandEscape", "Island Escape Koh Rong", "Ferry over to Cambodia's most beautiful island for two relaxed days of white sand and snorkeling.", 2, 12, "145", "Preah Sihanouk", "piseth"},
            {"northeastAdventure", "Northeast Nature Adventure", "Waterfalls, crater lakes and Irrawaddy dolphins across Mondulkiri and Ratanakiri.", 4, 6, "320", "Mondulkiri", "channary"},
            {"phnomPenhCity", "Phnom Penh City Discovery", "A one-day cultural walk from the Royal Palace to the landmarks that tell the capital's story.", 1, 15, "50", "Phnom Penh", "ratha"}
        };
        Map<String, TourPackages> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            TourPackages p = new TourPackages();
            p.setName((String) row[1]);
            p.setDescription((String) row[2]);
            p.setDurationDays((Integer) row[3]);
            p.setMaxPeople((Integer) row[4]);
            p.setPrice(new BigDecimal((String) row[5]));
            p.setProvinces(provinces.get((String) row[6]));
            p.setToureGuides(guides.get((String) row[7]));
            map.put((String) row[0], tourPackageRepository.save(p));
        }
        return map;
    }

    private void seedTourPackageStops(Map<String, TourPackages> packages, Map<String, TourismPlaces> places) {
        Object[][] rows = {
            {"angkorHeritage", 1, "Sunrise at Angkor Wat and the South Gate of Angkor Thom.", "angkorWat"},
            {"angkorHeritage", 2, "Bayon and Ta Prohm - stone faces and jungle-cloaked ruins.", "bayon"},
            {"angkorHeritage", 3, "Banteay Srei then a boat cruise on the Tonle Sap.", "banteaySrei"},
            {"islandEscape", 1, "Sailing across to Koh Rong and an afternoon of snorkeling.", "kohRong"},
            {"islandEscape", 2, "Morning swim at Otres Beach before the ferry back.", "otres"},
            {"northeastAdventure", 1, "Bou Sra Waterfall trek in Mondulkiri.", "bouSra"},
            {"northeastAdventure", 2, "Swim in the crater lake of Yeak Laom.", "yeakLaom"},
            {"northeastAdventure", 3, "Boat trip to see Irrawaddy dolphins near Kratie.", "dolphin"},
            {"phnomPenhCity", 1, "Royal Palace, Tuol Sleng and the domed Central Market.", "royalPalace"}
        };
        for (Object[] row : rows) {
            TourPackageStops s = new TourPackageStops();
            s.setTourPackages(packages.get((String) row[0]));
            s.setDayNumber((Integer) row[1]);
            s.setActivityDescription((String) row[2]);
            s.setTourismPlaces(places.get((String) row[3]));
            tourPackageStopRepository.save(s);
        }
    }

    private Map<String, Tickets> seedTickets(Map<String, TourismPlaces> places) {
        Object[][] rows = {
            {"angkor1Day", "Angkor 1-Day Pass", "37", "Single day entry to the Angkor Archaeological Park.", "angkorWat"},
            {"angkor3Day", "Angkor 3-Day Pass", "62", "Three non-consecutive days of temple access with a digital photo pass.", "angkorWat"},
            {"royalPalace", "Royal Palace Entry", "10", "Entry ticket to the Royal Palace and Silver Pagoda.", "royalPalace"},
            {"kohRongFerry", "Koh Rong Ferry (Return)", "30", "Return speedboat transfer from Sihanoukville to Koh Rong.", "kohRong"},
            {"bokor", "Bokor National Park Entry", "12", "Day pass into Bokor National Park and the hill station.", "bokor"},
            {"yeakLaom", "Yeak Laom Lake Entry", "2", "Entry to the crater lake and its rainforest trail.", "yeakLaom"},
            {"preahVihear", "Preah Vihear Entry", "10", "Access to the cliff-top temple complex.", "preahVihear"}
        };
        Map<String, Tickets> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Tickets t = new Tickets();
            t.setName((String) row[1]);
            t.setPrice(new BigDecimal((String) row[2]));
            t.setDescription((String) row[3]);
            t.setTourismPlaces(places.get((String) row[4]));
            t.setIsAvailable(true);
            map.put((String) row[0], ticketRepository.save(t));
        }
        return map;
    }

    private void seedReviews(Map<String, Users> users, Map<String, TourismPlaces> places,
                             Map<String, Hotels> hotels, Map<String, Restaurants> restaurants,
                             Map<String, TourPackages> packages) {
        Object[][] rows = {
            {users.get("dara"), "An unforgettable sunrise - get there before 5am!", 5, "p", "angkorWat", null, null},
            {users.get("sokha"), "Towering faces that humble you. Unmatched.", 5, "p", "bayon", null, null},
            {users.get("dara"), "The trees swallowing the walls make it feel like a lost world.", 4, "p", "taProhm", null, null},
            {users.get("sokha"), "Timeless elegance and impeccable service.", 5, "h", null, "raffles", null},
            {users.get("dara"), "Great terrace for people watching with an Angkor beer.", 4, "r", null, null, "redPiano"},
            {users.get("sokha"), "The grilled prawns and courteous staff made our evening.", 5, "r", null, null, "malis"},
            {users.get("dara"), "Perfect itinerary, our guide Ratha was fantastic.", 5, "t", null, null, "angkorHeritage"},
            {users.get("sokha"), "The dolphins appeared right on cue. Magical.", 5, "p", "dolphin", null, null},
            {users.get("dara"), "Softest sand we walked on in Cambodia.", 4, "p", "kohRong", null, null}
        };
        for (Object[] row : rows) {
            Reviews r = new Reviews();
            r.setUser((Users) row[0]);
            r.setComment((String) row[1]);
            r.setRating((Integer) row[2]);
            String kind = (String) row[3];
            switch (kind) {
                case "p" -> r.setTouristPlace(places.get((String) row[4]));
                case "h" -> r.setHotel(hotels.get((String) row[5]));
                case "r" -> r.setRestaurant(restaurants.get((String) row[6]));
                default -> r.setTourPackage(packages.get((String) row[6]));
            }
            reviewRepository.save(r);
        }
    }

    private void seedPromotions(Map<String, Hotels> hotels, Map<String, Restaurants> restaurants,
                                Map<String, TourPackages> packages) {
        Object[][] rows = {
            {"rainy", "Rainy Season Special", "PERCENT", "15", "RAINY15", "sokhaAngkor", "malis", "angkorHeritage"},
            {"weekend", "Weekend Getaway", "PERCENT", "10", "WEEKEND10", "sokhaBeach", "coconutBeach", "islandEscape"}
        };
        for (Object[] row : rows) {
            Promotions p = new Promotions();
            p.setName((String) row[1]);
            p.setDiscountType((String) row[2]);
            p.setDiscountValue(new BigDecimal((String) row[3]));
            p.setCode((String) row[4]);
            p.setHotels(hotels.get((String) row[5]));
            p.setRestraurants(restaurants.get((String) row[6]));
            p.setTourPackages(packages.get((String) row[7]));
            p.setStartAt(LocalDateTime.now().minusDays(2));
            p.setEndAt(LocalDateTime.now().plusDays(60));
            p.setStatus("ACTIVE");
            promotionRepository.save(p);
        }
    }

    private void seedNotifications(Map<String, Users> users) {
        Object[][] rows = {
            {users.get("dara"), "Welcome to KhmerStay", "Browse hotels, restaurants and tours across Cambodia, all in one place.", "system", false},
            {users.get("dara"), "Booking Confirmed", "Your Angkor Heritage Explorer tour on the 10th is confirmed.", "booking", false},
            {users.get("dara"), "New Promotion", "Use code RAINY15 for 15% off hotels, dining and tours this season.", "promotion", true},
            {users.get("sokha"), "Welcome to KhmerStay", "Create your first favorite and start planning your trip.", "system", false}
        };
        for (Object[] row : rows) {
            Notifications n = new Notifications();
            n.setUser((Users) row[0]);
            n.setTitle((String) row[1]);
            n.setMessage((String) row[2]);
            n.setType((String) row[3]);
            n.setIsRead((Boolean) row[4]);
            notificationRepository.save(n);
        }
    }

    private void seedCart(Users user, Restaurants restaurant, Map<String, Foods> foods) {
        Carts cart = new Carts();
        cart.setUser(user);
        cart.setRestaurants(restaurant);
        cartRepository.save(cart);

        CartItems a = new CartItems();
        a.setCart(cart);
        a.setFoods(foods.get("amok"));
        a.setQuantity(2);
        a.setUnitPrice(foods.get("amok").getPrice());
        a.setSubTotal(foods.get("amok").getPrice().multiply(BigDecimal.valueOf(2)));
        cartItemRepository.save(a);

        CartItems b = new CartItems();
        b.setCart(cart);
        b.setFoods(foods.get("grilledSquid"));
        b.setQuantity(1);
        b.setUnitPrice(foods.get("grilledSquid").getPrice());
        b.setSubTotal(foods.get("grilledSquid").getPrice());
        cartItemRepository.save(b);
    }

    private void seedOrderActivity(Map<String, Users> users, Map<String, Restaurants> restaurants,
                                   Map<String, Foods> foods, Map<String, TourPackages> packages,
                                   Map<String, Tickets> tickets, Map<Long, Rooms> rooms, Map<String, Hotels> hotels) {
        List<Rooms> roomList = new ArrayList<>(rooms.values());
        Collections.shuffle(roomList);

        RoomBookings rb = new RoomBookings();
        rb.setUsers(users.get("dara"));
        rb.setRooms(roomList.get(0));
        rb.setNumGuest(2);
        rb.setCheckIn(LocalDate.now().plusDays(7));
        rb.setCheckOut(LocalDate.now().plusDays(10));
        rb.setPaymentMethod("Card");
        rb.setAmount(new BigDecimal("555"));
        rb.setStatus("CONFIRMED");
        RoomBookings rbSaved = roomBookingRepository.save(rb);

        RoomBookings rb2 = new RoomBookings();
        rb2.setUsers(users.get("sokha"));
        rb2.setRooms(roomList.size() > 1 ? roomList.get(1) : roomList.get(0));
        rb2.setNumGuest(2);
        rb2.setCheckIn(LocalDate.now().plusDays(14));
        rb2.setCheckOut(LocalDate.now().plusDays(16));
        rb2.setPaymentMethod("ABA Bank");
        rb2.setAmount(new BigDecimal("520"));
        rb2.setStatus("CONFIRMED");
        roomBookingRepository.save(rb2);

        TicketBookings tb = new TicketBookings();
        tb.setUser(users.get("dara"));
        tb.setTickets(tickets.get("angkor3Day"));
        tb.setQuantity(2);
        tb.setTotalPrice(new BigDecimal("124"));
        tb.setVisiDate(LocalDate.now().plusDays(5));
        tb.setStatus("CONFIRMED");
        tb.setQrCode("QR-ANGKOR-001");
        TicketBookings tbSaved = ticketBookingRepository.save(tb);

        TicketBookings tb2 = new TicketBookings();
        tb2.setUser(users.get("sokha"));
        tb2.setTickets(tickets.get("kohRongFerry"));
        tb2.setQuantity(2);
        tb2.setTotalPrice(new BigDecimal("60"));
        tb2.setVisiDate(LocalDate.now().plusDays(20));
        tb2.setStatus("CONFIRMED");
        tb2.setQrCode("QR-KOHRONG-001");
        TicketBookings tbSaved2 = ticketBookingRepository.save(tb2);

        TourBookings tour = new TourBookings();
        tour.setUser(users.get("dara"));
        tour.setTourPackages(packages.get("angkorHeritage"));
        tour.setNumPeople(4);
        tour.setTourDate(LocalDate.now().plusDays(10));
        tour.setTotalPrice(new BigDecimal("740"));
        tour.setStatus("CONFIRMED");
        TourBookings tourSaved = tourBookingRepository.save(tour);

        FoodOrders fo = new FoodOrders();
        fo.setUser(users.get("dara"));
        fo.setRestuarants(restaurants.get("redPiano"));
        fo.setTotalPrice(new BigDecimal("27"));
        fo.setPickupTime(LocalDateTime.now().plusHours(3));
        fo.setStatus("CONFIRMED");
        FoodOrders foSaved = foodOrderRepository.save(fo);
        foodOrderItemRepository.save(orderItem(foSaved, foods.get("amok"), 2));
        foodOrderItemRepository.save(orderItem(foSaved, foods.get("angkorBeer"), 4));
        foodOrderItemRepository.save(orderItem(foSaved, foods.get("coconutCake"), 2));

        FoodOrders fo2 = new FoodOrders();
        fo2.setUser(users.get("sokha"));
        fo2.setRestuarants(restaurants.get("malis"));
        fo2.setTotalPrice(new BigDecimal("22.5"));
        fo2.setPickupTime(LocalDateTime.now().minusDays(1));
        fo2.setStatus("COMPLETED");
        FoodOrders foSaved2 = foodOrderRepository.save(fo2);
        foodOrderItemRepository.save(orderItem(foSaved2, foods.get("lokLak"), 2));
        foodOrderItemRepository.save(orderItem(foSaved2, foods.get("steamedPrawns"), 1));

        seedPayments(users.get("dara"), hotels, rbSaved, tbSaved, foSaved, tourSaved);
    }

    private FoodOrderItems orderItem(FoodOrders order, Foods food, int qty) {
        FoodOrderItems item = new FoodOrderItems();
        item.setFoodOrders(order);
        item.setFoods(food);
        item.setQuantity(qty);
        item.setUnitPrice(food.getPrice());
        item.setSubTotal(food.getPrice().multiply(BigDecimal.valueOf(qty)));
        return item;
    }

    private void seedPayments(Users user, Map<String, Hotels> hotels,
                              RoomBookings roomBooking, TicketBookings ticketBooking,
                              FoodOrders foodOrder, TourBookings tourBooking) {
        Payments p = new Payments();
        p.setRoomBookings(roomBooking);
        p.setTicketBookings(ticketBooking);
        p.setFoodOrders(foodOrder);
        p.setTourBookings(tourBooking);
        p.setAmount(new BigDecimal("1446"));
        p.setPaymentMethod("Card");
        p.setTransactionId("TXN-PAY-00001");
        p.setStatus("COMPLETED");
        p.setPaidAt(LocalDateTime.now().minusDays(1));
        paymentRepository.save(p);
    }

    private void seedFavorites(Map<String, Users> users, Map<String, TourismPlaces> places,
                               Map<String, Hotels> hotels, Map<String, Restaurants> restaurants,
                               Map<String, TourPackages> packages) {
        Object[][] rows = {
            {users.get("dara"), "angkorWat", "raffles", "malis", "angkorHeritage"},
            {users.get("sokha"), "kohRong", "sokhaBeach", "coconutBeach", "islandEscape"}
        };
        for (Object[] row : rows) {
            Favorites f = new Favorites();
            f.setUsers((Users) row[0]);
            f.setTourismPlaces(places.get((String) row[1]));
            f.setHotels(hotels.get((String) row[2]));
            f.setRestuarants(restaurants.get((String) row[3]));
            f.setTourPackages(packages.get((String) row[4]));
            favoriteRepository.save(f);
        }
    }
}