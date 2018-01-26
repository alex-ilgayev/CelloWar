package com.alar.cellowar.shared.messaging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 * Created by Alex on 4/11/2015.
 * This class in charge of turning Messages into stream of bytes, and vice versa.
 */
public class MessageCompression {
    private static MessageCompression _ins = new MessageCompression();

    private MessageCompression() {

    }

    public static MessageCompression getInstance(){
        return _ins;
    }

    public byte[] compress(IMessage message){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(message);
            return bos.toByteArray();
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
        finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try {
                bos.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public IMessage decompress(byte[] stream){
        ByteArrayInputStream bis = new ByteArrayInputStream(stream);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            Object o = in.readObject();
            return (IMessage)o;

        } catch(Exception ex) {
            ex.printStackTrace();
        }
        finally {
            try {
                bis.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
}
