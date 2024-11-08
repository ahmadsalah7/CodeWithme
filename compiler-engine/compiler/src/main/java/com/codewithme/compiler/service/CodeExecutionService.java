package com.codewithme.compiler.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.concurrent.CompletableFuture;

@Service
public class CodeExecutionService {

    @Async
    public CompletableFuture<String> execute(String language, String code, String input) {
        try {
            File codeFile = createTempCodeFile(language);

            // Adjust Java class name in code if needed
            if ("java".equalsIgnoreCase(language)) {
                String fileNameWithoutExtension = stripExtension(codeFile.getName());
                code = replaceClassName(code, fileNameWithoutExtension);
            }

            writeCodeToFile(codeFile, code);

            File inputFile = (input != null && !input.isEmpty()) ? saveInputToFile(input) : null;

            String dockerCommand = buildDockerCommand(language, codeFile, inputFile);
            System.out.println("Executing Docker command: " + dockerCommand);

            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", dockerCommand);
            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

                reader.lines().forEach(line -> output.append(line).append("\n"));

                errorReader.lines().forEach(errorLine -> errorOutput.append(errorLine).append("\n"));
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return CompletableFuture.completedFuture("Execution failed with exit code: " + exitCode + "\nErrors:\n" + errorOutput);
            }

            return CompletableFuture.completedFuture(output.toString());

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();  // Preserve interrupt status
            return CompletableFuture.completedFuture("Execution failed: " + e.getMessage());
        }
    }


    public File createTempCodeFile(String language) throws IOException {
        String extension;
        if (language.equalsIgnoreCase("java")) {
            extension = ".java";
        } else if (language.equalsIgnoreCase("python")) {
            extension = ".py";
        } else if (language.equalsIgnoreCase("cpp")) {
            extension = ".cpp";
        } else {
            throw new IllegalArgumentException("Unsupported language: " + language);
        }
        File codeDirectory = new File("/code");
        codeDirectory.mkdirs();
        return File.createTempFile("CodeSnippet", extension);
    }

    public void writeCodeToFile(File codeFile, String code) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(codeFile))) {
            writer.write(code);
        }
        codeFile.deleteOnExit(); // delete it since you used try with resources
    }

    public File saveInputToFile(String input) throws IOException {
        if (input == null || input.isEmpty()) {
            return null;
        }
        File tempFile = File.createTempFile("inputSnippet", ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write(input);
        }
        tempFile.deleteOnExit();
        return tempFile;
    }

    public String buildDockerCommand(String language, File codeFile, File inputFile) throws IOException {
        String dockerImage;
        String runCommand = "";

        switch (language.toLowerCase()) {
            case "java":
                dockerImage = "openjdk:17";
                runCommand = "javac " + codeFile.getName() + " && java " + stripExtension(codeFile.getName());
                break;
            case "python":
                dockerImage = "python:latest";
                runCommand = "python " + codeFile.getName();
                break;
            case "cpp":
                dockerImage = "gcc:latest";
                runCommand = "g++ " + codeFile.getName() + " -o output && ./output";
                break;
            default:
                throw new IllegalArgumentException("Unsupported language: " + language);
        }

        String codeFilePath = convertToUnixPath(codeFile.getAbsolutePath());

        StringBuilder dockerCommand = new StringBuilder();
        dockerCommand.append("docker run --rm ")
                .append("-v ").append(codeFilePath).append(":").append("/code/").append(codeFile.getName()).append(" ");

        if (inputFile != null) {
            String inputFilePath = convertToUnixPath(inputFile.getAbsolutePath());
            dockerCommand.append("-v ").append(inputFilePath).append(":").append("/code/").append(inputFile.getName()).append(" ");
        }

        dockerCommand.append(dockerImage).append(" /bin/sh -c 'cd /code/ && ").append(runCommand);
        if (inputFile != null) {
            dockerCommand.append(" < ").append("/code/").append(inputFile.getName());
        }
        dockerCommand.append("'");
        return dockerCommand.toString();
    }

    public String replaceClassName(String code, String newClassName) {
        return code.replaceAll("public class\\s+\\w+", "public class " + newClassName);
    }

    public String convertToUnixPath(String windowsPath) {
        return windowsPath.replace("\\", "/").replace("C:", "/mnt/c");
    }

    public String stripExtension(String filename) {
        return filename != null && filename.contains(".") ? filename.substring(0, filename.lastIndexOf('.')) : filename;
    }
}
