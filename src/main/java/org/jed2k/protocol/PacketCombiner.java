package org.jed2k.protocol;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.search.SearchRequest;

public abstract class PacketCombiner {
    
    private static Logger log = Logger.getLogger(PacketCombiner.class.getName());
    
    enum ProtocolType {
        OP_EDONKEYHEADER(0xE3), 
        OP_EDONKEYPROT(0xE3), 
        OP_PACKEDPROT(0xD4), 
        OP_EMULEPROT(0xC5);

        public final byte value;

        ProtocolType(int v) {
            this.value = (byte)v;
        }
    }
    
    enum ClientServerTcp {
        OP_LOGINREQUEST(0x01), // <HASH 16><ID 4><PORT 2><1 Tag_set>
        OP_REJECT(0x05), // (null)
        OP_GETSERVERLIST(0x14), // (null)client->server
        OP_OFFERFILES(0x15), // <count 4>(<HASH 16><ID 4><PORT 2><1
                             // Tag_set>)[count]
        OP_SEARCHREQUEST(0x16), // <Query_Tree>
        OP_DISCONNECT(0x18), // (not verified)
        OP_GETSOURCES(0x19), // <HASH 16>
                             // v2 <HASH 16><SIZE_4> (17.3) (mandatory on 17.8)
                             // v2large <HASH 16><FILESIZE 4(0)><FILESIZE 8>
                             // (17.9) (large files only)
        OP_SEARCH_USER(0x1A), // <Query_Tree>
        OP_CALLBACKREQUEST(0x1C), // <ID 4>
        // OP_QUERY_CHATS = 0x1D, // (deprecated, not supported by server any
        // longer)
        // OP_CHAT_MESSAGE = 0x1E, // (deprecated, not supported by server any
        // longer)
        // OP_JOIN_ROOM = 0x1F, // (deprecated, not supported by server any
        // longer)
        OP_QUERY_MORE_RESULT(0x21), // ?
        OP_GETSOURCES_OBFU(0x23), OP_SERVERLIST(0x32), // <count 1>(<IP 4><PORT
                                                       // 2>)[count]
                                                       // server->client
        OP_SEARCHRESULT(0x33), // <count 4>(<HASH 16><ID 4><PORT 2><1
                               // Tag_set>)[count]
        OP_SERVERSTATUS(0x34), // <USER 4><FILES 4>
        OP_CALLBACKREQUESTED(0x35), // <IP 4><PORT 2>
        OP_CALLBACK_FAIL(0x36), // (null notverified)
        OP_SERVERMESSAGE(0x38), // <len 2><Message len>
        // OP_CHAT_ROOM_REQUEST = 0x39, // (deprecated, not supported by server
        // any longer)
        // OP_CHAT_BROADCAST = 0x3A, // (deprecated, not supported by server any
        // longer)
        // OP_CHAT_USER_JOIN = 0x3B, // (deprecated, not supported by server any
        // longer)
        // OP_CHAT_USER_LEAVE = 0x3C, // (deprecated, not supported by server
        // any longer)
        // OP_CHAT_USER = 0x3D, // (deprecated, not supported by server any
        // longer)
        OP_IDCHANGE(0x40), // <NEW_ID 4>
        OP_SERVERIDENT(0x41), // <HASH 16><IP 4><PORT 2>{1 TAG_SET}
        OP_FOUNDSOURCES(0x42), // <HASH 16><count 1>(<ID 4><PORT 2>)[count]
        OP_USERS_LIST(0x43), // <count 4>(<HASH 16><ID 4><PORT 2><1
                             // Tag_set>)[count]
        OP_FOUNDSOURCES_OBFU(0x44); // <HASH 16><count 1>(<ID 4><PORT 2><obf
                                    // settings 1>(UserHash16 if
                                    // obf&0x08))[count]

        public final byte value;

        ClientServerTcp(int v) {
            value = (byte)v;
        }
    }
    
    enum StandardClientTcp
    {
        OP_HELLO(0x01), // 0x10<HASH 16><ID 4><PORT 2><1 Tag_set>
        OP_SENDINGPART(0x46), // <HASH 16><von 4><bis 4><Daten len:(von-bis)>
        OP_REQUESTPARTS(0x47), // <HASH 16><von[3] 4*3><bis[3] 4*3>
        OP_FILEREQANSNOFIL(0x48), // <HASH 16>
        OP_END_OF_DOWNLOAD(0x49), // <HASH 16> // Unused for sending
        OP_ASKSHAREDFILES(0x4A), // (null)
        OP_ASKSHAREDFILESANSWER(0x4B), // <count 4>(<HASH 16><ID 4><PORT 2><1 Tag_set>)[count]
        OP_HELLOANSWER(0x4C), // <HASH 16><ID 4><PORT 2><1 Tag_set><SERVER_IP 4><SERVER_PORT 2>
        OP_CHANGE_CLIENT_ID(0x4D), // <ID_old 4><ID_new 4> // Unused for sending
        OP_MESSAGE(0x4E), // <len 2><Message len>
        OP_SETREQFILEID(0x4F), // <HASH 16>
        OP_FILESTATUS(0x50), // <HASH 16><count 2><status(bit array) len:((count+7)/8)>
        OP_HASHSETREQUEST(0x51), // <HASH 16>
        OP_HASHSETANSWER(0x52), // <count 2><HASH[count] 16*count>
        OP_STARTUPLOADREQ(0x54), // <HASH 16>
        OP_ACCEPTUPLOADREQ(0x55), // (null)
        OP_CANCELTRANSFER(0x56), // (null)
        OP_OUTOFPARTREQS(0x57), // (null)
        OP_REQUESTFILENAME(0x58), // <HASH 16>    (more correctly file_name_request)
        OP_REQFILENAMEANSWER(0x59), // <HASH 16><len 4><NAME len>
        OP_CHANGE_SLOT(0x5B), // <HASH 16> // Not used for sending
        OP_QUEUERANK(0x5C), // <wert  4> (slot index of the request) // Not used for sending
        OP_ASKSHAREDDIRS(0x5D), // (null)
        OP_ASKSHAREDFILESDIR(0x5E), // <len 2><Directory len>
        OP_ASKSHAREDDIRSANS(0x5F), // <count 4>(<len 2><Directory len>)[count]
        OP_ASKSHAREDFILESDIRANS(0x60), // <len 2><Directory len><count 4>(<HASH 16><ID 4><PORT 2><1 T
        OP_ASKSHAREDDENIEDANS(0x61);  // (null)
                
        public final byte value;

        private StandardClientTcp(int v) {
            value = (byte)v;
        }
    }
    
    enum ExtendedClientTcp {
        OP_EMULEINFO                (0x01), //
        OP_EMULEINFOANSWER          (0x02), //
        OP_COMPRESSEDPART           (0x40), //
        OP_QUEUERANKING             (0x60), // <RANG 2>
        OP_FILEDESC                 (0x61), // <len 2><NAME len>
        OP_VERIFYUPSREQ             (0x71), // (never used)
        OP_VERIFYUPSANSWER          (0x72), // (never used)
        OP_UDPVERIFYUPREQ           (0x73), // (never used)
        OP_UDPVERIFYUPA             (0x74), // (never used)
        OP_REQUESTSOURCES           (0x81), // <HASH 16>
        OP_ANSWERSOURCES            (0x82), //
        OP_REQUESTSOURCES2          (0x83), // <HASH 16>
        OP_ANSWERSOURCES2           (0x84), //
        OP_PUBLICKEY                (0x85), // <len 1><pubkey len>
        OP_SIGNATURE                (0x86), // v1: <len 1><signature len>
        // v2:<len 1><signature len><sigIPused 1>
        OP_SECIDENTSTATE            (0x87), // <state 1><rndchallenge 4>
        OP_REQUESTPREVIEW           (0x90), // <HASH 16> // Never used for sending on aMule
        OP_PREVIEWANSWER            (0x91), // <HASH 16><frames 1>{frames * <len 4><frame len>} // Never used for sending on aMule
        OP_MULTIPACKET              (0x92),
        OP_MULTIPACKETANSWER        (0x93),
        // OP_PEERCACHE_QUERY       (0x94), // Unused on aMule - no PeerCache
        // OP_PEERCACHE_ANSWER      (0x95), // Unused on aMule - no PeerCache
        // OP_PEERCACHE_ACK         (0x96), // Unused on aMule - no PeerCache
        OP_PUBLICIP_REQ             (0x97),
        OP_PUBLICIP_ANSWER          (0x98),
        OP_CALLBACK                 (0x99), // <HASH 16><HASH 16><uint 16>
        OP_REASKCALLBACKTCP         (0x9A),
        OP_AICHREQUEST              (0x9B), // <HASH 16><uint16><HASH aichhashlen>
        OP_AICHANSWER               (0x9C), // <HASH 16><uint16><HASH aichhashlen> <data>
        OP_AICHFILEHASHANS          (0x9D),
        OP_AICHFILEHASHREQ          (0x9E),
        OP_BUDDYPING                (0x9F),
        OP_BUDDYPONG                (0xA0),
        OP_COMPRESSEDPART_I64       (0xA1), // <HASH 16><von 8><size 4><Data len:size>
        OP_SENDINGPART_I64          (0xA2), // <HASH 16><start 8><end 8><Data len:(end-start)>
        OP_REQUESTPARTS_I64         (0xA3), // <HASH 16><start[3] 8*3><end[3] 8*3>
        OP_MULTIPACKET_EXT          (0xA4),
        OP_CHATCAPTCHAREQ           (0xA5),
        OP_CHATCAPTCHARES           (0xA6);
        
        public final byte value;

        private ExtendedClientTcp(int v) {
            value = (byte)v;
        }
    }
    
    private PacketHeader header = new PacketHeader();
    private PacketHeader outgoingHeader = new PacketHeader();
    protected static final Map<PacketKey, Class<? extends Serializable>> supportedPacketsServer;
    protected static final Map<Class<? extends Serializable>, PacketKey> struct2KeyServer;
    
    protected static final Map<PacketKey, Class<? extends Serializable>> supportedPacketsClient;
    protected static final Map<Class<? extends Serializable>, PacketKey> struct2KeyClient;
    
    private static void addHandler(byte protocol, byte type, Class<? extends Serializable> clazz) {
        PacketKey pk = new PacketKey(protocol, type);
        assert(!supportedPacketsServer.containsKey(pk));
        assert(clazz != null);
        supportedPacketsServer.put(pk, clazz);
        struct2KeyServer.put(clazz, pk);
    }
    
    private static void addHandlerClient(byte protocol, byte type, Class<? extends Serializable> clazz) {
        PacketKey pk = new PacketKey(protocol, type);
        assert(!supportedPacketsClient.containsKey(pk));
        assert(clazz != null);
        supportedPacketsClient.put(pk, clazz);
        struct2KeyClient.put(clazz, pk);
    }
    
    static {
        supportedPacketsServer = new TreeMap<PacketKey, Class<? extends Serializable>>();
        struct2KeyServer = new HashMap<Class<? extends Serializable>, PacketKey>();
       
        supportedPacketsClient = new TreeMap<PacketKey, Class<? extends Serializable>>();
        struct2KeyClient = new HashMap<Class<? extends Serializable>, PacketKey>();
        
        // client <-> server tcp messages section
        addHandler(ProtocolType.OP_EDONKEYHEADER.value, ClientServerTcp.OP_LOGINREQUEST.value, LoginRequest.class);
        addHandler(ProtocolType.OP_EDONKEYHEADER.value, ClientServerTcp.OP_SERVERLIST.value, ServerList.class);
        addHandler(ProtocolType.OP_EDONKEYHEADER.value, ClientServerTcp.OP_GETSERVERLIST.value, ServerGetList.class);
        addHandler(ProtocolType.OP_EDONKEYHEADER.value, ClientServerTcp.OP_SERVERMESSAGE.value, ServerMessage.class);
        addHandler(ProtocolType.OP_EDONKEYHEADER.value, ClientServerTcp.OP_SERVERSTATUS.value, ServerStatus.class);
        addHandler(ProtocolType.OP_EDONKEYHEADER.value, ClientServerTcp.OP_IDCHANGE.value, ServerIdChange.class);
        addHandler(ProtocolType.OP_EDONKEYHEADER.value, ClientServerTcp.OP_SERVERIDENT.value, ServerInfo.class);
        
        addHandler(ProtocolType.OP_EDONKEYHEADER.value, ClientServerTcp.OP_SEARCHRESULT.value, SearchResult.class);
        addHandler(ProtocolType.OP_EDONKEYHEADER.value, ClientServerTcp.OP_SEARCHREQUEST.value, SearchRequest.class);
        addHandler(ProtocolType.OP_EDONKEYHEADER.value, ClientServerTcp.OP_QUERY_MORE_RESULT.value, SearchMore.class);        
        
        addHandler(ProtocolType.OP_EDONKEYHEADER.value, ClientServerTcp.OP_GETSOURCES.value, GetFileSources.class);
        addHandler(ProtocolType.OP_EDONKEYHEADER.value, ClientServerTcp.OP_FOUNDSOURCES.value, FoundFileSources.class);
        
        addHandler(ProtocolType.OP_EDONKEYHEADER.value, ClientServerTcp.OP_CALLBACKREQUEST.value, CallbackRequestOutgoing.class);
        addHandler(ProtocolType.OP_EDONKEYHEADER.value, ClientServerTcp.OP_CALLBACKREQUESTED.value, CallbackRequestIncoming.class);
        addHandler(ProtocolType.OP_EDONKEYHEADER.value, ClientServerTcp.OP_CALLBACK_FAIL.value, CallbackRequestFailed.class);
        
        // client <-> client tcp messages section
        addHandlerClient(ProtocolType.OP_EDONKEYPROT.value, StandardClientTcp.OP_HELLO.value, ClientHello.class);
        addHandlerClient(ProtocolType.OP_EDONKEYPROT.value, StandardClientTcp.OP_HELLOANSWER.value, ClientHelloAnswer.class);
        addHandlerClient(ProtocolType.OP_EMULEPROT.value, ExtendedClientTcp.OP_EMULEINFO.value, ClientExtHello.class);
        addHandlerClient(ProtocolType.OP_EMULEPROT.value, ExtendedClientTcp.OP_EMULEINFOANSWER.value, ClientExtHelloAnswer.class);
    }
    
    public Serializable unpack(ByteBuffer src) throws JED2KException {        
        if (!header.isDefined()) {
            if (src.remaining() >= header.bytesCount()) {
                header.get(src);
                log.info(header.toString());
            } else {
                return null;
            }
        }
        
        if (src.remaining() >= header.sizePacket()) {
            PacketKey key = header.key();
            Class<? extends Serializable> clazz = keyToPacket(key);
            Serializable ph = null;
            
            if (clazz != null) {
                try {
                    ph = clazz.newInstance();
                } catch(InstantiationException e) {
                    throw new JED2KException(e);
                } catch (IllegalAccessException e) {
                    throw new JED2KException(e);                    
                }
            } else {
                log.warning("unable to find correspond packet for " + header);
                ph = new BytesSkipper(header.sizePacket());
            }
            
            try {
                if (ph instanceof SoftSerializable) {
                    SoftSerializable ssp = (SoftSerializable)ph;
                    assert(ssp != null);
                    ssp.get(src, header.sizePacket());
                } else {
                    ph.get(src);
                }
            } catch(JED2KException e) {
                throw e;
            } catch(Exception e) {
                // catch any exception and convert it to our
                throw new JED2KException(e);
            }
            
            header.reset();
            return ph;
        } else {
            log.info("remaining " + src.remaining() + " less than packet size body " + header.sizePacket());
        }
        
        return null;
    }
    
    public boolean pack(Serializable object, ByteBuffer dst) throws JED2KException {
        PacketKey key = classToKey(object.getClass());
        log.info("pack for class " + object.getClass().getName());
        assert(key != null);
        if ((outgoingHeader.bytesCount() + object.bytesCount()) < dst.remaining()) {
            outgoingHeader.reset(key, object.bytesCount() + 1);
            assert(outgoingHeader.isDefined());
            outgoingHeader.put(dst);
            object.put(dst);
            return true;
        }
        
        return false;
    }
    
    protected abstract Class<? extends Serializable> keyToPacket(PacketKey key);
    protected abstract PacketKey classToKey(Class<? extends Serializable> clazz);
}