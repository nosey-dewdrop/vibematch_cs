package ui;

import javax.swing.SwingWorker;
import javax.swing.JOptionPane;

/*
 * A small helper so screens can talk to the server WITHOUT freezing the window.
 *
 * The problem: every server call waits for a reply. If we do that on the swing
 * (ui) thread the whole window freezes until it comes back. On one computer thats
 * a couple milliseconds, but once the server is out on the internet its more like
 * a tenth of a second each time, and the app would feel laggy.
 *
 * So: work() runs on a BACKGROUND thread (the waiting happens there, ui stays
 * smooth), and when its finished done() runs back on the ui thread so it can
 * safely touch components.
 *
 *   new BackgroundTask<ArrayList<Community>>() {
 *       protected ArrayList<Community> work()  { return api.homeMatches(name); }
 *       protected void done(ArrayList<Community> r) { showCards(r); }
 *   }.start();
 */
public abstract class BackgroundTask<T> {

    // runs off the ui thread. do the server call here.
    protected abstract T work();

    // runs on the ui thread with the result. update the screen here.
    protected abstract void done(T result);

    // runs on the ui thread if work() threw. default just shows the message.
    protected void failed(Exception e) {
        String message = e.getMessage();
        if (message == null) {
            message = "Something went wrong.";
        }
        JOptionPane.showMessageDialog(null, message);
    }

    public void start() {
        SwingWorker<T, Void> worker = new SwingWorker<T, Void>() {
            protected T doInBackground() throws Exception {
                return work();
            }
            protected void done() {
                try {
                    T result = get();
                    BackgroundTask.this.done(result);
                } catch (Exception e) {
                    // unwrap the real cause if swing wrapped it
                    Throwable cause = e.getCause();
                    if (cause instanceof Exception) {
                        BackgroundTask.this.failed((Exception) cause);
                    } else {
                        BackgroundTask.this.failed(e);
                    }
                }
            }
        };
        worker.execute();
    }
}
