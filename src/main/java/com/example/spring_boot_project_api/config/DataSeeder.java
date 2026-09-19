package com.example.spring_boot_project_api.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.spring_boot_project_api.enums.AttachmentFileType;
import com.example.spring_boot_project_api.enums.GenderEnum;
import com.example.spring_boot_project_api.enums.PaymentMethod;
import com.example.spring_boot_project_api.enums.PaymentStatus;
import com.example.spring_boot_project_api.model.Attachments;
import com.example.spring_boot_project_api.model.BusinesssOwnerProfiles;
import com.example.spring_boot_project_api.model.ContactMessages;
import com.example.spring_boot_project_api.model.FoodCategories;
import com.example.spring_boot_project_api.model.FoodOrderItems;
import com.example.spring_boot_project_api.model.FoodOrders;
import com.example.spring_boot_project_api.model.Foods;
import com.example.spring_boot_project_api.model.HotelAttachments;
import com.example.spring_boot_project_api.model.HotelRooms;
import com.example.spring_boot_project_api.model.Hotels;
import com.example.spring_boot_project_api.model.Location;
import com.example.spring_boot_project_api.model.Notifications;
import com.example.spring_boot_project_api.model.Payments;
import com.example.spring_boot_project_api.model.PlaceCategoties;
import com.example.spring_boot_project_api.model.Promotions;
import com.example.spring_boot_project_api.model.RestaurantAttachments;
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
import com.example.spring_boot_project_api.model.TourPackages;
import com.example.spring_boot_project_api.model.TourPlaceAttachments;
import com.example.spring_boot_project_api.model.TourPlaces;
import com.example.spring_boot_project_api.model.UserRoles;
import com.example.spring_boot_project_api.model.Users;
import com.example.spring_boot_project_api.repository.AttachmentRepository;
import com.example.spring_boot_project_api.repository.BusinessOwnerProfileRepository;
import com.example.spring_boot_project_api.repository.ContactMessageRepository;
import com.example.spring_boot_project_api.repository.FoodCategoryRepository;
import com.example.spring_boot_project_api.repository.FoodOrderItemRepository;
import com.example.spring_boot_project_api.repository.FoodOrderRepository;
import com.example.spring_boot_project_api.repository.FoodRepository;
import com.example.spring_boot_project_api.repository.HotelAttachmentRepository;
import com.example.spring_boot_project_api.repository.HotelRepository;
import com.example.spring_boot_project_api.repository.HotelRoomRepository;
import com.example.spring_boot_project_api.repository.LocationRepository;
import com.example.spring_boot_project_api.repository.NotificationRepository;
import com.example.spring_boot_project_api.repository.PaymentRepository;
import com.example.spring_boot_project_api.repository.PlaceCategoryRepository;
import com.example.spring_boot_project_api.repository.PromotionRepository;
import com.example.spring_boot_project_api.repository.RestaurantAttachmentRepository;
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
    private final TicketRepository ticketRepository;
    private final TicketBookingRepository ticketBookingRepository;
    private final RoomBookingRepository roomBookingRepository;
    private final RoomRepository roomRepository;
    private final TourBookingRepository tourBookingRepository;
    private final FoodOrderRepository foodOrderRepository;
    private final FoodOrderItemRepository foodOrderItemRepository;
    private final ReviewRepository reviewRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationRepository notificationRepository;
    private final ContactMessageRepository contactMessageRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
        seedRoleMetadata();
        seedDefaultAdmin();
        seedDefaultOwner();
        seedDefaultTourist();
        seedDemoContent();
        seedTransactionalContent();
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

    private static final Map<String, List<String>> ROLE_PERMISSION_SEEDS = new LinkedHashMap<>();
    static {
        ROLE_PERMISSION_SEEDS.put(RoleNames.ADMIN, List.of(
                "users.view", "users.create", "users.edit", "users.delete",
                "hotels.view", "hotels.edit", "rooms.manage", "bookings.view",
                "dining.view", "menu.manage", "orders.manage",
                "places.manage", "tickets.manage", "packages.manage",
                "payments.view", "payouts.manage", "reports.export",
                "settings.edit", "logs.view"));
        ROLE_PERMISSION_SEEDS.put(RoleNames.OWNER, List.of(
                "hotels.view", "hotels.edit", "rooms.manage", "bookings.view",
                "dining.view", "menu.manage", "orders.manage",
                "places.manage", "tickets.manage", "packages.manage",
                "payments.view", "payouts.manage", "reports.export"));
        ROLE_PERMISSION_SEEDS.put(RoleNames.TOURIST, List.of(
                "hotels.view", "dining.view", "bookings.view"));
        ROLE_PERMISSION_SEEDS.put("SUPER_OWNER", List.of(
                "users.view", "users.edit",
                "hotels.view", "hotels.edit", "rooms.manage", "bookings.view",
                "dining.view", "menu.manage", "orders.manage",
                "places.manage", "tickets.manage", "packages.manage",
                "payments.view", "payouts.manage", "reports.export",
                "settings.edit", "logs.view"));
        ROLE_PERMISSION_SEEDS.put("OWNER_TOUR", List.of(
                "places.manage", "tickets.manage", "packages.manage",
                "bookings.view", "payments.view", "reports.export"));
        ROLE_PERMISSION_SEEDS.put("OWNER_HOTEL", List.of(
                "hotels.view", "hotels.edit", "rooms.manage", "bookings.view",
                "payments.view", "reports.export"));
        ROLE_PERMISSION_SEEDS.put("OWNER_RESTUARANT", List.of(
                "dining.view", "menu.manage", "orders.manage",
                "bookings.view", "payments.view", "reports.export"));
    }

    private static final Map<String, String> ROLE_LABEL_SEEDS = new LinkedHashMap<>();
    private static final Map<String, String> ROLE_DESCRIPTION_SEEDS = new LinkedHashMap<>();
    private static final Map<String, String> ROLE_COLOR_SEEDS = new LinkedHashMap<>();
    static {
        ROLE_LABEL_SEEDS.put(RoleNames.ADMIN, "Super Administrator");
        ROLE_DESCRIPTION_SEEDS.put(RoleNames.ADMIN,
                "Full administrative privileges across user accounts, destinations, hotels, financial reconciliations, and global settings.");
        ROLE_COLOR_SEEDS.put(RoleNames.ADMIN, "purple");

        ROLE_LABEL_SEEDS.put(RoleNames.OWNER, "Business & Property Partner");
        ROLE_DESCRIPTION_SEEDS.put(RoleNames.OWNER,
                "Full management rights over registered properties, room inventories, dining menus, tour packages, and payout accounts.");
        ROLE_COLOR_SEEDS.put(RoleNames.OWNER, "green");

        ROLE_LABEL_SEEDS.put(RoleNames.TOURIST, "Customer & Tourist");
        ROLE_DESCRIPTION_SEEDS.put(RoleNames.TOURIST,
                "Default public traveler role for browsing attractions, making room & ticket bookings, and dining reservations.");
        ROLE_COLOR_SEEDS.put(RoleNames.TOURIST, "emerald");

        ROLE_LABEL_SEEDS.put("SUPER_OWNER", "Super Partner");
        ROLE_DESCRIPTION_SEEDS.put("SUPER_OWNER",
                "Top-tier partner account with extended management rights across the platform.");
        ROLE_COLOR_SEEDS.put("SUPER_OWNER", "amber");

        ROLE_LABEL_SEEDS.put("OWNER_TOUR", "Tour Operations Partner");
        ROLE_DESCRIPTION_SEEDS.put("OWNER_TOUR",
                "Manage tourist attractions, entrance tickets, and tour packages.");
        ROLE_COLOR_SEEDS.put("OWNER_TOUR", "blue");

        ROLE_LABEL_SEEDS.put("OWNER_HOTEL", "Hotel Partner");
        ROLE_DESCRIPTION_SEEDS.put("OWNER_HOTEL",
                "Manage hotel properties, room inventories, and reservations.");
        ROLE_COLOR_SEEDS.put("OWNER_HOTEL", "green");

        ROLE_LABEL_SEEDS.put("OWNER_RESTUARANT", "Restaurant Partner");
        ROLE_DESCRIPTION_SEEDS.put("OWNER_RESTUARANT",
                "Manage dining listings, menus, and food order fulfilment.");
        ROLE_COLOR_SEEDS.put("OWNER_RESTUARANT", "blue");
    }

    private void seedRoleMetadata() {
        roleRepository.findAll().forEach(role -> {
            String name = role.getName() == null ? "" : role.getName().trim().toUpperCase();
            boolean isSeeded = ROLE_PERMISSION_SEEDS.containsKey(name);
            if (!isSeeded) {
                return;
            }
            boolean needsUpdate = role.getLabel() == null || role.getColor() == null;
            if (role.getPermissions() == null || role.getPermissions().isEmpty()) {
                needsUpdate = true;
            }
            if (!needsUpdate) {
                return;
            }
            role.setLabel(ROLE_LABEL_SEEDS.get(name));
            role.setDescription(ROLE_DESCRIPTION_SEEDS.get(name));
            role.setColor(ROLE_COLOR_SEEDS.get(name));
            if (role.getPermissions() == null || role.getPermissions().isEmpty()) {
                role.getPermissions().addAll(ROLE_PERMISSION_SEEDS.get(name));
            }
            roleRepository.save(role);
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
        BusinesssOwnerProfiles profile = businessOwnerProfileRepository
                .findByUsersId(ownerUser.getId())
                .orElseGet(BusinesssOwnerProfiles::new);
        profile.setUsers(ownerUser);
        profile.setBusinessName(businessName);
        profile.setBusinessLicenseNo(licenseNo);
        profile.setVerificationStatus("VERIFIED");
        profile.setVerifiedAt(LocalDate.now());
        businessOwnerProfileRepository.save(profile);
    }

    private void seedDefaultTourist() {
        Optional<Roles> role = roleRepository.findByName(RoleNames.TOURIST);
        if (role.isEmpty()) {
            return;
        }
        Optional<Users> existingTourist = userRepository.findByUsername("tourist");
        if (existingTourist.isPresent()) {
            normalizePassword(existingTourist.get(), "tourist123");
        } else {
            Users tourist = newUser("Tourist Traveler", "tourist", "tourist@smart-tourism.com",
                    "tourist123");
            userRepository.save(tourist);
            assignRole(tourist, role.get());
        }

        Optional<Users> existingCustomer = userRepository.findByUsername("customer");
        if (existingCustomer.isPresent()) {
            normalizePassword(existingCustomer.get(), "customer123");
        } else {
            Users customer = newUser("Customer Dara", "customer", "customer@smart-tourism.com",
                    "customer123");
            userRepository.save(customer);
            assignRole(customer, role.get());
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
    // Transactional demo content (tickets, bookings, orders, reviews,
    // payments, notifications, contact messages)
    // ------------------------------------------------------------------

    @Transactional
    private void seedTransactionalContent() {
        seedTickets();
        seedRoomBookings();
        seedTicketBookings();
        seedTourBookings();
        seedFoodOrders();
        seedReviews();
        seedPayments();
        seedNotifications();
        seedContactMessages();
    }

    private void seedTickets() {
        if (ticketRepository.count() > 0) {
            return;
        }
        addTicket("Angkor Wat", "Angkor Wonder Pass (1 Day)", "37",
                "Full-day entry to the Angkor Archaeological Park with sunrise access.");
        addTicket("Bayon Temple", "Angkor Thom Heritage Pass", "20",
                "Entry to Bayon and the heart of the ancient Angkor Thom complex.");
        addTicket("Ta Prohm", "Tomb Raider Jungle Pass", "22",
                "Guided entry to the jungle temple made famous by its giant strangler figs.");
        addTicket("Royal Palace & Silver Pagoda", "Royal Palace Entry", "10",
                "Visit the Royal Palace, Throne Hall and the Silver Pagoda grounds.");
        addTicket("Koh Rong", "Koh Rong Ferry & Beach Pass", "25",
                "Round-trip ferry from Sihanoukville plus access to the island beaches.");
        addTicket("Independence Beach", "Beach Day Pass", "5",
                "Day pass with deck chair and beach amenities at Independence Beach.");
        addTicket("Bokor National Park", "Bokor Heritage Drive Pass", "15",
                "Entry for the misty mountain drive up to the French hill station.");
        addTicket("Bamboo Train", "Bamboo Train Ride", "8",
                "One-way ride on the rustic bamboo train through Battambang rice fields.");
        addTicket("Kep Beach", "Kep Shore Pass", "4",
                "Access to Kep's promenade, beaches and crab market area.");
        addTicket("Yeak Laom Lake", "Yeak Laom Lake Trek", "6",
                "Entry to the crater lake reserve with jungle walking trails.");
        addTicket("Sen Monorom Waterfall", "Waterfall Entry Pass", "3",
                "Entry to the Sen Monorom waterfall swimming pools and trails.");
    }

    private void addTicket(String placeName, String ticketName, String price, String description) {
        TourPlaces place = tourismPlaceRepository.findByNameContainingIgnoreCase(placeName).stream()
                .filter(p -> p.getName().equalsIgnoreCase(placeName)).findFirst().orElse(null);
        if (place == null) {
            return;
        }
        if (ticketRepository.findByTourPlacesId(place.getId()).stream()
                .anyMatch(t -> t.getName().equalsIgnoreCase(ticketName))) {
            return;
        }
        Tickets ticket = new Tickets();
        ticket.setName(ticketName);
        ticket.setPrice(new BigDecimal(price));
        ticket.setDescription(description);
        ticket.setIsAvailable(true);
        ticket.setTourPlaces(place);
        ticketRepository.save(ticket);
    }

    private void seedRoomBookings() {
        if (roomBookingRepository.count() > 0) {
            return;
        }
        Users tourist = userRepository.findByUsername("tourist").orElse(null);
        Hotels sokha = hotelRepository.findByHotelName("Sokha Angkor Resort").orElse(null);
        if (tourist == null || sokha == null) {
            return;
        }
        Rooms room = roomRepository.findByHotelsId(sokha.getId()).stream().findFirst()
                .orElseGet(() -> {
                    HotelRooms hr = hotelRoomRepository.findByHotelsId(sokha.getId()).stream()
                            .findFirst().orElse(null);
                    if (hr == null) {
                        return null;
                    }
                    Rooms r = new Rooms();
                    r.setHotels(hr.getHotels());
                    r.setRoomTypes(hr.getRoomTypes());
                    return roomRepository.save(r);
                });
        if (room == null) {
            return;
        }
        addRoomBooking(tourist, room, LocalDate.now().plusDays(3), LocalDate.now().plusDays(6),
                "BAKONG_KHQR", "180", "CONFIRMED", 2);
        addRoomBooking(tourist, room, LocalDate.now().plusDays(10), LocalDate.now().plusDays(12),
                "CARD", "540", "PENDING", 3);
    }

    private void addRoomBooking(Users user, Rooms room, LocalDate checkIn, LocalDate checkOut,
            String paymentMethod, String amount, String status, int guests) {
        RoomBookings booking = new RoomBookings();
        booking.setUsers(user);
        booking.setRooms(room);
        booking.setNumGuest(guests);
        booking.setCheckIn(checkIn);
        booking.setCheckOut(checkOut);
        booking.setPaymentMethod(paymentMethod);
        booking.setAmount(new BigDecimal(amount));
        booking.setStatus(status);
        roomBookingRepository.save(booking);
    }

    private void seedTicketBookings() {
        if (ticketBookingRepository.count() > 0) {
            return;
        }
        Users tourist = userRepository.findByUsername("tourist").orElse(null);
        Tickets angkor = ticketRepository.findByNameContainingIgnoreCase("Angkor Wonder")
                .stream().findFirst().orElse(null);
        Tickets kohRong = ticketRepository.findByNameContainingIgnoreCase("Koh Rong Ferry")
                .stream().findFirst().orElse(null);
        if (tourist == null || angkor == null || kohRong == null) {
            return;
        }
        addTicketBooking(tourist, angkor, 2, "74", LocalDate.now().plusDays(5),
                "CONFIRMED", "CARD", "SEED-TB-0001");
        addTicketBooking(tourist, angkor, 1, "37", LocalDate.now().plusDays(20),
                "PENDING", "BAKONG_KHQR", "SEED-TB-0002");
        addTicketBooking(tourist, kohRong, 3, "75", LocalDate.now().plusDays(14),
                "CONFIRMED", "CARD", "SEED-TB-0003");
    }

    private void addTicketBooking(Users user, Tickets ticket, int qty, String totalPrice,
            LocalDate visitDate, String status, String paymentMethod, String qrCode) {
        TicketBookings booking = new TicketBookings();
        booking.setUser(user);
        booking.setTickets(ticket);
        booking.setQuantity(qty);
        booking.setTotalPrice(new BigDecimal(totalPrice));
        booking.setVisiDate(visitDate);
        booking.setStatus(status);
        booking.setPaymentMethod(paymentMethod);
        booking.setQrCode(qrCode);
        ticketBookingRepository.save(booking);
    }

    private void seedTourBookings() {
        if (tourBookingRepository.count() > 0) {
            return;
        }
        Users tourist = userRepository.findByUsername("tourist").orElse(null);
        TourPackages angkorSunrise = tourPackageRepository
                .findByNameContainingIgnoreCase("Angkor Sunrise Tour").stream()
                .filter(p -> p.getName().equalsIgnoreCase("Angkor Sunrise Tour")).findFirst().orElse(null);
        TourPackages kohRong = tourPackageRepository
                .findByNameContainingIgnoreCase("Koh Rong Island Escape").stream()
                .filter(p -> p.getName().equalsIgnoreCase("Koh Rong Island Escape")).findFirst().orElse(null);
        if (tourist == null || angkorSunrise == null || kohRong == null) {
            return;
        }
        addTourBooking(tourist, angkorSunrise, 2, LocalDate.now().plusDays(7),
                "90", "CONFIRMED", "CARD");
        addTourBooking(tourist, kohRong, 4, LocalDate.now().plusDays(21),
                "480", "PENDING", "BAKONG_KHQR");
    }

    private void addTourBooking(Users user, TourPackages pkg, int people, LocalDate tourDate,
            String totalPrice, String status, String paymentMethod) {
        TourBookings booking = new TourBookings();
        booking.setUser(user);
        booking.setTourPackages(pkg);
        booking.setNumPeople(people);
        booking.setTourDate(tourDate);
        booking.setTotalPrice(new BigDecimal(totalPrice));
        booking.setStatus(status);
        booking.setPaymentMethod(paymentMethod);
        tourBookingRepository.save(booking);
    }

    private void seedFoodOrders() {
        if (foodOrderRepository.count() > 0) {
            return;
        }
        Users tourist = userRepository.findByUsername("tourist").orElse(null);
        Restaurants chanreyTree = restaurantRepository.findByNameContainingIgnoreCase("Chanrey Tree")
                .stream().filter(r -> r.getName().equalsIgnoreCase("Chanrey Tree")).findFirst().orElse(null);
        Restaurants fishMarket = restaurantRepository.findByNameContainingIgnoreCase("Sihanoukville Fish Market")
                .stream().filter(r -> r.getName().equalsIgnoreCase("Sihanoukville Fish Market")).findFirst().orElse(null);
        if (tourist == null || chanreyTree == null || fishMarket == null) {
            return;
        }
        addFoodOrder(tourist, chanreyTree, LocalDateTime.now().plusHours(3),
                "PENDING", "TC-0001");
        addFoodOrder(tourist, chanreyTree, LocalDateTime.now().minusDays(2).plusHours(4),
                "COMPLETED", "TC-0002");
        addFoodOrder(tourist, fishMarket, LocalDateTime.now().plusDays(1).plusHours(2),
                "CONFIRMED", "FM-0001");
    }

    private void addFoodOrder(Users user, Restaurants restaurant, LocalDateTime pickupTime,
            String status, String prefix) {
        List<Foods> menu = foodRepository.findByRestaurantsIdAndIsAvailableTrue(restaurant.getId())
                .stream().limit(3).collect(java.util.stream.Collectors.toList());
        if (menu.size() < 2) {
            return;
        }
        FoodOrders order = new FoodOrders();
        order.setUser(user);
        order.setRestuarants(restaurant);
        order.setPickupTime(pickupTime);
        order.setStatus(status);
        BigDecimal total = BigDecimal.ZERO;
        int itemNo = 1;
        for (Foods food : menu) {
            int qty = itemNo == 1 ? 2 : 1;
            BigDecimal subTotal = food.getPrice().multiply(BigDecimal.valueOf(qty));
            total = total.add(subTotal);
            FoodOrderItems item = new FoodOrderItems();
            item.setFoodOrders(order);
            item.setFoods(food);
            item.setQuantity(qty);
            item.setUnitPrice(food.getPrice());
            item.setSubTotal(subTotal);
            order.getFoodOrderItems().add(item);
            itemNo++;
        }
        order.setTotalPrice(total);
        foodOrderRepository.save(order);
    }

    private void seedReviews() {
        if (reviewRepository.count() > 0) {
            return;
        }
        Users tourist = userRepository.findByUsername("tourist").orElse(null);
        Users customer = userRepository.findByUsername("customer").orElse(null);
        if (tourist == null || customer == null) {
            return;
        }
        addReview(tourist, 5, "Breathtaking at sunrise, the five towers are unforgettable. "
                + "Skip the crowds and hire a guide.", "Angkor Wat", "PLACE");
        addReview(tourist, 4, "The jungle atmosphere makes this one the most unique temple "
                + "in the whole park.", "Ta Prohm", "PLACE");
        addReview(customer, 5, "Stayed for a weekend — impeccable service, great pool and "
                + "close to the temples.", "Sokha Angkor Resort", "HOTEL");
        addReview(customer, 4, "The fish amok was the best we had in Siem Reap. Lovely "
                + "courtyard seating.", "Chanrey Tree", "RESTAURANT");
        addReview(tourist, 5, "Perfect day trip, our guide knew every history fact and "
                + "the timing was spot on.", "Angkor Sunrise Tour", "PACKAGE");
    }

    private void addReview(Users user, int rating, String comment, String targetName,
            String targetType) {
        Reviews review = new Reviews();
        review.setUser(user);
        review.setRating(rating);
        review.setComment(comment);
        if ("PLACE".equals(targetType)) {
            review.setTourPlace(tourismPlaceRepository.findByNameContainingIgnoreCase(targetName)
                    .stream().filter(p -> p.getName().equalsIgnoreCase(targetName)).findFirst().orElse(null));
        } else if ("HOTEL".equals(targetType)) {
            review.setHotel(hotelRepository.findByHotelName(targetName).orElse(null));
        } else if ("RESTAURANT".equals(targetType)) {
            review.setRestaurant(restaurantRepository.findByNameContainingIgnoreCase(targetName)
                    .stream().filter(r -> r.getName().equalsIgnoreCase(targetName)).findFirst().orElse(null));
        } else if ("PACKAGE".equals(targetType)) {
            review.setTourPackage(tourPackageRepository.findByNameContainingIgnoreCase(targetName)
                    .stream().filter(p -> p.getName().equalsIgnoreCase(targetName)).findFirst().orElse(null));
        }
        reviewRepository.save(review);
    }

    private void seedPayments() {
        if (paymentRepository.count() > 0) {
            return;
        }
        RoomBookings rb = roomBookingRepository.findAll().stream().findFirst().orElse(null);
        TicketBookings tb = ticketBookingRepository.findAll().stream().findFirst().orElse(null);
        FoodOrders fo = foodOrderRepository.findAll().stream().findFirst().orElse(null);
        TourBookings tob = tourBookingRepository.findAll().stream().findFirst().orElse(null);
        if (rb == null || tb == null || fo == null || tob == null) {
            return;
        }
        savePayment("PAY-SEED-RB-001", rb.getAmount(), PaymentMethod.CARD, "TX-SEED-RB-001",
                rb, tb, fo, tob);
        savePayment("PAY-SEED-TB-001", tb.getTotalPrice(), PaymentMethod.BAKONG_KHQR, "TX-SEED-TB-001",
                rb, tb, fo, tob);
        savePayment("PAY-SEED-FO-001", fo.getTotalPrice(), PaymentMethod.CASH, "TX-SEED-FO-001",
                rb, tb, fo, tob);
        savePayment("PAY-SEED-TO-001", tob.getTotalPrice(), PaymentMethod.CARD, "TX-SEED-TO-001",
                rb, tb, fo, tob);
    }

    private void savePayment(String reference, BigDecimal amount, PaymentMethod method,
            String transactionId, RoomBookings rb, TicketBookings tb,
            FoodOrders fo, TourBookings tob) {
        Payments payment = new Payments();
        payment.setPaymentReference(reference);
        payment.setAmount(amount);
        payment.setPaymentMethod(method);
        payment.setTransactionId(transactionId);
        payment.setQrMd5(java.util.UUID.randomUUID().toString().replace("-", ""));
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now().minusDays(1));
        payment.setRoomBookings(rb);
        payment.setTicketBookings(tb);
        payment.setFoodOrders(fo);
        payment.setTourBookings(tob);
        paymentRepository.save(payment);
    }

    private void seedNotifications() {
        if (notificationRepository.count() > 0) {
            return;
        }
        Users admin = userRepository.findByUsername("admin").orElse(null);
        Users tourist = userRepository.findByUsername("tourist").orElse(null);
        if (admin == null || tourist == null) {
            return;
        }
        saveNotification(admin, "New booking received", "A visitor booked the Angkor Sunrise Tour for next week.",
                "BOOKING_CONFIRMED", false);
        saveNotification(admin, "Review submitted", "A 5-star review was just published for Angkor Wat.",
                "REVIEW", false);
        saveNotification(admin, "New promotion ready", "The 'Kep Weekend Deal' promotion is now live on the site.",
                "PROMOTION", true);
        saveNotification(tourist, "Welcome!",
                "Thanks for joining Smart Tourism Cambodia. Explore tours, stays and dining.",
                "SYSTEM", false);
        saveNotification(tourist, "Booking confirmed",
                "Your Angkor Wonder Pass booking has been confirmed. Enjoy your visit!",
                "BOOKING_CONFIRMED", false);
    }

    private void saveNotification(Users user, String title, String message, String type,
            boolean isRead) {
        Notifications notification = new Notifications();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setIsRead(isRead);
        notificationRepository.save(notification);
    }

    private void seedContactMessages() {
        if (contactMessageRepository.count() > 0) {
            return;
        }
        saveContactMessage("Sok Dara", "sok.dara@example.com", "Group tour for 12 people",
                "Hello, we would like to arrange a private Angkor Wat sunrise tour for a group "
                        + "of 12 next month. Could you share availability and pricing?");
        saveContactMessage("Maria Chen", "maria.chen@example.com", "Hotel near the airport",
                "Looking for a family-friendly hotel with airport shuttle service in Phnom Penh.");
        saveContactMessage("John Smith", "john.smith@example.com", "Food recommendation",
                "We are visiting Kep next weekend — any recommended places for crab dinner?");
    }

    private void saveContactMessage(String name, String email, String subject, String message) {
        ContactMessages contact = new ContactMessages();
        contact.setName(name);
        contact.setEmail(email);
        contact.setSubject(subject);
        contact.setMessage(message);
        contact.setIsRead(false);
        contactMessageRepository.save(contact);
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