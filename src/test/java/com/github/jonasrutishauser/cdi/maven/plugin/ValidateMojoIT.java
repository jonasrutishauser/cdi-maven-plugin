package com.github.jonasrutishauser.cdi.maven.plugin;

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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.takari.maven.testing.TestResources;
import io.takari.maven.testing.executor.MavenExecutionResult;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenRuntime.MavenRuntimeBuilder;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenJUnitTestRunner;

@RunWith(MavenJUnitTestRunner.class)
@MavenVersions({"3.3.3", "3.3.9", "3.5.0"})
public class ValidateMojoIT {

    @Rule
    public final TestResources resources = new TestResources();

    public final MavenRuntime mavenRuntime;

    public ValidateMojoIT(MavenRuntimeBuilder builder) throws Exception {
        this.mavenRuntime = builder.withCliOptions("-B", "-X").build();
    }

    @Test
    public void verifySimpleValid() throws Exception {
        File basedir = resources.getBasedir("simple");

        MavenExecutionResult result = mavenRuntime.forProject(basedir).execute("clean", "verify");

        result.assertErrorFreeLog().assertLogText("Session bean [class test.TestBean");
    }

    @Test
    public void verifySimpleNotValid() throws Exception {
        File basedir = resources.getBasedir("simple");
        Files.delete(basedir.toPath().resolve("simple-java").resolve("src").resolve("main").resolve("resources")
                .resolve("META-INF").resolve("beans.xml"));

        MavenExecutionResult result = mavenRuntime.forProject(basedir).execute("clean", "verify");

        result.assertLogText("Unsatisfied dependencies for type Foo with qualifiers @Default")
                .assertLogText("private test.TestBean.foo");
    }

    @Test
    public void verifyEjbsValid() throws Exception {
        File basedir = resources.getBasedir("ejbs");

        MavenExecutionResult result = mavenRuntime.forProject(basedir).execute("clean", "verify");

        result.assertErrorFreeLog().assertLogText("Managed Bean [class test.TestImpl]");
    }

    @Test
    public void verifyEjbsNotValid() throws Exception {
        File basedir = resources.getBasedir("ejbs");
        Files.write(basedir.toPath().resolve("simple-java").resolve("src").resolve("main").resolve("resources")
                .resolve("META-INF").resolve("beans.xml"), new byte[0]);

        MavenExecutionResult result = mavenRuntime.forProject(basedir).execute("clean", "verify");

        result.assertLogText("Ambiguous dependencies for type Foo with qualifiers @Default")
                .assertLogText("protected test.TestImpl(Foo)").assertLogText("Managed Bean [class test.FooImpl]")
                .assertLogText("Session bean [class test.FooBean");
    }

    @Test
    public void verifyEjbsValidSecondEjbLocalBean() throws Exception {
        File basedir = resources.getBasedir("ejbs");
        Path ejbDescriptor = basedir.toPath().resolve("simple-ejb").resolve("src").resolve("main").resolve("resources")
                .resolve("META-INF").resolve("ejb-jar.xml");
        Files.createDirectories(ejbDescriptor.getParent());
        Files.write(ejbDescriptor, Arrays.asList(
                "<ejb-jar version=\"3.1\" xmlns=\"http://java.sun.com/xml/ns/javaee\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/ejb-jar_3_1.xsd\">",
                "<enterprise-beans>", //
                "<session>", //
                "<ejb-name>OtherBean</ejb-name>", //
                "<ejb-class>test.FooBean</ejb-class>", //
                "<local-bean/>", //
                "</session>", //
                "</enterprise-beans>", //
                "</ejb-jar>"), StandardCharsets.UTF_8);

        MavenExecutionResult result = mavenRuntime.forProject(basedir).execute("clean", "verify");

        result.assertErrorFreeLog().assertLogText("local interfaces are [Foo]")
                .assertLogText("local interfaces are [FooBean]");
    }

    @Test
    public void verifyEjbsNotValidSecondEjb() throws Exception {
        File basedir = resources.getBasedir("ejbs");
        Path ejbDescriptor = basedir.toPath().resolve("simple-ejb").resolve("src").resolve("main").resolve("resources")
                .resolve("META-INF").resolve("ejb-jar.xml");
        Files.createDirectories(ejbDescriptor.getParent());
        Files.write(ejbDescriptor, Arrays.asList(
                "<ejb-jar version=\"3.1\" xmlns=\"http://java.sun.com/xml/ns/javaee\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/ejb-jar_3_1.xsd\">",
                "<enterprise-beans>", //
                "<session>", //
                "<ejb-name>OtherBean</ejb-name>", //
                "<ejb-class>test.FooBean</ejb-class>", //
                "</session>", //
                "</enterprise-beans>", //
                "</ejb-jar>"), StandardCharsets.UTF_8);

        MavenExecutionResult result = mavenRuntime.forProject(basedir).execute("clean", "verify");

        result.assertLogText("Ambiguous dependencies for type Foo with qualifiers @Default")
                .assertLogText("protected test.TestImpl(Foo)") //
                .assertLogText("Session bean [class test.FooBean");
    }

    @Test
    public void verifyWarsValid() throws Exception {
        File basedir = resources.getBasedir("wars");

        MavenExecutionResult result = mavenRuntime.forProject(basedir).execute("clean", "verify");

        result.assertErrorFreeLog().assertLogText("Managed Bean [class test.TestImpl]");
    }

    @Test
    public void verifyWarsNotValid() throws Exception {
        File basedir = resources.getBasedir("wars");
        Path javaBean = basedir.toPath().resolve("simple-war2/src/main/java/test/FooBean.java");
        List<String> lines = Files.readAllLines(javaBean, StandardCharsets.UTF_8);
        lines.add(7, "@javax.ejb.LocalBean");
        lines.add(7, "@javax.ejb.Local(FooBean.class)");
        Files.write(javaBean, lines, StandardCharsets.UTF_8);

        MavenExecutionResult result = mavenRuntime.forProject(basedir).execute("clean", "verify");

        result.assertLogText("Unsatisfied dependencies for type Foo with qualifiers @Default")
                .assertLogText("protected test.TestImpl(Foo)").assertLogText("SKIPPED");
    }

}
