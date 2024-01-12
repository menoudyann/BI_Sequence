package org.example;

import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GoogleDataObjectImpl implements IDataObject {

    private Storage storage;

    public GoogleDataObjectImpl(String credentialPathname) {
        Dotenv dotenv = Dotenv.load();

        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(dotenv.get(credentialPathname))).createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
            storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isBucket(URI remoteFullPath) {
        return remoteFullPath.getPath().equals("/");
    }

    @Override
    public boolean doesExist(URI remoteFullPath) {

        boolean result = false;

        if (isBucket(remoteFullPath)) {
            Bucket bucket = storage.get(remoteFullPath.getHost());
            if (bucket != null) {
                result = bucket.exists();
            }
        } else {
            Page<Blob> blobs =
                    storage.list(
                            remoteFullPath.getHost(),
                            Storage.BlobListOption.prefix(remoteFullPath.getPath().substring(1)));
            int size = 0;
            for (Blob blob : blobs.iterateAll()) {
                size++;
            }
            return size > 0;
        }
        return result;
    }

    @Override
    public void upload(URI localFullPath, URI remoteFullPath) throws IOException {

        String bucketName = remoteFullPath.getHost();
        String objectName = remoteFullPath.getPath().substring(1);

        Path path = Paths.get(localFullPath);

        File file = path.toFile();
        if (!file.exists()) {
            System.out.println("File not found");
        }

        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        storage.createFrom(blobInfo, path);
    }

    @Override
    public void download(URI localFullPath, URI remoteFullPath) throws ObjectNotFoundException {

        String bucketName = remoteFullPath.getHost();
        String objectName = remoteFullPath.getPath().substring(1);

        try {
            Blob blob = storage.get(BlobId.of(bucketName, objectName));
            blob.downloadTo(Paths.get(localFullPath));
        } catch (Exception e) {
            throw new ObjectNotFoundException();
        }

    }

    @Override
    public URL publish(URI remoteFullPath, int expirationTime) throws ObjectNotFoundException {

        String bucketName = remoteFullPath.getHost();
        String objectName = remoteFullPath.getPath().substring(1);

        Blob blob = storage.get(BlobId.of(bucketName, objectName));
        if (blob == null) {
            throw new ObjectNotFoundException();
        }

        return blob.signUrl(expirationTime, TimeUnit.SECONDS);
    }

    @Override
    public void remove(URI remoteFullPath, boolean isRecursive) {

        String bucketName = remoteFullPath.getHost();
        String objectName = remoteFullPath.getPath().substring(1);

        if (isRecursive) {
            Page<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(objectName));
            for (Blob blob : blobs.iterateAll()) {
                storage.delete(bucketName, blob.getName());
            }
        } else {
            Blob blob = storage.get(bucketName, objectName);
            if (blob == null) {
                return;
            }
            storage.delete(bucketName, objectName);
        }
    }

    public void callAPI(URI remoteFullPath, URI localFullPath) throws IOException {
        if (!doesExist(remoteFullPath)) {
            upload(localFullPath, remoteFullPath);
        }
    }

}
