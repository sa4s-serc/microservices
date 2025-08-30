package verifycode.service;

import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author fdse
 */
public interface VerifyCodeService {

    /**
     * Generate verification code image and set cookie
     *
     * @param response HTTP response
     * @throws IOException if image generation fails
     */
    void generateVerifyCode(HttpServletResponse response) throws IOException;

    /**
     * Verify the submitted verification code
     *
     * @param verifyCode the verification code to verify
     * @param headers HTTP headers containing cookies
     * @return true if verification is successful, false otherwise
     */
    boolean verifyCode(String verifyCode, HttpHeaders headers);
}