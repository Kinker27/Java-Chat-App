public class ChatMessage {
    private final String content;
    private final String timestamp;
    private final boolean sentByUser;

    public ChatMessage(String content, String timestamp, boolean sentByUser) {
        this.content = content;
        this.timestamp = timestamp;
        this.sentByUser = sentByUser;
    }

    public String getContent() {
        return content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean isSentByUser() {
        return sentByUser;
    }
}