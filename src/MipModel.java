
import edu.princeton.cs.algs4.In;
import ilog.concert.*;
import ilog.cplex.IloCplex;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.alg.connectivity.ConnectivityInspector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** 一个混合整数规划求解ESPPRC的模型（可包含三个有效不等式）
 /* 本求解方法的算例使用Solomon数据集，在改变算例的客户个数时请在VrpLibReader.java中更改客户数量Size
 */

public class MipModel {
    private final Scenario sce_;

    public static class Callback extends IloCplex.LazyConstraintCallback {
        double eps = 1.0e-6;
        private final IloCplex cplex_;
        private final IloIntVar[][] arcvars_;
        private final GraphModel g_;
        private final Scenario s_;

        private boolean flag=false;

        Callback(IloCplex cplex, IloIntVar[][] arcVars, GraphModel g, Scenario sce) {
            this.cplex_ = cplex;
            this.arcvars_ = arcVars;
            this.g_=g;
            s_ = sce;
        }

        public void main() throws IloException {
            int numVertices = g_.graph_.vertexSet().size();
            int v_size = g_.graph_.vertexSet().size();

            //定义三点切割不等式的约束表达式
            IloLinearIntExpr[][][] three_point= new IloLinearIntExpr[numVertices][numVertices][numVertices];


            //三点切割不等式
            //遍历所有的节点作为三点中的终点
            //在分支节点添加
            if(flag==false) {
                for (GraphModel.CustomVertex end : g_.graph_.vertexSet()) {
                    //k作为终点
                    int k = end.demand_.index;
                    //排除掉最后一个点
                    if (k != (v_size - 1)&&(k!=0)) {
                        //寻找一个节点i和节点j
                        for (GraphModel.CustomVertex start_first : g_.graph_.vertexSet()) {
                            int i = start_first.demand_.index;
                            if ((i != k) && (i != (v_size - 1))&&(i!=0)) {
                                for (GraphModel.CustomVertex start_second : g_.graph_.vertexSet()) {
                                    int j = start_second.demand_.index;
                                    if ((j > i) && (j != k) && (j != (v_size - 1))) {
                                        three_point[i][j][k] = cplex_.linearIntExpr();
                                        three_point[i][j][k].addTerm(1, arcvars_[i][k]);
                                        three_point[i][j][k].addTerm(1, arcvars_[j][k]);
                                        for (DefaultWeightedEdge k_out : g_.graph_.outgoingEdgesOf(end)) {
                                            GraphModel.CustomVertex temp = g_.graph_.getEdgeTarget(k_out);
                                            int l = temp.demand_.index;
                                            three_point[i][j][k].addTerm(-1, arcvars_[k][l]);
                                        }
                                        add(cplex_.le(three_point[i][j][k], 0));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static class UCallback extends IloCplex.UserCutCallback {
        double eps = 1.0e-6;
        private final IloCplex cplex_;
        private final IloIntVar[][] arcvars_;
        private final GraphModel g_;
        private final Scenario s_;

        private boolean flag=false;

        UCallback(IloCplex cplex, IloIntVar[][] arcVars, GraphModel g, Scenario sce) {
            this.cplex_ = cplex;
            this.arcvars_ = arcVars;
            this.g_=g;
            s_ = sce;
        }

        public void main() throws IloException {
            int numVertices = g_.graph_.vertexSet().size();
            int v_size = g_.graph_.vertexSet().size();
            //定义两点切割不等式的约束表达式
            IloLinearIntExpr[][] two_point= new IloLinearIntExpr[numVertices][numVertices];

            //两点切割不等式
            //在根节点添加
            if(flag==false) {
                for (DefaultWeightedEdge e : g_.graph_.edgeSet()) {
                    //边的起点，边的终点
                    GraphModel.CustomVertex start_e = g_.graph_.getEdgeSource(e);
                    GraphModel.CustomVertex end_e = g_.graph_.getEdgeTarget(e);
                    int i = start_e.demand_.index;
                    int j = end_e.demand_.index;
                    if (j != (v_size - 1)&& (start_e.demand_.timeWindows.start + start_e.demand_.duration + g_.graph_.getEdgeWeight(e) <= end_e.demand_.timeWindows.end) && (start_e.demand_.value + end_e.demand_.value <= s_.vehicle.capacity)) {

                        two_point[i][j] = cplex_.linearIntExpr();
                        two_point[i][j].addTerm(1, arcvars_[i][j]);
                        two_point[i][j].addTerm(1, arcvars_[j][i]);
                        //遍历所有j的出度
                        for (DefaultWeightedEdge j_out : g_.graph_.outgoingEdgesOf(end_e)) {
                            //边的终点
                            GraphModel.CustomVertex j_out_k = g_.graph_.getEdgeTarget(j_out);
                            int k = j_out_k.demand_.index;

                            two_point[i][j].addTerm(-1, arcvars_[j][k]);
                        }
                        add(cplex_.le(two_point[i][j], 0));
                        flag=true;
                    }
                }
            }
        }
    }

    //传入图和所有参数进入求解
    public MipModel(GraphModel g, Scenario sce)
    {
        sce_ = sce;
        try {
            IloCplex model_ = new IloCplex();
            int numVertices = g.graph_.vertexSet().size();
            int numEdges = g.graph_.edgeSet().size();
            /* 定义弧边整数变量的集合 */
            IloIntVar[][] arcVars = new IloIntVar[numVertices][numVertices];

            //定义到达节点时间的集合
            IloNumVar[] tao_time = new IloNumVar[numVertices];

            /* 顶点出度和入度的约束，这里是整数线性表达式 */
            IloLinearIntExpr second = model_.linearIntExpr();
            IloLinearIntExpr third = model_.linearIntExpr();
            IloLinearIntExpr[] forth = new IloLinearIntExpr[numVertices];


            //时间窗约束的表达式
            /* 设置一个MTZ约束来避免子环路，这里是实数线性表达式 */
            /* 子环路 Miller-Tucker-Zemlin */
            IloLinearNumExpr[][] mtz_cons = new IloLinearNumExpr[numVertices][numVertices];

            //定义容量约束不等式
            IloLinearIntExpr value_sum = model_.linearIntExpr();

            //定义时间增强的约束表达式
            IloLinearNumExpr[] time_enforce = new IloLinearNumExpr[numVertices];

            //定义访问次序增强的约束表达式
            IloLinearNumExpr[] subsequence_enforce = new IloLinearNumExpr[numVertices];


            /* define objective function */
            /* 定义目标函数 */
            IloLinearNumExpr obj = model_.linearNumExpr();

            /* instance parameters */
            /* 实例参数 */
            int v_capacity = sce_.vehicle.capacity;
            int v_size = g.graph_.vertexSet().size();
//          num_vechicles默认为1

            GraphModel.CustomVertex v_0 = null;
            GraphModel.CustomVertex v_n_1 = null;
            for(GraphModel.CustomVertex v : g.graph_.vertexSet()) {
                if (v.demand_.index == 0) {
                    v_0 = v;
                }
                if (v.demand_.index == (v_size - 1)) {
                    v_n_1 = v;
                }
            }


            //移除不符合需求的节点
            for(GraphModel.CustomVertex v : g.graph_.vertexSet())
            {
                if(v_0==v||v_n_1==v)
                {
                    continue;
                }
                //节点0到i的运行时间
                double t1 = sce_.getDistMetric().getDist(v_0.demand_.index,v.demand_.index);
                //时间窗左边
                int a_i = v.demand_.timeWindows.start;
                //节点i到n+1的时间
                double t2 = sce_.getDistMetric().getDist(v.demand_.index,v_n_1.demand_.index);
                //节点n+1时间窗右侧
                int b_n_1 = v_n_1.demand_.timeWindows.end;
                if(Math.max(t1,a_i)+t2>b_n_1)
                {
                    g.graph_.removeVertex(v);
                }
            }

            /* 初始化变量，定义所有的aij为0-1变量，对应小论文第8个式子 */
            //numVertices等于顶点数27
            for(int i = 0; i < numVertices; i++)
            {
                //int id_i = i+1;
                //初始化到达节点的时间
                tao_time[i] = model_.numVar(0, 1e15, "arrive time"+ i);
                for(int j = 0; j < numVertices ; j++) {
                    //int id_j = j+1;
                    if(i==j)
                    {
                        arcVars[i][j] = model_.intVar(0,0, "arcVar" + i + j);
                    }else if ((i==0&&j==(v_size-1))||(i==(v_size-1)&&j==0))
                    {
                        arcVars[i][j] = model_.intVar(0,0, "arcVar" + i + j);
                    }
                    else {
                        arcVars[i][j] = model_.intVar(0, 1, "arcVar" + i + j);
                    }
                }
            }



            /* formulate objective function
             * 制定目标函数 */
            //遍历所有的边，DefaultWeightedEdge是默认带权边，edgeSet()遍历全部边的Set集合
            for(DefaultWeightedEdge e : g.graph_.edgeSet())
            {
                //边的起点，边的终点
                GraphModel.CustomVertex start_e = g.graph_.getEdgeSource(e);
                GraphModel.CustomVertex end_e = g.graph_.getEdgeTarget(e);
                int i = start_e.demand_.index;
                int j = end_e.demand_.index;
                double[] temp = new double[v_size];
                //getEdgeWeight获取权值 Cij运输成本 Xij是整数变量判断是否走过这条边
                //请注意Cij运输成本由距离（该条边的权重）减去随机整数
                Random random = new Random();
                //生成0到20的随机整数
                int r = random.nextInt(21);
                double cij = g.graph_.getEdgeWeight(e) - r ;
                if(i==0)
                {
                    temp[j] = cij;
                }
                if(i==(v_size-1))
                {
                    cij = temp[j];
                }
                //addTerm方法是添加单项式，前面是系数，后面是变量
                obj.addTerm(cij, arcVars[i][j]);
            }

            /* formulate constraints
             * 制定约束条件 */
            for(GraphModel.CustomVertex v : g.graph_.vertexSet()) {
                //获取该顶点下标
                int v_id = v.demand_.index;
                //第二个式子
                if (v_id == 0) {
                    second = model_.linearIntExpr();
                    //遍历节点0的所有出弧
                    for (DefaultWeightedEdge e : g.graph_.outgoingEdgesOf(v)) {
                        //获取出弧(i,j)的节点j
                        GraphModel.CustomVertex j_v = g.graph_.getEdgeTarget(e);
                        int j_id = j_v.demand_.index;
                        second.addTerm(1, arcVars[v_id][j_id]);//前面
                    }
                    //遍历节点0的所有入弧
                    for (DefaultWeightedEdge e : g.graph_.incomingEdgesOf(v)) {
                        //获取入弧(j,i)的节点j
                        GraphModel.CustomVertex j_v = g.graph_.getEdgeSource(e);
                        int j_id = j_v.demand_.index;
                        second.addTerm(-1, arcVars[j_id][v_id]);
                    }
                    model_.addEq(second, 1);

                }//第三个式子
                else if(v_id == (v_size-1))
                {
                    third = model_.linearIntExpr();
                    //遍历节点n+1的所有出弧
                    for(DefaultWeightedEdge e : g.graph_.outgoingEdgesOf(v))
                    {
                        //获取出弧(i,j)的节点j
                        GraphModel.CustomVertex j_v = g.graph_.getEdgeTarget(e);
                        int j_id = j_v.demand_.index;
                        third.addTerm(1, arcVars[v_id][j_id]);//前面
                    }
                    //遍历节点n+1的所有入弧
                    for(DefaultWeightedEdge e : g.graph_.incomingEdgesOf(v))
                    {
                        //获取入弧(j,i)的节点j
                        GraphModel.CustomVertex j_v = g.graph_.getEdgeSource(e);
                        int j_id = j_v.demand_.index;
                        third.addTerm(-1, arcVars[j_id][v_id]);//后面
                    }
                    model_.addEq(third,-1);
                }
            }
            for(GraphModel.CustomVertex v : g.graph_.vertexSet())
            {
                int v_id = v.demand_.index;
                if(v_id!=0 && v_id!=(v_size-1))
                {
                    //第四个式子
                    forth[v_id] = model_.linearIntExpr();

                    //遍历顾客节点i属于N的所有出弧
                    for (DefaultWeightedEdge e : g.graph_.outgoingEdgesOf(v)) {
                        //获取出弧(i,j)的节点j
                        GraphModel.CustomVertex j_v = g.graph_.getEdgeTarget(e);
                        int j_id = j_v.demand_.index;
                        forth[v_id].addTerm(1, arcVars[v_id][j_id]);//前面
                    }
                    //遍历顾客节点i属于N的所有入弧
                    for (DefaultWeightedEdge e : g.graph_.incomingEdgesOf(v)) {
                        //获取入弧(j,i)的节点j
                        GraphModel.CustomVertex j_v = g.graph_.getEdgeSource(e);
                        int j_id = j_v.demand_.index;
                        forth[v_id].addTerm(-1, arcVars[j_id][v_id]);//后面
                    }
                    model_.addEq(forth[v_id], 0);
                }
            }

            //小论文第5式，添加时间窗约束
            for(DefaultWeightedEdge e : g.graph_.edgeSet()) {
                //边的起点，边的终点
                GraphModel.CustomVertex start_e = g.graph_.getEdgeSource(e);
                GraphModel.CustomVertex end_e = g.graph_.getEdgeTarget(e);
                int i = start_e.demand_.index;
                int j = end_e.demand_.index;
                int duration = start_e.demand_.duration;
                double travel_time = g.graph_.getEdgeWeight(e);
                double M_ij = Math.max((start_e.demand_.timeWindows.end + duration + travel_time - end_e.demand_.timeWindows.start), 0);
                double right = M_ij - travel_time - duration;
                mtz_cons[i][j] = model_.linearNumExpr();
                mtz_cons[i][j].addTerm(1, tao_time[i]);
                mtz_cons[i][j].addTerm(-1, tao_time[j]);
                mtz_cons[i][j].addTerm(M_ij, arcVars[i][j]);
                model_.addLe(mtz_cons[i][j], right);
            }

            //小论文第六式
            for(GraphModel.CustomVertex v : g.graph_.vertexSet())
            {
                int v_id = v.demand_.index;
                model_.addGe(tao_time[v_id], v.demand_.timeWindows.start);
                model_.addLe(tao_time[v_id], v.demand_.timeWindows.end);
            }

            //小论文第七式
            for(DefaultWeightedEdge e : g.graph_.edgeSet())
            {
                //边的起点，边的终点
                GraphModel.CustomVertex start_e = g.graph_.getEdgeSource(e);
                GraphModel.CustomVertex end_e = g.graph_.getEdgeTarget(e);
                int i = start_e.demand_.index;
                int j = end_e.demand_.index;
                value_sum = model_.linearIntExpr();
                value_sum.addTerm(start_e.demand_.value, arcVars[i][j]);
            }
            model_.addLe(value_sum, v_capacity);

            //时间增强不等式，遍历所有的节点i
            for(GraphModel.CustomVertex i : g.graph_.vertexSet()) {
                //找到符合的节点j
                for (GraphModel.CustomVertex j : g.graph_.vertexSet()) {
                    if ((i.demand_.timeWindows.start + Math.min(i.demand_.loc.distanceTo(j.demand_.loc) + i.demand_.duration, i.demand_.timeWindows.getDurationS()) > j.demand_.timeWindows.end)&&(i.demand_.timeWindows.start-j.demand_.timeWindows.end-j.demand_.duration-i.demand_.loc.distanceTo(j.demand_.loc)<=0)) {
                        time_enforce[i.demand_.index]= model_.linearNumExpr();
                        double dist = i.demand_.loc.distanceTo(j.demand_.loc);
                        double left = j.demand_.timeWindows.end - i.demand_.timeWindows.start + i.demand_.duration + dist;
                        double right = i.demand_.timeWindows.start - j.demand_.timeWindows.end - j.demand_.duration - dist;
                        //找到i的所有后置节点
                        for (DefaultWeightedEdge i_out : g.graph_.outgoingEdgesOf(i)) {
                            GraphModel.CustomVertex i_out_end = g.graph_.getEdgeTarget(i_out);
                            int index = i_out_end.demand_.index;
                            time_enforce[i.demand_.index].addTerm((-1 * left), arcVars[i.demand_.index][index]);
                        }
                        //找到j所有的后置节点
                        for (DefaultWeightedEdge j_out : g.graph_.outgoingEdgesOf(j)) {
                            GraphModel.CustomVertex j_out_end = g.graph_.getEdgeTarget(j_out);
                            int index = j_out_end.demand_.index;
                            time_enforce[i.demand_.index].addTerm(right, arcVars[j.demand_.index][index]);
                        }
                        time_enforce[i.demand_.index].addTerm(1, tao_time[i.demand_.index]);
                        time_enforce[i.demand_.index].addTerm(-1, tao_time[j.demand_.index]);
                        model_.addGe(time_enforce[i.demand_.index] ,j.demand_.duration + dist + right - left);
                    }
                }
            }

            //增强访问次序不等式
            for(GraphModel.CustomVertex i : g.graph_.vertexSet()) {
                subsequence_enforce[i.demand_.index] = model_.linearNumExpr();
                //找到符合的节点j
                for (GraphModel.CustomVertex j : g.graph_.vertexSet()) {
                    if ((i.demand_.timeWindows.start + Math.min(i.demand_.loc.distanceTo(j.demand_.loc) + i.demand_.duration, i.demand_.timeWindows.getDurationS()))>j.demand_.timeWindows.end) {
                        //找到符合的节点k
                        for(GraphModel.CustomVertex k : g.graph_.vertexSet())
                        {
                            if(k.demand_.timeWindows.start>i.demand_.timeWindows.end)
                            {
                                subsequence_enforce[i.demand_.index].addTerm(1,arcVars[j.demand_.index][k.demand_.index]);
                            }
                        }
                    }
                }
                //找到i的所有后置节点
                for (DefaultWeightedEdge i_out : g.graph_.outgoingEdgesOf(i)) {
                    GraphModel.CustomVertex i_out_end = g.graph_.getEdgeTarget(i_out);
                    int index = i_out_end.demand_.index;
                    subsequence_enforce[i.demand_.index].addTerm(1, arcVars[i.demand_.index][index]);
                }
                model_.addLe(subsequence_enforce[i.demand_.index],1);
            }

            //添加割平面约束
            model_.use(new Callback(model_, arcVars, g, sce));
            //添加割平面约束
            model_.use(new UCallback(model_, arcVars, g, sce));

            //模型最小化
            model_.addMinimize(obj);

            model_.exportModel("ESPPRC.lp");
            model_.setParam(IloCplex.Param.MIP.Display, 3);
            model_.setParam(IloCplex.Param.TimeLimit, 500);
            if(model_.solve())
            {
                System.out.println("Solution status: " + model_.getStatus());
                System.out.println("Obj: " + model_.getObjValue());
                System.out.println("Best gap is: "+model_.getMIPRelativeGap());
                System.out.println("Solution time: "+model_.getCplexTime());
                System.out.println("Node: "+model_.getIncumbentNode());
                model_.writeSolution("assignSolution.txt");
            }



            /* update graph g with values from the new solution */
            for(DefaultWeightedEdge e : g.graph_.edgeSet())
            {
                int start = g.graph_.getEdgeSource(e).demand_.index;
                int end = g.graph_.getEdgeTarget(e).demand_.index;
                g.graph_.setEdgeWeight(e, model_.getValue(arcVars[start][end]));
            }

        }catch(IloException e){
            System.err.println("Error: " + e.getMessage());
            System.exit(-1);
        }
    }

}