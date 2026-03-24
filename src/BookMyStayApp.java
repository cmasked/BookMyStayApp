import java.util.*;

/**
 * BookMyStayApp
 *
 * Hotel Booking Management System demonstrating:
 * - Abstraction
 * - Centralized inventory
 * - FIFO booking requests
 * - Safe room allocation
 * - Add-on service extensibility
 *
 * @author Varad
 * @version 2.0
 */

public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("====================================");
        System.out.println("     Welcome to BookMyStay");
        System.out.println("  Hotel Booking System v2.0");
        System.out.println("====================================");

        // Initialize room objects
        List<Room> rooms = new ArrayList<>();
        rooms.add(new SingleRoom());
        rooms.add(new DoubleRoom());
        rooms.add(new SuiteRoom());

        // Inventory
        RoomInventory inventory = new RoomInventory();

        // Search service
        RoomSearchService searchService = new RoomSearchService();

        System.out.println("\nSearching Available Rooms...\n");
        searchService.searchAvailableRooms(rooms, inventory);

        // Booking queue
        BookingRequestQueue bookingQueue = new BookingRequestQueue();

        System.out.println("\nGuests submitting booking requests...\n");

        bookingQueue.addRequest(new Reservation("Alice", "Single Room"));
        bookingQueue.addRequest(new Reservation("Bob", "Double Room"));
        bookingQueue.addRequest(new Reservation("Charlie", "Suite Room"));
        bookingQueue.addRequest(new Reservation("David", "Single Room"));

        System.out.println("\nProcessing booking requests...\n");

        BookingService bookingService = new BookingService();
        bookingService.processBookings(bookingQueue, inventory);

        // ===============================
        // Use Case 7: Add-On Services
        // ===============================

        AddOnServiceManager serviceManager = new AddOnServiceManager();

        AddOnService breakfast = new AddOnService("Breakfast", 500);
        AddOnService pickup = new AddOnService("Airport Pickup", 1200);
        AddOnService spa = new AddOnService("Spa Access", 2000);

        System.out.println("\nAdding services to reservations...\n");

        serviceManager.addService("RES-1", breakfast);
        serviceManager.addService("RES-1", pickup);
        serviceManager.addService("RES-2", spa);

        serviceManager.displayServices("RES-1");

        double totalServiceCost = serviceManager.calculateTotalServiceCost("RES-1");
        System.out.println("\nTotal Add-On Cost for RES-1: ₹" + totalServiceCost);

        System.out.println("\nApplication terminated.");
    }
}

/**
 * Abstract class representing a generic Room
 */
abstract class Room {

    protected String roomType;
    protected int beds;
    protected int size;
    protected double price;

    public Room(String roomType, int beds, int size, double price) {
        this.roomType = roomType;
        this.beds = beds;
        this.size = size;
        this.price = price;
    }

    public String getRoomType() {
        return roomType;
    }

    public void displayDetails() {
        System.out.println("Room Type : " + roomType);
        System.out.println("Beds      : " + beds);
        System.out.println("Size      : " + size + " sq.ft");
        System.out.println("Price     : ₹" + price + " per night");
    }
}

/**
 * Room Implementations
 */
class SingleRoom extends Room {
    public SingleRoom() {
        super("Single Room", 1, 200, 2500);
    }
}

class DoubleRoom extends Room {
    public DoubleRoom() {
        super("Double Room", 2, 350, 4000);
    }
}

class SuiteRoom extends Room {
    public SuiteRoom() {
        super("Suite Room", 3, 600, 8000);
    }
}

/**
 * RoomInventory
 */
class RoomInventory {

    private HashMap<String, Integer> availabilityMap;

    public RoomInventory() {
        availabilityMap = new HashMap<>();
        availabilityMap.put("Single Room", 10);
        availabilityMap.put("Double Room", 7);
        availabilityMap.put("Suite Room", 3);
    }

    public int getAvailability(String roomType) {
        return availabilityMap.getOrDefault(roomType, 0);
    }

    public void decrementAvailability(String roomType) {
        int current = availabilityMap.getOrDefault(roomType, 0);
        if (current > 0) {
            availabilityMap.put(roomType, current - 1);
        }
    }
}

/**
 * RoomSearchService
 */
class RoomSearchService {

    public void searchAvailableRooms(List<Room> rooms, RoomInventory inventory) {

        for (Room room : rooms) {

            int available = inventory.getAvailability(room.getRoomType());

            if (available > 0) {

                System.out.println("----- Available Room -----");
                room.displayDetails();
                System.out.println("Available Rooms: " + available);
                System.out.println();
            }
        }
    }
}

/**
 * Reservation (Guest booking request)
 */
class Reservation {

    private static int counter = 1;

    private String reservationId;
    private String guestName;
    private String roomType;

    public Reservation(String guestName, String roomType) {
        this.reservationId = "RES-" + counter++;
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getReservationId() {
        return reservationId;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getRoomType() {
        return roomType;
    }
}

/**
 * BookingRequestQueue (FIFO Queue)
 */
class BookingRequestQueue {

    private Queue<Reservation> requestQueue;

    public BookingRequestQueue() {
        requestQueue = new LinkedList<>();
    }

    public void addRequest(Reservation reservation) {
        requestQueue.add(reservation);
        System.out.println("Booking request added for "
                + reservation.getGuestName()
                + " (" + reservation.getRoomType()
                + ") ID: " + reservation.getReservationId());
    }

    public Reservation getNextRequest() {
        return requestQueue.poll();
    }

    public boolean isEmpty() {
        return requestQueue.isEmpty();
    }
}

/**
 * BookingService
 */
class BookingService {

    private Set<String> allocatedRoomIds = new HashSet<>();
    private HashMap<String, Set<String>> roomAllocations = new HashMap<>();
    private Map<String, Reservation> confirmedReservations = new HashMap<>();

    public void processBookings(BookingRequestQueue queue, RoomInventory inventory) {

        int roomCounter = 1;

        while (!queue.isEmpty()) {

            Reservation request = queue.getNextRequest();

            String roomType = request.getRoomType();
            int available = inventory.getAvailability(roomType);

            if (available > 0) {

                String roomId = roomType.replace(" ", "").toUpperCase()
                        + "-" + roomCounter++;

                if (!allocatedRoomIds.contains(roomId)) {

                    allocatedRoomIds.add(roomId);

                    roomAllocations
                            .computeIfAbsent(roomType, k -> new HashSet<>())
                            .add(roomId);

                    inventory.decrementAvailability(roomType);

                    confirmedReservations.put(request.getReservationId(), request);

                    System.out.println("Reservation Confirmed!");
                    System.out.println("Guest: " + request.getGuestName());
                    System.out.println("Reservation ID: " + request.getReservationId());
                    System.out.println("Room Type: " + roomType);
                    System.out.println("Assigned Room ID: " + roomId);
                    System.out.println();
                }

            } else {

                System.out.println("Reservation Failed for "
                        + request.getGuestName()
                        + " (No rooms available)");
                System.out.println();
            }
        }
    }
}

/**
 * AddOnService
 */
class AddOnService {

    private String serviceName;
    private double price;

    public AddOnService(String serviceName, double price) {
        this.serviceName = serviceName;
        this.price = price;
    }

    public String getServiceName() {
        return serviceName;
    }

    public double getPrice() {
        return price;
    }
}

/**
 * AddOnServiceManager
 *
 * Handles mapping between reservations and selected services
 */
class AddOnServiceManager {

    private Map<String, List<AddOnService>> serviceMap = new HashMap<>();

    public void addService(String reservationId, AddOnService service) {

        serviceMap
                .computeIfAbsent(reservationId, k -> new ArrayList<>())
                .add(service);

        System.out.println("Service added: "
                + service.getServiceName()
                + " for Reservation " + reservationId);
    }

    public void displayServices(String reservationId) {

        List<AddOnService> services = serviceMap.get(reservationId);

        if (services == null || services.isEmpty()) {
            System.out.println("No add-on services for " + reservationId);
            return;
        }

        System.out.println("\nAdd-On Services for " + reservationId);
        for (AddOnService s : services) {
            System.out.println("- " + s.getServiceName()
                    + " ₹" + s.getPrice());
        }
    }

    public double calculateTotalServiceCost(String reservationId) {

        List<AddOnService> services = serviceMap.get(reservationId);

        if (services == null) return 0;

        double total = 0;
        for (AddOnService s : services) {
            total += s.getPrice();
        }
        return total;
    }
}