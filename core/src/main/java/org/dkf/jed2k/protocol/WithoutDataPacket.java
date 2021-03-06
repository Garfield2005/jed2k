package org.dkf.jed2k.protocol;

import org.dkf.jed2k.exception.JED2KException;

import java.nio.ByteBuffer;

public class WithoutDataPacket implements Serializable {

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return dst;
    }

    @Override
    public int bytesCount() {
        return 0;
    }

}
