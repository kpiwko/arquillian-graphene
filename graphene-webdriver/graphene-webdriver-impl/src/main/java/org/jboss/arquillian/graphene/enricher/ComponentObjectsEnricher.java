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
package org.jboss.arquillian.graphene.enricher;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jboss.arquillian.graphene.context.GrapheneContext;
import org.jboss.arquillian.graphene.proxy.GrapheneProxy;
import org.jboss.arquillian.graphene.spi.annotations.Page;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.openqa.selenium.By;
import org.openqa.selenium.HasInputDevices;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Enricher is a class for injecting into fields initialised <code>WebElement</code> and <code>Component</code> objects.
 *
 * @author <a href="mailto:jhuska@redhat.com">Juraj Huska</a>
 *
 */
public class ComponentObjectsEnricher implements TestEnricher {

    private static final String FIND_BY_ANNOTATION = "org.openqa.selenium.support.FindBy";
    private static final String PAGE_ANNOTATION = "org.jboss.arquillian.graphene.spi.annotations.Page";

    @Override
    public void enrich(Object testCase) {

        // at first initialize findBy annotations
        if (ReflectionHelper.isClassPresent(FIND_BY_ANNOTATION)) {

            initFieldsAnnotatedByFindBy(testCase);
        }
        // initialize Page objects if there are any
        if (ReflectionHelper.isClassPresent(PAGE_ANNOTATION)) {

            List<Field> fields = ReflectionHelper.getFieldsWithAnnotation(testCase.getClass(), Page.class);

            initializePageObjectFields(testCase, fields);
        }
    }

    private void initFieldsAnnotatedByFindBy(Object object) {

        // gets all fields with findBy annotations and then removes these
        // which are not Components objects
        List<Field> fields = ReflectionHelper.getFieldsWithAnnotation(object.getClass(), FindBy.class);

        List<Field> copy = new ArrayList<Field>();
        copy.addAll(fields);

        fields = removePlainFindBy(fields);
        initComponentFields(fields, object);

        // initialize other non component Find By fields
        copy.removeAll(fields);
        initNotComponentFields(copy, object);
    }

    private void initializePageObjectFields(Object testCase, List<Field> fields) {

        for (Field i : fields) {

            try {
                Class declaredClass = Class.forName(i.getGenericType().toString().split(" ")[1]);
                Object page = declaredClass.newInstance();

                List<Field> componentFields = ReflectionHelper.getFieldsWithAnnotation(declaredClass, FindBy.class);

                initFieldsAnnotatedByFindBy(page);

                boolean accessible = i.isAccessible();
                if (!accessible) {
                    i.setAccessible(true);
                }
                i.set(testCase, page);
                if (!accessible) {
                    i.setAccessible(false);
                }
            } catch (Exception ex) {
                throw new RuntimeException("Can not initialise Page Object!");
            }
        }
    }

    private void initNotComponentFields(List<Field> fields, Object object) {

        for (Field i : fields) {

            FindBy findBy = i.getAnnotation(FindBy.class);
            final By by = Factory.getReferencedBy(findBy);

            WebElement element = setUpTheProxy(by);

            setObjectToField(i, object, element);
        }
    }

    private void setObjectToField(Field field, Object objectWithField, Object object) {

        boolean accessible = field.isAccessible();
        if (!accessible) {
            field.setAccessible(true);
        }
        try {
            field.set(objectWithField, object);
        } catch (Exception e) {
            // TODO more grained
            throw new RuntimeException("The components fields can not be initialised!", e);
        }
        if (!accessible) {
            field.setAccessible(false);
        }
    }

    private WebElement setUpTheProxy(final By by) {
        WebElement e = GrapheneProxy.getProxyForFutureTarget(new GrapheneProxy.FutureTarget() {

            @Override
            public Object getTarget() {
                WebDriver driver = GrapheneContext.getProxyForInterfaces(HasInputDevices.class);
                WebElement root = driver.findElement(by);
                return root;
            }
        }, WebElement.class);
        return e;
    }

    private void initComponentFields(List<Field> fields, Object object) {
        for (Field componentField : fields) {

            // sets the root of the component, retrieved from annotation
            FindBy findBy = componentField.getAnnotation(FindBy.class);
            final By by = Factory.getReferencedBy(findBy);

            WebElement rootElement = setUpTheProxy(by);

            // initialise component
            Class<?> implementationClass = componentField.getType();
            Object component = Factory.initializeComponent(implementationClass, rootElement);

            setObjectToField(componentField, object, component);
        }
    }

    /**
     * It removes all components from given list which does not extend the
     * <code>org.jboss.arquillian.graphene.spi.components.common.AbstractComponent</code>
     *
     * @param findByFields
     * @return
     */
    private List<Field> removePlainFindBy(List<Field> findByFields) {

        for (Iterator<Field> i = findByFields.iterator(); i.hasNext();) {

            Field field = i.next();

            Class<?> fieldType = field.getType();

            if (fieldType.equals(WebElement.class)) {
                i.remove();
            }
        }

        return findByFields;
    }

    @Override
    public Object[] resolve(Method method) {
        return new Object[method.getParameterTypes().length];
    }

}
