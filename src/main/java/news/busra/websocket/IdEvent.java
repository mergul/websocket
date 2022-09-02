package news.busra.websocket;

import java.util.List;

public
class IdEvent {
    private final String id;
    private final String event;
    private final String room;
    private final List<String> users;

    public IdEvent(String event, String id, String room, List<String> users) {
        this.event = event;
        this.id = id;
        this.room = room;
        this.users = users;
    }

    public String getEvent() {
        return event;
    }

    public String getId() {
        return id;
    }

    public String getRoom() {
        return room;
    }

    public List<String> getUsers() {
        return users;
    }
}
