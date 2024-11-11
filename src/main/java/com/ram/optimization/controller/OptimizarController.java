/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ram.optimization.controller;

import com.ram.optimization.Google.GoogleCloudStorageService;
import com.ram.optimization.LogicAlgoritmo.Data.InputData;
import com.ram.optimization.LogicAlgoritmo.Metaheuristics.GeneticAlgorithm;
import com.ram.optimization.LogicAlgoritmo.Metaheuristics.MetaHeuristic;
import com.ram.optimization.LogicAlgoritmo.Solution.Solution;
import com.ram.optimization.models.Urls;
import java.io.File;
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
            String msg = "No es posible generar la solucion";
            System.out.println("Vehiculo: " + vehiculo);
            System.out.println("Orders: " + orders);
            Map<String, Object> auxResponse = new HashMap<>();
            InputData data = new InputData(35, vehiculo, orders);
            System.out.println(data);

            MetaHeuristic algorithm = new GeneticAlgorithm(data);
            algorithm.Run();

            if (algorithm.isFeasible()) {
                Solution sol = algorithm.getBestSolution();
                System.out.println(sol);
                //sol.toCSV(data);
                //sol.close();
                msg = "Solucion Completa";
                File csvFile = sol.toCSV(data); // Asegúrate de que `toCSV` devuelve un `File` ahora.
                sol.close();

                String projectId = "routesandmaps";
                String bucketName = "routesandmaps-files";
                String destinationBlobName = csvFile.getName(); // Utiliza el nombre del archivo para el blob.

                GoogleCloudStorageService gcsService = new GoogleCloudStorageService(projectId, bucketName);

                try {
                    // Subir el archivo al bucket
                    gcsService.uploadFile(csvFile.getPath(), destinationBlobName);
                    msg = "Solución completa. Archivo subido exitosamente: ";
                    auxResponse.put("nameFile", destinationBlobName);
                } catch (Exception e) {
                    msg = "Solución completa, pero ocurrió un error al subir el archivo: " + e.getMessage();
                    e.printStackTrace();
                    Map<String, Object> response = new HashMap<>();

                    response.put("valid", false);
                    return new ResponseEntity<>(response, HttpStatus.OK);
                }
            }
            data.close();

            // Crear el mapa para la respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("msg", msg);
            response.put("valid", true);
            response.put("data", auxResponse);

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
