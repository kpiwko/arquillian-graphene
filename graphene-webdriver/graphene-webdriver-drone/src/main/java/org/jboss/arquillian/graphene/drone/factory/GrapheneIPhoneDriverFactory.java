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
package org.jboss.arquillian.graphene.drone.factory;

import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.configuration.IPhoneDriverConfiguration;
import org.jboss.arquillian.drone.webdriver.configuration.TypedWebDriverConfiguration;
import org.jboss.arquillian.drone.webdriver.factory.IPhoneDriverFactory;
import org.jboss.arquillian.graphene.context.GrapheneContext;
import org.openqa.selenium.iphone.IPhoneDriver;

/**
 * Extends the {@link IPhoneDriverFactory} and provides the created instance to the {@link GrapheneContext}.
 *
 * @author Lukas Fryc
 *
 */
public class GrapheneIPhoneDriverFactory extends IPhoneDriverFactory implements
        Configurator<IPhoneDriver, TypedWebDriverConfiguration<IPhoneDriverConfiguration>>,
        Instantiator<IPhoneDriver, TypedWebDriverConfiguration<IPhoneDriverConfiguration>>, Destructor<IPhoneDriver> {

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.spi.Sortable#getPrecedence()
     */
    @Override
    public int getPrecedence() {
        return 20;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.spi.Destructor#destroyInstance(java.lang.Object)
     */
    @Override
    public void destroyInstance(IPhoneDriver instance) {
        try {
            super.destroyInstance(instance);
        } finally {
            GrapheneContext.reset();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.arquillian.drone.spi.Instantiator#createInstance(org.jboss.arquillian.drone.spi.DroneConfiguration)
     */
    @Override
    public IPhoneDriver createInstance(TypedWebDriverConfiguration<IPhoneDriverConfiguration> configuration) {
        IPhoneDriver driver = super.createInstance(configuration);
        IPhoneDriver proxy = GrapheneContext.getProxyForDriver(IPhoneDriver.class);
        GrapheneContext.set(driver);
        return proxy;
    }
}