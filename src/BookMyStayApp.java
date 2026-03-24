import java.util.*;

/**
 * BookMyStayApp v4.0
 * Demonstrates:
 * - FIFO booking
 * - Add-on services
 * - Booking history & reporting
 * - Input validation & custom exceptions
 */

public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("====================================");
        System.out.println("     Welcome to BookMyStay");
        System.out.println("  Hotel Booking System v4.0");
        System.out.println("====================================");

        List<Room> rooms = Arrays.asList(
                new SingleRoom(),
                new DoubleRoom(),
                new SuiteRoom()
        );

        RoomInventory inventory = new RoomInventory();
        BookingHistory history = new BookingHistory();
        BookingValidator validator = new BookingValidator(rooms, inventory);
        BookingService bookingService = new BookingService(history, validator);

        BookingRequestQueue queue = new BookingRequestQueue();

        // Some valid and invalid requests
        queue.addRequest(new Reservation("Alice", "Single Room"));
        queue.addRequest(new Reservation("", "Suite Room"));          // invalid name
        queue.addRequest(new Reservation("Bob", "Penthouse"));        // invalid room
        queue.addRequest(new Reservation("Charlie", "Double Room"));

        bookingService.processBookings(queue, inventory);

        // Reporting
        BookingReportService report = new BookingReportService();
        report.displayAllBookings(history);
        report.generateSummary(history);
    }
}

/* ===========================
   Custom Exceptions
   =========================== */

class InvalidRoomTypeException extends Exception {
    public InvalidRoomTypeException(String msg) { super(msg); }
}

class NoAvailabilityException extends Exception {
    public NoAvailabilityException(String msg) { super(msg); }
}

class InvalidReservationException extends Exception {
    public InvalidReservationException(String msg) { super(msg); }
}

/* ===========================
   Room Model
   =========================== */

abstract class Room {
    protected String roomType;
    protected double price;

    public Room(String roomType, double price) {
        this.roomType = roomType;
        this.price = price;
    }

    public String getRoomType() { return roomType; }
}

class SingleRoom extends Room {
    public SingleRoom() { super("Single Room", 2500); }
}

class DoubleRoom extends Room {
    public DoubleRoom() { super("Double Room", 4000); }
}

class SuiteRoom extends Room {
    public SuiteRoom() { super("Suite Room", 8000); }
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
   Inventory
   =========================== */

class RoomInventory {

    private Map<String, Integer> availability = new HashMap<>();

    public RoomInventory() {
        availability.put("Single Room", 2);
        availability.put("Double Room", 2);
        availability.put("Suite Room", 1);
    }

    public int getAvailability(String roomType) {
        return availability.getOrDefault(roomType, 0);
    }

    public void decrementAvailability(String roomType) throws NoAvailabilityException {
        int current = getAvailability(roomType);
        if (current <= 0) {
            throw new NoAvailabilityException("No rooms left for " + roomType);
        }
        availability.put(roomType, current - 1);
    }
}

/* ===========================
   Booking Validator
   =========================== */

class BookingValidator {

    private Set<String> validRoomTypes = new HashSet<>();
    private RoomInventory inventory;

    public BookingValidator(List<Room> rooms, RoomInventory inventory) {
        for (Room r : rooms) validRoomTypes.add(r.getRoomType());
        this.inventory = inventory;
    }

    public void validate(Reservation r)
            throws InvalidReservationException,
            InvalidRoomTypeException,
            NoAvailabilityException {

        if (r.getGuestName() == null || r.getGuestName().trim().isEmpty()) {
            throw new InvalidReservationException("Guest name cannot be empty.");
        }

        if (!validRoomTypes.contains(r.getRoomType())) {
            throw new InvalidRoomTypeException(
                    "Invalid room type: " + r.getRoomType());
        }

        if (inventory.getAvailability(r.getRoomType()) <= 0) {
            throw new NoAvailabilityException(
                    "No availability for " + r.getRoomType());
        }
    }
}

/* ===========================
   Booking Queue
   =========================== */

class BookingRequestQueue {

    private Queue<Reservation> queue = new LinkedList<>();

    public void addRequest(Reservation r) {
        queue.add(r);
    }

    public Reservation next() {
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

    private List<Reservation> history = new ArrayList<>();

    public void addReservation(Reservation r) {
        history.add(r);
    }

    public List<Reservation> getAllReservations() {
        return Collections.unmodifiableList(history);
    }
}

/* ===========================
   Booking Service
   =========================== */

class BookingService {

    private BookingHistory history;
    private BookingValidator validator;
    private int roomCounter = 1;

    public BookingService(BookingHistory history, BookingValidator validator) {
        this.history = history;
        this.validator = validator;
    }

    public void processBookings(BookingRequestQueue queue, RoomInventory inventory) {

        while (!queue.isEmpty()) {

            Reservation r = queue.next();

            try {
                validator.validate(r);

                String roomId = r.getRoomType().replace(" ", "").toUpperCase()
                        + "-" + roomCounter++;

                inventory.decrementAvailability(r.getRoomType());

                history.addReservation(r);

                System.out.println("Confirmed: " + r.getReservationId()
                        + " -> " + roomId);

            } catch (Exception e) {
                System.out.println("Booking failed for "
                        + r.getReservationId()
                        + ": " + e.getMessage());
            }
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

        Map<String, Integer> map = new HashMap<>();

        for (Reservation r : history.getAllReservations()) {
            map.put(r.getRoomType(),
                    map.getOrDefault(r.getRoomType(), 0) + 1);
        }

        System.out.println("Total bookings: "
                + history.getAllReservations().size());

        for (String k : map.keySet())
            System.out.println(k + ": " + map.get(k));
    }
}