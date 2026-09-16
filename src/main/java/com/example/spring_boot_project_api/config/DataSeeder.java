package com.example.spring_boot_project_api.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.enums.AttachmentFileType;
import com.example.spring_boot_project_api.enums.GenderEnum;
import com.example.spring_boot_project_api.model.Attachments;
import com.example.spring_boot_project_api.model.BusinesssOwnerProfiles;
import com.example.spring_boot_project_api.model.FoodCategories;
import com.example.spring_boot_project_api.model.Foods;
import com.example.spring_boot_project_api.model.HotelAttachments;
import com.example.spring_boot_project_api.model.HotelRooms;
import com.example.spring_boot_project_api.model.Hotels;
import com.example.spring_boot_project_api.model.Location;
import com.example.spring_boot_project_api.model.PlaceCategoties;
import com.example.spring_boot_project_api.model.Promotions;
import com.example.spring_boot_project_api.model.RestaurantAttachments;
import com.example.spring_boot_project_api.model.Restaurants;
import com.example.spring_boot_project_api.model.Roles;
import com.example.spring_boot_project_api.model.RoomTypes;
import com.example.spring_boot_project_api.model.TourGuides;
import com.example.spring_boot_project_api.model.TourPackages;
import com.example.spring_boot_project_api.model.TourPlaceAttachments;
import com.example.spring_boot_project_api.model.TourPlaces;
import com.example.spring_boot_project_api.model.UserRoles;
import com.example.spring_boot_project_api.model.Users;
import com.example.spring_boot_project_api.repository.AttachmentRepository;
import com.example.spring_boot_project_api.repository.BusinessOwnerProfileRepository;
import com.example.spring_boot_project_api.repository.FoodCategoryRepository;
import com.example.spring_boot_project_api.repository.FoodRepository;
import com.example.spring_boot_project_api.repository.HotelAttachmentRepository;
import com.example.spring_boot_project_api.repository.HotelRepository;
import com.example.spring_boot_project_api.repository.HotelRoomRepository;
import com.example.spring_boot_project_api.repository.LocationRepository;
import com.example.spring_boot_project_api.repository.PlaceCategoryRepository;
import com.example.spring_boot_project_api.repository.PromotionRepository;
import com.example.spring_boot_project_api.repository.RestaurantAttachmentRepository;
import com.example.spring_boot_project_api.repository.RestaurantRepository;
import com.example.spring_boot_project_api.repository.RoleRepository;
import com.example.spring_boot_project_api.repository.RoomTypeRepository;
import com.example.spring_boot_project_api.repository.TourGuideRepository;
import com.example.spring_boot_project_api.repository.TourPackageRepository;
import com.example.spring_boot_project_api.repository.TourPlaceAttachmentRepository;
import com.example.spring_boot_project_api.repository.TourismPlaceRepository;
import com.example.spring_boot_project_api.repository.UserRepository;
import com.example.spring_boot_project_api.repository.UserRoleRepository;
import com.example.spring_boot_project_api.util.RoleNames;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final BusinessOwnerProfileRepository businessOwnerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final LocationRepository locationRepository;
    private final PlaceCategoryRepository placeCategoryRepository;
    private final FoodCategoryRepository foodCategoryRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final TourismPlaceRepository tourismPlaceRepository;
    private final HotelRepository hotelRepository;
    private final HotelRoomRepository hotelRoomRepository;
    private final RestaurantRepository restaurantRepository;
    private final FoodRepository foodRepository;
    private final TourGuideRepository tourGuideRepository;
    private final TourPackageRepository tourPackageRepository;
    private final PromotionRepository promotionRepository;
    private final AttachmentRepository attachmentRepository;
    private final HotelAttachmentRepository hotelAttachmentRepository;
    private final TourPlaceAttachmentRepository tourPlaceAttachmentRepository;
    private final RestaurantAttachmentRepository restaurantAttachmentRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
        seedDefaultAdmin();
        seedDefaultOwner();
        seedDefaultTourist();
        seedDemoContent();
    }

    // ------------------------------------------------------------------
    // Roles & default accounts
    // ------------------------------------------------------------------

    private void seedRoles() {
        List<String> names = Arrays.asList(RoleNames.ADMIN, RoleNames.OWNER, RoleNames.TOURIST);
        names.forEach(name -> {
            if (!roleRepository.existsByName(name)) {
                Roles role = new Roles();
                role.setName(name);
                roleRepository.save(role);
            }
        });
    }

    private void seedDefaultAdmin() {
        Optional<Roles> role = roleRepository.findByName(RoleNames.ADMIN);
        if (role.isEmpty()) {
            return;
        }
        Optional<Users> existing = userRepository.findByUsername("admin");
        if (existing.isEmpty()) {
            Users admin = newUser("System Administrator", "admin", "admin@smart-tourism.com",
                    "admin123");
            userRepository.save(admin);
            assignRole(admin, role.get());
        } else {
            normalizePassword(existing.get(), "admin123");
        }
    }

    private void seedDefaultOwner() {
        Optional<Roles> role = roleRepository.findByName(RoleNames.OWNER);
        if (role.isEmpty()) {
            return;
        }

        // 1. Hotel Owner
        seedSingleOwner("Sovann Hotel Owner", "owner", "owner@smart-tourism.com", "owner123",
                "Sovann Hotels & Resorts Group", "LIC-HOTEL-001", role.get());
        seedSingleOwner("Sovann Hotel Owner", "owner_hotel", "owner.hotel@smart-tourism.com", "owner123",
                "Sovann Hotels & Resorts Group", "LIC-HOTEL-001B", role.get());

        // 2. Restaurant Owner
        seedSingleOwner("Chann Restaurant Owner", "owner_restaurant", "owner.restaurant@smart-tourism.com", "owner123",
                "Chann Khmer Dining & Cuisines", "LIC-REST-002", role.get());

        // 3. Tourists / Tour Owner
        seedSingleOwner("Bopha Tour Owner", "owner_tour", "owner.tour@smart-tourism.com", "owner123",
                "Bopha Angkor Tours & Adventures", "LIC-TOUR-003", role.get());
        seedSingleOwner("Bopha Tour Owner", "owner_tourist", "owner.tourist@smart-tourism.com", "owner123",
                "Bopha Angkor Tours & Adventures", "LIC-TOUR-003B", role.get());
    }

    private void seedSingleOwner(String fullname, String username, String email, String password,
            String businessName, String licenseNo, Roles role) {
        Optional<Users> existing = userRepository.findByUsername(username);
        Users ownerUser;
        if (existing.isPresent()) {
            ownerUser = existing.get();
            normalizePassword(ownerUser, password);
        } else {
            ownerUser = newUser(fullname, username, email, password);
            userRepository.save(ownerUser);
            assignRole(ownerUser, role);
        }
        if (!businessOwnerProfileRepository.existsByBusinessLicenseNo(licenseNo)) {
            BusinesssOwnerProfiles profile = new BusinesssOwnerProfiles();
            profile.setBusinessName(businessName);
            profile.setBusinessLicenseNo(licenseNo);
            profile.setVerificationStatus("VERIFIED");
            profile.setVerifiedAt(LocalDate.now());
            profile.setUsers(ownerUser);
            businessOwnerProfileRepository.save(profile);
        }
    }

    private void seedDefaultTourist() {
        Optional<Roles> role = roleRepository.findByName(RoleNames.TOURIST);
        if (role.isEmpty()) {
            return;
        }
        Optional<Users> existing = userRepository.findByUsername("tourist");
        if (existing.isPresent()) {
            normalizePassword(existing.get(), "tourist123");
        } else {
            Users tourist = newUser("Tourist", "tourist", "tourist@smart-tourism.com",
                    "tourist123");
            userRepository.save(tourist);
            assignRole(tourist, role.get());
        }
    }

    private void normalizePassword(Users user, String rawPassword) {
        String current = user.getPassword();
        if (current == null || !current.matches("^\\$2[aby]\\$\\d{2}\\$.*")) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
        }
    }

    private Users newUser(String fullname, String username, String email, String rawPassword) {
        Users user = new Users();
        user.setFullname(fullname);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setGender(GenderEnum.Male);
        return user;
    }

    private void assignRole(Users user, Roles role) {
        if (userRoleRepository.existsByUserIdAndRoleId(user.getId(), role.getId())) {
            return;
        }
        UserRoles userRole = new UserRoles();
        userRole.setUser(user);
        userRole.setRole(role);
        userRoleRepository.save(userRole);
    }

    // ------------------------------------------------------------------
    // Demo content (provinces, categories, stays, tours, dining, offers)
    // ------------------------------------------------------------------

    private void seedDemoContent() {
        Users owner = userRepository.findByUsername("owner").orElse(null);

        List<String[]> locations = Arrays.asList(
                new String[]{"Siem Reap", "Siem Reap"},
                new String[]{"Siem Reap", "Angkor"},
                new String[]{"Phnom Penh", "Chamkar Mon"},
                new String[]{"Phnom Penh", "Dangkao"},
                new String[]{"Sihanoukville", "Preah Sihanouk"},
                new String[]{"Kampot", "Kampot"},
                new String[]{"Battambang", "Battambang"},
                new String[]{"Kep", "Kep"},
                new String[]{"Mondulkiri", "Sen Monorom"},
                new String[]{"Ratanakiri", "Banlung"});
        for (String[] loc : locations) {
            saveLocation(loc[0], loc[1]);
        }

        PlaceCategoties temple = savePlaceCategory("Temple",
                img("photo-1508159441828-3d031a33e1e3"));
        PlaceCategoties beach = savePlaceCategory("Beach",
                img("photo-1507525428034-b723cf961d3e"));
        PlaceCategoties heritage = savePlaceCategory("Heritage",
                img("photo-1569949381669-ecf31ae866fd"));
        PlaceCategoties nature = savePlaceCategory("Nature",
                img("photo-1441974231531-c6227db76b6e"));
        PlaceCategoties city = savePlaceCategory("City",
                img("photo-1477959858617-67f85cf4f1df"));
        PlaceCategoties mountain = savePlaceCategory("Mountain",
                img("photo-1464822759023-fed622ff2c3b"));

        FoodCategories khmer = saveFoodCategory("Khmer Food");
        FoodCategories seafood = saveFoodCategory("Seafood");
        FoodCategories street = saveFoodCategory("Street Food");
        FoodCategories desserts = saveFoodCategory("Desserts");
        FoodCategories veggie = saveFoodCategory("Vegetarian");

        RoomTypes standard = saveRoomType("Standard Room", 2);
        RoomTypes deluxe = saveRoomType("Deluxe Room", 2);
        RoomTypes suite = saveRoomType("Executive Suite", 3);
        RoomTypes family = saveRoomType("Family Room", 4);

        Location angkor = findLocation("Siem Reap", "Angkor");
        Location siemReap = findLocation("Siem Reap", "Siem Reap");
        Location chamkarMon = findLocation("Phnom Penh", "Chamkar Mon");
        Location sihanoukville = findLocation("Sihanoukville", "Preah Sihanouk");
        Location kampot = findLocation("Kampot", "Kampot");
        Location battambang = findLocation("Battambang", "Battambang");
        Location kep = findLocation("Kep", "Kep");
        Location senMonorom = findLocation("Mondulkiri", "Sen Monorom");
        Location banlung = findLocation("Ratanakiri", "Banlung");

        // ----- Tour places -----
        TourPlaces angkorWat = saveTourPlace(owner, temple, angkor,
                "Angkor Wat", "The world's largest religious monument and the crown jewel of Khmer "
                        + "architecture. Watch the sunrise over the iconic five towers.",
                "Angkor Wat, Krong Siem Reap", "13.4125", "103.8670",
                new BigDecimal("4.9"), "Open");
        TourPlaces bayon = saveTourPlace(owner, temple, siemReap,
                "Bayon Temple", "Famous for its serene stone faces, Bayon is the mystic heart of "
                        + "the ancient city of Angkor Thom.",
                "Angkor Thom, Krong Siem Reap", "13.4408", "103.8590",
                new BigDecimal("4.8"), "Open");
        TourPlaces taProhm = saveTourPlace(owner, heritage, siemReap,
                "Ta Prohm", "The 'Tomb Raider' temple where giant silk-cotton trees embrace the "
                        + "ruins — a jungle-clad masterpiece.",
                "Angkor Archaeological Park, Siem Reap", "13.4347", "103.8936",
                new BigDecimal("4.7"), "Open");
        TourPlaces royalPalace = saveTourPlace(owner, city, chamkarMon,
                "Royal Palace & Silver Pagoda", "The dazzling seat of the Cambodian monarchy, set "
                        + "in lush gardens along the banks of the Tonle Sap.",
                "Samdech Sothearos Blvd, Phnom Penh", "11.5634", "104.9310",
                new BigDecimal("4.6"), "Open");
        TourPlaces kohRong = saveTourPlace(owner, beach, sihanoukville,
                "Koh Rong", "A tropical island paradise of white-sand beaches, turquoise water "
                        + "and glowing bioluminescent plankton.",
                "Koh Rong, Preah Sihanouk Province", "10.7171", "103.2357",
                new BigDecimal("4.7"), "Open");
        TourPlaces independenceBeach = saveTourPlace(owner, beach, sihanoukville,
                "Independence Beach", "A long stretch of soft sand just west of Sihanoukville "
                        + "town, perfect for sunset strolls.",
                "Sihanoukville", "10.6081", "103.5056",
                new BigDecimal("4.4"), "Open");
        TourPlaces bokor = saveTourPlace(owner, mountain, kampot,
                "Bokor National Park", "Mist-shrouded mountain roads lead to the haunting French "
                        + "hill station and sweeping coastal views.",
                "Bokor, Kampot Province", "10.6306", "104.0314",
                new BigDecimal("4.6"), "Open");
        TourPlaces bambooTrain = saveTourPlace(owner, heritage, battambang,
                "Bamboo Train", "A thrilling ride on a rustic bamboo car along abandoned "
                        + "railway tracks through the countryside.",
                "O Dambang II, Battambang", "13.0758", "103.1512",
                new BigDecimal("4.5"), "Open");
        TourPlaces kepBeach = saveTourPlace(owner, beach, kep,
                "Kep Beach", "Laid-back shores, sunset views across the water and the famous "
                        + "Kep crab market nearby.",
                "Kep Province", "10.4872", "104.3107",
                new BigDecimal("4.4"), "Open");
        TourPlaces yeakLaom = saveTourPlace(owner, nature, banlung,
                "Yeak Laom Lake", "A perfectly round volcanic crater lake surrounded by lush "
                        + "rainforest in Ratanakiri province.",
                "Banlung, Ratanakiri", "13.7325", "107.0164",
                new BigDecimal("4.6"), "Open");
        TourPlaces senMonoromFalls = saveTourPlace(owner, nature, senMonorom,
                "Sen Monorom Waterfall", "Cascading falls in the cool green hills of Mondulkiri, "
                        + "home to elephants and rolling pine landscapes.",
                "Sen Monorom, Mondulkiri", "12.4567", "107.1871",
                new BigDecimal("4.5"), "Open");

        addTourPlaceImage(angkorWat, img("photo-1508159441828-3d031a33e1e3"), "PRIMARY", 0);
        addTourPlaceImage(bayon, img("photo-1508159441828-3d031a33e1e3"), "PRIMARY", 0);
        addTourPlaceImage(taProhm, img("photo-1544551763-46a013bb70d5"), "PRIMARY", 0);
        addTourPlaceImage(royalPalace, img("photo-1477959858617-67f85cf4f1df"), "PRIMARY", 0);
        addTourPlaceImage(kohRong, img("photo-1507525428034-b723cf961d3e"), "PRIMARY", 0);
        addTourPlaceImage(independenceBeach, img("photo-1505118380757-91f5f5632de0"), "PRIMARY", 0);
        addTourPlaceImage(bokor, img("photo-1464822759023-fed622ff2c3b"), "PRIMARY", 0);
        addTourPlaceImage(bambooTrain, img("photo-1469854523086-cc02fe5d8800"), "PRIMARY", 0);
        addTourPlaceImage(kepBeach, img("photo-1544551763-46a013bb70d5"), "PRIMARY", 0);
        addTourPlaceImage(yeakLaom, img("photo-1441974231531-c6227db76b6e"), "PRIMARY", 0);
        addTourPlaceImage(senMonoromFalls, img("photo-1501785888041-af3ef285b470"), "PRIMARY", 0);

        // ----- Hotels -----
        Hotels angkorMiracle = saveHotel("Angkor Miracle Resort & Spa",
                "+855 63 761 234", "info@angkormiracleresort.com", owner, angkor);
        addHotelRoom(angkorMiracle, standard, 20, 2, "45");
        addHotelRoom(angkorMiracle, deluxe, 15, 2, "65");
        addHotelRoom(angkorMiracle, suite, 8, 3, "110");
        addHotelImage(angkorMiracle, img("photo-1566073771259-6a8506099945"), "COVER", 0);

        Hotels sokhaAngkor = saveHotel("Sokha Angkor Resort",
                "+855 63 969 999", "reservation@sokhaangkor.com", owner, angkor);
        addHotelRoom(sokhaAngkor, deluxe, 25, 2, "70");
        addHotelRoom(sokhaAngkor, family, 10, 4, "95");
        addHotelRoom(sokhaAngkor, suite, 12, 3, "150");
        addHotelImage(sokhaAngkor, img("photo-1551882547-ff40c63fe5fa"), "COVER", 0);

        Hotels hyatt = saveHotel("Hyatt Regency Phnom Penh",
                "+855 23 961 888", "phnompenh.regency@hyatt.com", owner, chamkarMon);
        addHotelRoom(hyatt, standard, 40, 2, "55");
        addHotelRoom(hyatt, deluxe, 30, 2, "85");
        addHotelRoom(hyatt, suite, 15, 3, "180");
        addHotelImage(hyatt, img("photo-1571896349842-33c89424de2d"), "COVER", 0);

        Hotels rosewood = saveHotel("Rosewood Phnom Penh",
                "+855 23 936 888", "vip.saigon@rosewoodhotels.com", owner, chamkarMon);
        addHotelRoom(rosewood, deluxe, 24, 2, "120");
        addHotelRoom(rosewood, suite, 20, 3, "220");
        addHotelImage(rosewood, img("photo-1520250497591-112f2f40a3f4"), "COVER", 0);

        Hotels independence = saveHotel("Independence Hotel Resort & Spa",
                "+855 34 933 444", "reservation@independencehotel.com", owner, sihanoukville);
        addHotelRoom(independence, standard, 30, 2, "35");
        addHotelRoom(independence, deluxe, 20, 2, "55");
        addHotelRoom(independence, family, 10, 4, "75");
        addHotelImage(independence, img("photo-1582719508461-905c673771fd"), "COVER", 0);

        Hotels kepBay = saveHotel("Kep Bay Resort & Spa",
                "+855 78 664 424", "hello@kepbay.com", owner, kep);
        addHotelRoom(kepBay, deluxe, 12, 2, "90");
        addHotelRoom(kepBay, suite, 10, 3, "130");
        addHotelImage(kepBay, img("photo-1504214208698-ea1916a2195a"), "COVER", 0);

        // ----- Restaurants -----
        Restaurants chanreyTree = saveRestaurant("Chanrey Tree",
                "Refined Khmer fine dining in a lush tropical courtyard, famous for its fish amok.",
                "11:00", "22:00", angkorWat);
        addFood(chanreyTree, khmer, "Amok Trey", "12.50", true,
                img("photo-1565299624946-b28f40a0ae38"));
        addFood(chanreyTree, khmer, "Khmer Red Curry", "10.00", true,
                img("photo-1546833999-b9f581a1996d"));
        addFood(chanreyTree, khmer, "Beef Lok Lak", "11.50", true,
                img("photo-1555939594-58d7cb561ad1"));
        addFood(chanreyTree, veggie, "Fried Morning Glory", "6.00", true,
                img("photo-1546069901-ba9599a7e63c"));
        addFood(chanreyTree, desserts, "Mango Sticky Rice", "5.50", true,
                img("photo-1517673132405-a56a62b18caf"));
        addRestaurantImage(chanreyTree, img("photo-1517248135467-4c7edcad34c4"), "COVER", 0);

        Restaurants haven = saveRestaurant("Haven Training Restaurant",
                "A social-enterprise restaurant serving modern Khmer dishes with a warm welcome.",
                "11:00", "21:00", angkorWat);
        addFood(haven, khmer, "Fish Amok", "9.00", true,
                img("photo-1565299624946-b28f40a0ae38"));
        addFood(haven, khmer, "Beef Lok Lak", "10.50", true,
                img("photo-1555939594-58d7cb561ad1"));
        addFood(haven, veggie, "Vegetable Noodles", "7.00", true,
                img("photo-1546069901-ba9599a7e63c"));
        addFood(haven, street, "Passion Fruit Shake", "4.00", true,
                img("photo-1544145945-f90425340c7e"));
        addRestaurantImage(haven, img("photo-1414235077428-338989a2e8c0"), "COVER", 0);

        Restaurants malis = saveRestaurant("Malis Restaurant",
                "Award-winning Khmer fine dining showcasing royal recipes and fresh local produce.",
                "11:00", "22:00", royalPalace);
        addFood(malis, khmer, "Royal Khmer Curry", "16.00", true,
                img("photo-1546833999-b9f581a1996d"));
        addFood(malis, khmer, "Grilled Beef Skewers", "12.00", true,
                img("photo-1555939594-58d7cb561ad1"));
        addFood(malis, veggie, "Banana Flower Salad", "9.00", true,
                img("photo-1546069901-ba9599a7e63c"));
        addRestaurantImage(malis, img("photo-1559339352-11d035aa65de"), "COVER", 0);

        Restaurants fishMarket = saveRestaurant("Sihanoukville Fish Market",
                "Fresh-off-the-boat seafood grilled to order right on the pier.",
                "10:00", "22:00", independenceBeach);
        addFood(fishMarket, seafood, "Grilled Lobster", "28.00", true,
                img("photo-1519708227418-c8fd9a32b7a2"));
        addFood(fishMarket, seafood, "Steamed Whole Fish", "18.00", true,
                img("photo-1510130315046-1e47cc196aa4"));
        addFood(fishMarket, seafood, "Salt & Pepper Squid", "14.00", true,
                img("photo-1565680018434-b513d5e5fd47"));
        addRestaurantImage(fishMarket, img("photo-1555939594-58d7cb561ad1"), "COVER", 0);

        Restaurants bokorLodge = saveRestaurant("Bokor Mountain Lodge",
                "Hearty Khmer mountain fare with panoramic views over Kampot province.",
                "08:00", "21:00", bokor);
        addFood(bokorLodge, khmer, "Bokor Fried Chicken", "9.00", true,
                img("photo-1562967914-608f82629710"));
        addFood(bokorLodge, street, "Fresh Spring Rolls", "5.50", true,
                img("photo-1546069901-ba9599a7e63c"));
        addFood(bokorLodge, veggie, "Garden Vegetables", "5.00", true,
                img("photo-1540189549336-e6e99c3679fe"));
        addRestaurantImage(bokorLodge, img("photo-1414235077428-338989a2e8c0"), "COVER", 0);

        Restaurants blueCrab = saveRestaurant("The Blue Crab",
                "Kep's legendary crab restaurant serving the province's famous seafood by the sea.",
                "09:00", "21:30", kepBeach);
        addFood(blueCrab, seafood, "Steamed Kep Crab", "25.00", true,
                img("photo-1510130315046-1e47cc196aa4"));
        addFood(blueCrab, seafood, "Crab Amok", "22.00", true,
                img("photo-1565299624946-b28f40a0ae38"));
        addFood(blueCrab, seafood, "Pepper Prawns", "18.00", true,
                img("photo-1565680018434-b513d5e5fd47"));
        addRestaurantImage(blueCrab, img("photo-1559339352-11d035aa65de"), "COVER", 0);

        Restaurants starVeggie = saveRestaurant("Star Veggie House",
                "Popular vegetarian Khmer restaurant known for its healthy local dishes.",
                "10:00", "20:00", bambooTrain);
        addFood(starVeggie, veggie, "Tofu Amok", "7.00", true,
                img("photo-1546069901-ba9599a7e63c"));
        addFood(starVeggie, veggie, "Sautéed Morning Glory", "5.50", true,
                img("photo-1540189549336-e6e99c3679fe"));
        addFood(starVeggie, desserts, "Sweet Rice Cakes", "4.50", true,
                img("photo-1517673132405-a56a62b18caf"));
        addRestaurantImage(starVeggie, img("photo-1517248135467-4c7edcad34c4"), "COVER", 0);

        // ----- Tour guide + packages (needed by promotions) -----
        TourGuides guide = saveTourGuide(owner, "English, Khmer", 8, "60");
        TourPackages angkorSunrise = saveTourPackage("Angkor Sunrise Tour",
                "Catch first light over Angkor Wat, then explore the temple complex with a "
                        + "local expert guide.",
                1, 15, "45", angkor, guide);
        TourPackages kohRongEscape = saveTourPackage("Koh Rong Island Escape",
                "Two days of pristine beaches, snorkelling and bioluminescent plankton.",
                2, 12, "120", sihanoukville, guide);

        // ----- Promotions -----
        savePromotion("Angkor Stay & Save", "ANGKOR15", "PERCENT", "15",
                "ACTIVE", angkorMiracle, chanreyTree, angkorSunrise);
        savePromotion("Phnom Penh Gourmet", "PPFOOD10", "PERCENT", "10",
                "ACTIVE", hyatt, malis, angkorSunrise);
        savePromotion("Sihanoukville Summer", "SUM5OFF", "FIXED", "5",
                "ACTIVE", independence, fishMarket, kohRongEscape);
        savePromotion("Kep Weekend Deal", "KEP20", "PERCENT", "20",
                "ACTIVE", kepBay, blueCrab, kohRongEscape);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private String img(String photoId) {
        return "https://images.unsplash.com/" + photoId + "?w=1200&h=800&fit=crop&q=80";
    }

    private Location saveLocation(String province, String district) {
        if (locationRepository.existsByProvinceAndDistrict(province, district)) {
            return findLocation(province, district);
        }
        Location location = new Location();
        location.setProvince(province);
        location.setDistrict(district);
        return locationRepository.save(location);
    }

    private Location findLocation(String province, String district) {
        return locationRepository.findByProvinceAndDistrict(province, district).orElseThrow();
    }

    private PlaceCategoties savePlaceCategory(String name, String image) {
        return placeCategoryRepository.findByName(name).orElseGet(() -> {
            PlaceCategoties category = new PlaceCategoties();
            category.setName(name);
            category.setImage(image);
            return placeCategoryRepository.save(category);
        });
    }

    private FoodCategories saveFoodCategory(String name) {
        return foodCategoryRepository.findByName(name).orElseGet(() -> {
            FoodCategories category = new FoodCategories();
            category.setName(name);
            return foodCategoryRepository.save(category);
        });
    }

    private RoomTypes saveRoomType(String roomType, int capacity) {
        return roomTypeRepository.findByRoomType(roomType).orElseGet(() -> {
            RoomTypes type = new RoomTypes();
            type.setRoomType(roomType);
            type.setCapacity(capacity);
            return roomTypeRepository.save(type);
        });
    }

    private TourPlaces saveTourPlace(Users owner, PlaceCategoties category, Location district,
                                     String name, String description, String address,
                                     String latitude, String longitude,
                                     BigDecimal rating, String status) {
        if (tourismPlaceRepository.findByNameContainingIgnoreCase(name).stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(name))) {
            return tourismPlaceRepository.findByNameContainingIgnoreCase(name).stream()
                    .filter(p -> p.getName().equalsIgnoreCase(name)).findFirst().orElseThrow();
        }
        TourPlaces place = new TourPlaces();
        place.setName(name);
        place.setDescription(description);
        place.setAddress(address);
        place.setLatitude(new BigDecimal(latitude));
        place.setLongitude(new BigDecimal(longitude));
        place.setStaus(status);
        place.setRating(rating);
        place.setPlaceCategoty(category);
        place.setUser(owner);
        place.setDistrict(district);
        return tourismPlaceRepository.save(place);
    }

    private Hotels saveHotel(String name, String phone, String email,
                             Users owner, Location location) {
        if (hotelRepository.existsByHotelName(name)) {
            return hotelRepository.findByHotelName(name).orElseThrow();
        }
        Hotels hotel = new Hotels();
        hotel.setHotelName(name);
        hotel.setPhoneContact(phone);
        hotel.setEmailContact(email);
        hotel.setOwner(owner);
        hotel.setLocation(location);
        return hotelRepository.save(hotel);
    }

    private void addHotelRoom(Hotels hotel, RoomTypes roomType, int total, int capacity,
                              String price) {
        if (hotelRoomRepository.existsByHotelsIdAndRoomTypesId(hotel.getId(), roomType.getId())) {
            return;
        }
        HotelRooms room = new HotelRooms();
        room.setHotels(hotel);
        room.setRoomTypes(roomType);
        room.setTotalRoom(total);
        room.setCapacity(capacity);
        room.setPricePerNight(new BigDecimal(price));
        hotelRoomRepository.save(room);
    }

    private Restaurants saveRestaurant(String name, String description, String open, String close,
                                       TourPlaces tourPlace) {
        if (restaurantRepository.existsByName(name)) {
            return restaurantRepository.findByNameContainingIgnoreCase(name).stream()
                    .filter(r -> r.getName().equalsIgnoreCase(name)).findFirst().orElseThrow();
        }
        Restaurants restaurant = new Restaurants();
        restaurant.setName(name);
        restaurant.setDescription(description);
        restaurant.setOpenTime(LocalTime.parse(open));
        restaurant.setClossTime(LocalTime.parse(close));
        restaurant.setTourPlaces(tourPlace);
        return restaurantRepository.save(restaurant);
    }

    private void addFood(Restaurants restaurant, FoodCategories category, String name,
                         String price, boolean available, String image) {
        if (foodRepository.findByRestaurantsIdAndIsAvailableTrue(restaurant.getId()).stream()
                .anyMatch(f -> f.getName().equalsIgnoreCase(name))) {
            return;
        }
        Foods food = new Foods();
        food.setName(name);
        food.setPrice(new BigDecimal(price));
        food.setImage(image);
        food.setIsAvailable(available);
        food.setRestaurants(restaurant);
        food.setFoodCategories(category);
        foodRepository.save(food);
    }

    private TourGuides saveTourGuide(Users user, String language, int years, String rate) {
        if (tourGuideRepository.findByUserId(user.getId()).isPresent()) {
            return tourGuideRepository.findByUserId(user.getId()).orElseThrow();
        }
        TourGuides guide = new TourGuides();
        guide.setLanguageSpoken(language);
        guide.setExperienceYear(years);
        guide.setRatePerDay(new BigDecimal(rate));
        guide.setUser(user);
        return tourGuideRepository.save(guide);
    }

    private TourPackages saveTourPackage(String name, String description, int days, int maxPeople,
                                         String price, Location location, TourGuides guide) {
        List<TourPackages> existing = tourPackageRepository.findByNameContainingIgnoreCase(name);
        return existing.stream().filter(p -> p.getName().equalsIgnoreCase(name)).findFirst()
                .orElseGet(() -> {
                    TourPackages pkg = new TourPackages();
                    pkg.setName(name);
                    pkg.setDescription(description);
                    pkg.setDurationDays(days);
                    pkg.setMaxPeople(maxPeople);
                    pkg.setPrice(new BigDecimal(price));
                    pkg.setLocations(location);
                    pkg.setToureGuides(guide);
                    return tourPackageRepository.save(pkg);
                });
    }

    private void savePromotion(String name, String code, String discountType, String discountValue,
                               String status, Hotels hotel, Restaurants restaurant,
                               TourPackages tourPackage) {
        if (promotionRepository.existsByCode(code)) {
            return;
        }
        Promotions promotion = new Promotions();
        promotion.setName(name);
        promotion.setCode(code);
        promotion.setDiscountType(discountType);
        promotion.setDiscountValue(new BigDecimal(discountValue));
        promotion.setStatus(status);
        promotion.setStartAt(LocalDateTime.now().minusDays(1));
        promotion.setEndAt(LocalDateTime.now().plusDays(45));
        promotion.setHotels(hotel);
        promotion.setRestraurants(restaurant);
        promotion.setTourPackages(tourPackage);
        promotionRepository.save(promotion);
    }

    private Attachments saveAttachment(Users uploadedBy, String url) {
        String fileName = "seed-" + System.nanoTime();
        Attachments attachment = new Attachments();
        attachment.setFileName(fileName + ".jpg");
        attachment.setOriginalName(fileName + ".jpg");
        attachment.setUploadedBy(uploadedBy);
        attachment.setCloudinaryUrl(url);
        attachment.setCloudinaryPublicId("seed/" + fileName);
        attachment.setCloudinaryResourceType("image");
        attachment.setFileType(AttachmentFileType.IMAGE);
        attachment.setMimeType("image/jpeg");
        attachment.setFileSize(0L);
        return attachmentRepository.save(attachment);
    }

    private void addHotelImage(Hotels hotel, String url, String type, int sortOrder) {
        if (!hotelAttachmentRepository.findByHotels_Id(hotel.getId()).isEmpty()) {
            return;
        }
        Users owner = hotel.getOwner();
        Attachments attachment = saveAttachment(owner, url);
        HotelAttachments link = new HotelAttachments();
        link.setHotels(hotel);
        link.setAttachments(attachment);
        link.setType(type);
        link.setSortOrder(sortOrder);
        hotelAttachmentRepository.save(link);
    }

    private void addTourPlaceImage(TourPlaces place, String url, String type, int sortOrder) {
        if (!tourPlaceAttachmentRepository.findByTourPlaces_Id(place.getId()).isEmpty()) {
            return;
        }
        Users owner = place.getUser();
        Attachments attachment = saveAttachment(owner, url);
        TourPlaceAttachments link = new TourPlaceAttachments();
        link.setTourPlaces(place);
        link.setAttachments(attachment);
        link.setType(type);
        link.setSortOrder(sortOrder);
        tourPlaceAttachmentRepository.save(link);
    }

    private void addRestaurantImage(Restaurants restaurant, String url, String type, int sortOrder) {
        if (!restaurantAttachmentRepository.findByRestaurants_Id(restaurant.getId()).isEmpty()) {
            return;
        }
        Users owner = restaurant.getTourPlaces().getUser();
        Attachments attachment = saveAttachment(owner, url);
        RestaurantAttachments link = new RestaurantAttachments();
        link.setRestaurants(restaurant);
        link.setAttachments(attachment);
        link.setType(type);
        link.setSortOrder(sortOrder);
        restaurantAttachmentRepository.save(link);
    }
}