package net.xdclass.fileupload;

import lombok.extern.slf4j.Slf4j;
import net.xdclass.config.OssClientManager;
import net.xdclass.service.OssService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class OssManagerTest {

    @Autowired
    private OssClientManager ossClientManager;


    @Autowired
    private OssService ossService;


    @Test
    public void testDeleteFile(){
        String fileUrl = "https://mockmate-pri.oss-cn-beijing.aliyuncs.com/Spring AI.pdf";
        //跟进URL解析存储桶
        OssClientManager.BucketClient bucketClient = ossClientManager.resolveByUrl(fileUrl);
        //提取fileKey
        String fileKey = ossService.extractFileKey(fileUrl);
        //使用客户端删除
        bucketClient.getClient().deleteObject(bucketClient.getBucketName(),fileKey);
        log.info("删除成功");
    }


    @Test
    public void testGeneratePreviewUrl(){
        String fileUrl = "https://mockmate-pri.oss-cn-beijing.aliyuncs.com/kling_20260813_作品_生成一张心有猛虎_细_5169_0.png";

        //解析bucket
        OssClientManager.BucketClient bucketClient = ossClientManager.resolveByUrl(fileUrl);

        if(bucketClient.isPublicBucket()){
            log.info("公开存储桶，直接返回URL");
        }

        String previewUrl = ossService.generatePreviewUrl(fileUrl);
        log.info("预览URL：{}",previewUrl);


    }

}
