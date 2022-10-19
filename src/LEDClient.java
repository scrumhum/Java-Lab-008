import com.google.gson.Gson;
import com.google.gson.JsonArray;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import java.util.concurrent.TimeUnit;

class LEDClient {
    private ZContext zctx;
    private ZMQ.Socket zsocket;
    private Gson gson;
    private String connStr;
    private final String topic = "GPIO";

    private static final int[] OFF = {0, 0, 0};

    public LEDClient(String protocol, String host, int port) {
        zctx = new ZContext();
        zsocket = zctx.createSocket(SocketType.PUB);
        this.connStr = String.format("%s://%s:%d", protocol, "*", port);
        zsocket.bind(connStr);
        this.gson = new Gson();
    }

    public void send(int[] color) throws InterruptedException {
        JsonArray ja = gson.toJsonTree(color).getAsJsonArray();
        String message = topic + " " + ja.toString();
        System.out.println(message);
        zsocket.send(message);
    }

    public void strobe(int[] color, int times, int miliseconds) throws InterruptedException {
        for (int i = 0; i < times; i++) {
            send(color);
            TimeUnit.MILLISECONDS.sleep(miliseconds);
            send(LEDClient.OFF);
            TimeUnit.MILLISECONDS.sleep(miliseconds);
        }
    }

    public void close() throws InterruptedException {
        TimeUnit.SECONDS.sleep(2); // Allow the socket a chance to flush.
        this.zsocket.close();
        this.zctx.close();
    }

    public static void main(String[] args) throws InterruptedException {
        LEDClient ledClient = new LEDClient("tcp", "192.168.1.117", 5001);

        int[] color1 = {224, 64, 251}; // purple
        int[] color2 = {64, 196, 255}; //blue
        int[] color3 = {255, 111, 0}; //orange
        int[][] colors = {color1, color2, color3};

        for (int i = 0; i < 3; i++) {
            ledClient.strobe(colors[i], 5, 500);
        }

    }
}