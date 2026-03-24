import java.util.*;

/**
 * BookMyStayApp v5.0
 * Complete lifecycle:
 * Booking → Validation → Add-ons → History → Reporting → Cancellation
 */

public class BookMyStayApp {

    public static void main(String[] args) {

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

        queue.addRequest(new Reservation("Alice", "Single Room"));
        queue.addRequest(new Reservation("Bob", "Double Room"));

        bookingService.processBookings(queue, inventory);

        // Cancellation
        CancellationService cancellationService =
                new CancellationService(history, inventory);

        cancellationService.cancelReservation("RES-1");
        cancellationService.cancelReservation("RES-1"); // duplicate attempt

        // Reporting after cancellation
        BookingReportService report = new BookingReportService();
        report.displayAllBookings(history);
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
    private String allocatedRoomId;
    private boolean cancelled;

    public Reservation(String guestName, String roomType) {
        this.reservationId = "RES-" + counter++;
        this.guestName = guestName;
        this.roomType = roomType;
        this.cancelled = false;
    }

    public String getReservationId() { return reservationId; }
    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }

    public void setAllocatedRoomId(String id) {
        this.allocatedRoomId = id;
    }

    public String getAllocatedRoomId() { return allocatedRoomId; }

    public boolean isCancelled() { return cancelled; }

    public void cancel() { this.cancelled = true; }
}

/* ===========================
   Cancellation Service
   =========================== */

class CancellationService {

    private BookingHistory history;
    private RoomInventory inventory;
    private Stack<String> releasedRoomIds = new Stack<>();

    public CancellationService(BookingHistory history, RoomInventory inventory) {
        this.history = history;
        this.inventory = inventory;
    }

    public void cancelReservation(String reservationId) {

        Reservation r = history.findReservation(reservationId);

        if (r == null) {
            System.out.println("Cancellation failed: Reservation not found.");
            return;
        }

        if (r.isCancelled()) {
            System.out.println("Cancellation failed: Already cancelled.");
            return;
        }

        // rollback
        releasedRoomIds.push(r.getAllocatedRoomId());
        inventory.incrementAvailability(r.getRoomType());
        r.cancel();

        System.out.println("Reservation cancelled: " + reservationId);
    }
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

    public int getAvailability(String type) {
        return availability.getOrDefault(type, 0);
    }

    public void decrementAvailability(String type) {
        availability.put(type, getAvailability(type) - 1);
    }

    public void incrementAvailability(String type) {
        availability.put(type, getAvailability(type) + 1);
    }
}

/* ===========================
   Booking History
   =========================== */

class BookingHistory {

    private List<Reservation> list = new ArrayList<>();

    public void addReservation(Reservation r) {
        list.add(r);
    }

    public Reservation findReservation(String id) {
        for (Reservation r : list)
            if (r.getReservationId().equals(id))
                return r;
        return null;
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

                String roomId = r.getRoomType()
                        .replace(" ", "").toUpperCase()
                        + "-" + roomCounter++;

                inventory.decrementAvailability(r.getRoomType());

                r.setAllocatedRoomId(roomId);
                history.addReservation(r);

                System.out.println("Confirmed: " + r.getReservationId()
                        + " -> " + roomId);

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

    private Set<String> validRoomTypes = new HashSet<>();
    private RoomInventory inventory;

    public BookingValidator(List<Room> rooms, RoomInventory inventory) {
        for (Room r : rooms) validRoomTypes.add(r.getRoomType());
        this.inventory = inventory;
    }

    public void validate(Reservation r) throws Exception {

        if (r.getGuestName() == null || r.getGuestName().isEmpty())
            throw new Exception("Guest name empty.");

        if (!validRoomTypes.contains(r.getRoomType()))
            throw new Exception("Invalid room type.");

        if (inventory.getAvailability(r.getRoomType()) <= 0)
            throw new Exception("No rooms available.");
    }
}

/* ===========================
   Supporting Classes
   =========================== */

abstract class Room {
    protected String roomType;
    public Room(String type) { roomType = type; }
    public String getRoomType() { return roomType; }
}

class SingleRoom extends Room { public SingleRoom() { super("Single Room"); } }
class DoubleRoom extends Room { public DoubleRoom() { super("Double Room"); } }
class SuiteRoom extends Room { public SuiteRoom() { super("Suite Room"); } }

class BookingRequestQueue {

    private Queue<Reservation> queue = new LinkedList<>();

    public void addRequest(Reservation r) { queue.add(r); }

    public Reservation next() { return queue.poll(); }

    public boolean isEmpty() { return queue.isEmpty(); }
}

/* ===========================
   Reporting
   =========================== */

class BookingReportService {

    public void displayAllBookings(BookingHistory history) {

        for (Reservation r : history.getAllReservations()) {

            System.out.println(
                    r.getReservationId() + " | "
                            + r.getGuestName() + " | "
                            + r.getRoomType() + " | "
                            + (r.isCancelled() ? "CANCELLED" : "ACTIVE")
            );
        }
    }
}