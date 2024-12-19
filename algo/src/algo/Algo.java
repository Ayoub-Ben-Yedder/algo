package algo;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * The Algo class serves as the entry point for running the algorithm.
 * It contains methods to read the source code, run the lexer to generate tokens,
 * and interpret the tokens to execute the program.
 */
public class Algo {
    public static String runAlgo(String sourceCode) {
        try {
            // Redirect stderr to a log file
        	PrintStream logCapture =  new PrintStream(new FileOutputStream("logs.log"));
            System.setErr(logCapture);

            // Run the lexer and interpreter
            Lexer lexer = new Lexer(sourceCode);
            List<Token> tokens = lexer.scanTokens();
            Interpreter interpreter = new Interpreter(tokens);
            interpreter.interpret();

            return "Program executed successfully.";
        } catch (Exception ex) {
            // Add the error message to log file
            System.err.println("Error during execution: " + ex.getMessage());
            return ex.getMessage();
        } finally {
            // Restore stderr
            System.setErr(System.err);
        }
    }

    public static void main(String[] args) {
        // Check if the file path is provided as an argument
    	if (args.length != 1) {
            System.err.println("Usage: java Algo <file-path>");
            return;
        }
        String filePath = args[0];
        String output;

        try {
            // Read the source code from the file
            String fileContent = new String(Files.readAllBytes(Paths.get(filePath)));
            // Run the algorithm
            output = runAlgo(fileContent);
        } catch (IOException ex) {
            // Handle file errors
            System.err.println("File error: could not open " + ex.getMessage());
            output = "File error: " + ex.getMessage();
        } catch (Exception ex) {
            // Handle unexpected errors
            System.err.println("An unexpected error occurred: " + ex.getMessage());
            ex.printStackTrace();
            output = "Error: " + ex.getMessage();
        }
        // Print the output
        System.out.println(output);
    }

}
