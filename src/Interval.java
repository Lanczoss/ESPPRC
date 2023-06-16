
/** A time interval, and associated actions.
 * Note that the interval end is a c++-style "end" - it is one time-step
 * beyond the actual finish - hence a non-overlapping interval can
 * commence with start time == this one's end time.
 *
 */

public class Interval implements Comparable
{
    /** Start of period */
    protected int start;

    /** The end time  */
    protected int end;

    /** New interval with given start and end times.
     *  Both times are inclusive
     */
    public Interval (int start, int end)
    {
        //if (Do.debug)
            //Debug.assert_ (start <= end, "Start " + start + " > end " + end);
        this.start = start;
        this.end = end;
    }

    /** Return the start of interval */
    public double getStart () {return start;}
    /** return duration of interval in seconds */
    public int getDurationS () {return end - start;}
    /** Return the time at the second after then end of the interval */
    public double getEnd () {return end;}
    /** Set start time */
    public void setStart (int start) {this.start = start;}
    /** Set end time */
    public void setEnd (int end) {this.end = end;}


    /** Move forward by a delta */
    public void advance (double offsetS)
    {
        start += offsetS;
        end += offsetS;
    }

    /** Extend by delta time */
    public void extend (double offsetS)
    {
        end += offsetS;
    }

    /** Is this interval strictly before another */
    public boolean isBefore (Interval other)
    {
        return end < other.start;
    }

    /** Is this interval strictly after another */
    public boolean isAfter (Interval other)
    {
        return start > other.end;
    }

    /** Do two intervals overlap? */
    public boolean overlaps (Interval other)
    {
        return !isAfter(other) && !isBefore(other);
    }

    /** Does the interval include a given time */
    public boolean includes (double time)
    {
        return start <= time && time <= end;
    }

    /** Does this interval follow or include the given time */
    public boolean follows (double time)
    {
        return time <= start;
    }


    /** Is this time the same value as another */
    public boolean equals (Object obj)
    {
        if (!(obj instanceof Interval))
            return false;

        Interval other = (Interval)obj;
        return
                (Math.abs (start - other.start) < 0.1) &&
                        (Math.abs (end - other.end) < 0.1);
    }

    /** Copy the given time */
    public void copy (Interval other)
    {
        this.start = other.start;
        this.end = other.end;
    }

    public int compareTo(Object obj)
    {
        Interval other  = (Interval)obj;
        if(start < other.start)
            return -1;
        else if(start > other.start)
            return 1;
        else if (end < other.end)
            return -1;
        else if (end > other.end)
            return 1;
        else
            return 0;
    }


    /** Return the string value of the integer */
    public String toString()
    {
        //return "[" + Time.format(start) + "-" + Time.format(end) + "]";
        return "[" + start + "-" + end + "]";
    }
}

