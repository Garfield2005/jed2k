package org.jed2k.protocol;

import org.jed2k.hash.MD4;

public final class Hash implements Serializable{
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private final byte[] value = { 
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, 
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, 
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
    
    public static final Hash TERMINAL   = fromString("31D6CFE0D16AE931B73C59D7E0C089C0");
    public static final Hash LIBED2K    = fromString("31D6CFE0D14CE931B73C59D7E0C04BC0");
    public static final Hash EMULE      = fromString("31D6CFE0D10EE931B73C59D7E0C06FC0");
    public static final Hash INVALID    = new Hash();
    
    public static Hash fromString(String value) {
        assert(value.length() == MD4.HASH_SIZE*2);
        if (value.length() != MD4.HASH_SIZE*2) return INVALID;
        Hash res = new Hash();
        for (int i = 0; i < MD4.HASH_SIZE*2; i += 2) {
            res.value[i/2] = (byte) ((Character.digit(value.charAt(i), 16) << 4) + Character.digit(value.charAt(i+1), 16));
        }
        
        return res;
    }
    
    public static Hash fromBytes(byte[] value) {
        assert(value.length == MD4.HASH_SIZE);
        if (value.length != MD4.HASH_SIZE) return INVALID;
        Hash res = new Hash();
        for(int i = 0; i < MD4.HASH_SIZE; ++i) {
            res.value[i] = value[i];
        }
        
        return res;
    }
    
    @Override
    public String toString() {
        char[] hexChars = new char[value.length * 2];
        for ( int j = 0; j < value.length; j++ ) {
            int v = value[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Hash) {
            return java.util.Arrays.equals(value, ((Hash)obj).value);
        }

        return false;
    }

    @Override
    public Buffer get(Buffer src) {
        return src.get(value);
    }

    @Override
    public Buffer put(Buffer dst) {
        return dst.put(value);
    }
}