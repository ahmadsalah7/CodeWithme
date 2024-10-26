package com.codewithme.compiler.executionservice;


import com.codewithme.compiler.service.CodeExecutionService;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
public class CreatingFilesInExecutionCode {
    CodeExecutionService codeExecutionService = new CodeExecutionService();

    @Test
    void testCreateTempFileJava() throws IOException {
        String language = "java";

        File testFile = codeExecutionService.createTempCodeFile(language);

        assertNotNull(testFile ,"Created temp file should not be null");
        assertTrue(testFile.exists() ,"Created temp file should be exist");
        assertTrue(testFile.getName().startsWith("CodeSnippet"),"File name should start with CodeSnippet");
        assertTrue(testFile.getName().endsWith(".java"),"File extension should be .java");

        assertTrue(testFile.delete(), "Temp file should be deleted");
    }
    @Test
    void testCreateTempFilePython() throws IOException {
        String language = "python";

        File testFile = codeExecutionService.createTempCodeFile(language);

        assertNotNull(testFile ,"Created temp file should not be null");
        assertTrue(testFile.exists() ,"Created temp file should be exist");
        assertTrue(testFile.getName().startsWith("CodeSnippet"),"File name should start with CodeSnippet");
        assertTrue(testFile.getName().endsWith(".py"),"File extension should be .py");

        assertTrue(testFile.delete(), "Temp file should be deleted");
    }
    @Test
    void testCreateTempFileCpp() throws IOException {
        String language = "Cpp";

        File testFile = codeExecutionService.createTempCodeFile(language);

        assertNotNull(testFile ,"Created temp file should not be null");
        assertTrue(testFile.exists() ,"Created temp file should be exist");
        assertTrue(testFile.getName().startsWith("CodeSnippet"),"File name should start with CodeSnippet");
        assertTrue(testFile.getName().endsWith(".cpp"),"File extension should be .cpp");

        assertTrue(testFile.delete(), "Temp file should be deleted");
    }

    @Test
    void testCreateTempCodeFileForUnsupportedLanguage() {

        String unsupportedLanguage = "ruby";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            codeExecutionService.createTempCodeFile(unsupportedLanguage);
        });

        String expectedMessage = "Unsupported language";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testWriteCodeToFile() throws IOException {
        String code = "this is a code";

        File testFile = codeExecutionService.createTempCodeFile("java");
        codeExecutionService.writeCodeToFile(testFile, code);

        String fileContent = new String(Files.readAllBytes(testFile.toPath()));
        assertEquals(code, fileContent, "File content should match the provided code");

        assertTrue(testFile.delete(), "Temp file should be deleted");
    }
    @Test
    void testConvertToUnixPath() {
        String windowsPath = "C:\\Users\\user\\Desktop\\code.java";

        String expectedUnixPath = "/mnt/c/Users/user/Desktop/code.java";

        String actualUnixPath = codeExecutionService.convertToUnixPath(windowsPath);

        assertEquals(expectedUnixPath, actualUnixPath);
    }
    @Test
    void testReplaceClassName() {

        String originalCode = "public class OriginalClass { }";
        String newClassName = "NewClass";

        String expectedCode = "public class NewClass { }";

        String modifiedCode = codeExecutionService.replaceClassName(originalCode, newClassName);

        assertEquals(expectedCode, modifiedCode);
    }
    @Test
    void testStripExtension() {
        String filenameWithExtension = "codeSnippet.java";

        String expectedFilenameWithoutExtension = "codeSnippet";

        String actualFilenameWithoutExtension = codeExecutionService.stripExtension(filenameWithExtension);

        assertEquals(expectedFilenameWithoutExtension, actualFilenameWithoutExtension);

        String filenameWithoutExtension = "codeSnippet"; // try to pass with no extension
        assertEquals(filenameWithoutExtension, codeExecutionService.stripExtension(filenameWithoutExtension));
    }

    @Test
    void testSaveInputToFile() throws IOException {
        String input = "Sample input data";
        File inputFile = codeExecutionService.saveInputToFile(input);

        assertNotNull(inputFile, "Input file should not be null");
        assertTrue(inputFile.exists(), "Input file should exist");

        assertTrue(inputFile.getName().startsWith("inputSnippet"),"File name should start with CodeSnippet");
        assertTrue(inputFile.getName().endsWith(".txt"),"File name should ends with .txt");

        String fileContent = new String(Files.readAllBytes(inputFile.toPath()));
        assertEquals(input, fileContent, "Input file content should match the provided input");

        assertTrue(inputFile.delete(), "Input file should be deleted");
    }
}
