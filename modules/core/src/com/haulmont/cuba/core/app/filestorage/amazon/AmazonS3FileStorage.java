/*
 * Copyright (c) 2008-2016 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.haulmont.cuba.core.app.filestorage.amazon;

import com.haulmont.bali.util.Preconditions;
import com.haulmont.cuba.core.app.FileStorageAPI;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.FileStorageException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.haulmont.bali.util.Preconditions.checkNotNullArgument;

public class AmazonS3FileStorage implements FileStorageAPI {
    @Inject
    protected AmazonS3Config amazonS3Config;

    protected S3Client s3;

    protected S3Client getS3Client() {
        AwsCredentialsProvider awsCredentialsProvider;
        if (getAccessKey() != null && getSecretAccessKey() != null) {
            AwsCredentials awsCredentials = AwsBasicCredentials.create(getAccessKey(), getSecretAccessKey());
            awsCredentialsProvider = StaticCredentialsProvider.create(awsCredentials);
        } else {
            awsCredentialsProvider = DefaultCredentialsProvider.create();
        }
        return S3Client.builder()
                .credentialsProvider(awsCredentialsProvider)
                .region(Region.of(getRegionName()))
                .build();
    }

    @Override
    public long saveStream(FileDescriptor fileDescr, InputStream inputStream) throws FileStorageException {
        Preconditions.checkNotNullArgument(fileDescr.getSize());
        try {
            saveFile(fileDescr, IOUtils.toByteArray(inputStream));
        } catch (IOException e) {
            String message = String.format("Could not save file %s.",
                    getFileName(fileDescr));
            throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION, message);
        }
        return fileDescr.getSize();
    }

    @Override
    public void saveFile(FileDescriptor fileDescr, byte[] data) throws FileStorageException {
        checkNotNullArgument(data, "File content is null");
        try {
            s3 = getS3Client();
            int chunkSize = amazonS3Config.getChunkSize() * 1024;

            CreateMultipartUploadRequest createMultipartUploadRequest = CreateMultipartUploadRequest.builder()
                    .bucket(getBucket()).key(resolveFileName(fileDescr))
                    .build();
            CreateMultipartUploadResponse response = s3.createMultipartUpload(createMultipartUploadRequest);

            List<CompletedPart> completedParts = new ArrayList<>();
            int filePosition = 0;
            for (int i = 1; filePosition < data.length; i++) {
                UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                        .bucket(getBucket())
                        .key(resolveFileName(fileDescr))
                        .uploadId(response.uploadId())
                        .partNumber(i)
                        .build();
                int endChunkPosition = Math.min(filePosition + chunkSize, data.length);
                byte[] chunkBytes = getChunkBytes(data, filePosition, endChunkPosition);
                String eTag = s3.uploadPart(uploadPartRequest, RequestBody.fromBytes(chunkBytes)).eTag();
                CompletedPart part = CompletedPart.builder().partNumber(i).eTag(eTag).build();
                completedParts.add(part);

                filePosition += chunkSize;
            }
            CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder().parts(completedParts).build();
            CompleteMultipartUploadRequest completeMultipartUploadRequest =
                    CompleteMultipartUploadRequest.builder()
                            .bucket(getBucket())
                            .key(resolveFileName(fileDescr))
                            .uploadId(response.uploadId())
                            .multipartUpload(completedMultipartUpload).build();
            s3.completeMultipartUpload(completeMultipartUploadRequest);
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
    }

    protected byte[] getChunkBytes(byte[] data, int start, int end) {
        byte[] chunkBytes = new byte[end - start];
        System.arraycopy(data, start, chunkBytes, 0, end-start);
        return chunkBytes;
    }

    @Override
    public void removeFile(FileDescriptor fileDescr) throws FileStorageException {
        s3 = getS3Client();
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(getBucket()).key(resolveFileName(fileDescr)).build();
        s3.deleteObject(deleteObjectRequest);
    }

    @Override
    public InputStream openStream(FileDescriptor fileDescr) throws FileStorageException {
        InputStream is = null;
        try {
            s3 = getS3Client();
            is = s3.getObject(GetObjectRequest.builder().bucket(getBucket()).key(resolveFileName(fileDescr)).build(),
                    ResponseTransformer.toInputStream());
        } catch (
                SdkClientException e) {
            String message = String.format("Could not load file %s.",
                    getFileName(fileDescr));
            throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION, message);
        }
        return is;
    }

    @Override
    public byte[] loadFile(FileDescriptor fileDescr) throws FileStorageException {
        InputStream inputStream = openStream(fileDescr);
        try {
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION, fileDescr.getId().toString(), e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    @Override
    public boolean fileExists(FileDescriptor fileDescr) {
        ListObjectsV2Request listObjectsReqManual = ListObjectsV2Request.builder()
                .bucket(getBucket())
                .maxKeys(1)
                .build();
        ListObjectsV2Response listObjResponse = s3.listObjectsV2(listObjectsReqManual);
        return listObjResponse.contents().stream()
                .map(S3Object::key)
                .collect(Collectors.toList())
                .contains(resolveFileName(fileDescr));
    }

    protected String resolveFileName(FileDescriptor fileDescr) {
        return getStorageDir(fileDescr.getCreateDate()) + "/" + getFileName(fileDescr);
    }

    /**
     * INTERNAL. Don't use in application code.
     */
    protected String getStorageDir(Date createDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(createDate);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        return String.format("%d/%s/%s",
                year, StringUtils.leftPad(String.valueOf(month), 2, '0'), StringUtils.leftPad(String.valueOf(day), 2, '0'));
    }

    protected String getFileName(FileDescriptor fileDescriptor) {
        if (StringUtils.isNotBlank(fileDescriptor.getExtension())) {
            return fileDescriptor.getId().toString() + "." + fileDescriptor.getExtension();
        } else {
            return fileDescriptor.getId().toString();
        }
    }

    protected String getS3StorageClass() {
        return "REDUCED_REDUNDANCY";
    }

    protected String getRegionName() {
        return amazonS3Config.getRegionName();
    }

    protected String getBucket() {
        return amazonS3Config.getBucket();
    }

    protected String getSecretAccessKey() {
        return amazonS3Config.getSecretAccessKey();
    }

    protected String getAccessKey() {
        return amazonS3Config.getAccessKey();
    }
}