package org.jed2k.protocol;

import org.jed2k.number.UByte;
import org.jed2k.number.UInteger;
import org.jed2k.number.ULong;
import org.jed2k.number.UShort;

public abstract class Buffer{

    public abstract Buffer put(UByte v);
    public abstract Buffer get(UByte v);
    public abstract Buffer put(UShort v);
    public abstract Buffer get(UShort v);
    public abstract Buffer put(UInteger v);
    public abstract Buffer get(UInteger v);
    public abstract Buffer put(ULong v);
    public abstract Buffer get(ULong v);
    
    public abstract Buffer get(UInt8 v);
    public abstract Buffer put(UInt8 v);   
    public abstract Buffer get(UInt16 v);
    public abstract Buffer put(UInt16 v);
    public abstract Buffer get(UInt32 v);
    public abstract Buffer put(UInt32 v);
    public abstract Buffer get(byte[] v);
    public abstract Buffer put(byte[] v);
    
    
    public abstract Buffer put(byte v);
    public abstract Buffer put(short v);
    public abstract Buffer put(int v);
    public abstract Buffer put(float v);
    
    public abstract byte getByte();
    public abstract short getShort();
    public abstract int getInt();
    public abstract float getFloat();
    //public abstract Buffer get(UBytes v);
    //public abstract Buffer put(UBytes v);
}