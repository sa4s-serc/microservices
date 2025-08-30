package verifycode.controller;

import edu.fudan.common.util.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import verifycode.service.VerifyCodeService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author fdse
 */
@RestController
@RequestMapping("/api/v1/verifycode")
public class VerifyCodeController {

    @Autowired
    private VerifyCodeService verifyCodeService;

    private static final Logger LOGGER = LoggerFactory.getLogger(VerifyCodeController.class);

    @GetMapping(path = "/welcome")
    public String home(@RequestHeader HttpHeaders headers) {
        return "Welcome to [ Verification Code Service ] !";
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/generate")
    public void generateVerifyCode(HttpServletResponse response) throws IOException {
        LOGGER.info("[generateVerifyCode][Generate Verification Code]");
        
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        
        verifyCodeService.generateVerifyCode(response);
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/verify/{verifyCode}")
    public ResponseEntity<Response<Boolean>> verifyCode(@PathVariable String verifyCode,
                                                       @RequestHeader HttpHeaders headers) {
        LOGGER.info("[verifyCode][Verify Code][VerifyCode: {}]", verifyCode);
        
        boolean isValid = verifyCodeService.verifyCode(verifyCode, headers);
        Response<Boolean> response = new Response<>(1, "Verification result", isValid);
        
        return ResponseEntity.ok(response);
    }
}