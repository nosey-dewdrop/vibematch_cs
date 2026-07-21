package view;

import javax.swing.SwingUtilities;

// entry point. builds the Frame on the Swing event thread (the right place to
// touch UI). pass a server host to reach another machine: java view.Main 192.168.1.20
public class Main {
    public static void main(String[] args) {
        final String host = (args.length > 0 && args[0].trim().length() > 0)
                ? args[0].trim() : "127.0.0.1";
        SwingUtilities.invokeLater(() -> new Frame(host));
    }
}
