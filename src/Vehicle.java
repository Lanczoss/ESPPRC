import java.io.*;
import java.util.*;

/**
 * A vehicle.
 *
 * This version has a single time window for availability, and
 * a unique start and end locations.
 *
 * @author Phil Kilby
 * @version 1.0 22/1/2008
 */

public class Vehicle
{
    /** The location where it becomes available */
    protected Location startLoc;

    /** The interval during which we are available */
    protected Interval avail;

    /** The location where it must finish */
    protected Location endLoc;

    /** The capacity for each commodity available */
    protected int capacity;

    /** The limit on each metric */
    protected int[] limit;

    /** A unique (within vehicles) index */
    protected int index;

    /** The number of vehicles */
    static private int numVehicles = 1;

    /** Construct a vehicle */
    public Vehicle (
            Interval avail, Location startLoc, Location endLoc, int capacity
    )
    {
        this.avail = avail;
        this.startLoc = startLoc;
        this.endLoc = endLoc;
        this.capacity = capacity;

        limit = new int [Metric.getNumMetrics()];
    }

    public Location getStartLoc() {return startLoc;}
    public Location getEndLoc() {return endLoc;}
    public int getCapacity() {return capacity;}
    public int getLimit(int i) {return limit[i];}
    public int getIndex() {return index;}

    public int getNumVehicles() {return numVehicles;}

    public void show (PrintStream out)
    {

        if (startLoc != endLoc)
            out.print (" finish at " + endLoc);
        out.println();
        out.print ("   Capacities:");
        //for (int i = 0; i < capacity.length; i++)
        //out.print (" " + DblUtil.format2 (capacity[i]));
        out.println();
        out.print ("   Limits:");
        //for (int i = 0; i < limit.length; i++)
        //out.print (" " + DblUtil.format2 (limit[i]));
        out.println();
    }
}