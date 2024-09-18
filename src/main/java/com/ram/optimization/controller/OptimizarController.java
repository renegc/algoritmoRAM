/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ram.optimization.controller;

import com.ram.optimization.LogicAlgoritmo.Data.InputData;
import com.ram.optimization.LogicAlgoritmo.Metaheuristics.GeneticAlgorithm;
import com.ram.optimization.LogicAlgoritmo.Metaheuristics.MetaHeuristic;
import com.ram.optimization.LogicAlgoritmo.Solution.Solution;
import com.ram.optimization.models.Urls;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OptimizarController {

    @PostMapping("/Optimizacion")
    public ResponseEntity<Map<String, Object>> getOptimization(@RequestBody Urls request) throws Exception {

        try {
            // Aquí leemos el objeto que viene en el cuerpo de la petición
            String vehiculo = request.getVehiculo();
            String orders = request.getOrders();

            System.out.println("Vehiculo: " + vehiculo);
            System.out.println("Orders: " + orders);

            InputData data = new InputData(35);
            System.out.println(data);

            MetaHeuristic algorithm = new GeneticAlgorithm(data);
            algorithm.Run();

            if (algorithm.isFeasible()) {
                Solution sol = algorithm.getBestSolution();
                System.out.println(sol);
                sol.toCSV(data);
                sol.close();
            }
            data.close();

            // Crear el mapa para la respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("data", "se fue se fue se fue");
            response.put("valid", true);

            // Devolver el objeto ResponseEntity con el cuerpo y el código HTTP 200
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            // Crear el mapa para la respuesta
            Map<String, Object> response = new HashMap<>();

            response.put("valid", false);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

    }
}
