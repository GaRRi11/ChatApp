//import javax.servlet.http.Cookie;
//import javax.servlet.http.HttpServletResponse;
//
//public class LogoutManager {
//    private RedisManager redisManager;
//
//    public LogoutManager() {
//        redisManager = new RedisManager();
//    }
//
//    public void logout(HttpServletResponse response) {
//        Cookie sessionCookie = new Cookie(SessionManager.SESSION_COOKIE_NAME, null);
//        sessionCookie.setMaxAge(0);
//        sessionCookie.setHttpOnly(true);
//        response.addCookie(sessionCookie);
//
//        // Delete session data from Redis
//        String sessionId = /* Retrieve the session ID from the request */;
//        redisManager.getJedis().del(sessionId);
//    }
//}
