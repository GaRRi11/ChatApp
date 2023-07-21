//import javax.servlet.http.Cookie;
//import javax.servlet.http.HttpServletRequest;
//import java.util.Arrays;
//
//public class SessionVerifier {
//    private RedisManager redisManager;
//
//    public SessionVerifier() {
//        redisManager = new RedisManager();
//    }
//
//    public boolean isSessionValid(HttpServletRequest request) {
//        Cookie[] cookies = request.getCookies();
//        if (cookies != null) {
//            String sessionId = Arrays.stream(cookies)
//                    .filter(cookie -> SessionManager.SESSION_COOKIE_NAME.equals(cookie.getName()))
//                    .map(Cookie::getValue)
//                    .findFirst()
//                    .orElse(null);
//
//            if (sessionId != null) {
//                String sessionData = redisManager.getJedis().get(sessionId);
//
//                if (sessionData != null) {
//                    // If the session is valid, you can retrieve the session data and use it as needed
//                    return true;
//                }
//            }
//        }
//
//        return false;
//    }
//}