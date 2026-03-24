import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;

/**
 * BookMyStayApp
 *
 * Hotel Booking Management System demonstrating
 * abstraction, centralized inventory, read-only search,
 * and FIFO booking request handling.
 *
 * @author Varad
 * @version 1.0
 */

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

        // Initialize centralized inventory
        RoomInventory inventory = new RoomInventory();

        // Initialize search service
        RoomSearchService searchService = new RoomSearchService();

        System.out.println("\nSearching Available Rooms...\n");

        // Perform read-only search
        searchService.searchAvailableRooms(rooms, inventory);

        // Initialize booking request queue
        BookingRequestQueue bookingQueue = new BookingRequestQueue();

        System.out.println("\nGuests submitting booking requests...\n");

        bookingQueue.addRequest(new Reservation("Alice", "Single Room"));
        bookingQueue.addRequest(new Reservation("Bob", "Double Room"));
        bookingQueue.addRequest(new Reservation("Charlie", "Suite Room"));
        bookingQueue.addRequest(new Reservation("David", "Single Room"));

        System.out.println("\nCurrent Booking Request Queue:");
        bookingQueue.displayQueue();

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
 * Single Room Implementation
 */
class SingleRoom extends Room {

    public SingleRoom() {
        super("Single Room", 1, 200, 2500);
    }
}


/**
 * Double Room Implementation
 */
class DoubleRoom extends Room {

    public DoubleRoom() {
        super("Double Room", 2, 350, 4000);
    }
}


/**
 * Suite Room Implementation
 */
class SuiteRoom extends Room {

    public SuiteRoom() {
        super("Suite Room", 3, 600, 8000);
    }
}


/**
 * RoomInventory
 * Centralized availability storage
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

    public void updateAvailability(String roomType, int newCount) {
        availabilityMap.put(roomType, newCount);
    }
}


/**
 * RoomSearchService
 * Handles read-only room search
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
 * Reservation
 * Represents a guest booking request
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
 * BookingRequestQueue
 * Manages booking requests using FIFO queue
 */
class BookingRequestQueue {

    private Queue<Reservation> requestQueue;

    public BookingRequestQueue() {
        requestQueue = new LinkedList<>();
    }

    // Add booking request
    public void addRequest(Reservation reservation) {
        requestQueue.add(reservation);
        System.out.println("Booking request added for " + reservation.getGuestName()
                + " (" + reservation.getRoomType() + ")");
    }

    // Display queue
    public void displayQueue() {

        for (Reservation r : requestQueue) {
            System.out.println(r.getGuestName() + " requested " + r.getRoomType());
        }
    }
}