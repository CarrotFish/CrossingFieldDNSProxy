import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class DNSServer {
    final static String bind_addr = "0.0.0.0";
    final static int bind_port = 6001;
    final static long timeout = 10000;

    // 转发
    class Handler implements Runnable{
        Socket s;

        class Checker implements Runnable{
            Thread t;

            @Override
            public void run() {
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                t.interrupt();
                try{
                    s.close();
                }
                catch(Exception e){}
            }

            public Checker(Thread t){
                this.t = t;
            }
            
        }

        @Override
        public void run() {
            try {
                InputStream in = s.getInputStream();
                byte[] data = new byte[2048];
                int len = in.read(data);
                DatagramSocket c = new DatagramSocket();
                c.connect(new InetSocketAddress("114.114.114.114", 53));
                c.send(new DatagramPacket(data, len));
                byte[] result = new byte[2048];
                DatagramPacket packet = new DatagramPacket(result, result.length);
                c.receive(packet);
                c.disconnect();
                c.close();
                s.getOutputStream().write(packet.getData());
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public Handler(Socket s){
            this.s = s;
            Thread h = new Thread(this, "Handler");
            Thread checker = new Thread(new Checker(h), "Checker");
            checker.start();
            h.start();
        }
        
    }

    public DNSServer(){
        try {
            ServerSocket server = new ServerSocket();
            server.bind(new InetSocketAddress(bind_addr, bind_port));
            while (!server.isClosed()) {
                Socket client = server.accept();
                new Handler(client);
            }
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
