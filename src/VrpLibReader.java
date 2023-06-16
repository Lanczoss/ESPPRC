import edu.princeton.cs.algs4.In;

import java.io.*;
import java.util.*;

public class VrpLibReader {
    /**
     * The file we are reading
     */
    protected String filename;

    /**
     * A scanner to do the reading
     */
    protected Scanner scanner;

    /**
     * Problem data
     */
    protected int size;
    protected int capacity;
    protected int numVehicles;

    /**
     * Construct a vrpLibReader
     */
    public VrpLibReader(String filename) {
        this.filename = filename;

        File in = new File(filename);
        try {
            scanner = new Scanner(in);
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + filename);
            System.exit(1);
        }
    }

    public Scenario read() {
        //从arg[0]读到文件名
        System.out.println("Reading " + filename);
        //将文件名作为Scenario id传入
        Scenario scenario = new Scenario(filename);
        //跳过四行
        for (int i = 0; i < 4; i++) {
            scanner.nextLine();
        }
        //跳过第一个数字
        scanner.next();
        //获取CAPACITY
        String tag = scanner.next();
        capacity = Integer.parseInt(tag);
        //if (Do.debug) Debug.println ('D', "  Capacity : " + capacity);
        //跳过四行
        for (int i = 0; i < 4; i++) {
            scanner.nextLine();
        }

        //算例有几个客户，记得改一下
        size = 100;
        int size_d = size +2;

        //车辆默认为1
        numVehicles = 1;

        //key和value的关系
        HashMap<String, Location> nodes = new HashMap<String, Location>(1000);


        while (scanner.hasNext()) {

            //next()方法碰到空格结束扫描

            //if (Do.debug) Debug.println ('D', "Tag: " + tag);

            for (int i = 0; i < size+1; i++) {
                //数据第一行第一个数字开始录入
                String id = scanner.next();
                double x = scanner.nextDouble();
                double y = scanner.nextDouble();
                Location loc = new Location(id, x, y);
                //获取需求
                int value = scanner.nextInt();


                //扫描开始时间和结束时间
                int time1 = scanner.nextInt();
                int time2 = scanner.nextInt();
                Interval timeWindows = new Interval(time1, time2);
                int duration = scanner.nextInt();
                if(id.equals("0")){
                    //录入车辆信息
                    Vehicle vehicle = new Vehicle(timeWindows, loc, loc, capacity);
                    scenario.addVehicle(vehicle);
                }
                //录入坐标
                nodes.put(id, loc);
                Demand demand =
                        new Demand(id, loc, value, timeWindows, duration);

                //添加该节点需求
                scenario.addDemand(demand);
                if(id.equals(Integer.toString(size)))
                {
                    id = Integer.toString(size_d-1);
                    x = scenario.vehicle.startLoc.getX();
                    y = scenario.vehicle.startLoc.getY();
                    loc = new Location(id, x, y);
                    value = 0;
                    timeWindows = scenario.vehicle.avail;
                    duration = scenario.vehicle.avail.getDurationS();
                    //录入坐标
                    nodes.put(id, loc);
                    demand =
                            new Demand(id, loc, value, timeWindows, duration);

                    //添加该节点需求
                    scenario.addDemand(demand);
                }
            }
        }

        DistMatrix dist = new DistMatrix("ESPPRC", size_d);
        //获取nodes中所有的location对象，存放所有点之间的距离
        dist.setUsingLocations(nodes.values());
        scenario.setDistMetric(dist);
        //由于距离和运行时间一致（车速为1），先不录入到timemetric中
        //scenario.setTimeMetric (new UnitTimeMetric ("time", dist));

        return scenario;
    }

    private void error(String message) {
        System.err.println("Error reading VRPLIB file " + filename);
        System.err.println(message);
        System.exit(1);
    }

    public String toString() {
        return filename;
    }
}

