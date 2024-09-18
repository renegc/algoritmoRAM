package com.ram.optimization.LogicAlgoritmo.Solution;



import com.ram.optimization.LogicAlgoritmo.Data.InputData;
import com.ram.optimization.LogicAlgoritmo.Data.Vehicle;
import com.ram.optimization.LogicAlgoritmo.Data.Order;
import com.ram.optimization.LogicAlgoritmo.Data.VisitTime;
import com.ram.optimization.LogicAlgoritmo.Solution.LSM.LocalSearchMotion;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Othmane
 */
public class AuxiliaryGraph implements AutoCloseable{
    private int ThreadsCounter;
    private final int Length;
    private final int BoundingCost;
    private final int[] GiantTour;
    private AuxiliaryGraphNode[] Nodes;
    private final InputData Data;
    private final List<Task> Tasks = new LinkedList<>();
    private final ReentrantLock Lock = new ReentrantLock();

    AuxiliaryGraph(InputData data, int[] gt, int bound){
        this.Data = data;
        this.BoundingCost = bound;
        this.ThreadsCounter = 0;
        this.GiantTour = gt;
        this.Length = this.GiantTour.length;
        this.Nodes = new AuxiliaryGraphNode[this.Length + 1]; 
        for(int i = 0; i < this.Nodes.length; i++)
            this.Nodes[i] = new AuxiliaryGraphNode(i);
    }

    void setArcs() throws Exception{
        this.ThreadsCounter++;
        this.run(this.Nodes[0]);
    }

    private boolean getWaitCondition(AuxiliaryGraphNode StartingNode){
        boolean c;
        this.Lock.lock();
        try {
            c = this.Tasks.stream()
                        .filter(t -> t.StartingNode.NodeIndex != StartingNode.NodeIndex)
                        .filter(t -> t.StartingNode.Label < StartingNode.Label)
                        .anyMatch(t -> t.StartingNode.NodeProcessingWith <= StartingNode.NodeProcessingWith);
        }
        finally {
            this.Lock.unlock();
        }
        return c;
    }

    private void setNewThread(AuxiliaryGraphNode node, Set<Thread> Threads){
        int FirstIndex;
        this.Lock.lock();
        try{
            FirstIndex = this.Tasks.stream()
                                .map(t -> t.StartingNode)
                                .mapToInt(StartingNode -> StartingNode.NodeIndex)
                                .min()
                                .orElse(0);
        }
        finally{
            this.Lock.unlock();
        }
        for(int i = FirstIndex; i < node.NodeIndex; i++)
            if(this.Nodes[i].NodeProcessingWith < node.NodeIndex)
                return;
        boolean c = this.ThreadsCounter == node.NodeIndex;
        if(c) {
            this.ThreadsCounter++;
            if(node.isFeasible() && node.Label < this.BoundingCost) {
                Runnable r = () -> {
                                    try {
                                        this.run(node);
                                    } catch (Exception ex) {
                                        Logger.getLogger(AuxiliaryGraph.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                };
                Task t = new Task(r, node);
                t.start();
                Threads.add(t);
                this.Lock.lock();
                try{
                    while(!this.Tasks.isEmpty()
                            && this.Tasks.get(0).StartingNode.NodeProcessingWith == this.Length)
                        this.Tasks.remove(0);
                    this.Tasks.add(t);
                }
                finally{
                    this.Lock.unlock();
                }
            }
            else
                node.NodeProcessingWith = this.Length;
        }
        if(c && node.NodeIndex + 1 < this.Length)
            this.setNewThread(this.Nodes[node.NodeIndex + 1], Threads);
    }
    
    void run(AuxiliaryGraphNode StartingNode) throws Exception {
        int sum_demand_pieces = 0;
        double sum_demand_weight = 0;
        double sum_demand_volume = 0;
        AuxiliaryGraphNode EndingNode;
        Set<Thread> Threads = new HashSet<>(2, 1f);
        for(int i = StartingNode.NodeIndex; i < this.Length; i++) {
            EndingNode = this.Nodes[i + 1];
            Order order = this.Data.Orders.get(this.GiantTour[i]);
            sum_demand_pieces += order.getPieces();
            sum_demand_weight += order.getWeight();
            sum_demand_volume += order.getVolume();
            if(StartingNode.Label >= EndingNode.Label){
                StartingNode.NodeProcessingWith++;
                synchronized(this.Tasks){
                    this.Tasks.notifyAll();
                }
                if(EndingNode.NodeIndex < this.Length)
                    this.setNewThread(EndingNode, Threads);
                continue;
            }
            synchronized(this.Tasks) {
                while(this.getWaitCondition(StartingNode))
                    this.Tasks.wait();
            }
            int[] sequence = IntStream.range(StartingNode.NodeIndex, EndingNode.NodeIndex)
                                    .map(index -> this.GiantTour[index])
                                    .toArray();
            boolean Break = true;
            for(Vehicle vehicle : this.Data.Vehicles.values()) {
                if(vehicle.getMaxPieces() < sum_demand_pieces
                    || vehicle.getMaxWeight() < sum_demand_weight
                    || vehicle.getMaxVolume() < sum_demand_volume
                    || vehicle.getMaxStops() < sequence.length + StartingNode.getSumStops(vehicle)
                    || (vehicle.getRouteType() == 2 && StartingNode.Routes
                                                            .stream()
                                                            .anyMatch(r -> r.AssignedVehicle.getId() == vehicle.getId()))
                    || IntStream.of(sequence)
                                .mapToObj(this.Data.Orders::get)
                                .anyMatch(ordr -> !ordr.getSkillsFit(vehicle)))
                    continue;
                Break = false;
                VisitTime ready_time = StartingNode.getReadyTime(vehicle);
                Route new_route = new Route(sequence.clone(), vehicle, ready_time);
                double traveled_distance = StartingNode.getTraveled_distance(vehicle);
                boolean c = vehicle.getRouteType() < 3 || StartingNode.Routes
                                                                .stream()
                                                                .allMatch(r -> r.AssignedVehicle.getId() != vehicle.getId());
                boolean isfeasible = new_route.isFeasible(this.Data, traveled_distance);
                if(c && isfeasible)
                    EndingNode.UpdateLabel(StartingNode, new_route);
                else if (c || (vehicle.getRouteType() == 3 && StartingNode.Routes
                                                                    .stream()
                                                                    .allMatch(r -> r.AssignedVehicle.getId() != vehicle.getId()))) {
                    if(new_route.Improve(this.Data, traveled_distance, ready_time, isfeasible))
                        EndingNode.UpdateLabel(StartingNode, new_route);
                    else
                        new_route.close();
                }
                if(ready_time.compare(new VisitTime(vehicle.getShift().getEarliest())) != 0)
                    for(Route old_route : StartingNode.Routes) {
                        if(old_route.AssignedVehicle.getId() != vehicle.getId() || old_route.TripEndTime.compare(ready_time) != 0)
                            continue;
                        double cumulative_traveled_distance = StartingNode.getTraveled_distance(vehicle, old_route);
                        if(vehicle.getMaxPieces() >= sum_demand_pieces + old_route.SumDemandPieces
                            && vehicle.getMaxWeight() >= sum_demand_weight + old_route.SumDemandWeight
                            && vehicle.getMaxVolume() >= sum_demand_volume + old_route.SumDemandVolume) {
                            int[] sequence1 = IntStream.range(0, old_route.Sequence.length + sequence.length)
                                                    .map(index -> {
                                                        if(index < old_route.Length)
                                                            return old_route.Sequence[index];
                                                        return sequence[index - old_route.Sequence.length];
                                                    })
                                                    .toArray();
                            Route new_route1 = new Route(sequence1, vehicle, old_route.TripLoadingStartTime);
                            if(new_route1.isFeasible(this.Data, cumulative_traveled_distance))
                                EndingNode.UpdateLabel(StartingNode, old_route, new_route1);
                            else {
                                if(new_route1.Improve(this.Data, cumulative_traveled_distance, old_route.TripLoadingStartTime, false))
                                    EndingNode.UpdateLabel(StartingNode, old_route, new_route1);
                                else
                                    new_route1.close();
                            }
                            int[] sequence2 = IntStream.range(0, old_route.Sequence.length + sequence.length)
                                                    .map(index -> {
                                                        if(index < sequence.length)
                                                            return sequence[index];
                                                        return old_route.Sequence[index - sequence.length];
                                                    })
                                                    .toArray();
                            Route new_route2 = new Route(sequence2, vehicle, old_route.TripLoadingStartTime);
                            if(new_route2.isFeasible(this.Data, cumulative_traveled_distance))
                                EndingNode.UpdateLabel(StartingNode, old_route, new_route2);
                            else {
                                if(new_route2.Improve(this.Data, cumulative_traveled_distance, old_route.TripLoadingStartTime, false))
                                    EndingNode.UpdateLabel(StartingNode, old_route, new_route2);
                                else
                                    new_route2.close();
                            }
                        }
//                        if(vehicle.getRouteType() == 1) {
//                            LocalSearchMotion lsm = old_route.getLSM(this.Data, new Route(sequence.clone(), vehicle, ready_time), cumulative_traveled_distance, isfeasible);
//                            if(lsm != null && lsm.getRoute2() == null)
//                                lsm.getRoute1().Improve(this.Data, traveled_distance, old_route.TripLoadingStartTime, true);
//                            EndingNode.UpdateLabel(StartingNode, old_route, lsm);
//                        }
                        break;
                     }
            }
            if(Break)
                for(Route old_route:StartingNode.Routes)
                    if((old_route.AssignedVehicle.getMaxPieces() < sum_demand_pieces
                        || old_route.AssignedVehicle.getMaxWeight() < sum_demand_weight
                        || old_route.AssignedVehicle.getMaxVolume() < sum_demand_volume)
                        && old_route.AssignedVehicle.getRouteType() == 1
                        && old_route.TripEndTime.compare(StartingNode.getReadyTime(old_route.AssignedVehicle)) == 0
                        && old_route.AssignedVehicle.getMaxStops() >= sequence.length + StartingNode.getSumStops(old_route.AssignedVehicle)
                        && IntStream.of(sequence)
                                    .mapToObj(this.Data.Orders::get)
                                    .allMatch(ordr -> ordr.getSkillsFit(old_route.AssignedVehicle))) {
                            VisitTime ready_time = StartingNode.getReadyTime(old_route.AssignedVehicle);
                            double traveled_distance = StartingNode.getTraveled_distance(old_route.AssignedVehicle, old_route);
                            LocalSearchMotion lsm = old_route.getLSM(this.Data, new Route(sequence.clone(), old_route.AssignedVehicle, ready_time), traveled_distance, false);
                            if(lsm != null && lsm.getRoute2() == null)
                                lsm.getRoute1().Improve(this.Data, traveled_distance, old_route.TripLoadingStartTime, true);
                            EndingNode.UpdateLabel(StartingNode, old_route, lsm);
                    
                    }
            StartingNode.NodeProcessingWith = Break ? this.Length : StartingNode.NodeProcessingWith + 1;
            synchronized(this.Tasks) {
                this.Tasks.notifyAll();
            }
            if(EndingNode.NodeIndex < this.Length)
                this.setNewThread(EndingNode, Threads);
            if(Break)
                break;
        }
        Threads.forEach(t -> {
                        try{
                            t.join();
                        }catch(InterruptedException ex) {
                            Logger.getLogger(Solution.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });
        Threads.clear();
    }

    AuxiliaryGraphNode getLastNode(){
        return this.Nodes[this.Length];
    }

    boolean isFeasible(){
        return this.getLastNode().isFeasible();
    }

    int getLabel(){
        return this.getLastNode().Label;
    }

    Set<Route> getRoutes(){
        return this.getLastNode().getRoutes();
    }

    int getRoutesCount(){
        return this.getLastNode().getRoutesCount();
    }

    int getRoutesCounter(){
        return this.getRoutes().size();
    }
    
    int[] getGiantTour(){
        return this.getLastNode().getGiantTour(this.Data);
    }
    
    @Override
    public String toString(){
        return this.getLastNode().toString(this.Data);
    }

    void toCSV(InputData data) {
        this.getLastNode().toCSV(data);
    }

    @Override
    public void close() {
        new Thread(() -> {
            for (AuxiliaryGraphNode node : this.Nodes) 
                try {
                    node.close();
                } catch (Exception ex) {
                    Logger.getLogger(AuxiliaryGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
            this.Nodes = null;
            this.Tasks.clear();
        }).start();
    }
}

class Task extends Thread {
   final AuxiliaryGraphNode StartingNode;

   Task(Runnable r, AuxiliaryGraphNode node) {
       super(r);
       this.StartingNode = node;
   }
}
