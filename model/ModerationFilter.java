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
 */
public class ModerationFilter {

    List<String> prohibitedWords = new ArrayList<>();

    public boolean checkMessage(Message message){
        // TODO: normalize message.content (lowercase, strip spacing/symbols) and
        // check it against prohibitedWords. return true if its CLEAN i think? or
        // false if clean -- double check what GroupChat.postMessage() expects before wiring this up
        return true;
    }

    public void blockMessage(Message message){
        // TODO: message.isBlocked = true; + log it somewhere so the list can be tuned later
    }

}
