package com.github.jonasrutishauser.cdi.maven.plugin.weld.services;

/*
 * Copyright (C) 2017 Jonas Rutishauser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/lgpl-3.0.txt>.
 */

import java.security.Principal;

import org.jboss.weld.security.spi.SecurityServices;

public final class MockSecurityServices implements SecurityServices {
    @Override
    public void cleanup() {
        // nothing
    }

    @Override
    public Principal getPrincipal() {
        return null;
    }
}