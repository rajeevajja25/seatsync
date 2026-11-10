# Syllabus map - CSE 307 Advanced Java Programming  ->  where each topic is used

Run the **concepts file** (backend only, no GUI): `java -cp sqlite-jdbc-3.41.2.2.jar concepts/AdvancedJavaConcepts.java`
Run the **real project**: `cd transport-booking && mvn clean javafx:run`

| Unit | Syllabus topic | Concepts file (`AdvancedJavaConcepts.java`) | Real project (`transport-booking`) | One-line explanation |
|---|---|---|---|---|
| 1 | Data types, variables, arrays, operators, control statements | `unit1()` top half | - | 8 primitives, 1-D / 2-D / jagged arrays, `?:`, `switch`, loops |
| 1 | Classes, objects, constructors, `this`, `static`, `final` | `class Seat` | `Seat.java` | `this(...)` chains constructors; `static` = one copy per class; `final` = assigned once |
| 1 | Inheritance, abstract class, interface, multilevel, `super`, `final` | `Vehicle`, `Bus`, `AcSleeperBus`, `Bookable`, `TicketPrinter` | `AutoReleaseThread extends Thread`, `MainApp extends Application` | `abstract` forces subclasses; interface = contract; `super(...)` runs parent constructor |
| 1 | Packages, access specifiers | comments on `Seat` fields | `package com.transport;` in every file | private < default < protected < public |
| 2 | Compile-time polymorphism (overloading) | `FareCalculator`, `Seat` constructors | `Seat`, `DatabaseManager` | compiler picks the method from the argument types |
| 2 | Run-time polymorphism, dynamic dispatch | `Vehicle[] fleet` loop in `unit2()` | `BookingAdapter` / `BookingListener` calls in `BookingService.fire` | the JVM picks the method from the real object class |
| 2 | Exceptions: try/catch/finally, multi-catch, nested try, throw, throws, user-defined, uncaught | `unit2()`, `SeatUnavailableException`, `InvalidPassengerException` | JDBC code in `DatabaseManager` (try-with-resources, rollback) | checked must be handled/declared; unchecked need not; `finally` always runs |
| 2 | Object class | `Seat.equals/hashCode/toString`, hierarchy walk | - | root of every class |
| 3 | Thread model, life cycle | `unit3()` NEW -> RUNNABLE -> TIMED_WAITING -> TERMINATED | `AutoReleaseThread` (daemon) | a daemon thread never keeps the JVM alive |
| 3 | Thread class vs Runnable, priorities | `HelloThread`, `HelloTask`, priorities | `AutoReleaseThread`, stress-test threads | prefer Runnable (class can still extend another class) |
| 3 | Synchronizing threads | `UnsafeCounter` vs `SafeCounter` (`synchronized`), `project()` | per-seat `ReentrantLock` in `Seat` / `BookingService` | `n++` is read-add-write; a lock makes it atomic |
| 3 | Inter-thread communication | `PaymentQueue` (`wait` / `notifyAll`) | `ConcurrentLinkedQueue` hand-off | `wait()` releases the monitor and sleeps until `notifyAll()` |
| 3 | I/O: byte streams, `FileInputStream`, `OutputStream` | `DataOutputStream` / `FileInputStream` in `unit3()` | `BookingDesk.exportActivity()` (`FileOutputStream`) | bytes in, bytes out; try-with-resources closes them |
| 3 | Delegation event model, event classes, `KeyEvent`, listener interfaces, adapter classes | `BookingEvent`, `BookingListener`, `BookingAdapter`, `BookingEngine` | `BookingEvent`, `BookingListener`, `BookingAdapter`, `BookingService.fire`, `BookingDesk` (+ `KeyCode.ESCAPE` handler) | source -> event object -> registered listener; adapter = empty implementations |
| 3 | AWT event handling | - | `frontend-awt/AwtEventDemo.java` (`ActionListener`, `KeyAdapter`, `MouseAdapter`, `WindowAdapter`) | same model with AWT components |
| 4 | JDBC with SQLite: DB, connect, create table, CRUD | `jdbcDemo()` | `DatabaseManager` (set `db.url=jdbc:sqlite:transport.db`) | driver -> Connection -> PreparedStatement -> ResultSet |
| 4 | Transactions | commit / rollback in `jdbcDemo()` | `DatabaseManager.insertBooking/updateStatus` | all-or-nothing |
| 4 | Collections: List, Set, Map, ArrayList, LinkedList, Vector, HashSet, TreeSet, Hashtable, HashMap, Iterator | `collectionsDemo()` | `ConcurrentHashMap` seat map, `ConcurrentLinkedQueue` reservation queue, `ObservableList` / `FilteredList` / `SortedList` in `BookingDesk` | pick the structure by need: order, duplicates, key lookup, thread safety |
| 4 | Streams + lambdas (CLO 2) | end of `collectionsDemo()` | counts in `BookingDesk.updateSummary()` | filter -> map -> collect |
| 5 | Generic class, two type params, general form, bounded types | `Pair<K,V>`, `Ticket<T extends Vehicle>` | `Optional<String>`, `ListView<Row>`, `ComboBox<Trip>` | compile-time type safety, no casts |
| 5 | Wildcards, generic method, generic interface, class hierarchies | `Util`, `Repository<T,ID>`, `SeatRepository`, `NamedPair` | `Comparator<? super Row>` in `BookingDesk` | `? extends` reads, `? super` writes |
| 5 | JavaFX application skeleton | - | `MainApp.start()` / `launch()` / `stop()` | `Application` -> `Stage` -> `Scene` -> nodes |
| 5 | Controls: RadioButton, ListView, ComboBox, TextField, TreeView | - | `BookingDesk` (all five) | `ToggleGroup` makes radio buttons exclusive |
| 5 | Effects and transforms | - | `BookingDesk` (`DropShadow`, `Glow`, `Reflection`, `Rotate`, `Scale`, `RotateTransition`) | effect = visual filter on a node; transform = move/rotate/scale |
| 5 | Menus: MenuBar, Menu, MenuItem, image on MenuItem, Toolbar | - | `BookingDesk.buildMenuBar()`, `icon()`, `buildToolBar()` | `MenuItem(text, graphic)` takes any Node, e.g. an `ImageView` |
| - | The core algorithm | `project()` | `BookingService.reserve/confirmPayment`, `AutoReleaseThread` | lock seat -> re-check status -> change -> unlock; daemon frees unpaid seats |

## File split
**Backend only (no GUI):** `concepts/AdvancedJavaConcepts.java`; in the project: `Seat`, `BookingService`, `AutoReleaseThread`, `DatabaseManager`, `BookingEvent`, `BookingListener`, `BookingAdapter`.
**Frontend:** `MainApp` (Waygo design), `BookingDesk` (Unit 5 controls), `ClassicApp` (Railway/Cinema themes), `themes/*.css`, `frontend-awt/AwtEventDemo.java`.
