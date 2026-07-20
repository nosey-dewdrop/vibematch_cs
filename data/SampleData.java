package data;

import model.Comment;
import model.Community;
import model.Post;
import model.User;
import util.PasswordUtil;

import java.util.ArrayList;

/*
 * Puts some starter data in the database the very first time the app runs, so a
 * brand new user actually sees communities and people instead of an empty app.
 * Only runs when there are no communities yet.
 *
 * Demo accounts all use the password "vibe1234" if you want to log in as one.
 */
public class SampleData {

    private static UserDao userDao = new UserDao();
    private static CommunityDao communityDao = new CommunityDao();
    private static PostDao postDao = new PostDao();

    public static void seedIfNeeded() {
        if (!communityDao.isEmpty()) {
            return; // already seeded
        }
        seedUsers();
        seedCommunities();
        seedForum();
        seedFriends();
    }

    // a few starter friendships + one pending request so the demo isnt empty
    private static void seedFriends() {
        FriendDao friendDao = new FriendDao();
        friendDao.request("ada", "can");
        friendDao.accept("ada", "can");
        friendDao.request("ada", "zeynep");
        friendDao.accept("ada", "zeynep");
        friendDao.request("can", "mert");
        friendDao.accept("can", "mert");
        // elif sent ada a request that is still waiting
        friendDao.request("elif", "ada");
    }

    private static void seedUsers() {
        makeUser("ada", "Ada Yilmaz", "ada@ug.bilkent.edu.tr", "INFP",
                new String[] { "Hiking", "Photography", "Coffee" });
        makeUser("mert", "Mert Demir", "mert@ug.bilkent.edu.tr", "INTP",
                new String[] { "Gaming", "Coding", "Football" });
        makeUser("zeynep", "Zeynep Kaya", "zeynep@ug.bilkent.edu.tr", "INFJ",
                new String[] { "Books", "Writing", "Coffee" });
        makeUser("can", "Can Ozturk", "can@ug.bilkent.edu.tr", "ENFP",
                new String[] { "Music", "Film", "Art" });
        makeUser("elif", "Elif Sahin", "elif@ug.bilkent.edu.tr", "ENFJ",
                new String[] { "Sustainability", "Volunteering", "Debate" });
    }

    private static void makeUser(String username, String name, String email, String mbti, String[] interests) {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash("vibe1234", salt);
        User u = new User(username, name, email);
        u.setSalt(salt);
        u.setPassHash(hash);
        u.setVerified(true);
        u.setMbtiType(mbti);
        u.setBio("Hey, I'm " + name.split(" ")[0] + " :)");
        userDao.insert(u);

        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < interests.length; i++) {
            list.add(interests[i]);
        }
        userDao.setInterests(username, list);
    }

    private static void seedCommunities() {
        int outdoor = makeCommunity("Bilkent Outdoor Society",
                "We hike every other weekend and run beginner friendly trips around Ankara. No experience needed, just bring your energy.",
                "Sports", "🥾", "D6F0E4", new String[] { "Hiking", "Climbing", "Travel" });

        int frame = makeCommunity("Frame Photo Club",
                "Photo walks, editing nights and the occasional exhibition. All cameras (and phones) welcome.",
                "Arts", "📷", "FFD6E0", new String[] { "Photography", "Art", "Film" });

        int pages = makeCommunity("Page Turners",
                "A cozy book club. One book a month, lots of tea and very strong opinions.",
                "Culture", "📚", "FDF2C9", new String[] { "Books", "Writing" });

        int gamejam = makeCommunity("GameJam BILK",
                "Make games in a weekend, learn engines, find a team. Coders, artists and ideas people all welcome.",
                "Tech", "🎮", "FFE2CF", new String[] { "Gaming", "Coding", "Robotics" });

        int green = makeCommunity("Green Campus",
                "Sustainability projects on campus, clean ups and a community garden in the works.",
                "Social", "🌱", "D6F0E4", new String[] { "Sustainability", "Volunteering" });

        int debate = makeCommunity("Debate Society",
                "Weekly debates, public speaking practice and the odd tournament. Great for getting over stage fright.",
                "Academic", "🎤", "D7E8FB", new String[] { "Debate", "Politics", "Languages" });

        int coffee = makeCommunity("Coffee and Chats",
                "Low pressure meetups to practice languages and just hang out over coffee.",
                "Social", "☕", "FFD6E0", new String[] { "Coffee", "Languages", "Cooking" });

        int music = makeCommunity("Bilkent Music Collective",
                "Jam sessions, small gigs and a space to find people to make music with.",
                "Arts", "🎵", "D7E8FB", new String[] { "Music", "Dance", "Theatre" });

        // give the communities some members so counts arent all zero
        communityDao.join("ada", outdoor);
        communityDao.join("ada", frame);
        communityDao.join("can", frame);
        communityDao.join("can", music);
        communityDao.join("zeynep", pages);
        communityDao.join("zeynep", coffee);
        communityDao.join("mert", gamejam);
        communityDao.join("elif", green);
        communityDao.join("elif", debate);
        communityDao.join("mert", debate);
        communityDao.join("ada", coffee);
    }

    // a few starter discussions so the forums arent empty
    private static void seedForum() {
        int outdoor = findCommunityId("Bilkent Outdoor Society");
        int frame = findCommunityId("Frame Photo Club");
        int pages = findCommunityId("Page Turners");

        if (outdoor > 0) {
            int p1 = postDao.insertPost(new Post(outdoor, "ada",
                "Weekend hike to Elmadag, who's in?",
                "Thinking of leaving from the main gate around 8am Saturday. Beginner friendly pace, we'll be back by evening. Comment if you want to come!"));
            postDao.insertComment(new Comment(p1, "can", "I'm in! Should I bring trekking poles?", 0));
            postDao.insertComment(new Comment(p1, "ada", "Not needed but they help on the way down :)", 1));
            postDao.insertComment(new Comment(p1, "elif", "Count me in too, so excited", 0));
        }

        if (frame > 0) {
            int p2 = postDao.insertPost(new Post(frame, "can",
                "Golden hour photo walk by the lake",
                "The light has been unreal lately. Lets meet Friday 6.30pm near the lake and shoot until sunset."));
            postDao.insertComment(new Comment(p2, "ada", "Yes please, I just got a new lens i want to try", 0));
        }

        if (pages > 0) {
            int p3 = postDao.insertPost(new Post(pages, "zeynep",
                "This month's book: pick between two",
                "It's between a short story collection and a novel. Drop your vote in the comments."));
            postDao.insertComment(new Comment(p3, "ada", "Short stories! easier to keep up with classes", 0));
            postDao.insertComment(new Comment(p3, "zeynep", "Noted, leaning that way too", 1));
        }
    }

    private static int findCommunityId(String name) {
        ArrayList<Community> all = communityDao.getAll();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getName().equals(name)) {
                return all.get(i).getId();
            }
        }
        return -1;
    }

    private static int makeCommunity(String name, String desc, String category, String emoji,
                                     String coverHex, String[] tags) {
        Community c = new Community();
        c.setName(name);
        c.setDescription(desc);
        c.setCategory(category);
        c.setEmoji(emoji);
        c.setCoverColor(coverHex);
        c.setCreatedBy("ada");
        int id = communityDao.insert(c);
        for (int i = 0; i < tags.length; i++) {
            communityDao.addTag(id, tags[i]);
        }
        return id;
    }

    private SampleData() {
    }
}
