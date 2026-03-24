import java.util.HashMap;


public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("====================================");
        System.out.println("     Welcome to BookMyStay");
        System.out.println("  Hotel Booking System v1.0");
        System.out.println("====================================");

        System.out.println("\nAvailable Room Types:\n");

        // Create room objects
        Room single = new SingleRoom();
        Room doubleRoom = new DoubleRoom();
        Room suite = new SuiteRoom();

        // Initialize centralized inventory
        RoomInventory inventory = new RoomInventory();

        System.out.println("----- Single Room -----");
        single.displayDetails();
        System.out.println("Available Rooms: " + inventory.getAvailability("Single Room"));

        System.out.println("\n----- Double Room -----");
        doubleRoom.displayDetails();
        System.out.println("Available Rooms: " + inventory.getAvailability("Double Room"));

        System.out.println("\n----- Suite Room -----");
        suite.displayDetails();
        System.out.println("Available Rooms: " + inventory.getAvailability("Suite Room"));

        System.out.println("\nInventory Snapshot:");
        inventory.displayInventory();

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
 *
 * Centralized component responsible for managing
 * room availability using a HashMap.
 */
class RoomInventory {

    private HashMap<String, Integer> availabilityMap;

    // Constructor initializes inventory
    public RoomInventory() {

        availabilityMap = new HashMap<>();

        availabilityMap.put("Single Room", 10);
        availabilityMap.put("Double Room", 7);
        availabilityMap.put("Suite Room", 3);
    }

    // Get availability
    public int getAvailability(String roomType) {
        return availabilityMap.getOrDefault(roomType, 0);
    }

    // Update availability
    public void updateAvailability(String roomType, int newCount) {
        availabilityMap.put(roomType, newCount);
    }

    // Display entire inventory
    public void displayInventory() {

        for (String roomType : availabilityMap.keySet()) {
            System.out.println(roomType + " : " + availabilityMap.get(roomType) + " rooms available");
        }
    }
}