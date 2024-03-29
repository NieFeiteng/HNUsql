package BUFFERMANAGER;

import java.util.Arrays;

public class Block {

    public static final int BLOCKSIZE = 4096;  //4KB for 1 block
    private int LRUCount = 0; //block replace strategy
    private int blockOffset = 0; //to check the block
    private boolean isDirty = false;  //true if the block has been modified
    private boolean isValid = false;  //true if the block is valid
    private boolean isLocked = false; //true if the block is pinned
    private String filename;  //record where the block from
    private byte[] blockData = new byte[BLOCKSIZE];  //allocate 4KB memory for 1 block

    public Block() {
        //do noting
    }

    public boolean write_data(int offset, byte[] data) {  //offset from 0 to 4096
        if (offset + data.length > BLOCKSIZE) return false;
        for (int i = 0; i < data.length; i++)
            blockData[i + offset] = data[i];
        isDirty = true;
        LRUCount++;
        return true;
    }

    public void reset_modes() {
        isDirty = isLocked = isValid = false;  //reset all modes
        LRUCount = 0;  //reset LRU counter
    }

    public int read_integer(int offset) {  //read integer from block data -- big-endian method
        if (offset + 4 > BLOCKSIZE) return 0;
        int b0 = blockData[offset] & 0xFF;
        int b1 = blockData[offset + 1] & 0xFF;
        int b2 = blockData[offset + 2] & 0xFF;
        int b3 = blockData[offset + 3] & 0xFF;
        LRUCount++;
        return (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
    }

    public boolean write_integer(int offset, int val) {
        if (offset + 4 > BLOCKSIZE) return false;
        blockData[offset] = (byte) (val >> 24 & 0xFF);
        blockData[offset + 1] = (byte) (val >> 16 & 0xFF);
        blockData[offset + 2] = (byte) (val >> 8 & 0xFF);
        blockData[offset + 3] = (byte) (val & 0xFF);
        LRUCount++;
        isDirty = true;
        return true;
    }

    public float read_float(int offset) {
        int dat = read_integer(offset);
        return Float.intBitsToFloat(dat);
    }

    public boolean write_float(int offset, float val) {
        int dat = Float.floatToIntBits(val);
        return write_integer(offset, dat);
    }

    //读入或写入一个长度为length的字符串到offset字节数处
    public String read_string(int offset, int length) {
        byte[] buf = new byte[length];
        for (int i = 0; i < length && i < BLOCKSIZE - offset; i++)
            buf[i] = blockData[offset + i];
        LRUCount++;
        return new String(buf);
    }


    public boolean write_string(int offset, String str) {
        byte[] buf = str.getBytes();
        if (offset + buf.length > BLOCKSIZE) return false;
        for (int i = 0; i < buf.length; i++)
            blockData[offset + i] = buf[i];
        LRUCount++;
        isDirty = true;
        return true;
    }

    public boolean dirty() {
        return this.isDirty;
    }

    //返回块是否背锁住
    public boolean lock() {
        return this.isLocked;
    }

    public boolean valid() {
        return this.isValid;
    }

    public String get_filename() {
        return this.filename;
    }

    public int get_block_offset() {
        return this.blockOffset;
    }

    public int get_LRU() {
        return this.LRUCount;
    }

    public byte[] get_block_data() {
        return blockData;
    }

    //设置块中存储数据
    public void set_block_data(byte[] data) {
        this.blockData = data;
    }

    public void set_block_data() {
        Arrays.fill(blockData, (byte) 0);  //memset block data to zero
    }

    public void dirty(boolean flag) {
        this.isDirty = flag;
    }

    public void lock(boolean flag) {
        this.isLocked = flag;
    }

    public void valid(boolean flag) {
        this.isValid = flag;
    }

    public void set_filename(String fname) {
        this.filename = fname;
    }

    public void set_block_offset(int ofs) {
        this.blockOffset = ofs;
    }


}
