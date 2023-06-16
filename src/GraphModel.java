
import edu.princeton.cs.algs4.StdDraw;
import ilog.cplex.IloCplex;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.awt.*;

import java.util.ArrayList;
import java.util.List;

//一个基于VRPTW的ESPPRC模型，图使用jgrapht API生成

public class GraphModel {
    static class CustomVertex
    {
        Demand demand_;//定义顶点的需求（包括容量、时间窗口）

        //public CustomVertex(){}

        public CustomVertex(Demand demand)
        {
            this.demand_ = demand;
        }

        public Demand getDemand()
        {
            return demand_;
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();//可修改的字符串
            sb.append(demand_.id);
            return sb.toString();
        }
    }


    //jgrapht无向带权图
    DefaultDirectedWeightedGraph<CustomVertex, DefaultWeightedEdge> graph_
            = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
    String name_;


//    generate a graph from a MIP solution
//    public GraphModel(IloCplex model)
//    {
//
//    }

//    generate a graph from an input file
    public GraphModel(Scenario sce)
    {
        name_ = sce.id;
        //数组存储点和数组存储边
        List<CustomVertex> vertices = new ArrayList<>();
        List<DefaultWeightedEdge> edges = new ArrayList<>();
        for(Demand dem : sce.demands)
        {
            CustomVertex v = new CustomVertex(dem);
            //将所有的
            graph_.addVertex(v);
            vertices.add(v);
        }
        //假设录入的Solomon数据集为1个配送中心和25个客户，遍历全部节点之间的距离，并添加边的权重（距离）
        for(int i = 0; i < sce.getNumDemands()-1; i++)
        {
            CustomVertex a = vertices.get(i);
            for(int j = i+1; j < sce.getNumDemands(); j++)
            {
                CustomVertex b = vertices.get(j);
                double dist_a_b = a.demand_.loc.distanceTo(b.demand_.loc);
                System.out.println("distance is " + dist_a_b);
                String  str = String.format("%.2f",dist_a_b);
                double dist = Double.parseDouble(str);
                DefaultWeightedEdge e1 = graph_.addEdge(a, b);
                DefaultWeightedEdge e2 = graph_.addEdge(b, a);
                graph_.setEdgeWeight(e1, dist);
                graph_.setEdgeWeight(e2, dist);
                edges.add(e1);
                edges.add(e2);
            }
        }
    }

//    public void plot_initial(){
//        StdDraw.setXscale(0,100);
//        StdDraw.setYscale(0,100);
//        Font font = new Font("Arial", Font.PLAIN, 10);
//        StdDraw.setFont(font);
//        for(CustomVertex v : graph_.vertexSet())
//        {
//            if(v.depot_==true) {
//                StdDraw.setPenColor(Color.RED);
//                System.out.println("this node is a depot node");
//            }
//            else
//                StdDraw.setPenColor(Color.BLUE);
//            StdDraw.filledCircle(v.demand_.loc.getX(), v.demand_.loc.getY(), 1);
//        }
//
//        for(DefaultWeightedEdge e : graph_.edgeSet())
//        {
//            CustomVertex e_start = graph_.getEdgeSource(e);
//            CustomVertex e_end = graph_.getEdgeTarget(e);
//            double start_x = e_start.demand_.loc.getX();
//            double start_y = e_start.demand_.loc.getY();
//            double end_x = e_end.demand_.loc.getX();
//            double end_y = e_end.demand_.loc.getY();
//            StdDraw.setPenColor(Color.GRAY);
//            StdDraw.line(start_x, start_y, end_x, end_y);
//        }
//    }
//
public void plot_solution() {
    StdDraw.setXscale(0, 100);
    StdDraw.setYscale(0, 100);
    Font font = new Font("Arial", Font.PLAIN, 10);
    StdDraw.setFont(font);
    for (CustomVertex v : graph_.vertexSet()) {
        StdDraw.setPenColor(Color.black);
        StdDraw.filledCircle(v.demand_.loc.getX(), v.demand_.loc.getY(), 1);
        StdDraw.text(v.demand_.loc.getX() + 2, v.demand_.loc.getY() + 2, v.demand_.id);
    }

    for (DefaultWeightedEdge e : graph_.edgeSet()) {
        CustomVertex e_start = graph_.getEdgeSource(e);
        CustomVertex e_end = graph_.getEdgeTarget(e);
        double start_x = e_start.demand_.loc.getX();
        double start_y = e_start.demand_.loc.getY();
        double end_x = e_end.demand_.loc.getX();
        double end_y = e_end.demand_.loc.getY();
        StdDraw.setPenColor(Color.red);
        if (graph_.getEdgeWeight(e) == 1)
        {System.out.println("arcVars"+e_start.demand_.index+ e_end.demand_.index+" = 1");
            StdDraw.line(start_x, start_y, end_x, end_y);}
    }
}

    public static void main(String[] args)
    {
        String filename = null;
        if(args[0] == null)
            System.out.println("No Input Data Specified!");
        else
            filename = args[0];

        VrpLibReader reader = new VrpLibReader (filename);
        Scenario scenario = reader.read();

        //此时图拥有所有节点和边的权重（节点已包含时间窗、需求和服务时间）
        GraphModel graph = new GraphModel(scenario);

        //采用混合整数规划来进行求解
        MipModel mip = new MipModel(graph, scenario);
        graph.plot_solution();

    }
}
