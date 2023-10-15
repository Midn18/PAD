package messageAgent.utilities;

import lombok.Data;

@Data
public class Letter {

    private final String name;
    private final String message;
    private boolean sent;

    public Letter(String name, String message) {
        this.name = name;
        this.message = message;
        this.sent = false;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }
}
