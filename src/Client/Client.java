
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Set;


/**
 *
 * @author Lazarko
 */
public class Client implements Runnable{
    LinkedList<String> queue = new LinkedList<>();
    private Selector selector;
    private String inputFromUser;
    private final static String INIT_MSG = "CONNECTED TO SERVER";
    private final static String QUIT_MSG = "Session closed";
    private SocketChannel socketChannel;
    String last = "null";
    
    
    public static void main(String [] args){
        Client c = new Client();
        c.start();
    }
    protected void start (){
        initChannel();
       
        new Thread(this).start();
        Scanner in = new Scanner(System.in);
        while(true){
              inputFromUser = in.nextLine();
              queue.push(inputFromUser);
        }
        
    }
    @Override
    public void run() {
        boolean isSending = true;
        while(isSending) {
            try {
                if (selector.select() > 0) {
                    Set set = selector.selectedKeys();
                    Iterator iterator = set.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = (SelectionKey)
                                iterator.next();
                        iterator.remove();
                        if (key.isConnectable()) {
                            connect();
                            System.out.println(INIT_MSG);
                        }
                        if (key.isReadable()) {
                            String msg = readFromProccess(key);
                            if(msg != null){
                                 System.out.println(msg);
                            }
                           
                        }
                        if (key.isWritable()) {
                            if (queue.peek() != null) {
                                last = queue.poll();
                                isSending = sendMSG(last, key);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!isSending) {
            try {
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(QUIT_MSG);
        } 
    }
    private boolean sendMSG(String s, SelectionKey key){
        if (s.equalsIgnoreCase("")) {
            return false;
        }
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.wrap(s.getBytes());
        try {
            channel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
    private void initChannel(){
        try {
            InetAddress hostIP = InetAddress.getByName("localhost"); //83.254.4.8
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(hostIP, 9393));
            int op = SelectionKey.OP_CONNECT|SelectionKey.OP_READ|SelectionKey.OP_WRITE;
            socketChannel.register(selector, op);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }
    private void connect(){
        try {
            socketChannel.finishConnect();
        } catch (IOException ex) {
            try {
                Thread.sleep(1000);
                socketChannel.close();
                selector.close();
                initChannel();
                this.run();
            }catch (IOException | InterruptedException e){
                System.out.println(e);
            }

        }
    }
    public static String readFromProccess(SelectionKey key) {
        SocketChannel sc = (SocketChannel) key.channel();
        ByteBuffer bb = ByteBuffer.allocate(1024);
        try {
            sc.read(bb);
        } catch (IOException e) {
            e.printStackTrace();
        }
        bb.flip();
        Charset charset = Charset.forName("UTF-8");
        CharsetDecoder decoder = charset.newDecoder();
        CharBuffer charBuffer = null;
        try {
            charBuffer = decoder.decode(bb);
        } catch (CharacterCodingException e) {
            e.printStackTrace();
        }
        String message = charBuffer.toString();
        return message;
    }


}
