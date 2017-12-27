
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ForkJoinPool;

/**
 *
 * @author Lazarko
 */
public class Server {
    
    private Selector selector;
    private InetAddress hostAddress;
    private ServerSocketChannel ssc;
    private final static int PORT_NUMBER = 9393;
    private final static String hostString = "localhost";
    private Iterator iterator;
    private SocketChannel sc;
    private ByteBuffer buffer;
    HashMap<Integer, StringBuilder> clients = new HashMap<>();
    
 
    
    
    public static void main(String[] args){
        Server server = new Server();
        server.init();
    }
    
    private void init(){
        try{
            hostAddress = InetAddress.getByName(hostString);
            selector = Selector.open();
            ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            ssc.socket().bind(new InetSocketAddress(hostAddress, PORT_NUMBER));
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            listener();
          
        }catch(IOException e){
            e.printStackTrace();
        }   
    }
    
    private void listener() throws IOException {
        while (true) {
               if (selector.select() == 0) {
                        System.out.println("no channels ready");
                        continue;
                    }
            try {
                iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey newKey = (SelectionKey) iterator.next();
                    iterator.remove();
                    if (newKey.isAcceptable()) {
                        ssc = (ServerSocketChannel) newKey.channel();
                        sc = (SocketChannel) ssc.accept();
                        sc.configureBlocking(false);
                        sc.register(newKey.selector(), SelectionKey.OP_READ);
                        clients.put(sc.hashCode(), new StringBuilder());
                    }
                    if(newKey.isReadable()){
                        String msg = readFrom(newKey);
                        if(msg != null){
                             System.out.println(msg);
                        }
                        send(msg, newKey);
                       
                        
                    }
                }
            } catch (IOException e) {
                e.addSuppressed(e);
            }
        }
    }
    
    private String readFrom(SelectionKey key) {
        try {
            sc = (SocketChannel) key.channel();
            buffer = ByteBuffer.allocate(1024);
            int bytesCount = sc.read(buffer);
            if (bytesCount > 0) {
                buffer.flip();
                return new String(buffer.array());
            }
        } catch (IOException e) {
            e.addSuppressed(e);
        }
        return "NoMessage";
    }
    
    private void send(String message, SelectionKey key){
        ForkJoinPool.commonPool().submit(new Handler((SocketChannel) key.channel(), message, clients)); 
    }
      
}
