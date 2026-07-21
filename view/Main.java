package view;

//import java.util.Random;

// entry point. this whole file is in the "view" layer per the MVC restructure --
// its the launcher, it doesnt know anything about model/ or controller/ at all,
// it literally just pops the main Frame open and thats it
public class Main {
// main class , dont put logic here just runs the frame
    public static void main(String[] args) {
        new Frame();
    }

}
