import java.util.*;

/**
 * A distance metric
 *
 * @author Phil Kilby
 * @version 1.0 22/1/2008
 */

public abstract class DistMetric extends Metric
{
    public DistMetric (String id)
    {
        super (id);
        this.id = id;
    }

    /** Find the distance from location a to location b */
    public abstract double getDist (Location a, Location b);

    /** Find the distance from location a to location b */
    public double getDist (Demand a, Demand b)
    {
        return getDist (a.getLocation(), b.getLocation());
    }

    /** Find the distance from location index i to location index j */
    public abstract double getDist (int i, int j);
}

