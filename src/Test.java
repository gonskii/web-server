import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Test
{
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(8080);
        Socket socket = server.accept();

        System.out.println(socket.getInetAddress());
    }
}
