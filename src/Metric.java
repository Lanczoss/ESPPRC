import java.io.*;
import java.util.*;

/**
 * A metric. this can be used to put limits on vehicles.
 *
 * @author Phil Kilby
 * @version 1.0 22/1/2008
 */

public class Metric
{
    /** The name of this metric */
    protected String id;

    /** The uniqe index */
    protected int index;

    /** The number of Metrics created */
    static private int numMetrics = 0;

    public Metric (String id)
    {
        this.id = id;
        this.index = numMetrics++;
    }

    public String getId() {return id;}
    public int getIndex() {return index;}
    static public int getNumMetrics() {return numMetrics;}

    public void show (PrintStream out)
    {
        out.println ("Metric " + index + ": " + id);
    }

    public String toString()
    {
        return id;
    }
}

