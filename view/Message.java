package view;

// same deal as Community.java -- this is the dummy chat-bubble holder for the
// prototype's chat screen, not the real Model Message class (thats model/Message.java,
// has messageId/senderId/isBlocked etc, different package so no name clash)
public class Message {

    String sender;
    String content;
    String time;

    Message(String sender, String content, String time){
        this.sender = sender;
        this.content = content;
        this.time = time;
    }

    public String get_sender(){
        return this.sender;
    }
    public String get_content(){
        return this.content;
    }
    public String get_time(){
        return this.time;
    }


}
