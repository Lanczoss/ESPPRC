import java.awt.geom.*;
import java.util.*;
/**
 * A location.
 * Name is used for display.
 * X and y are used for display.
 */

public class Location
{
    /** The name */
    protected String id;

    /** Unique index amongst all locations */
    protected int index;

    /** The coords - used only for display */
    protected Point2D coords;

    public Location (String id, double x, double y)
    {
        this.id = id;
        coords = new Point2D.Double(x, y);
    }

    public String getId() {return id;}
    public int getIndex() {return index;}
    public Point2D getCoords() {return coords;}
    public double getX() {return coords.getX();}
    public double getY() {return coords.getY();}

    public double distanceTo (Location other)
    {
        //Point2D中的distance方法使用了欧几里得距离公式
        return coords.distance (other.coords);
    }


    public String toString()
    {
        return id;
    }
}
