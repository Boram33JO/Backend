package com.sparta.i_mu.global.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.sparta.i_mu.global.errorCode.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AwsS3Util {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadImage(MultipartFile multipartFile) {


        String fileName = createFileName(multipartFile);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(multipartFile.getSize());
        objectMetadata.setContentType(multipartFile.getContentType());

        try(InputStream inputStream = multipartFile.getInputStream()) {
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch(IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다.");
        }

        return amazonS3.getUrl(bucket, fileName).toString();
    }

    public void deleteImage(String profileImageUrl) {
        if (profileImageUrl == null) {
            return;
        }

        String deleteImageUrl = profileImageUrl.substring(profileImageUrl.lastIndexOf("/")+1);

        amazonS3.deleteObject(bucket, deleteImageUrl);
    }

    private String createFileName(MultipartFile multipartFile) {
        return UUID.randomUUID().toString().concat(getFileExtension(multipartFile));
    }

    private String getFileExtension(MultipartFile multipartFile) {

        String contentType = multipartFile.getContentType();

        if (contentType == null || !contentType.contains("image")) {
            throw new IllegalArgumentException(ErrorCode.FILE_NOT_IMAGE.getMessage());
        }

        String fileName = multipartFile.getOriginalFilename();

        if (fileName == null || !fileName.contains(".")) {
            throw new IllegalArgumentException(ErrorCode.FILE_NOT_EXTENSION.getMessage());
        }

        return fileName.substring(fileName.lastIndexOf("."));

    }

}
