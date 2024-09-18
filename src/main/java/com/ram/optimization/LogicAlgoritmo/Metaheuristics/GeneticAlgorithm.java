/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ram.optimization.LogicAlgoritmo.Metaheuristics;


import com.ram.optimization.LogicAlgoritmo.Data.InputData;
import com.ram.optimization.LogicAlgoritmo.Solution.Solution;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 *
 * @author Othmane
 */
public class GeneticAlgorithm extends MetaHeuristic {
    private final ReentrantLock Lock = new ReentrantLock();
    private final List<Thread> AliveThreads = new LinkedList<>();
    private final double MutationRate = 0.1d;
    private final double CrossoverRate = 0.9d;
    private final int PopulationSize;
    private final long StopTime;
    private final Solution[] Population;
    
    public GeneticAlgorithm(InputData data){
        super(data);
        this.PopulationSize = (int) Math.max(20, 10 * Math.log10(data.OrdersCount));
        this.StopTime = (long) Math.max(1000, 1000 * Math.log10(data.OrdersCount));
        this.Population = new Solution[this.PopulationSize];
    }
    
    @Override
    public void Run() {
        this.RunTime = RunTime * 1000l;
        System.out.println("Solution approach = Genetic Algorithm");
        System.out.println();
        this.StartTime = System.currentTimeMillis();
        try {
            this.InitialPopulation();
        } catch (Exception ex) {
            Logger.getLogger(GeneticAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.BestSolutionReachingTime = System.currentTimeMillis();
        long population_ready_time = this.BestSolutionReachingTime;
        if(this.Population[0].isFeasible()) {
            System.out.println();
            System.out.println(this.Population[0].getFitness() + " after " + (this.BestSolutionReachingTime - this.StartTime) + " ms");
        }
        else {
            System.out.println("No feasible solution found");
            return;
        }
        this.setBestSolution(this.Population[0]);
        long current_time = this.BestSolutionReachingTime;
        double probability = 0d;
        byte iteration = 0;
        do{
            iteration++;
            this.Selection();
            synchronized(this.AliveThreads){
                while(this.AliveThreads.stream().filter(Thread::isAlive).count() > 5)
                    try{
                        this.AliveThreads.wait();
                    }catch(InterruptedException ex){
                        Logger.getLogger(GeneticAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
                    }
            }
            probability = 0d;
            if(iteration == 100){
                iteration = 0;
                current_time = System.currentTimeMillis();
                probability = current_time - this.BestSolutionReachingTime;
                probability /= (double) (current_time - population_ready_time);
            }
        } while(current_time - this.BestSolutionReachingTime < this.StopTime || Math.random() > probability);
        this.Join();
        System.out.println();
        this.setBestSolution(this.Population[0]);
        IntStream.range(1, this.PopulationSize)
                .mapToObj(i -> this.Population[i])
                .forEach(solution -> {
                    try {
                        solution.close();
                    } catch (Exception ex) {
                        Logger.getLogger(GeneticAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
    }
    
    private void Selection(){
        int half = this.PopulationSize / 2;
        int i = (int)(Math.random() * half);
        int j;
        if(Math.random() < 0.7d)
            do{
                j = (int)(Math.random() * half);
            } while(i == j);
        else
            j = half + (int)(Math.random() * (this.PopulationSize - half));
        this.Crossover(Math.random() < this.MutationRate, this.Population[i], this.Population[j]);
    }
    
    private void Crossover(boolean mutation, Solution ... parents){
        boolean CrossoverCondition = Math.random() < this.CrossoverRate;
        int CutPoint1 = (int) (this.Data.OrdersCount * Math.random());
        int CutPoint2 = (Math.random() < 0.7d) ? CutPoint1 : CutPoint1 + (int)((this.Data.OrdersCount - CutPoint1) * Math.random());
        Thread t1 = new Thread(() -> {
            Solution Child = null;
            try {
                Child = (CrossoverCondition) ? parents[0].Crossover(this.Data,parents[1], mutation, CutPoint1, CutPoint2) : new Solution(this.Data);
            } catch (java.lang.NullPointerException ex) {
                try {
                    Child = new Solution(this.Data);
                } catch (Exception ex1) {
                    Logger.getLogger(GeneticAlgorithm.class.getName()).log(Level.SEVERE, null, ex1);
                }
            } catch (Exception ex) {
                Logger.getLogger(GeneticAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.UpdatePopulation(Child);
            synchronized(this.AliveThreads){
                this.AliveThreads.notify();
            }
        });
        Thread t2 = new Thread(() -> {
            Solution Child = null;
            try {
                Child = (CrossoverCondition) ? parents[1].Crossover(this.Data,parents[0], mutation, CutPoint1, CutPoint2) : new Solution(this.Data);
            } catch (java.lang.NullPointerException ex) {
                try {
                    Child = new Solution(this.Data);
                } catch (Exception ex1) {
                    Logger.getLogger(GeneticAlgorithm.class.getName()).log(Level.SEVERE, null, ex1);
                }
            } catch (Exception ex) {
                Logger.getLogger(GeneticAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.UpdatePopulation(Child);
            synchronized(this.AliveThreads){
                this.AliveThreads.notify();
            }
        });
        t1.start();
        t2.start();
        while(!this.AliveThreads.isEmpty() && !this.AliveThreads.get(0).isAlive())
            this.AliveThreads.remove(0);
        this.AliveThreads.add(t1);
        this.AliveThreads.add(t2);
    }
    
    private void UpdatePopulation(Solution newSolution) {
        this.Lock.lock();
        try{
            if(newSolution.isFeasible() && newSolution.getFitness() < this.Population[this.PopulationSize - 1].getFitness()){
                int half = this.PopulationSize / 2;
                int i = half + (int)(Math.random() * (this.Population.length - half));
                this.Population[i].close();
                this.Population[i] = newSolution;
                if(newSolution.getFitness() < this.Population[0].getFitness()){
                    this.BestSolutionReachingTime = System.currentTimeMillis();
                    if((int) newSolution.getFitness() < (int) this.Population[0].getFitness())
                        System.out.println(newSolution.getFitness() + " after " + (this.BestSolutionReachingTime  - this.StartTime) + " ms");
                }
                Arrays.sort(this.Population, Solution::compare);
            }
            else
                newSolution.close();
        } catch (Exception ex) {
            Logger.getLogger(GeneticAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            this.Lock.unlock();
        }
    }
    
    private void InitialPopulation() throws Exception{
        System.out.println("Initial population");
        for(int i = 0; i < this.PopulationSize; i++){
            if(i == 0) {
                byte failure_count = 0;
                do{
                    failure_count++;
                    this.Population[i] = new Solution(this.Data);
                    if(!this.Population[i].isFeasible()) {
                        this.Population[i].close();
                        if(failure_count > 10)
                            return;
                    }
                } while (!this.Population[i].isFeasible());
                System.out.println(this.Population[i].getFitness());
                continue;
            }
            else {
                final int j = i;
                Thread t = new Thread(() -> {
                    do{
                        try {
                            this.Population[j] = new Solution(this.Data);
                            if(!this.Population[j].isFeasible())
                                this.Population[j].close();
                        } catch (Exception ex) {
                            Logger.getLogger(GeneticAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }while(!this.Population[j].isFeasible());
                    synchronized(this.AliveThreads){
                        this.AliveThreads.notify();
                    }
                    System.out.println(this.Population[j].getFitness());
                });
                t.start();
                this.AliveThreads.add(t);
            }
            synchronized(this.AliveThreads){
                while(this.AliveThreads.stream().filter(Thread::isAlive).count() > 5)
                    try{
                        this.AliveThreads.wait();
                    }catch(InterruptedException ex){
                        Logger.getLogger(GeneticAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
                    }
            }
        }
        this.Join();
        Arrays.sort(this.Population, Solution::compare);
    }
    
    private void Join(){
        this.AliveThreads.stream()
                        .filter(Thread::isAlive)
                        .forEach(t -> {
                            try {
                                t.join();
                            } catch (InterruptedException ex) {
                                Logger.getLogger(GeneticAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        });
        this.AliveThreads.clear();
    }
}
