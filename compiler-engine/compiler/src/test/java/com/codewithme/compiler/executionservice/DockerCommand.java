package com.codewithme.compiler.executionservice;

import com.codewithme.compiler.service.CodeExecutionService;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
public class DockerCommand {
    CodeExecutionService codeExecutionService = new CodeExecutionService();

    @Test
    void testBuildDockerCommandWithoutInputForJava() throws IOException {

        File codeFile = codeExecutionService.createTempCodeFile("java");

        String dockerCommand = codeExecutionService.buildDockerCommand("java", codeFile, null);

        String expectedCommand = "docker run --rm -v " + codeExecutionService.convertToUnixPath(codeFile.getAbsolutePath()) +
                ":/code/" + codeFile.getName() + " openjdk:17 /bin/sh -c 'cd /code/ && javac " + codeFile.getName() +
                " && java " + codeFile.getName().replace(".java", "") + "'";

        assertEquals(expectedCommand, dockerCommand);
        assertTrue(codeFile.delete(), "Temp file should be deleted");
    }
    @Test
    void testBuildDockerCommandForPython() throws IOException {

        File codeFile = codeExecutionService.createTempCodeFile("python");

        String dockerCommand = codeExecutionService.buildDockerCommand("python", codeFile, null);

        String expectedCommand = "docker run --rm -v " + codeExecutionService.convertToUnixPath(codeFile.getAbsolutePath()) +
                ":/code/" + codeFile.getName() + " python:latest /bin/sh -c 'cd /code/ && python " + codeFile.getName() + "'";

        assertEquals(expectedCommand, dockerCommand);

        assertTrue(codeFile.delete(), "Temp file should be deleted");
    }
    @Test
    void testBuildDockerCommandForCpp() throws IOException {
        File codeFile = codeExecutionService.createTempCodeFile("cpp");

        String dockerCommand = codeExecutionService.buildDockerCommand("cpp", codeFile, null);

        String expectedCommand = "docker run --rm -v " + codeExecutionService.convertToUnixPath(codeFile.getAbsolutePath()) +
                ":/code/" + codeFile.getName() + " gcc:latest /bin/sh -c 'cd /code/ && g++ " + codeFile.getName() +
                " -o output && ./output'";

        assertEquals(expectedCommand, dockerCommand);
        assertTrue(codeFile.delete(), "Temp file should be deleted");
    }
    @Test
    void testBuildDockerCommandWithInputFile() throws IOException {
        File codeFile = codeExecutionService.createTempCodeFile("java");

        File inputFile = codeExecutionService.saveInputToFile("Sample input data");

        String dockerCommand = codeExecutionService.buildDockerCommand("java", codeFile, inputFile);

        String expectedCommand = "docker run --rm -v " + codeExecutionService.convertToUnixPath(codeFile.getAbsolutePath()) +
                ":/code/" + codeFile.getName() + " -v " + codeExecutionService.convertToUnixPath(inputFile.getAbsolutePath()) +
                ":/code/" + inputFile.getName() + " openjdk:17 /bin/sh -c 'cd /code/ && javac " + codeFile.getName() +
                " && java " + codeFile.getName().replace(".java", "") + " < /code/" + inputFile.getName() + "'";

        assertEquals(expectedCommand, dockerCommand);

        assertTrue(codeFile.delete(), "Temp code file should be deleted");
        assertTrue(inputFile.delete(), "Temp input file should be deleted");
    }

    @Test
    void testBuildDockerCommandForUnsupportedLanguage() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            codeExecutionService.buildDockerCommand("ruby", new File("dummy"), null);
        });

        String expectedMessage = "Unsupported language";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

}
