import java.util.*;

/**
 * BookMyStayApp v3.0
 * Demonstrates:
 * - Room inventory
 * - FIFO booking
 * - Add-on services
 * - Booking history
 * - Reporting
 */

public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("====================================");
        System.out.println("     Welcome to BookMyStay");
        System.out.println("  Hotel Booking System v3.0");
        System.out.println("====================================");

        List<Room> rooms = new ArrayList<>();
        rooms.add(new SingleRoom());
        rooms.add(new DoubleRoom());
        rooms.add(new SuiteRoom());

        RoomInventory inventory = new RoomInventory();
        RoomSearchService searchService = new RoomSearchService();

        System.out.println("\nSearching Available Rooms...\n");
        searchService.searchAvailableRooms(rooms, inventory);

        BookingRequestQueue bookingQueue = new BookingRequestQueue();

        bookingQueue.addRequest(new Reservation("Alice", "Single Room"));
        bookingQueue.addRequest(new Reservation("Bob", "Double Room"));
        bookingQueue.addRequest(new Reservation("Charlie", "Suite Room"));
        bookingQueue.addRequest(new Reservation("David", "Single Room"));

        BookingHistory history = new BookingHistory();

        BookingService bookingService = new BookingService(history);
        bookingService.processBookings(bookingQueue, inventory);

        // ===============================
        // Add-On Services
        // ===============================
        AddOnServiceManager serviceManager = new AddOnServiceManager();

        serviceManager.addService("RES-1", new AddOnService("Breakfast", 500));
        serviceManager.addService("RES-1", new AddOnService("Airport Pickup", 1200));
        serviceManager.addService("RES-2", new AddOnService("Spa Access", 2000));

        serviceManager.displayServices("RES-1");

        // ===============================
        // Booking History & Reporting
        // ===============================
        BookingReportService reportService = new BookingReportService();

        System.out.println("\n===== Booking History =====");
        reportService.displayAllBookings(history);

        System.out.println("\n===== Booking Summary =====");
        reportService.generateSummary(history);
    }
}

/* ===========================
   Room Model
   =========================== */

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

/* ===========================
   Inventory
   =========================== */

class RoomInventory {

    private Map<String, Integer> availability = new HashMap<>();

    public RoomInventory() {
        availability.put("Single Room", 10);
        availability.put("Double Room", 7);
        availability.put("Suite Room", 3);
    }

    public int getAvailability(String roomType) {
        return availability.getOrDefault(roomType, 0);
    }

    public void decrementAvailability(String roomType) {
        availability.put(roomType, availability.get(roomType) - 1);
    }
}

/* ===========================
   Search
   =========================== */

class RoomSearchService {

    public void searchAvailableRooms(List<Room> rooms, RoomInventory inventory) {

        for (Room room : rooms) {
            int available = inventory.getAvailability(room.getRoomType());

            if (available > 0) {
                System.out.println("----- Available Room -----");
                room.displayDetails();
                System.out.println("Available: " + available);
                System.out.println();
            }
        }
    }
}

/* ===========================
   Reservation
   =========================== */

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

    public String getReservationId() { return reservationId; }
    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }
}

/* ===========================
   Booking Queue
   =========================== */

class BookingRequestQueue {

    private Queue<Reservation> queue = new LinkedList<>();

    public void addRequest(Reservation r) {
        queue.add(r);
        System.out.println("Request added: " + r.getReservationId());
    }

    public Reservation getNextRequest() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}

/* ===========================
   Booking History
   =========================== */

class BookingHistory {

    private List<Reservation> confirmedBookings = new ArrayList<>();

    public void addReservation(Reservation r) {
        confirmedBookings.add(r);
    }

    public List<Reservation> getAllReservations() {
        return Collections.unmodifiableList(confirmedBookings);
    }
}

/* ===========================
   Booking Service
   =========================== */

class BookingService {

    private Set<String> allocatedRoomIds = new HashSet<>();
    private BookingHistory history;

    public BookingService(BookingHistory history) {
        this.history = history;
    }

    public void processBookings(BookingRequestQueue queue, RoomInventory inventory) {

        int counter = 1;

        while (!queue.isEmpty()) {

            Reservation r = queue.getNextRequest();
            String type = r.getRoomType();

            if (inventory.getAvailability(type) > 0) {

                String roomId = type.replace(" ", "").toUpperCase() + "-" + counter++;

                if (!allocatedRoomIds.contains(roomId)) {

                    allocatedRoomIds.add(roomId);
                    inventory.decrementAvailability(type);

                    history.addReservation(r);

                    System.out.println("Confirmed: " + r.getReservationId()
                            + " -> " + roomId);
                }
            }
        }
    }
}

/* ===========================
   Add-On Services
   =========================== */

class AddOnService {
    private String name;
    private double price;

    public AddOnService(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
}

class AddOnServiceManager {

    private Map<String, List<AddOnService>> services = new HashMap<>();

    public void addService(String reservationId, AddOnService service) {

        services.computeIfAbsent(reservationId, k -> new ArrayList<>())
                .add(service);
    }

    public void displayServices(String reservationId) {

        List<AddOnService> list = services.get(reservationId);

        if (list == null) {
            System.out.println("No services.");
            return;
        }

        for (AddOnService s : list) {
            System.out.println(s.getName() + " ₹" + s.getPrice());
        }
    }
}

/* ===========================
   Reporting
   =========================== */

class BookingReportService {

    public void displayAllBookings(BookingHistory history) {

        for (Reservation r : history.getAllReservations()) {
            System.out.println(r.getReservationId()
                    + " | " + r.getGuestName()
                    + " | " + r.getRoomType());
        }
    }

    public void generateSummary(BookingHistory history) {

        Map<String, Integer> counts = new HashMap<>();

        for (Reservation r : history.getAllReservations()) {
            counts.put(r.getRoomType(),
                    counts.getOrDefault(r.getRoomType(), 0) + 1);
        }

        System.out.println("Total Bookings: " + history.getAllReservations().size());

        for (String type : counts.keySet()) {
            System.out.println(type + ": " + counts.get(type));
        }
    }
}