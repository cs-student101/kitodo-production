/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.services.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.LocalDateTime;
import org.kitodo.security.SecurityConfig;
import org.kitodo.security.SecuritySession;
import org.kitodo.security.SecurityUserDetails;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

public class SessionService {

    private static SessionService instance = null;
    private SessionRegistry sessionRegistry = null;
    private static final Logger logger = LogManager.getLogger(SessionService.class);

    private SessionService() {
        WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
        SecurityConfig securityConfig = context.getBean(SecurityConfig.class);
        this.sessionRegistry = securityConfig.getSessionRegistry();
    }

    /**
     * Expires all active sessions of a spring security UserDetails object.
     *
     * @param user
     *      The UserDetails Object.
     */
    public void expireSessionsOfUser(UserDetails user) {
        List<SessionInformation> activeUserSessions = sessionRegistry.getAllSessions(user,false);
        for (SessionInformation sessionInformation : activeUserSessions) {
            sessionInformation.expireNow();
        }
    }

    /**
     * Gets all active sessions.
     *
     * @return The active sessions.
     */
    public List<SecuritySession> getActiveSessions() {

        List<Object> allPrincipals = sessionRegistry.getAllPrincipals();

        List<SecuritySession> activeSessions = new ArrayList<>();

        for (final Object principal : allPrincipals) {
            if (principal instanceof SecurityUserDetails) {

                try {
                    SecurityUserDetails user = (SecurityUserDetails) principal;

                    List<SessionInformation> activeSessionInformations = new ArrayList<>();
                    activeSessionInformations.addAll(sessionRegistry.getAllSessions(principal, false));

                    for (SessionInformation sessionInformation : activeSessionInformations) {
                        SecuritySession securitySession = new SecuritySession();
                        securitySession.setUserName(user.getUsername());
                        securitySession.setSessionId(sessionInformation.getSessionId());
                        securitySession.setLastRequest(new LocalDateTime(sessionInformation.getLastRequest()));

                        activeSessions.add(securitySession);
                    }
                } catch (Exception e) {
                    logger.error("Error at creating list of active sessions",e);
                }
            }
        }
        return activeSessions;
    }

    /**
     * Return singleton variable of type SessionService.
     *
     * @return unique instance of SessionService
     */
    public static SessionService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (SessionService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new SessionService();
                }
            }
        }
        return instance;
    }
}
