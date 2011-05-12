/**
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.arquillian.ajocado.testng.ftest;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.ajocado.framework.AjaxSelenium;
import org.jboss.arquillian.ajocado.utils.URLUtils;
import org.jboss.arquillian.api.ArquillianResource;
import org.jboss.arquillian.api.RunAsClient;
import org.jboss.arquillian.drone.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

@RunAsClient
@RunWith(Arquillian.class)
public class AbstractTest {

    @Drone
    protected AjaxSelenium selenium;

    @ArquillianResource
    protected URL applicationPath;

    protected static WebArchive createDeploymentForClass(Class<? extends AbstractTest> testClass) {
        return ShrinkWrap.create(WebArchive.class, "ftest-app.war")
            .addAsWebInfResource(new File("src/test/webapp/WEB-INF/web.xml"))
            .addAsResource(new File("src/test/webapp/" + testClass.getSimpleName() + ".jsp"));
    }

    protected void openContext() {
        selenium.open(URLUtils.buildUrl(applicationPath, this.getClass().getSimpleName() + ".jsp"));
    }
}
