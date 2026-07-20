package model;

import java.util.List;
import java.util.ArrayList;

/*
 * MODEL -- ModerationFilter
 * owner: Damla Su Bilge   (Table 5)
 *
 * "Automatically screens every message for slurs and prohibited language."
 * "the first line of defense described in the requirements" (4.4)
 * UML: Message depends on ModerationFilter ("checked by", dashed arrow).
 *
 * status: SCAFFOLD ONLY.
 * see design risks table in the report -- "Moderation filter accuracy" is
 * literally called out: a keyword filter can miss disguised slurs (leetspeak,
 * spacing) or flag innocent words, in both english and turkish. so this
 * needs more than a naive list eventually, but a blocklist + normalization
 * (lowercasing, stripping spacing/symbols) is the starting point per the report.
 *
 * status: first pass. the blocklist itself is short + placeholder on purpose --
 * the real turkish/english slur list is something the team fills in, i didnt want
 * to hardcode a big offensive wordlist into the repo. the mechanism is the point.
 */
public class ModerationFilter {

    List<String> prohibitedWords = new ArrayList<>();

    public ModerationFilter(){
        // starter list. keep these lowercase + already-normalized so they match
        // whatever normalize() produces. team extends this later (see report 4.4 risk row)
        prohibitedWords.add("slur1");
        prohibitedWords.add("slur2");
    }

    // returns true if the message is CLEAN (safe to show), false if it hit the
    // blocklist. named it so send()/postMessage read as "if (checkMessage) allow it".
    // the normalize step is the whole reason this isnt just content.contains() --
    // someone typing "s l u r" or "s1ur" would sail through a naive check
    public boolean checkMessage(Message message){
        boolean clean = true;
        String normalized = normalize(message.content);
        int i = 0;
        while (i < prohibitedWords.size()){
            // normalize the blocklist word too, not just the message. otherwise "slur1"
            // in the list never matches, because normalize() already turned the messages
            // "1" into an "i" -- they have to go through the exact same folding to line up
            String bad = normalize(prohibitedWords.get(i));
            if (normalized.contains(bad)){
                clean = false;
            }
            i = i + 1;
        }
        return clean;
    }

    public void blockMessage(Message message){
        message.isBlocked = true;
        // just print for now so we can watch it working while testing. a real build
        // would push this to a log the team can read back to tune the wordlist
        System.out.println("blocked a message from " + message.senderId);
    }

    // lowercases, strips anything thats not a letter/number, and folds the common
    // leetspeak swaps back to letters. thats what lets "S1ur!" and "s l u r" both
    // collapse down to the same thing the blocklist is looking for
    private String normalize(String content){
        if (content == null){
            return "";
        }
        String lower = content.toLowerCase();
        lower = lower.replace('0', 'o');
        lower = lower.replace('1', 'i');
        lower = lower.replace('3', 'e');
        lower = lower.replace('4', 'a');
        lower = lower.replace('5', 's');
        StringBuilder cleaned = new StringBuilder();
        int i = 0;
        while (i < lower.length()){
            char c = lower.charAt(i);
            if (Character.isLetterOrDigit(c)){
                cleaned.append(c);
            }
            i = i + 1;
        }
        return cleaned.toString();
    }

}
