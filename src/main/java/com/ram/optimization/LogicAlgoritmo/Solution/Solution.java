package com.ram.optimization.LogicAlgoritmo.Solution;


import com.ram.optimization.LogicAlgoritmo.Data.InputData;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.IntStream;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Othmane
 */
public class Solution implements AutoCloseable{
    public int[] GiantTour;
    public AuxiliaryGraph AuxiliaryGraph = null;
    
    private Solution(InputData data, LinkedList<Integer> gt, boolean mutation) throws Exception{
        this.GiantTour = gt.stream()
                        .flatMapToInt(x -> IntStream.of(x))
                        .toArray();
        gt.clear();
        if(mutation)
            this.Mutation();
        this.Split(data);
    }
    
    public Solution(InputData data) throws Exception{
        this.setRandomGiantTour(data);
        this.Split(data);
    }
    
    private void setRandomGiantTour(InputData data){
        this.GiantTour = IntStream.range(0, data.OrdersCount)
                            .toArray();
        IntStream.range(0, this.GiantTour.length)
                .forEach(i -> new Motion(i, (int)(Math.random() * this.GiantTour.length)).Swap(this.GiantTour));
    }
    
    private void Mutation(){
        int x = (int)(Math.random() * this.GiantTour.length);
        int y = (int)(Math.random() * this.GiantTour.length);
        new Motion(Math.min(x, y), Math.max(x,y))._2opt(this.GiantTour);
    }
    
    public Solution Crossover(InputData data, Solution parent, boolean mutation, int ... cut_points) throws Exception{
        int n = cut_points.length == 0 ? 0 : cut_points[0];
        int p = cut_points[cut_points.length == 1 ? 0 : 1];
        int i = 0;
        LinkedList<Integer> child_gt = new LinkedList<>();
        for(int j = n; j < p; j++)
            child_gt.add(parent.GiantTour[j]);
        for(int j = p; j < this.GiantTour.length; j++)
            if(!child_gt.contains(this.GiantTour[j]))
                if(child_gt.size() < this.GiantTour.length - n)
                    child_gt.add(this.GiantTour[j]);
                else{
                    child_gt.add(i, this.GiantTour[j]);
                    i++;
                }
        for(int j = 0; j < p; j++)
            if(!child_gt.contains(this.GiantTour[j]))
                if(child_gt.size() < this.GiantTour.length - n)
                    child_gt.add(this.GiantTour[j]);
                else{
                    child_gt.add(i, this.GiantTour[j]);
                    i++;
                }
        return new Solution(data, child_gt, mutation);
    }  
    
    private void Split(InputData data) throws Exception{
        this.Split(data, Integer.MAX_VALUE, 0);
    }
    
    private void Split(InputData data, int bound, int iteration) throws Exception {
        if(bound < Integer.MAX_VALUE) {
            int[] new_gt = this.getNewGiantTour();
            if(IntStream.range(0, new_gt.length).allMatch(i -> this.GiantTour[i] == new_gt[i]))
                return;
            this.GiantTour = new_gt;
        }
        AuxiliaryGraph graph = this.getGraph(data, bound);
        boolean c = graph.isFeasible();
        int label = graph.getLabel();
        if(c)
            if(label < bound) {
                if(this.AuxiliaryGraph != null)
                    this.AuxiliaryGraph.close();
                this.AuxiliaryGraph = graph;
                this.Split(data, label, iteration + 1);
            }
            else {
                graph.close();
                if(label > bound && iteration < 10)
                    this.Split(data, bound, iteration + 1);
            }
    }
    
    private AuxiliaryGraph getGraph(InputData data, int bound) throws Exception {
        AuxiliaryGraph graph = new AuxiliaryGraph(data, this.GiantTour, bound);
        graph.setArcs();
        return graph;
    }
    
    Set<Route> getRoutes(){
        return this.AuxiliaryGraph.getRoutes();
    }
    
    int getRoutesCount(){
        return this.AuxiliaryGraph.getRoutesCount();
    }
       
    public int compare(Solution s){
        return (int)(this.getFitness() * 100d - s.getFitness() * 100d);
    }
    
    @Override
    public String toString(){
        return this.AuxiliaryGraph.toString();
    }

    public int getFitness(){
        return this.AuxiliaryGraph.getLabel();
    }

    public boolean isFeasible() {
        if(this.AuxiliaryGraph != null)
            return this.AuxiliaryGraph.isFeasible();
        return false;
    }

    int[] getNewGiantTour() {
        return this.AuxiliaryGraph.getGiantTour();
    }

    public void toCSV(InputData data) {
        this.AuxiliaryGraph.toCSV(data);
    }

    @Override
    public void close() {
        this.GiantTour = null;
        try{
            this.AuxiliaryGraph.close();
        } catch (java.lang.NullPointerException e) {}
    }
}