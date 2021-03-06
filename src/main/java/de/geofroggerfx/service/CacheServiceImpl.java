/*
 * Copyright (c) Andreas Billmann <abi@geofroggerfx.de>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package de.geofroggerfx.service;

import de.geofroggerfx.dao.CacheDAO;
import de.geofroggerfx.dao.SettingsDAO;
import de.geofroggerfx.dao.UserDAO;
import de.geofroggerfx.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Andreas on 10.03.2015.
 */
@Service
public class CacheServiceImpl implements CacheService {

    @Autowired
    private CacheDAO cacheDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private SettingsDAO settingsDAO;


    @Override
    public void storeCaches(List<Cache> cacheList) {
        List<User> users = extractUserListAndMarkFound(cacheList);
        userDAO.save(users);
        cacheDAO.save(cacheList);
    }

    @Override
    public List<CacheListEntry> getAllCacheEntriesSortBy(String name, String asc) {
        return cacheDAO.getAllCacheEntriesSortBy(name, asc);
    }

    @Override
    public Cache getCacheForId(long id) {
        return cacheDAO.getCacheForId(id);
    }

    private List<User> extractUserListAndMarkFound(List<Cache> cacheList) {

        Map<Long, User> users = new HashMap<>();

        String username = settingsDAO.getSettings().getMyUsername();

        for (Cache cache: cacheList) {
            User owner = cache.getOwner();
            addUserToMap(users, owner);
            for (Log log: cache.getLogs()) {
                User finder = log.getFinder();

                if (username.equals(finder.getName()) &&
                        (log.getType().equals(LogType.FOUND_IT) || (log.getType().equals(LogType.ATTENDED)))) {
                    cache.setFound(true);
                }

                addUserToMap(users, finder);
            }
        }

        return new ArrayList<>(users.values());
    }

    private void addUserToMap(Map<Long, User> users, User finder) {
        if (!users.containsKey(finder.getId())) {
            users.put(finder.getId(), finder);
        }
    }
}
