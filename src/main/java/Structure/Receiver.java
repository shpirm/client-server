package Structure;

import java.net.Socket;

public interface Receiver {
    void receiveMessage(byte[] packet) throws Exception;
}
