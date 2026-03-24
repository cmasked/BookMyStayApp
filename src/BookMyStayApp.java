import java.io.*;
import java.util.*;

/**
 * BookMyStayApp v7.0
 * Demonstrates file-based persistence and system recovery.
 */

public class BookMyStayApp {

    public static void main(String[] args) {

        PersistenceService persistence = new PersistenceService();

        // Try to restore previous state
        SystemState state = persistence.loadState();

        RoomInventory inventory;
        BookingHistory history;

        if (state != null) {
            System.out.println("System state restored from file.");
            inventory = state.inventory;
            history = state.history;
        } else {
            System.out.println("No previous state found. Starting fresh.");
            inventory = new RoomInventory();
            history = new BookingHistory();
        }

        List<Room> rooms = Arrays.asList(
                new SingleRoom(),
                new DoubleRoom(),
                new SuiteRoom()
        );

        BookingValidator validator = new BookingValidator(rooms, inventory);
        BookingService bookingService = new BookingService(history, validator);

        BookingRequestQueue queue = new BookingRequestQueue();

        queue.addRequest(new Reservation("Alice", "Single Room"));
        queue.addRequest(new Reservation("Bob", "Double Room"));

        bookingService.processBookings(queue, inventory);

        // Save state before exit
        persistence.saveState(new SystemState(inventory, history));

        System.out.println("System state saved. Application exiting.");
    }
}

/* ===========================
   Serializable Wrapper
   =========================== */

class SystemState implements Serializable {
    RoomInventory inventory;
    BookingHistory history;

    public SystemState(RoomInventory inventory, BookingHistory history) {
        this.inventory = inventory;
        this.history = history;
    }
}

/* ===========================
   Persistence Service
   =========================== */

class PersistenceService {

    private static final String FILE_NAME = "hotel_state.dat";

    public void saveState(SystemState state) {

        try (ObjectOutputStream out =
                     new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {

            out.writeObject(state);
            System.out.println("State saved to file.");

        } catch (IOException e) {
            System.out.println("Failed to save state: " + e.getMessage());
        }
    }

    public SystemState loadState() {

        try (ObjectInputStream in =
                     new ObjectInputStream(new FileInputStream(FILE_NAME))) {

            return (SystemState) in.readObject();

        } catch (FileNotFoundException e) {
            return null; // first run
        } catch (Exception e) {
            System.out.println("State file corrupted. Starting fresh.");
            return null;
        }
    }
}

/* ===========================
   Reservation
   =========================== */

class Reservation implements Serializable {

    private static int counter = 1;

    private String reservationId;
    private String guestName;
    private String roomType;
    private boolean cancelled;

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

class RoomInventory implements Serializable {

    private Map<String, Integer> availability = new HashMap<>();

    public RoomInventory() {
        availability.put("Single Room", 2);
        availability.put("Double Room", 2);
        availability.put("Suite Room", 1);
    }

    public int getAvailability(String type) {
        return availability.getOrDefault(type, 0);
    }

    public void decrementAvailability(String type) {
        availability.put(type, getAvailability(type) - 1);
    }
}

/* ===========================
   Booking History
   =========================== */

class BookingHistory implements Serializable {

    private List<Reservation> list = new ArrayList<>();

    public void addReservation(Reservation r) {
        list.add(r);
    }

    public List<Reservation> getAllReservations() {
        return list;
    }
}

/* ===========================
   Booking Service
   =========================== */

class BookingService {

    private BookingHistory history;
    private BookingValidator validator;

    public BookingService(BookingHistory history, BookingValidator validator) {
        this.history = history;
        this.validator = validator;
    }

    public void processBookings(BookingRequestQueue queue, RoomInventory inventory) {

        int counter = 1;

        while (!queue.isEmpty()) {

            Reservation r = queue.next();

            try {
                validator.validate(r);

                inventory.decrementAvailability(r.getRoomType());
                history.addReservation(r);

                System.out.println("Confirmed: " + r.getReservationId());

            } catch (Exception e) {
                System.out.println("Booking failed: " + e.getMessage());
            }
        }
    }
}

/* ===========================
   Validator
   =========================== */

class BookingValidator {

    private Set<String> validTypes = new HashSet<>();
    private RoomInventory inventory;

    public BookingValidator(List<Room> rooms, RoomInventory inventory) {
        for (Room r : rooms) validTypes.add(r.getRoomType());
        this.inventory = inventory;
    }

    public void validate(Reservation r) throws Exception {

        if (r.getGuestName() == null || r.getGuestName().isEmpty())
            throw new Exception("Guest name empty");

        if (!validTypes.contains(r.getRoomType()))
            throw new Exception("Invalid room type");

        if (inventory.getAvailability(r.getRoomType()) <= 0)
            throw new Exception("No availability");
    }
}

/* ===========================
   Queue
   =========================== */

class BookingRequestQueue {

    private Queue<Reservation> queue = new LinkedList<>();

    public void addRequest(Reservation r) { queue.add(r); }

    public Reservation next() { return queue.poll(); }

    public boolean isEmpty() { return queue.isEmpty(); }
}

/* ===========================
   Room Model
   =========================== */

abstract class Room {
    protected String roomType;
    public Room(String type) { roomType = type; }
    public String getRoomType() { return roomType; }
}

class SingleRoom extends Room { public SingleRoom() { super("Single Room"); } }
class DoubleRoom extends Room { public DoubleRoom() { super("Double Room"); } }
class SuiteRoom extends Room { public SuiteRoom() { super("Suite Room"); } }