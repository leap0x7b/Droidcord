package leap.droidcord;

public class Snowflake {
    public static final long EPOCH = 1420070400000L;

    public long id;

    public long sfTimestamp;
    public int internalWorkerID;
    public int internalProcessID;
    public int increment;

    public Snowflake(long snowflake) {
        id = snowflake;
        sfTimestamp = (snowflake >> 22) + EPOCH;
        internalWorkerID = (int) ((snowflake & 0x3E0000) >> 17);
        internalProcessID = (int) ((snowflake & 0x1F000) >> 12);
        increment = (int) (snowflake & 0xFFF);
    }
}
