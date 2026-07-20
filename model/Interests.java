package model;

/*
 * The list of interests a user can pick from during onboarding. Communities are
 * also tagged with these same words so matching is just comparing the two sets.
 *
 * Keep this list reasonable, if it gets too long the picker screen turns into a
 * wall of checkboxes.
 */
public class Interests {

    // the emoji is just for showing nicely on the chips
    public static final String[] ALL = {
        "Art", "Music", "Photography", "Film", "Theatre", "Dance",
        "Books", "Writing", "Gaming", "Coding", "Robotics", "Entrepreneurship",
        "Debate", "Volunteering", "Sustainability", "Politics",
        "Football", "Basketball", "Hiking", "Climbing", "Yoga", "Swimming",
        "Coffee", "Cooking", "Travel", "Languages", "Anime", "Board games",
        "Psychology", "Astronomy", "Fashion"
    };

    // tiny helper so chips can show an emoji next to the name
    public static String emojiFor(String interest) {
        if (interest.equals("Art")) return "🎨";
        if (interest.equals("Music")) return "🎵";
        if (interest.equals("Photography")) return "📷";
        if (interest.equals("Film")) return "🎬";
        if (interest.equals("Theatre")) return "🎭";
        if (interest.equals("Dance")) return "🩰";
        if (interest.equals("Books")) return "📚";
        if (interest.equals("Writing")) return "✍️";
        if (interest.equals("Gaming")) return "🎮";
        if (interest.equals("Coding")) return "💻";
        if (interest.equals("Robotics")) return "🤖";
        if (interest.equals("Entrepreneurship")) return "💡";
        if (interest.equals("Debate")) return "🎤";
        if (interest.equals("Volunteering")) return "🤝";
        if (interest.equals("Sustainability")) return "🌱";
        if (interest.equals("Politics")) return "🏛️";
        if (interest.equals("Football")) return "⚽";
        if (interest.equals("Basketball")) return "🏀";
        if (interest.equals("Hiking")) return "🥾";
        if (interest.equals("Climbing")) return "🧗";
        if (interest.equals("Yoga")) return "🧘";
        if (interest.equals("Swimming")) return "🏊";
        if (interest.equals("Coffee")) return "☕";
        if (interest.equals("Cooking")) return "🍳";
        if (interest.equals("Travel")) return "✈️";
        if (interest.equals("Languages")) return "🗣️";
        if (interest.equals("Anime")) return "🌸";
        if (interest.equals("Board games")) return "🎲";
        if (interest.equals("Psychology")) return "🧠";
        if (interest.equals("Astronomy")) return "🔭";
        if (interest.equals("Fashion")) return "👗";
        return "•"; // fallback if we forgot one
    }

    private Interests() {
    }
}
