/**
 * BookMyStayApp
 *
 * Hotel Booking Management System - Demonstration of
 * abstraction, inheritance, polymorphism and static availability.
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

        System.out.println("\nAvailable Room Types:\n");

        // Polymorphic Room references
        Room single = new SingleRoom();
        Room doubleRoom = new DoubleRoom();
        Room suite = new SuiteRoom();

        // Static availability variables
        int singleAvailable = 10;
        int doubleAvailable = 7;
        int suiteAvailable = 3;

        System.out.println("----- Single Room -----");
        single.displayDetails();
        System.out.println("Available Rooms: " + singleAvailable);

        System.out.println("\n----- Double Room -----");
        doubleRoom.displayDetails();
        System.out.println("Available Rooms: " + doubleAvailable);

        System.out.println("\n----- Suite Room -----");
        suite.displayDetails();
        System.out.println("Available Rooms: " + suiteAvailable);

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