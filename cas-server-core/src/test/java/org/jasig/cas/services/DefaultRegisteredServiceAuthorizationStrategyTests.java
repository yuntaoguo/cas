/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.cas.services;

import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * This is test cases for
 * {@link org.jasig.cas.services.DefaultRegisteredServiceAuthorizationStrategy}.
 *
 * @author Misagh Moayyed mmoayyed@unicon.net
 * @since 4.1
 */
public class DefaultRegisteredServiceAuthorizationStrategyTests {
    @Test
     public void checkDefaultAuthzStrategyConfig() {
        final RegisteredServiceAuthorizationStrategy authz =
                new DefaultRegisteredServiceAuthorizationStrategy();
        assertTrue(authz.isServiceAuthorized());
        assertTrue(authz.isServiceAuthorizedForSso());
    }

    @Test
    public void checkDisabledAuthzStrategyConfig() {
        final RegisteredServiceAuthorizationStrategy authz =
                new DefaultRegisteredServiceAuthorizationStrategy(false, true);
        assertFalse(authz.isServiceAuthorized());
        assertTrue(authz.isServiceAuthorizedForSso());
    }

    @Test
    public void checkDisabledSsoAuthzStrategyConfig() {
        final RegisteredServiceAuthorizationStrategy authz =
                new DefaultRegisteredServiceAuthorizationStrategy(true, false);
        assertTrue(authz.isServiceAuthorized());
        assertFalse(authz.isServiceAuthorizedForSso());
    }

    @Test
    public void setAuthzStrategyConfig() {
        final DefaultRegisteredServiceAuthorizationStrategy authz =
                new DefaultRegisteredServiceAuthorizationStrategy(false, false);
        authz.setEnabled(true);
        authz.setSsoEnabled(true);
        assertTrue(authz.isServiceAuthorized());
        assertTrue(authz.isServiceAuthorizedForSso());
        assertTrue(authz.isRequireAllAttributes());
    }

    @Test
    public void checkAuthzPrincipalNoAttrRequirements() {
        final DefaultRegisteredServiceAuthorizationStrategy authz =
                new DefaultRegisteredServiceAuthorizationStrategy();
        assertTrue(authz.isServiceAccessAuthorizedForPrincipal(new HashMap<String, Object>()));
    }

    @Test
    public void checkAuthzPrincipalWithAttrRequirementsEmptyPrincipal() {
        final DefaultRegisteredServiceAuthorizationStrategy authz =
                new DefaultRegisteredServiceAuthorizationStrategy();
        authz.setRequiredAttributes(this.getRequiredAttributes());
        assertFalse(authz.isServiceAccessAuthorizedForPrincipal(new HashMap<String, Object>()));
    }

    @Test
    public void checkAuthzPrincipalWithAttrRequirementsAll() {
        final DefaultRegisteredServiceAuthorizationStrategy authz =
                new DefaultRegisteredServiceAuthorizationStrategy();
        authz.setRequiredAttributes(this.getRequiredAttributes());
        assertTrue(authz.isServiceAccessAuthorizedForPrincipal(
                this.getPrincipalAttributes()));
    }

    @Test
    public void checkAuthzPrincipalWithAttrRequirementsMissingOne() {
        final DefaultRegisteredServiceAuthorizationStrategy authz =
                new DefaultRegisteredServiceAuthorizationStrategy();
        authz.setRequiredAttributes(this.getRequiredAttributes());

        final Map<String, Object> pAttrs = this.getPrincipalAttributes();
        pAttrs.remove("cn");

        assertFalse(authz.isServiceAccessAuthorizedForPrincipal(pAttrs));
    }

    @Test
    public void checkAuthzPrincipalWithAttrRequirementsMissingOneButNotAllNeeded() {
        final DefaultRegisteredServiceAuthorizationStrategy authz =
                new DefaultRegisteredServiceAuthorizationStrategy();
        authz.setRequiredAttributes(this.getRequiredAttributes());
        authz.setRequireAllAttributes(false);
        final Map<String, Object> pAttrs = this.getPrincipalAttributes();
        pAttrs.remove("cn");

        assertTrue(authz.isServiceAccessAuthorizedForPrincipal(pAttrs));
    }

    @Test
    public void checkAuthzPrincipalWithAttrRequirementsNoValueMatch() {
        final DefaultRegisteredServiceAuthorizationStrategy authz =
                new DefaultRegisteredServiceAuthorizationStrategy();
        authz.setRequiredAttributes(this.getRequiredAttributes());
        authz.setRequireAllAttributes(false);
        final Map<String, Object> pAttrs = this.getPrincipalAttributes();
        pAttrs.remove("cn");
        pAttrs.put("givenName", "theName");
        assertFalse(authz.isServiceAccessAuthorizedForPrincipal(pAttrs));
    }

    @Test
    public void checkAuthzPrincipalWithAttrValueCaseSensitiveComparison() {
        final DefaultRegisteredServiceAuthorizationStrategy authz =
                new DefaultRegisteredServiceAuthorizationStrategy();
        authz.setRequiredAttributes(this.getRequiredAttributes());
        final Map<String, Object> pAttrs = this.getPrincipalAttributes();
        pAttrs.put("cn", "CAS");
        pAttrs.put("givenName", "kaz");
        assertFalse(authz.isServiceAccessAuthorizedForPrincipal(pAttrs));
    }


    private Map<String, Set<String>> getRequiredAttributes() {
        final Map<String, Set<String>> map = new HashMap<>();
        map.put("cn", Sets.newHashSet("cas", "SSO"));
        map.put("givenName", Sets.newHashSet("CAS", "KAZ"));
        return map;
    }

    private Map<String, Object> getPrincipalAttributes() {
        final Map<String, Object> map = new HashMap<>();
        map.put("cn", "cas");
        map.put("givenName", Arrays.asList("cas", "KAZ"));
        map.put("sn", "surname");
        map.put("phone", "123-456-7890");

        return map;
    }

}
