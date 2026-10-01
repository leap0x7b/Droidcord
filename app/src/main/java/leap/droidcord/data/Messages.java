package leap.droidcord.data;

import java.util.Vector;

import leap.droidcord.model.Message;

public class Messages {
    private Vector<Message> messages;

    public Messages() {
        this.messages = new Vector<Message>();
    }

    public Message get(int index) {
        if (index < 0 || index >= messages.size())
            return null;
        return messages.get(index);
    }

    public void add(Message message) {
        if (message != null)
            messages.add(message);
    }

    public void addFirst(Message message) {
        if (message != null)
            messages.add(0, message);
    }

    public void cluster() {
        if (messages.size() > 1) {
            Message previous = null;
            long clusterStart = 0;
            for (int i = 0; i < messages.size(); i++) {
                Message message = (Message) messages.get(i);
                message.showAuthor = message.shouldShowAuthor(previous, clusterStart);
                if (message.showAuthor)
                    clusterStart = message.id;
                previous = message;
            }
        }
    }

    public void reset() {
        messages.clear();
    }

    public int size() {
        return messages.size();
    }
}
