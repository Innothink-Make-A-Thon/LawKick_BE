package azaza.lawkick.config.s3;

import azaza.lawkick.config.exception.handler.FileHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static azaza.lawkick.config.code.status.ErrorStatus.MULTIPARTFILE_TO_FILE_ERROR;

@Slf4j
@Service
@Component
@RequiredArgsConstructor
public class S3Uploader {
    private final AmazonS3 amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    //MultiFile 형태의 파일을 File 형태로 전환 후 S3 저장
    public String fileUpload(MultipartFile multipartFile, String dirName) throws IOException {
        File uploadFile = convert(multipartFile)
                .orElseThrow(() -> new FileHandler(MULTIPARTFILE_TO_FILE_ERROR));
        return upload(uploadFile,dirName);
    }

    //변환된 파일 업로드
    private String upload(File uploadFile, String dirName){
        String fileName = dirName + "/" +uploadFile.getName(); //UUID 고유 식별자 추가
        String uploadImageUrl = putS3(uploadFile, fileName);
        removeNewFile(uploadFile);
        return uploadImageUrl;
    }

    //파일 Read 권한으로 put
    private String putS3(File uploadFile, String fileName){
        amazonS3Client.putObject(
                new PutObjectRequest(bucket, fileName, uploadFile)
                        .withCannedAcl(CannedAccessControlList.PublicRead)
        );
        return amazonS3Client.getUrl(bucket,fileName).toString();
    }

    //로컬에 저장된 파일 삭제
    private void removeNewFile(File targetFile){
        if(targetFile.delete()){
            log.info("파일이 삭제되었습니다.");
        } else{
            log.info("파일이 삭제되지 않았습니다.");
        }
    }

    //파일 형식 전환
    private Optional<File> convert(MultipartFile file) throws IOException{
        File convertFile = new File(System.getProperty("user.dir") + "/" + UUID.randomUUID() + file.getOriginalFilename());
        if(convertFile.createNewFile()){
            try (FileOutputStream fos = new FileOutputStream(convertFile)){

                fos.write(file.getBytes());
            }
            return Optional.of(convertFile);
        }
        return Optional.empty();
    }

    //S3 이미지 URL을 가져오는 함수
    public String getImageFilePath(String path){
        return amazonS3Client.getUrl(bucket, path).toString();
    }

    //파일의 S3 내부 진짜 경로로 변경
    public String changeFileKeyPath(String fileName){
        String fileKey = fileName.replace(String.format("https://%s.s3.%s.amazonaws.com/", bucket, amazonS3Client.getRegion()),"");
        return fileKey;
    }
    //파일 삭제
    public void deleteFile(String fileName){
        try{
            amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, fileName));
            log.info("기존 파일이 정상적으로 삭제되었습니다.");
        } catch (AmazonS3Exception e){
            e.printStackTrace();
            log.info("기존 파일 삭제에 실패했습니다.");

        }
    }
}
