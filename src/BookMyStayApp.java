import java.util.*;
public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("====================================");
        System.out.println("     Welcome to BookMyStay");
        System.out.println("  Hotel Booking System v1.0");
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

    private String guestName;
    private String roomType;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
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
                + " (" + reservation.getRoomType() + ")");
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
 *
 * Handles reservation confirmation and room allocation.
 */
class BookingService {

    // Store all allocated room IDs
    private Set<String> allocatedRoomIds = new HashSet<>();

    // Map room type -> allocated room IDs
    private HashMap<String, Set<String>> roomAllocations = new HashMap<>();


    public void processBookings(BookingRequestQueue queue, RoomInventory inventory) {

        int roomCounter = 1;

        while (!queue.isEmpty()) {

            Reservation request = queue.getNextRequest();

            String roomType = request.getRoomType();

            int available = inventory.getAvailability(roomType);

            if (available > 0) {

                String roomId = roomType.replace(" ", "").toUpperCase()
                        + "-" + roomCounter++;

                // Ensure uniqueness
                if (!allocatedRoomIds.contains(roomId)) {

                    allocatedRoomIds.add(roomId);

                    roomAllocations
                            .computeIfAbsent(roomType, k -> new HashSet<>())
                            .add(roomId);

                    inventory.decrementAvailability(roomType);

                    System.out.println("Reservation Confirmed!");
                    System.out.println("Guest: " + request.getGuestName());
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