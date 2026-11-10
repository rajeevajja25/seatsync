/*
 * ============================================================================================
 *  ADVANCED JAVA PROGRAMMING (CSE 307)  -  Syllabus concepts, shown on the Transport Booking idea
 *  BACKEND ONLY: no JavaFX / no GUI code in this file. Plain console program.
 * ============================================================================================
 *  Run (Java 17+, single file, no Maven needed):
 *        java -cp sqlite-jdbc-3.41.2.2.jar AdvancedJavaConcepts.java
 *  (the jar is only needed for the JDBC part of Unit 4; everything else runs without it)
 *
 *  Section map                                   Syllabus unit
 *  -------------------------------------------   ---------------------------------------------
 *  unit1()  Overview of Java & Inheritance       Unit 1
 *  unit2()  Polymorphism & Exception handling    Unit 2
 *  unit3()  Multithreading, I/O, Event handling  Unit 3  (CLO 2, CLO 3)
 *  unit4()  JDBC (SQLite) & Collections          Unit 4  (+ streams, CLO 2)
 *  unit5()  Generics                             Unit 5  (JavaFX controls of Unit 5 are in the frontend project)
 *  project() The real project algorithm          ReentrantLock per seat + timed auto-release
 *
 *  "Packages / access specifiers" (Unit 1): the Maven project uses package com.transport;
 *  the four access levels (private / default / protected / public) are marked on class Seat below.
 * ============================================================================================
 */
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AdvancedJavaConcepts {

    public static void main(String[] args) throws Exception {
        banner("UNIT 1 - Overview of Java and Inheritance");      unit1();
        banner("UNIT 2 - Polymorphism and Exception handling");   unit2();
        banner("UNIT 3 - Multithreading, I/O and Event handling"); unit3();
        banner("UNIT 4 - JDBC (SQLite) and Collections");         unit4();
        banner("UNIT 5 - Generics");                              unit5();
        banner("PROJECT - ReentrantLock + timed auto-release");   project();
    }

    static void p(String s) { System.out.println(s); }
    static void banner(String s) { p("\n=================== " + s + " ==================="); }

    // =========================================================================================
    // UNIT 1 : data types, variables, arrays, operators, expressions, control statements,
    //          classes, objects, constructors, this, static, final, inheritance, abstract class,
    //          interface, multilevel hierarchy, super, final with inheritance, access specifiers
    // =========================================================================================
    static void unit1() {
        // ---- data types and variables
        int totalSeats = 40;  byte rows = 8;  short cols = 5;  long stamp = System.currentTimeMillis();
        float rating = 4.9f;  double farePerSeat = 450.0;  char rowLabel = 'A';  boolean available = true;
        final int SERVICE_FEE = 30;                       // final variable = constant
        p("types: " + totalSeats + " seats, " + rows + "x" + cols + " grid, rating " + rating + ", row " + rowLabel);

        // ---- arrays: one-dimensional, two-dimensional, jagged
        int[] fares = {450, 500, 650};
        String[][] layout = new String[rows][cols];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) layout[r][c] = "S" + (r * cols + c + 1);
        int[][] jagged = {{1, 2}, {1, 2, 3}, {1}};
        p("layout[7][4] = " + layout[7][4] + ", fares.length = " + fares.length + ", jagged rows = " + jagged.length);

        // ---- operators and expressions
        int wanted = 3;
        double total = wanted * farePerSeat + SERVICE_FEE;            // arithmetic
        boolean group = wanted >= 3 && available;                     // relational + logical
        int discount = group ? 10 : 0;                                // ternary
        int bits = 0b1010 & 0b0110;                                   // bitwise AND
        wanted += 2; wanted++;                                        // compound assignment, unary
        p("total=" + total + " group=" + group + " discount=" + discount + "% bits=" + bits + " wanted=" + wanted);

        // ---- control statements: if/else, switch, for, for-each, while, do-while, break, continue
        if (total > 1000) p("expensive booking"); else p("normal booking");
        switch (rowLabel) {
            case 'A' -> p("row A = front of the coach");
            case 'H' -> p("row H = back of the coach");
            default -> p("middle row");
        }
        int sum = 0;
        for (int f : fares) sum += f;                                 // enhanced for
        int i = 0;
        while (true) { i++; if (i % 2 == 0) continue; if (i > 5) break; sum += i; }
        do { sum--; } while (sum > 1600);
        p("loops done, sum=" + sum);

        // ---- classes, objects, constructors (overloaded), this, static, final
        Seat s1 = new Seat();                       // Seat()           -> this("S0")
        Seat s2 = new Seat("S13");                  // Seat(String)     -> this(id, BASE_PRICE)
        Seat s3 = new Seat("S14", 500);             // Seat(String,double)
        p(s1 + " " + s2 + " " + s3 + "  | static counter Seat.getSeatCount() = " + Seat.getSeatCount());
        p("equals() compares by id: " + s2.equals(new Seat("S13")) + ", hashCode equal: " + (s2.hashCode() == new Seat("S13").hashCode()));

        // ---- inheritance: abstract class, interface, multilevel hierarchy (Vehicle <- Bus <- AcSleeperBus)
        Vehicle v = new AcSleeperBus("AP39-AC-01");
        p(v.describe());                            // super.describe() chain
        p("final method (cannot be overridden): regNo = " + v.getRegNo());
        p("interface default method: " + ((Bookable) v).bookingPolicy() + " | static: " + Bookable.currency());
        p("instanceof chain: Bus? " + (v instanceof Bus) + ", AcSleeperBus? " + (v instanceof AcSleeperBus));
    }

    // =========================================================================================
    // UNIT 2 : compile-time polymorphism (overloading), run-time polymorphism (overriding,
    //          dynamic method dispatch), exception handling (try/catch/finally, multiple catch,
    //          built-in + user-defined, nested try, throw, throws, uncaught), Object class
    // =========================================================================================
    static void unit2() throws Exception {
        // ---- compile-time polymorphism: the COMPILER picks the method from the argument types
        FareCalculator fc = new FareCalculator();
        p("overloading: fare(2)=" + fc.fare(2) + " | fare(2,true)=" + fc.fare(2, true)
                + " | fare(\"Airport\",2)=" + fc.fare("Airport", 2) + " | fare(12.5km)=" + fc.fare(12.5));

        // ---- run-time polymorphism: the JVM picks the method from the OBJECT's real class
        Vehicle[] fleet = { new Bus("AP39-01", 40), new AcSleeperBus("AP39-02") };
        for (Vehicle v : fleet)                     // same call, different behaviour = dynamic method dispatch
            p("dispatch: " + v.getClass().getSimpleName() + " -> " + v.describe() + " fare=" + v.farePerSeat());

        // ---- try / catch / multiple catch / finally with built-in exceptions
        for (String in : new String[]{"2", "abc", "1", "5"}) {
            try {
                int n = Integer.parseInt(in);                   // NumberFormatException
                int[] row = new int[3];
                row[n] = 100 / (n - 1);                         // ArithmeticException (n=1), ArrayIndexOutOfBounds (n=5)
                p("input " + in + " ok -> " + row[n]);
            } catch (NumberFormatException e) {
                p("input " + in + " -> not a number (" + e.getMessage() + ")");
            } catch (ArithmeticException | ArrayIndexOutOfBoundsException e) {   // multi-catch
                p("input " + in + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
            } finally {
                p("   finally always runs (cleanup) for input " + in);
            }
        }

        // ---- exception class hierarchy: walk up to Object (the root of every class)
        Throwable t = new ArrayIndexOutOfBoundsException();
        StringBuilder chain = new StringBuilder();
        for (Class<?> c = t.getClass(); c != null; c = c.getSuperclass()) chain.append(c.getSimpleName()).append(" -> ");
        p("hierarchy: " + chain + "end");

        // ---- user-defined exceptions + throw + throws + nested try
        Bus tiny = new Bus("TINY-1", 2);
        try {                                                   // outer try
            try {                                               // nested try
                tiny.book("Asha"); tiny.book("Ravi");
                tiny.book("Kiran");                             // 3rd call: no seat left -> throw
            } finally {
                p("inner finally (runs even though exception is propagating)");
            }
        } catch (SeatUnavailableException e) {                  // user-defined CHECKED exception
            p("caught checked: " + e.getMessage());
        }
        try {
            checkName("   ");                                   // user-defined UNCHECKED exception
        } catch (InvalidPassengerException e) {
            p("caught unchecked: " + e.getMessage());
        }

        // ---- uncaught exception: nobody catches it, so the thread dies; a handler lets us observe it
        Thread bad = new Thread(() -> { throw new IllegalStateException("nobody caught me"); }, "bad-thread");
        bad.setUncaughtExceptionHandler((th, ex) -> p("uncaught in " + th.getName() + ": " + ex.getMessage()));
        bad.start(); bad.join();
    }

    /** Starts 4 threads at the SAME instant (latch); each runs the action 1,000,000 times; waits for all. */
    static void runWorkers(Runnable action) throws InterruptedException {
        CountDownLatch start = new CountDownLatch(1);
        Thread[] workers = new Thread[4];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Thread(() -> {
                try { start.await(); } catch (InterruptedException e) { return; }
                for (int k = 0; k < 1_000_000; k++) action.run();
            });
            workers[i].start();
        }
        start.countDown();
        for (Thread w : workers) w.join();
    }

    /** 'throws' declares what a method may propagate; 'throw' actually raises it. */
    static void checkName(String name) throws InvalidPassengerException {
        if (name == null || name.isBlank()) throw new InvalidPassengerException("passenger name is required");
    }

    // =========================================================================================
    // UNIT 3 : thread model, life cycle, Thread class vs Runnable, priorities, synchronization,
    //          inter-thread communication, byte streams, delegation event model, adapter classes
    // =========================================================================================
    static void unit3() throws Exception {
        // ---- life cycle: NEW -> RUNNABLE -> TIMED_WAITING -> TERMINATED
        Thread life = new Thread(() -> { try { Thread.sleep(200); } catch (InterruptedException ignored) { } }, "life");
        p("state after creation : " + life.getState());
        life.start();                       p("state after start()  : " + life.getState());
        Thread.sleep(50);                   p("state while sleeping : " + life.getState());
        life.join();                        p("state after finishing: " + life.getState());

        // ---- creating threads: (1) extends Thread  (2) implements Runnable  (3) lambda
        Thread a = new HelloThread("thread-A");
        Thread b = new Thread(new HelloTask(), "thread-B");
        Thread c = new Thread(() -> p("  [thread-C] running via lambda"), "thread-C");
        a.start(); a.join(); b.start(); b.join(); c.start(); c.join();

        // ---- priorities (1..10); only a hint to the scheduler, never a guarantee of order
        Thread low = new Thread(() -> { }), high = new Thread(() -> { });
        low.setPriority(Thread.MIN_PRIORITY); high.setPriority(Thread.MAX_PRIORITY);
        p("priorities: low=" + low.getPriority() + " normal=" + Thread.NORM_PRIORITY + " high=" + high.getPriority());

        // ---- synchronization: the same work with and without a lock (run separately, 4 threads x 1,000,000)
        UnsafeCounter unsafe = new UnsafeCounter();
        runWorkers(unsafe::inc);
        SafeCounter safe = new SafeCounter();
        runWorkers(safe::inc);
        p("race condition: unsynchronized=" + unsafe.n + " (expected 4000000 -> updates were LOST)"
                + "  |  synchronized=" + safe.get() + " (always 4000000)");

        // ---- inter-thread communication: wait() / notifyAll() (producer-consumer)
        PaymentQueue queue = new PaymentQueue();
        Thread producer = new Thread(() -> {
            try { for (int i = 1; i <= 6; i++) { queue.submit("PAY-S" + i); } } catch (InterruptedException ignored) { }
        }, "producer");
        Thread consumer = new Thread(() -> {
            try { for (int i = 1; i <= 6; i++) { p("  consumer processed " + queue.take()); } } catch (InterruptedException ignored) { }
        }, "consumer");
        producer.start(); consumer.start(); producer.join(); consumer.join();

        // ---- Input/Output: byte streams (InputStream / OutputStream / FileInputStream)
        File f = File.createTempFile("receipt", ".dat");
        f.deleteOnExit();
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)))) {
            out.writeUTF("S13"); out.writeUTF("Asha"); out.writeInt(450);    // bytes in a defined format
        }
        try (DataInputStream in = new DataInputStream(new FileInputStream(f))) {
            p("read back from file: " + in.readUTF() + ", " + in.readUTF() + ", Rs " + in.readInt() + " (" + f.length() + " bytes)");
        }
        try (InputStream raw = new FileInputStream(f)) {
            int count = 0; while (raw.read() != -1) count++;                  // one byte at a time
            p("raw FileInputStream counted " + count + " bytes");
        }

        // ---- Event handling: DELEGATION event model (source -> event object -> listener), adapter class
        BookingEngine engine = new BookingEngine();                           // event SOURCE
        engine.addBookingListener(new AuditLogger());                         // listener implementing ALL methods
        engine.addBookingListener(new BookingAdapter() {                      // adapter: override only what we need
            @Override public void onBooked(BookingEvent e) { p("  [adapter] ticket SMS sent for " + e.getSeatId()); }
        });
        engine.reserve("S13", "Asha");
        engine.pay("S13", "Asha");
        engine.release("S14", "Ravi");
        p("(same pattern as AWT ActionListener / KeyListener / KeyAdapter - see frontend AwtEventDemo.java)");
    }

    // =========================================================================================
    // UNIT 4 : JDBC with SQLite (create DB, connect, create table, CRUD, transactions)
    //          Collections: List, Set, Map, ArrayList, LinkedList, Vector, HashSet, TreeSet,
    //          Hashtable, HashMap, Iterator;  plus streams (CLO 2)
    // =========================================================================================
    static void unit4() {
        String url = "jdbc:sqlite:transport_demo.db";            // creates the file in the working directory
        try {
            Class.forName("org.sqlite.JDBC");                    // step 1: load/register the JDBC driver class
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            p("JDBC skipped: driver jar not on the classpath\n   -> run with:  java -cp sqlite-jdbc-3.41.2.2.jar AdvancedJavaConcepts.java");
            url = null;
        }
        if (url != null) jdbcDemo(url);
        collectionsDemo();
    }

    /** JDBC steps: driver -> Connection -> Statement/PreparedStatement -> ResultSet -> close (try-with-resources). */
    static void jdbcDemo(String url) {
        try (Connection con = DriverManager.getConnection(url)) {   // step 2: connect (creates the DB file if missing)
            con.setAutoCommit(false);                            // we control the transaction
            try (Statement st = con.createStatement()) {
                st.executeUpdate("DROP TABLE IF EXISTS bookings");
                st.executeUpdate("CREATE TABLE bookings (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "seat_id TEXT NOT NULL, passenger_name TEXT NOT NULL, status TEXT NOT NULL, "
                        + "booking_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            }
            // ---- C: Create
            String insert = "INSERT INTO bookings (seat_id, passenger_name, status) VALUES (?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(insert)) {
                String[][] rows = {{"S1", "Asha", "RESERVED"}, {"S2", "Ravi", "RESERVED"}, {"S3", "Kiran", "BOOKED"}};
                for (String[] r : rows) { ps.setString(1, r[0]); ps.setString(2, r[1]); ps.setString(3, r[2]); ps.executeUpdate(); }
            }
            con.commit();
            // ---- U: Update
            try (PreparedStatement ps = con.prepareStatement("UPDATE bookings SET status = ? WHERE seat_id = ?")) {
                ps.setString(1, "BOOKED"); ps.setString(2, "S1"); p("updated rows: " + ps.executeUpdate());
            }
            // ---- D: Delete
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM bookings WHERE seat_id = ?")) {
                ps.setString(1, "S2"); p("deleted rows: " + ps.executeUpdate());
            }
            con.commit();
            // ---- R: Retrieve
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id, seat_id, passenger_name, status FROM bookings ORDER BY id")) {
                while (rs.next())
                    p("  row " + rs.getInt("id") + " | " + rs.getString("seat_id") + " | " + rs.getString("passenger_name") + " | " + rs.getString("status"));
            }
            // ---- transaction: a failing insert is rolled back, nothing is half-saved
            try (PreparedStatement ps = con.prepareStatement(insert)) {
                ps.setString(1, "S9"); ps.setString(2, "Mina"); ps.setString(3, "RESERVED"); ps.executeUpdate();
                ps.setString(1, "S10"); ps.setString(2, null); ps.setString(3, "RESERVED"); ps.executeUpdate(); // NOT NULL violated
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM bookings")) {
                    rs.next(); p("rollback after error -> still " + rs.getInt(1) + " rows (S9 was NOT saved)");
                }
            }
        } catch (SQLException e) {
            p("JDBC error: " + e.getMessage());
        }
    }

    static void collectionsDemo() {
        // ---- Collections framework
        List<String> arrayList = new ArrayList<>(List.of("S1", "S2", "S3", "S4"));        // fast random access
        LinkedList<String> waitlist = new LinkedList<>();                                  // fast add/remove at ends
        waitlist.addLast("Asha"); waitlist.addLast("Ravi"); waitlist.addFirst("VIP-Kiran");
        String next = waitlist.removeFirst();
        Vector<Integer> vector = new Vector<>(List.of(1, 2, 3));                           // legacy, synchronized list
        p("ArrayList=" + arrayList + " | LinkedList waitlist next=" + next + " left=" + waitlist + " | Vector=" + vector);

        Set<String> hashSet = new HashSet<>(List.of("S5", "S1", "S5", "S3"));              // no duplicates, no order
        TreeSet<Integer> treeSet = new TreeSet<>(List.of(30, 5, 12, 5));                   // no duplicates, SORTED
        p("HashSet(dups removed)=" + hashSet + " | TreeSet(sorted)=" + treeSet + " first=" + treeSet.first() + " <20:" + treeSet.headSet(20));

        Hashtable<String, String> table = new Hashtable<>();                               // legacy, synchronized, no null
        table.put("S1", "Asha");
        HashMap<String, Seat> map = new HashMap<>();                                       // key -> value, 1 null key allowed
        for (int i = 1; i <= 5; i++) map.put("S" + i, new Seat("S" + i));
        TreeMap<String, String> sortedMap = new TreeMap<>(Map.of("S3", "c", "S1", "a", "S2", "b")); // sorted by key
        p("Hashtable=" + table + " | HashMap size=" + map.size() + " get(S2)=" + map.get("S2") + " | TreeMap=" + sortedMap);

        Iterator<String> it = arrayList.iterator();                                        // safe removal while looping
        while (it.hasNext()) if (it.next().equals("S2")) it.remove();
        p("after Iterator.remove(S2): " + arrayList);

        // ---- Streams + lambdas (CLO 2)
        List<Seat> seats = IntStream.rangeClosed(1, 40).mapToObj(n -> new Seat("S" + n)).collect(Collectors.toList());
        for (int n : new int[]{2, 5, 6, 10, 13, 14}) seats.get(n - 1).status = n < 10 ? "BOOKED" : "RESERVED";
        long free = seats.stream().filter(s -> s.status.equals("AVAILABLE")).count();
        Map<String, Long> byStatus = seats.stream().collect(Collectors.groupingBy(s -> s.status, TreeMap::new, Collectors.counting()));
        List<String> reservedIds = seats.stream().filter(s -> s.status.equals("RESERVED")).map(Seat::getId).sorted().toList();
        double revenue = seats.stream().filter(s -> s.status.equals("BOOKED")).mapToDouble(s -> s.price).sum();
        p("streams: free=" + free + " byStatus=" + byStatus + " reserved=" + reservedIds + " revenue=Rs " + revenue);
    }

    // =========================================================================================
    // UNIT 5 : generic class, two type parameters, general form, bounded types, wildcard
    //          arguments, generic method, generic interface, generic class hierarchies
    // =========================================================================================
    static void unit5() {
        Pair<String, Integer> seatFare = new Pair<>("S13", 450);                 // two type parameters <K, V>
        NamedPair<Double> rating = new NamedPair<>("rating", 4.9);               // generic class hierarchy
        p("Pair=" + seatFare + " | NamedPair=" + rating + " | swapped=" + seatFare.swap());

        Ticket<AcSleeperBus> ticket = new Ticket<>(new AcSleeperBus("AP39-AC-09"));   // bounded type T extends Vehicle
        p("bounded generic: " + ticket.totalFare(3));

        p("generic method max: " + Util.max(List.of(3, 9, 4)) + ", " + Util.max(List.of("S2", "S7", "S5")));
        List<Bus> buses = List.of(new Bus("B1", 40), new AcSleeperBus("B2"));
        p("wildcard '? extends Vehicle' total capacity = " + Util.totalCapacity(buses));
        List<Object> sink = new ArrayList<>();
        Util.fill(sink);                                                          // '? super Seat'
        Util.printAll(List.of(1, "two", 3.0));                                   // unbounded wildcard '?'

        Repository<Seat, String> repo = new SeatRepository();                     // generic interface
        repo.save(new Seat("S1")); repo.save(new Seat("S2"));
        p("generic interface Repository<Seat,String>: findById(S2)=" + repo.findById("S2").orElse(null) + " all=" + repo.findAll());
    }

    // =========================================================================================
    // PROJECT : the real algorithm of the Transport Booking System (console version)
    //   - ConcurrentHashMap<String, LockedSeat>  : the 40-seat map, one ReentrantLock per seat
    //   - ConcurrentLinkedQueue<String>          : reservation queue
    //   - daemon thread                          : releases unpaid seats after a timeout
    // =========================================================================================
    static void project() throws Exception {
        final long TIMEOUT_MS = 1500;                                             // 15 s in the app, shortened here
        ConcurrentHashMap<String, LockedSeat> seatMap = new ConcurrentHashMap<>();
        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
        for (int i = 1; i <= 40; i++) seatMap.put("S" + i, new LockedSeat("S" + i));

        // Thread type 2: auto-release monitor (daemon, loops every 200 ms)
        Thread monitor = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                for (String id : queue) {
                    LockedSeat ls = seatMap.get(id);
                    ls.lock.lock();
                    try {
                        if (ls.seat.status.equals("RESERVED") && System.currentTimeMillis() - ls.reservedAt >= TIMEOUT_MS) {
                            ls.seat.status = "AVAILABLE"; ls.seat.passenger = null; queue.remove(id);
                            p("  [monitor] auto-released " + id);
                        }
                    } finally { ls.lock.unlock(); }
                }
                try { Thread.sleep(200); } catch (InterruptedException e) { return; }
            }
        }, "auto-release");
        monitor.setDaemon(true);
        monitor.start();

        // Thread type 1: many users click the SAME seat at the same time
        AtomicInteger winners = new AtomicInteger();
        CountDownLatch go = new CountDownLatch(1);
        Thread[] users = new Thread[10];
        for (int i = 0; i < users.length; i++) {
            final String user = "user-" + (i + 1);
            users[i] = new Thread(() -> {
                try { go.await(); } catch (InterruptedException e) { return; }
                LockedSeat ls = seatMap.get("S13");
                ls.lock.lock();                                                   // acquire this seat's lock
                try {
                    if (!ls.seat.status.equals("AVAILABLE")) return;             // double-check inside the critical section
                    ls.seat.status = "RESERVED"; ls.seat.passenger = user; ls.reservedAt = System.currentTimeMillis();
                    queue.add("S13"); winners.incrementAndGet();
                } finally { ls.lock.unlock(); }
            });
            users[i].start();
        }
        go.countDown();
        for (Thread u : users) u.join();
        p("10 threads raced for S13 -> winners = " + winners.get() + " (always exactly 1)  holder = " + seatMap.get("S13").seat.passenger);

        // second seat is paid in time, so the monitor must NOT release it
        LockedSeat s20 = seatMap.get("S20");
        s20.lock.lock();
        try { s20.seat.status = "RESERVED"; s20.seat.passenger = "Asha"; s20.reservedAt = System.currentTimeMillis(); queue.add("S20"); }
        finally { s20.lock.unlock(); }
        s20.lock.lock();
        try { s20.seat.status = "BOOKED"; queue.remove("S20"); } finally { s20.lock.unlock(); }   // payment confirmed
        Thread.sleep(2200);
        p("after timeout: S13=" + seatMap.get("S13").seat.status + " (released), S20=" + seatMap.get("S20").seat.status + " (paid, kept)");
        monitor.interrupt();
    }
}

// =============================================================================================
//  Supporting classes (kept in the same file so the whole demo is a single runnable source file)
// =============================================================================================

/** Unit 1: class, constructors (overloading + this()), static, final, access specifiers, Object methods. */
class Seat {
    static int seatCount = 0;                       // static: one copy shared by all objects
    static final double BASE_PRICE = 450.0;         // static final = constant
    private final String id;                        // private  : this class only (encapsulation) ; final: set once
    protected String passenger;                     // protected: same package + subclasses
    String status = "AVAILABLE";                    // default  : same package only
    public double price;                            // public   : everywhere

    Seat() { this("S0"); }                          // this(...) calls another constructor
    Seat(String id) { this(id, BASE_PRICE); }
    Seat(String id, double price) { this.id = id; this.price = price; seatCount++; }

    String getId() { return id; }
    static int getSeatCount() { return seatCount; } // static method: no 'this'

    @Override public String toString() { return "Seat[" + id + "," + status + "]"; }
    @Override public boolean equals(Object o) { return o instanceof Seat s && s.id.equals(id); }
    @Override public int hashCode() { return id.hashCode(); }
}

/** Unit 1: interface with abstract, default and static members. */
interface Bookable {
    boolean book(String passenger) throws SeatUnavailableException;
    default String bookingPolicy() { return "pay within the hold time"; }
    static String currency() { return "INR"; }
}

/** Unit 1: abstract class (cannot be instantiated), final method, protected member. */
abstract class Vehicle {
    private final String regNo;
    protected int capacity;
    Vehicle(String regNo, int capacity) { this.regNo = regNo; this.capacity = capacity; }
    final String getRegNo() { return regNo; }       // final method: subclasses cannot override
    abstract double farePerSeat();                  // abstract method: subclasses MUST implement
    String describe() { return "Vehicle " + regNo + " (" + capacity + " seats)"; }
}

/** Unit 1: inheritance (extends) + interface (implements) + super(...). */
class Bus extends Vehicle implements Bookable {
    protected final List<Seat> seats = new ArrayList<>();
    Bus(String regNo, int capacity) {
        super(regNo, capacity);                     // super(...) must be the first statement
        for (int i = 1; i <= capacity; i++) seats.add(new Seat("S" + i));
    }
    @Override double farePerSeat() { return 450; }  // overriding = run-time polymorphism
    @Override String describe() { return "Bus -> " + super.describe(); }
    @Override public boolean book(String passenger) throws SeatUnavailableException {
        for (Seat s : seats) {
            if (s.status.equals("AVAILABLE")) { s.status = "RESERVED"; s.passenger = passenger; return true; }
        }
        throw new SeatUnavailableException("Bus " + getRegNo() + " is full");
    }
}

/** Unit 1: multilevel inheritance  Vehicle <- Bus <- AcSleeperBus. */
class AcSleeperBus extends Bus {
    AcSleeperBus(String regNo) { super(regNo, 40); }
    @Override double farePerSeat() { return 650; }
    @Override String describe() { return "AC Sleeper " + super.describe(); }
}

/** Unit 1: 'final' class cannot be extended. */
final class TicketPrinter { String print(Seat s) { return "TICKET " + s.getId(); } }

/** Unit 2: compile-time polymorphism = method overloading. */
class FareCalculator {
    double fare(int seats) { return seats * 450; }
    double fare(int seats, boolean student) { return fare(seats) * (student ? 0.8 : 1.0); }
    double fare(String destination, int seats) { return (destination.equals("Airport") ? 600 : 450) * seats; }
    double fare(double distanceKm) { return 20 + distanceKm * 12; }
}

/** Unit 2: user-defined exceptions (checked extends Exception, unchecked extends RuntimeException). */
class SeatUnavailableException extends Exception {
    SeatUnavailableException(String message) { super(message); }
}
class InvalidPassengerException extends RuntimeException {
    InvalidPassengerException(String message) { super(message); }
}

// ---------------------------------------------- Unit 3 helpers
class HelloThread extends Thread {                  // way 1: extend Thread, override run()
    HelloThread(String name) { super(name); }
    @Override public void run() { System.out.println("  [" + getName() + "] running via extends Thread"); }
}
class HelloTask implements Runnable {               // way 2: implement Runnable (preferred: class can extend something else)
    @Override public void run() { System.out.println("  [" + Thread.currentThread().getName() + "] running via Runnable"); }
}
/** n++ is really three steps (read, add, write). Another thread can run in between => lost update.
 *  Thread.yield() makes that gap visible on every machine, even a single-core one. */
class UnsafeCounter {
    int n;
    void inc() { int read = n; if (read % 1000 == 0) Thread.yield(); n = read + 1; }
}
class SafeCounter { private int n; synchronized void inc() { n++; } synchronized int get() { return n; } }

/** Inter-thread communication: a bounded buffer using wait()/notifyAll() on the object's monitor. */
class PaymentQueue {
    private final LinkedList<String> items = new LinkedList<>();
    private static final int CAPACITY = 3;
    synchronized void submit(String payment) throws InterruptedException {
        while (items.size() == CAPACITY) wait();    // buffer full -> release the monitor and sleep
        items.addLast(payment);
        System.out.println("  producer queued " + payment);
        notifyAll();                                // wake the consumer
    }
    synchronized String take() throws InterruptedException {
        while (items.isEmpty()) wait();             // nothing to do -> sleep until notified
        String p = items.removeFirst();
        notifyAll();                                // wake the producer
        return p;
    }
}

// ---------------------------------------------- Unit 3: delegation event model
/** The EVENT OBJECT: describes what happened (like java.awt.event.KeyEvent). */
class BookingEvent extends EventObject {
    private final String seatId, passenger;
    BookingEvent(Object source, String seatId, String passenger) { super(source); this.seatId = seatId; this.passenger = passenger; }
    String getSeatId() { return seatId; }
    String getPassenger() { return passenger; }
}
/** The LISTENER INTERFACE: every handler method must be implemented. */
interface BookingListener {
    void onReserved(BookingEvent e);
    void onBooked(BookingEvent e);
    void onReleased(BookingEvent e);
}
/** The ADAPTER CLASS: empty implementations, so a subclass overrides only what it needs. */
abstract class BookingAdapter implements BookingListener {
    @Override public void onReserved(BookingEvent e) { }
    @Override public void onBooked(BookingEvent e) { }
    @Override public void onReleased(BookingEvent e) { }
}
class AuditLogger implements BookingListener {
    @Override public void onReserved(BookingEvent e) { System.out.println("  [audit] " + e.getSeatId() + " reserved by " + e.getPassenger()); }
    @Override public void onBooked(BookingEvent e) { System.out.println("  [audit] " + e.getSeatId() + " booked by " + e.getPassenger()); }
    @Override public void onReleased(BookingEvent e) { System.out.println("  [audit] " + e.getSeatId() + " released"); }
}
/** The EVENT SOURCE: keeps a list of registered listeners and delegates the handling to them. */
class BookingEngine {
    private final List<BookingListener> listeners = new CopyOnWriteArrayList<>();
    void addBookingListener(BookingListener l) { listeners.add(l); }
    void removeBookingListener(BookingListener l) { listeners.remove(l); }
    void reserve(String seat, String who) { BookingEvent e = new BookingEvent(this, seat, who); listeners.forEach(l -> l.onReserved(e)); }
    void pay(String seat, String who) { BookingEvent e = new BookingEvent(this, seat, who); listeners.forEach(l -> l.onBooked(e)); }
    void release(String seat, String who) { BookingEvent e = new BookingEvent(this, seat, who); listeners.forEach(l -> l.onReleased(e)); }
}

// ---------------------------------------------- Unit 5: generics
/** Generic class with TWO type parameters. General form:  class Name<T1, T2, ...> { ... } */
class Pair<K, V> {
    private final K key; private final V value;
    Pair(K key, V value) { this.key = key; this.value = value; }
    K getKey() { return key; }
    V getValue() { return value; }
    Pair<V, K> swap() { return new Pair<>(value, key); }
    @Override public String toString() { return "(" + key + ", " + value + ")"; }
}
/** Generic class hierarchy: subclass fixes K = String and stays generic in V. */
class NamedPair<V> extends Pair<String, V> {
    NamedPair(String name, V value) { super(name, value); }
}
/** Bounded type parameter: T must be a Vehicle (so we may call farePerSeat()). */
class Ticket<T extends Vehicle> {
    private final T vehicle;
    Ticket(T vehicle) { this.vehicle = vehicle; }
    String totalFare(int seats) { return vehicle.getRegNo() + " x" + seats + " = Rs " + vehicle.farePerSeat() * seats; }
}
/** Generic interface. */
interface Repository<T, ID> {
    void save(T item);
    Optional<T> findById(ID id);
    List<T> findAll();
}
class SeatRepository implements Repository<Seat, String> {
    private final Map<String, Seat> store = new LinkedHashMap<>();
    @Override public void save(Seat s) { store.put(s.getId(), s); }
    @Override public Optional<Seat> findById(String id) { return Optional.ofNullable(store.get(id)); }
    @Override public List<Seat> findAll() { return new ArrayList<>(store.values()); }
}
/** Generic methods and wildcard arguments. */
class Util {
    static <T extends Comparable<T>> T max(List<T> list) {            // generic method with a bound
        T best = list.get(0);
        for (T t : list) if (t.compareTo(best) > 0) best = t;
        return best;
    }
    static double totalCapacity(List<? extends Vehicle> vehicles) {   // upper-bounded wildcard: read-only producer
        double sum = 0; for (Vehicle v : vehicles) sum += v.capacity; return sum;
    }
    static void fill(List<? super Seat> sink) { sink.add(new Seat("S1")); System.out.println("lower-bounded wildcard '? super Seat' accepted: " + sink); }
    static void printAll(List<?> any) { System.out.println("unbounded wildcard '?' items: " + any); }
}

/** Project helper: a Seat together with its own lock (fine-grained locking). */
class LockedSeat {
    final Seat seat;
    final ReentrantLock lock = new ReentrantLock();
    long reservedAt;
    LockedSeat(String id) { this.seat = new Seat(id); }
}
