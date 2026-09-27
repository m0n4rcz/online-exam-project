package shared;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * Handles binary stream encoding and decoding for the Custom Application-Level Protocol.
 * 
 * CORE NETWORKING RIGOR (ANTI-STICKY PACKET / FRAGMENTATION DEFENSE):
 * TCP is a streaming byte protocol without message boundaries. A single socket read might:
 *   1. Return only part of a packet (Packet Fragmentation / Cat Goi)
 *   2. Return multiple packets glued together (Sticky Packets / Dinh Goi)
 * 
 * PacketCodec guarantees complete frame extraction by:
 *   - Verifying the 4-byte Magic Number (0x4558414D)
 *   - Reading the exact 4-byte Payload Length prefix
 *   - Accumulating incoming bytes until the complete payload has arrived
 */
public class PacketCodec {

    // Maximum permissible payload size (1 MB) to guard against Denial-of-Service / memory exhaustion attacks
    public static final int MAX_PAYLOAD_SIZE = 1024 * 1024;

    /**
     * Serializes a Packet into a raw byte array ready for network transmission.
     */
    public static byte[] encode(Packet packet) {
        byte[] payload = packet.getPayload() != null ? packet.getPayload() : new byte[0];
        ByteBuffer buffer = ByteBuffer.allocate(Packet.HEADER_SIZE + payload.length);
        
        buffer.putInt(Packet.MAGIC_NUMBER);
        buffer.putShort(packet.getOpCode());
        buffer.putShort(packet.getStatus());
        buffer.putInt(payload.length);
        buffer.put(payload);

        return buffer.array();
    }

    /**
     * Writes and flushes an encoded Packet directly to a blocking socket OutputStream.
     */
    public static void writePacket(OutputStream out, Packet packet) throws IOException {
        byte[] bytes = encode(packet);
        out.write(bytes);
        out.flush();
    }

    /**
     * Reads a single full Packet from a blocking socket InputStream.
     * Blocks until the full 12-byte header and full payload are received.
     * 
     * @param in Socket input stream
     * @return The reconstructed Packet, or null if the client closed connection (EOF)
     * @throws IOException on protocol corruption or network drop
     */
    public static Packet readPacket(InputStream in) throws IOException {
        DataInputStream dis = (in instanceof DataInputStream) ? (DataInputStream) in : new DataInputStream(in);

        // 1. Read the fixed 12-byte header
        int magic;
        try {
            magic = dis.readInt();
        } catch (EOFException e) {
            // Normal socket disconnect
            return null;
        }

        // Verify magic number
        if (magic != Packet.MAGIC_NUMBER) {
            throw new IOException(String.format("Corrupted packet: invalid magic 0x%08X (expected 0x%08X)", 
                    magic, Packet.MAGIC_NUMBER));
        }

        short opCode = dis.readShort();
        short status = dis.readShort();
        int length = dis.readInt();

        // Safety check for payload size
        if (length < 0 || length > MAX_PAYLOAD_SIZE) {
            throw new IOException("Illegal packet payload length: " + length + " bytes (max allowed is 1MB)");
        }

        // 2. Read exact payload bytes (guaranteed full read)
        byte[] payload = new byte[length];
        if (length > 0) {
            dis.readFully(payload);
        }

        Packet packet = new Packet();
        packet.setMagic(magic);
        packet.setOpCode(opCode);
        packet.setStatus(status);
        packet.setPayload(payload);

        return packet;
    }

    /**
     * Non-blocking / Buffer-Accumulator Decoder:
     * Used when working with byte buffers (e.g. Java NIO Selector or byte accumulator).
     * Decodes the next available packet if enough bytes have arrived; otherwise returns null.
     */
    public static Packet decodeFromBuffer(ByteBuffer buffer) throws IOException {
        if (buffer.remaining() < Packet.HEADER_SIZE) {
            // Not enough bytes to read header yet
            return null;
        }

        buffer.mark();

        int magic = buffer.getInt();
        if (magic != Packet.MAGIC_NUMBER) {
            buffer.reset();
            // Discard 1 corrupted byte and search for magic alignment
            buffer.get();
            throw new IOException("Corrupted stream: magic bytes mismatch. Resynchronizing stream...");
        }

        short opCode = buffer.getShort();
        short status = buffer.getShort();
        int length = buffer.getInt();

        if (length < 0 || length > MAX_PAYLOAD_SIZE) {
            throw new IOException("Payload length exceeds limit: " + length);
        }

        if (buffer.remaining() < length) {
            // Header received, but full payload has NOT arrived yet (Fragmentation)
            // Roll back position and wait for next network chunk
            buffer.reset();
            return null;
        }

        byte[] payload = new byte[length];
        buffer.get(payload);

        Packet packet = new Packet();
        packet.setMagic(magic);
        packet.setOpCode(opCode);
        packet.setStatus(status);
        packet.setPayload(payload);

        return packet;
    }
}
