package com.aa.msw.mocks.auth;

import com.aa.msw.auth.threadlocal.UserContext;
import com.aa.msw.database.exceptions.NoSuchUserException;
import com.aa.msw.database.helpers.id.UserExtId;
import com.aa.msw.database.helpers.id.UserId;
import com.aa.msw.database.repository.dao.UserDao;
import com.aa.msw.database.services.UserDbService;
import com.aa.msw.integrationtest.TestUser;
import com.aa.msw.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.aa.msw.integrationtest.TestUser.ALL_TEST_USERS;

@Profile("test")
@Component
public class TestRequestUserInterceptor implements HandlerInterceptor {

    private final UserDao userDao;
    private final UserDbService userDbService;

    public TestRequestUserInterceptor(UserDao userDao, UserDbService userDbService) {
        this.userDao = userDao;
        this.userDbService = userDbService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("authorization");
        TestUser testUser = getTestUser(token);
        UserExtId extUserId = new UserExtId(testUser.extUserId());
        User user;
        try {
            user = userDao.getUser(extUserId);
            UserContext.setCurrentUser(user);
        } catch (NoSuchUserException e) { // on register or downsync from firebase
            user = new User(
                    new UserId(),
                    extUserId,
                    testUser.email(),
                    "");
            UserContext.setCurrentUser(user);
            userDbService.registerUserAndAddPublicSpots();
        }

        return true;
    }

    private TestUser getTestUser(String tokenValue) {
        for (TestUser testUser : ALL_TEST_USERS) {
            if (("Bearer " + testUser.token()).equals(tokenValue)) {
                return testUser;
            }
        }
        throw new NotImplementedException("Looks like the testUser with the token " + tokenValue + " does not exist.");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}

