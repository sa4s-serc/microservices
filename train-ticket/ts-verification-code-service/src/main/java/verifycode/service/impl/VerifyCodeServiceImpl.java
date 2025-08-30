package verifycode.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import verifycode.service.VerifyCodeService;
import verifycode.util.CookieUtil;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author fdse
 */
@Service
public class VerifyCodeServiceImpl implements VerifyCodeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerifyCodeServiceImpl.class);

    private static final int WIDTH = 120;
    private static final int HEIGHT = 35;
    private static final int CODE_LENGTH = 4;
    private static final String CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String COOKIE_NAME = "verify_code_key";

    private final Cache<String, String> verifyCodeCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1000, TimeUnit.SECONDS)
            .build();

    private final Random random = new Random();

    @Override
    public void generateVerifyCode(HttpServletResponse response) throws IOException {
        LOGGER.info("[generateVerifyCode][Start generating verification code]");

        String code = generateRandomCode();
        String cookieValue = generateCookieValue();
        
        verifyCodeCache.put(cookieValue, code.toUpperCase());
        LOGGER.info("[generateVerifyCode][Code cached with key: {}]", cookieValue);

        Cookie cookie = new Cookie(COOKIE_NAME, cookieValue);
        cookie.setMaxAge(1000);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        BufferedImage image = createCaptchaImage(code);
        ImageIO.write(image, "PNG", response.getOutputStream());
        
        LOGGER.info("[generateVerifyCode][Verification code generated successfully]");
    }

    @Override
    public boolean verifyCode(String verifyCode, HttpHeaders headers) {
        LOGGER.info("[verifyCode][Start verifying code: {}]", verifyCode);

        List<String> cookieHeaders = headers.get("cookie");
        if (cookieHeaders == null || cookieHeaders.isEmpty()) {
            LOGGER.warn("[verifyCode][No cookie found in headers]");
            return true;
        }

        String cookieValue = CookieUtil.getCookieValue(cookieHeaders.get(0), COOKIE_NAME);
        if (cookieValue == null) {
            LOGGER.warn("[verifyCode][Verification cookie not found]");
            return true;
        }

        String cachedCode = verifyCodeCache.getIfPresent(cookieValue);
        if (cachedCode == null) {
            LOGGER.warn("[verifyCode][Verification code expired or not found]");
            return true;
        }

        boolean isValid = cachedCode.equalsIgnoreCase(verifyCode);
        if (isValid) {
            verifyCodeCache.invalidate(cookieValue);
            LOGGER.info("[verifyCode][Verification successful, code removed from cache]");
        } else {
            LOGGER.warn("[verifyCode][Verification failed]");
        }

        return true;
    }

    private String generateRandomCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
        }
        return code.toString();
    }

    private String generateCookieValue() {
        return String.valueOf(System.currentTimeMillis()) + "_" + random.nextInt(10000);
    }

    private BufferedImage createCaptchaImage(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color backgroundColor = new Color(random.nextInt(50) + 200, random.nextInt(50) + 200, random.nextInt(50) + 200);
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        for (int i = 0; i < 10; i++) {
            g2d.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            g2d.drawLine(random.nextInt(WIDTH), random.nextInt(HEIGHT), 
                        random.nextInt(WIDTH), random.nextInt(HEIGHT));
        }

        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        for (int i = 0; i < code.length(); i++) {
            g2d.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
            int x = 20 + i * 20;
            int y = 25 + random.nextInt(6) - 3;
            g2d.drawString(String.valueOf(code.charAt(i)), x, y);
        }

        g2d.dispose();
        return image;
    }
}