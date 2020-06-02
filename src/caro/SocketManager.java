package caro;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketManager {

    ServerSocket server;
    Socket client, socket;
    ObjectOutputStream os;
    ObjectInputStream is;

    public SocketManager() {

    }

    public void CreateServer() {
        try {
            server = new ServerSocket(1999);
        } catch (IOException ex) {
            Logger.getLogger(SocketManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            socket = server.accept();
        } catch (IOException ex) {
            Logger.getLogger(SocketManager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void ConnectServer() throws IOException {
        client = new Socket("localhost", 1999);
    }

    private byte[] convertToBytes(Object object) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(object);
            return bos.toByteArray();
        }
    }

    private Object convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                ObjectInput in = new ObjectInputStream(bis)) {
            return in.readObject();
        }
    }
    
    void Send(String s,Object data){
        if(s.equals("Server")){
            try {
                os = new ObjectOutputStream(socket.getOutputStream());
            } catch (IOException ex) {
                Logger.getLogger(SocketManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                os.writeObject(data);
            } catch (IOException ex) {
                Logger.getLogger(SocketManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            try {
                os = new ObjectOutputStream(client.getOutputStream());
            } catch (IOException ex) {
                Logger.getLogger(SocketManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                os.writeObject(data);
            } catch (IOException ex) {
                Logger.getLogger(SocketManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    Object Receive(String s){
         if(s.equals("Server")){
             try {
                 is = new ObjectInputStream(socket.getInputStream());
             } catch (IOException ex) {
                 Logger.getLogger(SocketManager.class.getName()).log(Level.SEVERE, null, ex);
             }
             try {
                 return is.readObject();
             } catch (IOException | ClassNotFoundException ex) {
                 Logger.getLogger(SocketManager.class.getName()).log(Level.SEVERE, null, ex);
             }
        }else{
             try {
                 is = new ObjectInputStream(client.getInputStream());
             } catch (IOException ex) {
                 Logger.getLogger(SocketManager.class.getName()).log(Level.SEVERE, null, ex);
             }
             try {
                 return is.readObject();
             } catch (IOException | ClassNotFoundException ex) {
                 Logger.getLogger(SocketManager.class.getName()).log(Level.SEVERE, null, ex);
             }
        }
        return null;
    }
}


