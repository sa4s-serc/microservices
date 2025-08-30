package verifycode.util;

/**
 * @author fdse
 */
public class CookieUtil {

    /**
     * Extract cookie value by name from cookie header string
     *
     * @param cookieHeader the cookie header string
     * @param cookieName the name of the cookie to extract
     * @return the cookie value or null if not found
     */
    public static String getCookieValue(String cookieHeader, String cookieName) {
        if (cookieHeader == null || cookieName == null) {
            return null;
        }

        String[] cookies = cookieHeader.split(";");
        for (String cookie : cookies) {
            String[] parts = cookie.trim().split("=");
            if (parts.length == 2 && cookieName.equals(parts[0].trim())) {
                return parts[1].trim();
            }
        }
        return null;
    }
}