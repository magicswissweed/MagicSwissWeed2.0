package com.aa.msw.auth;

import com.aa.msw.auth.threadlocal.UserContext;
import com.aa.msw.database.exceptions.NoSuchUserException;
import com.aa.msw.database.helpers.id.UserExtId;
import com.aa.msw.database.helpers.id.UserId;
import com.aa.msw.database.repository.dao.UserDao;
import com.aa.msw.database.services.UserDbService;
import com.aa.msw.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerInterceptor;

@Profile("!test")
@Component
public class RequestUserInterceptor implements HandlerInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(RequestUserInterceptor.class);

    private final UserDao userDao;
    private final UserDbService userDbService;

    public RequestUserInterceptor(UserDao userDao, UserDbService userDbService) {
        this.userDao = userDao;
        this.userDbService = userDbService;
    }

    @Transactional
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Jwt jwt) {
            try {
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(jwt.getTokenValue());
                UserExtId extUserId = new UserExtId(decodedToken.getUid());
                User user;
                try {
                    user = userDao.getUser(extUserId);
                    UserContext.setCurrentUser(user);
                } catch (NoSuchUserException e) { // on register or downsync from firebase
                    user = new User(
                            new UserId(),
                            extUserId,
                            decodedToken.getEmail(),
                            "");
                    UserContext.setCurrentUser(user);
                    userDbService.registerUserAndAddPublicSpots();
                }
            } catch (FirebaseAuthException e) {
                LOG.info("Exception when decoding token: " + e.getMessage());
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
