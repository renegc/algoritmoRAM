package com.ram.optimization.LogicAlgoritmo.Metaheuristics;


import com.ram.optimization.LogicAlgoritmo.Solution.Solution;
import com.ram.optimization.LogicAlgoritmo.Data.InputData;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Othmane
 */
public abstract class MetaHeuristic {
    InputData Data;
    long RunTime;// Run Time in milliseconds
    long StartTime;// Start Time in milliseconds
    long BestSolutionReachingTime;
    private Solution BestSolution = null;

    public MetaHeuristic(InputData data){
        this.Data = data;
    }

    public Solution getBestSolution() {
        return this.BestSolution;
    }

    public void setBestSolution(Solution solution) {
        this.BestSolution = solution;
    }
    
    public boolean isFeasible(){
        return this.BestSolution != null;
    }
    
    public abstract void Run();
}