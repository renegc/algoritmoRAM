/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ram.optimization.Google;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.Bucket;

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
}