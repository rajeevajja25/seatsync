/*
 * Unit 3 (Event handling) - the DELEGATION EVENT MODEL with AWT, listener interfaces and adapter classes.
 * Standalone single file (needs a desktop, not a headless server).   Run:   java AwtEventDemo.java
 *
 *   event SOURCE  : Button, TextField, Frame (components that generate events)
 *   event OBJECT  : ActionEvent, KeyEvent, MouseEvent, WindowEvent
 *   event LISTENER: ActionListener (1 method) / KeyAdapter, MouseAdapter, WindowAdapter (adapter classes)
 *   the source DELEGATES the handling to the registered listener object via addXxxListener(...)
 */
import java.awt.*;
import java.awt.event.*;

public class AwtEventDemo {
    public static void main(String[] args) {
        Frame frame = new Frame("AWT - delegation event model");
        frame.setLayout(new FlowLayout());
        TextField name = new TextField(18);
        Button reserve = new Button("Reserve S1");
        Label status = new Label("Type a name, press Enter or click Reserve                    ");
        frame.add(new Label("Passenger:"));
        frame.add(name);
        frame.add(reserve);
        frame.add(status);

        Runnable doReserve = () -> status.setText(name.getText().isBlank()
                ? "Name required" : "S1 reserved for " + name.getText().trim());

        // (1) listener INTERFACE with one method: ActionListener (anonymous inner class)
        reserve.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { doReserve.run(); }
        });

        // (2) ADAPTER class: KeyAdapter implements KeyListener's 3 methods as empty bodies; we override only one
        name.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {                  // KeyEvent class
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doReserve.run();
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) name.setText("");
            }
        });

        // (3) MouseAdapter: react only to enter/exit
        reserve.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { reserve.setBackground(Color.GREEN); }
            @Override public void mouseExited(MouseEvent e) { reserve.setBackground(Color.LIGHT_GRAY); }
        });

        // (4) WindowAdapter: close the program when the window is closed
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { frame.dispose(); System.exit(0); }
        });

        frame.setSize(620, 110);
        frame.setVisible(true);
    }
}
