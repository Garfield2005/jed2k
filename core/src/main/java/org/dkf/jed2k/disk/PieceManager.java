package org.dkf.jed2k.disk;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.BlockManager;
import org.dkf.jed2k.BlocksEnumerator;
import org.dkf.jed2k.Constants;
import org.dkf.jed2k.data.PieceBlock;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Hash;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.NonWritableChannelException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by inkpot on 15.07.2016.
 * executes write data into file on disk
 */
@Slf4j
public class PieceManager extends BlocksEnumerator {
    private final FileHandler handler;
    private LinkedList<BlockManager> blockMgrs = new LinkedList<BlockManager>();

    public PieceManager(final FileHandler handler, int pieceCount, int blocksInLastPiece) {
        super(pieceCount, blocksInLastPiece);
        this.handler = handler;
        assert handler != null;
    }

    private BlockManager getBlockManager(int piece) {
        for(BlockManager mgr: blockMgrs) {
            if (mgr.getPieceIndex() == piece) return mgr;
        }

        blockMgrs.addLast(new BlockManager(piece, blocksInPiece(piece)));
        return blockMgrs.getLast();
    }

    /**
     * actual write data to file
     * @param b block
     * @param buffer data source
     */
    public List<ByteBuffer> writeBlock(PieceBlock b, final ByteBuffer buffer) throws JED2KException {
        FileChannel c = handler.getWriteChannel();
        assert c != null;
        long bytesOffset = b.blocksOffset()* Constants.BLOCK_SIZE;
        BlockManager mgr = getBlockManager(b.pieceIndex);
        assert(mgr != null);

        // TODO - add error handling here with correct buffer return to requester
        try {
            log.debug("write block {} started", b);
            // stage 1 - write block to disk, possibly error occurred
            // buffer must have remaining data
            assert(buffer.hasRemaining());
            handler.getWriteChannel().position(bytesOffset);
            while(buffer.hasRemaining()) handler.getWriteChannel().write(buffer);
            buffer.rewind();
            log.debug("write block {} finished", b);
        }
        catch(IOException e) {
            log.error("i/o error on write block {}", e);
            handler.closeChannels();    // do not use total close since in Android we are not able to open it again
            throw new JED2KException(ErrorCode.IO_EXCEPTION);
        } catch(NonWritableChannelException e) {
            log.error("i/o error non writeable channel writing {}", e);
            handler.closeChannels();    // do not use total close since in Android we are not able to open it again
            throw new JED2KException(ErrorCode.NON_WRITEABLE_CHANNEL);
        } catch(Exception e) {
            log.error("common error on write block on disk {}", e);
            handler.closeChannels();    // do not use total close since in Android we are not able to open it again
            throw new JED2KException(ErrorCode.INTERNAL_ERROR);
        }

        // stage 2 - prepare getHash and return obsolete blocks if possible
        return mgr.registerBlock(b.pieceBlock, buffer);
    }

    /**
     * restore block in piece managers after application restart
     * works like write block method but reads data from file to buffer
     *
     * @param b piece block of data
     * @param buffer buffer from common session pool as memory for operation
     * @param fileSize size of file associated with transfer
     * @return free buffers
     * @throws JED2KException
     */
    public List<ByteBuffer> restoreBlock(PieceBlock b, ByteBuffer buffer, long  fileSize) throws JED2KException {
        FileChannel c = handler.getReadChannel();
        assert c != null;
        assert(fileSize > 0);

        long bytesOffset = b.blocksOffset()*Constants.BLOCK_SIZE;
        BlockManager mgr = getBlockManager(b.pieceIndex);

        // prepare buffer for reading from file
        buffer.clear();
        buffer.limit(b.size(fileSize));

        try {
            // read data from file to buffer
            handler.getReadChannel().position(bytesOffset);
            while(buffer.hasRemaining()) handler.getReadChannel().read(buffer);
            buffer.flip();
        }
        catch(IOException e) {
            throw new JED2KException(ErrorCode.IO_EXCEPTION);
        }

        // register buffer as usual in blocks manager and return free blocks
        assert(buffer.remaining() == b.size(fileSize));
        List<ByteBuffer> res = mgr.registerBlock(b.pieceBlock, buffer);
        assert res != null;
        return res;
    }

    public Hash hashPiece(int pieceIndex) {
        BlockManager mgr = getBlockManager(pieceIndex);
        assert(mgr != null);
        assert(mgr.getByteBuffersCount() == 0); // all buffers must be released
        blockMgrs.remove(mgr);
        return mgr.pieceHash();
    }

    /**
     * close file and release resources
     * @return list of ByteBuffers for buffer pool deallocation
     */
    public List<ByteBuffer> releaseFile(boolean deleteFile) {
        handler.close();
        try {
            if (deleteFile) handler.deleteFile();
        } catch(JED2KException e) {
            log.error("unable to delete file {}", e);
        }

        return abort();
    }

    /**
     * abort piece manager - clear all block managers buffers and remove them
     * @return
     */
    public List<ByteBuffer> abort() {
        List<ByteBuffer> res = new LinkedList<>();
        for(BlockManager mgr: blockMgrs) {
            res.addAll(mgr.getBuffers());
        }

        blockMgrs.clear();

        return res;
    }

    /**
     * delete file on disk
     */
    public void deleteFile() throws JED2KException {
        handler.deleteFile();
    }

    public final File getFile() {
        return handler.getFile();
    }
}
