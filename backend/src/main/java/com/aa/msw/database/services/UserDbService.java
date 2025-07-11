package com.aa.msw.database.services;

import com.aa.msw.auth.threadlocal.UserContext;
import com.aa.msw.database.repository.dao.UserDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDbService {
    private final UserDao userDao;
    private final SpotDbService spotDbService;

    public UserDbService(UserDao userDao, SpotDbService spotDbService) {
        this.userDao = userDao;
        this.spotDbService = spotDbService;
    }

    @Transactional
    public void registerUserAndAddPublicSpots() {
        userDao.persist(UserContext.getCurrentUser());
        spotDbService.addAllPublicSpotsToUser();
    }
}
