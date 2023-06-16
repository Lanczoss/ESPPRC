import java.io.*;
import java.util.*;

/**
 * A scenario - encapsulates all the elements of a VRP.
 *
 * This version does not consider drivers - drivers are equivalent
 * to vehicles.
 */

public class Scenario {
    /**
     * A display name for the scenario
     */
    protected String id;

    /**
     * The Demands
     */
    protected Vector<Demand> demands;

    /**
     * A map for finding demands
     */
    protected Map<String, Demand> demandMap;

    /**
     * The Vehicle
     */
    protected Vehicle vehicle;

    /**
     * The metric we use for dist
     */
    protected DistMetric distMetric;

    /**
     * The valid objective types
     */
    public enum ObjectiveType {DIST_AND_VALUE}

    ;
    protected ObjectiveType objType;
    /**
     * What sign are demands?
     */
    protected int demandSignum;

    /**
     * Construct a scenario
     */
    public Scenario(String id) {
        //这里是文件名
        this.id = id;
        demands = new Vector<Demand>(1000);
        objType = ObjectiveType.DIST_AND_VALUE;
        demandMap = new HashMap<String, Demand>(1000);

        demandSignum = 0;

    }

    public String getId() {
        return id;
    }

    public ObjectiveType getObjType() {
        return objType;
    }

    public Iterable<Demand> getDemands() {
        return demands;
    }

    public Demand getDemand(int i) {
        return demands.elementAt(i);
    }

    public int getNumDemands() {
        return demands.size();
    }


    public DistMetric getDistMetric() {
        return distMetric;
    }


    public void setId(String id) {
        this.id = id;
    }


    /**
     * Add a metric. If isTimeMetric is true, this metric is used for calculating
     * times. If isObj is true, this metric is used as the objective.
     */
    public void setDistMetric(DistMetric distMetric) {
        this.distMetric = distMetric;
    }


    public void addDemand(Demand demand) {
        demands.add(demand);
        demandMap.put(demand.getId(), demand);
    }

    public void addVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Demand getDemand(String id) {
        return demandMap.get(id);
    }

    public void show(PrintStream out) {
        out.println("Scenario " + id);
        out.println("  Dist metric " + distMetric);

        for (Demand d : demands)
            d.show(out);
    }

//    public Route[] oneRoutePerVeh() {
//        Route[] route = new Route[vehicles.size()];
//        for (int k = 0; k < vehicles.size(); k++) {
//            Vehicle veh = vehicles.elementAt(k);
//            route[k] =
//                    new Route(
//                            "RouteFor" + veh, this, veh,
//                            veh.getStartLoc(), veh.getEndLoc(),
//                            veh.getAvailable().getStart(),
//                            veh.getAvailable().getEnd(),
//                            k
//                    );
//        }
//        return route;
//    }

    public String toString() {
        return id;
    }
}