package app;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.ServerClient;
import screens.AppFrame;

/*
 * Entry point for the desktop CLIENT. It does NOT touch the database anymore,
 * that all lives on the server now. All this does is connect to the server over
 * a socket and open the login window.
 *
 * The server address: by default localhost, but you can pass a host on the
 * command line (java app.Main 192.168.1.20) so a second computer on the same
 * network can connect to the machine running the server.
 */
public class Main {

    public static void main(String[] args) {
        final String host = (args.length > 0) ? args[0] : "127.0.0.1";
        final int port = 5050;

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // no big deal, use the default look
        }

        boolean ok = ServerClient.getInstance().connect(host, port);
        if (!ok) {
            JOptionPane.showMessageDialog(null,
                "Couldn't reach the vibematch server at " + host + ":" + port + ".\n\n"
                + "Start it first with:  java -cp \"build:desktop/lib/*\" server.ServerMain\n"
                + "(or run ./run-server.sh)",
                "No server", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                AppFrame frame = new AppFrame();
                frame.showLogin();
                frame.setVisible(true);
            }
        });
    }
}
