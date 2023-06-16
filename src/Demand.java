import java.io.*;
import java.util.*;

/**
 * The data on the demands for a location.某个位置所需求的数据
 *
 * @author Phil Kilby
 * @version 1.0 22/1/2008
 */

public class Demand {
    /**
     * A display name for the demand
     */
    protected String id;

    /**
     * The location of the demand
     */
    protected Location loc;

    /**
     * The value of supplying the demand
     */
    protected int value;

    /**
     * A list of intervals representing valid start times
     */
    protected Interval timeWindows; //线性表的时间窗 Interval是创建一个时间窗线性表

    /**
     * The duration of service - fixed currently 服务期限
     */
    protected int duration;

    /**
     * The uniqe index 下标 指针
     */
    protected int index;

    /**
     * The number of Demands created 需求默认为0
     */
    static private int numDemands = 0;

    /**
     * Construct a demand
     */
    public Demand(
            String id,  Location loc, int value,
            Interval timeWindows, int duration
    ) {
        this.id = "{" + id + "}";
        this.loc = loc;
        this.value = value;
        this.index = numDemands++;
        this.timeWindows = timeWindows;
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public Location getLocation() {
        return loc;
    }

    public double getValue() {
        return value;
    }

    public Interval getTimeWindows() {
        return timeWindows;
    }


    public int getIndex() {
        return index;
    }

    static public int getNumDemands() {
        return numDemands;
    }

    /**
     * Add an entry to the lsit of allowable start times.
     */
    public void addTimeWindow(int earlyStart, int lateStart) {
        timeWindows.start = earlyStart;
        timeWindows.end = lateStart;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void show(PrintStream out) {
        out.print(
                "Demand " + index + ": " + id +
                        " at loc " + loc +
                        " value " + value +
                        " Quantitities:"
        );
    }

    public String toString() {
        return id;
    }
}
