package model;

/*
 * The categories communities are grouped under. Kept here in model because both
 * the server (when it needs them) and the client's discover screen use them, so
 * neither side has to reach into the other's code.
 */
public class Categories {

    public static final String[] ALL = {
        "Arts", "Sports", "Tech", "Culture", "Academic", "Social"
    };

    private Categories() {
    }
}
