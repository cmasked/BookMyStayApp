import java.util.*;

/**
 * BookMyStayApp v6.0
 * Demonstrates concurrent booking with thread safety.
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

        BookingRequestQueue sharedQueue = new BookingRequestQueue();

        // Simulate many users submitting at once
        sharedQueue.addRequest(new Reservation("Alice", "Single Room"));
        sharedQueue.addRequest(new Reservation("Bob", "Single Room"));
        sharedQueue.addRequest(new Reservation("Charlie", "Single Room"));
        sharedQueue.addRequest(new Reservation("David", "Double Room"));
        sharedQueue.addRequest(new Reservation("Eve", "Suite Room"));

        ConcurrentBookingProcessor processor =
                new ConcurrentBookingProcessor(sharedQueue, inventory, history, validator);

        processor.startProcessing();

        // Wait before reporting
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        BookingReportService report = new BookingReportService();
        report.displayAllBookings(history);
    }
}

/* ===========================
   Concurrent Processor
   =========================== */

class ConcurrentBookingProcessor {

    private BookingRequestQueue queue;
    private RoomInventory inventory;
    private BookingHistory history;
    private BookingValidator validator;

    public ConcurrentBookingProcessor(
            BookingRequestQueue queue,
            RoomInventory inventory,
            BookingHistory history,
            BookingValidator validator) {

        this.queue = queue;
        this.inventory = inventory;
        this.history = history;
        this.validator = validator;
    }

    public void startProcessing() {

        for (int i = 0; i < 3; i++) {
            Thread t = new Thread(new BookingWorker(), "Worker-" + i);
            t.start();
        }
    }

    private class BookingWorker implements Runnable {

        public void run() {

            while (true) {

                Reservation r;

                synchronized (queue) {
                    if (queue.isEmpty()) break;
                    r = queue.next();
                }

                if (r == null) break;

                try {
                    validator.validate(r);

                    String roomId;
                    synchronized (inventory) {

                        if (inventory.getAvailability(r.getRoomType()) <= 0)
                            throw new Exception("No availability");

                        roomId = r.getRoomType()
                                .replace(" ", "").toUpperCase()
                                + "-" + UUID.randomUUID();

                        inventory.decrementAvailability(r.getRoomType());
                    }

                    r.setAllocatedRoomId(roomId);

                    synchronized (history) {
                        history.addReservation(r);
                    }

                    System.out.println(Thread.currentThread().getName()
                            + " confirmed " + r.getReservationId()
                            + " -> " + roomId);

                } catch (Exception e) {
                    System.out.println(Thread.currentThread().getName()
                            + " failed: " + e.getMessage());
                }
            }
        }
    }
}

/* ===========================
   Inventory (Thread Safe)
   =========================== */

class RoomInventory {

    private Map<String, Integer> availability = new HashMap<>();

    public RoomInventory() {
        availability.put("Single Room", 2);
        availability.put("Double Room", 2);
        availability.put("Suite Room", 1);
    }

    public synchronized int getAvailability(String type) {
        return availability.getOrDefault(type, 0);
    }

    public synchronized void decrementAvailability(String type) {
        availability.put(type, getAvailability(type) - 1);
    }
}

/* ===========================
   Queue
   =========================== */

class BookingRequestQueue {

    private Queue<Reservation> queue = new LinkedList<>();

    public synchronized void addRequest(Reservation r) {
        queue.add(r);
    }

    public synchronized Reservation next() {
        return queue.poll();
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }
}

/* ===========================
   Rest of System (unchanged)
   =========================== */

class Reservation {
    private static int counter = 1;
    private String id;
    private String guest;
    private String type;
    private String roomId;

    public Reservation(String guest, String type) {
        this.id = "RES-" + counter++;
        this.guest = guest;
        this.type = type;
    }

    public String getReservationId() { return id; }
    public String getGuestName() { return guest; }
    public String getRoomType() { return type; }
    public void setAllocatedRoomId(String id) { this.roomId = id; }
}

class BookingHistory {
    private List<Reservation> list = new ArrayList<>();
    public synchronized void addReservation(Reservation r) { list.add(r); }
    public List<Reservation> getAllReservations() { return list; }
}

class BookingValidator {
    private Set<String> validTypes = new HashSet<>();
    private RoomInventory inventory;

    public BookingValidator(List<Room> rooms, RoomInventory inventory) {
        for (Room r : rooms) validTypes.add(r.getRoomType());
        this.inventory = inventory;
    }

    public void validate(Reservation r) throws Exception {
        if (!validTypes.contains(r.getRoomType()))
            throw new Exception("Invalid room type");
    }
}

abstract class Room {
    protected String roomType;
    public Room(String type) { roomType = type; }
    public String getRoomType() { return roomType; }
}

class SingleRoom extends Room { public SingleRoom() { super("Single Room"); } }
class DoubleRoom extends Room { public DoubleRoom() { super("Double Room"); } }
class SuiteRoom extends Room { public SuiteRoom() { super("Suite Room"); } }

class BookingReportService {
    public void displayAllBookings(BookingHistory history) {
        for (Reservation r : history.getAllReservations()) {
            System.out.println(r.getReservationId()
                    + " | " + r.getGuestName()
                    + " | " + r.getRoomType());
        }
    }
}