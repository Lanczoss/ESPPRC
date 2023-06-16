import java.util.*;

/**
 * Distances stored in an array.
 *
 * A "set" method can accept a collection of customers.
 *
 */

public class DistMatrix extends DistMetric
{
    protected double[][] dist;

    public DistMatrix (String id, int size)
    {
        super (id);

        dist = new double [size][size];
    }

    public void set (int i, int j, double d)
    {
        dist[i][j] = d;
    }

    //使用欧几里得距离公式计算两点的距离
    //nodes.values()返回的是一个Collection<Location>的集合
    public void setUsingLocations (Collection<Location> locations)
    {
        for (Location a : locations) {
            for (Location b : locations) {
                dist[Integer.parseInt(a.id)][Integer.parseInt(b.id)] =
                        a.distanceTo(b);
            }
        }
    }

    public double getDist (Location a, Location b)
    {
        return dist[a.getIndex()][b.getIndex()];
    }

    /** Find the distance from location index i to location index j */
    public double getDist (int i, int j)
    {
        return dist[i][j];
    }
}
