/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ram.optimization.Google;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.Bucket;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GoogleCloudStorageService {

    private Storage storage;
    private String bucketName;

    public GoogleCloudStorageService(String projectId, String bucketName) {

        this.storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
        this.bucketName = bucketName;
    }

    public Bucket getBucket() {
        return storage.get(bucketName);
    }

    public Blob getFile(String fileName) {
        return storage.get(bucketName, fileName);
    }

    public void uploadFile(String filePath, String destinationBlobName) {
        File file = new File(filePath);
        try {
            Path path = Paths.get(filePath);
            byte[] data = Files.readAllBytes(path);

            // Determinar el tipo de contenido (en este ejemplo, asumimos que es CSV)
            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "application/octet-stream"; // Tipo por defecto si no se puede determinar
            }
            contentType = "text/csv";

            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, destinationBlobName)
                    .setContentType(contentType)
                    .build();
            storage.create(blobInfo, data);

            System.out.println("Archivo " + filePath + " subido exitosamente al bucket como " + destinationBlobName);
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al subir el archivo: " + e.getMessage(), e);
        } finally {
            // Eliminar el archivo local después de subirlo
            if (file.exists()) {
                if (file.delete()) {
                    System.out.println("Archivo " + filePath + " eliminado exitosamente del sistema local.");
                } else {
                    System.err.println("No se pudo eliminar el archivo " + filePath);
                }
            }
        }
    }
}
