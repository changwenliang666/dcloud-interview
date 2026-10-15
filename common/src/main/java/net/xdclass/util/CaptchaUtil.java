package net.xdclass.util;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * 图形验证码工具类
 */
@Slf4j
@Component
public class CaptchaUtil {

    private final Producer captchaProducer;
    private final StringRedisTemplate redisTemplate;
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    private static final int CAPTCHA_EXPIRE_MINUTES = 5;

    public CaptchaUtil(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.captchaProducer = createCaptchaProducer();
    }

    /**
     * 生成图形验证码并返回图片流
     * @param identifier 手机号或邮箱
     * @param type 验证码类型（login/register）
     * @return 验证码图片的字节数组
     */
    public byte[] generateCaptchaImage(String identifier, String type) {
        // 生成验证码文本
        String captchaText = captchaProducer.createText();

        // 将验证码保存到Redis
        String captchaKey = getCaptchaKey(identifier, CheckCodeTypeEnum.valueOf(type).name());
        log.info("保存图形验证码的key：{}", captchaKey);
        redisTemplate.opsForValue().set(captchaKey, captchaText, CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 生成验证码图片
        BufferedImage image = captchaProducer.createImage(captchaText);

        // 转换为字节数组
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("生成验证码图片失败", e);
        }
    }

    /**
     * 校验验证码
     * @param identifier 手机号或邮箱
     * @param type 验证码类型（login/register）
     */
    public void verifyCaptcha(String identifier, String captcha, String type) {
        String captchaKey = getCaptchaKey(identifier, type);
        log.info("验证图形验证码的key：{}", captchaKey);
        String savedCaptcha = redisTemplate.opsForValue().get(captchaKey);
        if (savedCaptcha == null) {
            throw new RuntimeException("验证码已过期");
        }

        if (!savedCaptcha.equalsIgnoreCase(captcha)) {
            throw new RuntimeException("验证码错误");
        }

        // 验证通过后删除验证码
        redisTemplate.delete(captchaKey);
    }

    private String getCaptchaKey(String identifier, String type) {
        return CAPTCHA_KEY_PREFIX + type + ":" + identifier;
    }

    private Producer createCaptchaProducer() {
        Properties properties = new Properties();
        // 图片宽度
        properties.setProperty("kaptcha.image.width", "120");
        // 图片高度
        properties.setProperty("kaptcha.image.height", "40");
        // 字体大小
        properties.setProperty("kaptcha.textproducer.font.size", "30");
        // 字体颜色
        properties.setProperty("kaptcha.textproducer.font.color", "black");
        // 字符长度
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        // 字体
        properties.setProperty("kaptcha.textproducer.font.names", "Arial");
        // 只使用数字
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789");
        // 字符间距
        properties.setProperty("kaptcha.textproducer.char.space", "4");
        // 背景颜色
        properties.setProperty("kaptcha.background.clear.from", "white");
        properties.setProperty("kaptcha.background.clear.to", "white");
        // 干扰线颜色
        properties.setProperty("kaptcha.noise.color", "gray");
        // 干扰线数量
        properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.NoNoise");

        Config config = new Config(properties);
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }
}
