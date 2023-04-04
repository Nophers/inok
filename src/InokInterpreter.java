import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class InokInterpreter {

    public static void interpret(File file) throws IOException, Exception {
        List<String> lines = Files.readAllLines(file.toPath());
        InterpreterState state = new InterpreterState(file);
        Map<String, String> variables = new HashMap<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if(checkComments(line)) continue;
            switch (state.mode) {
                case MAIN_FUNCTION:
                    interpretMainFunctionLine(line, i, state, variables);
                    break;
                case NON_MAIN_FUNCTION:
                    interpretNonMainFunctionLine(line, i, state, variables);
                    break;
                default:
                    throw new IllegalStateException("Unknown interpreter mode: " + state.mode);
            }
        }
        checkForMissingClosingCurlyBracket(state, file);
        printSyntaxErrors(state, file);
    }

    private static void interpretMainFunctionLine(String line, int lineNumber, InterpreterState state, Map<String, String> variables) throws Exception {
        Matcher displayMatcher = Regex.DISPLAY_STATEMENT_PATTERN.matcher(line);
        if (displayMatcher.matches()) {
            interpretDisplayStatement(displayMatcher.group(1), lineNumber, state, variables);
            return;
        }
        Matcher variableDeclarationMatcher = Regex.VARIABLE_DECLARATION_PATTERN.matcher(line);
        if (variableDeclarationMatcher.matches()) {
            interpretVariableDeclaration(variableDeclarationMatcher.group(1), variableDeclarationMatcher.group(2), lineNumber, state, variables);
            return;
        }
        switch (line.trim()) {
            case "}" -> {
                state.hasClosingCurlyBracket = true;
                state.mode = InterpreterMode.NON_MAIN_FUNCTION;
            }
            default -> interpretUnknownSyntax(line.trim(), lineNumber, state);
        }
    }

    private static void interpretNonMainFunctionLine(String line, int lineNumber, InterpreterState state, Map<String, String> variables)
            throws Exception {
        Matcher mainFunctionMatcher = Regex.MAIN_FUNCTION_PATTERN.matcher(line);
        if (mainFunctionMatcher.matches()) {
            handleMainFunction(lineNumber, state);
        } else if (!line.trim().isEmpty()) {
            interpretUnknownSyntax(line.trim(), lineNumber, state);
        }
    }

    private static void handleMainFunction(int lineNumber, InterpreterState state) throws Exception {
        state.numMainFunctions++;
        if (state.numMainFunctions > 1) {
            throw new Exception("Multiple main functions found in file: " + state.file.getAbsolutePath());
        }
        state.mode = InterpreterMode.MAIN_FUNCTION;
    }

    private static boolean checkComments(String line) {
        return line.trim().startsWith("$$");
    }

    private static void interpretDisplayStatement(String message, int lineNumber, InterpreterState state, Map<String, String> variables) {
        try {
            String evaluatedMessage = evaluateVariables(message, variables);
            if (evaluatedMessage.trim().isEmpty()) {
                handleEmptyDisplayStatement(lineNumber, state);
            } else {
                evaluatedMessage = evaluatedMessage.replaceAll("\"", "");
                System.out.println(evaluatedMessage);
            }
        } catch (Exception e) {
            System.err.println("Error on line " + (lineNumber + 1) + ": " + e.getMessage());
            state.hasSyntaxError = true;
        }
    }

    private static void interpretVariableDeclaration(String variableName, String variableValue, int lineNumber, InterpreterState state, Map<String, String> variables) throws Exception {
        if (variableValue == null || variableValue.isEmpty()) {
            throw new Exception("Invalid syntax, " + variableName + " is empty on line " + (lineNumber + 1));
        }
        String evaluatedValue = evaluateVariables(variableValue, variables);
        variables.put(variableName, evaluatedValue);
    }

    private static String evaluateVariables(String message, Map<String, String> variables) throws Exception {
        Matcher variableReferenceMatcher = Regex.VARIABLE_REFERENCE_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (variableReferenceMatcher.find()) {
            String variableName = variableReferenceMatcher.group(1);
            String variableValue = variables.get(variableName);
            if (variableValue == null || variableValue.isEmpty()) {
                throw new Exception("Invalid syntax, " + variableName + " is empty");
            }
            variableReferenceMatcher.appendReplacement(sb, variableValue);
        }
        variableReferenceMatcher.appendTail(sb);
        return sb.toString();
    }

    private static void handleEmptyDisplayStatement(int lineNumber, InterpreterState state) {
        System.err.println("Error on line " + (lineNumber + 1) + ": Empty message in display statement");
        state.hasSyntaxError = true;
    }

    private static void interpretUnknownSyntax(String syntax, int lineNumber, InterpreterState state) {
        System.err.println("Error on line " + (lineNumber + 1) + ": Unknown syntax \"" + syntax + "\"");
        state.hasSyntaxError = true;
    }

    private static void checkForMissingClosingCurlyBracket(InterpreterState state, File file) {
        if (state.mode == InterpreterMode.MAIN_FUNCTION && state.numMainFunctions == 1
                && !state.hasClosingCurlyBracket) {
            System.err.println("Error: Missing closing curly bracket for main function");
            state.hasSyntaxError = true;
        }
    }

    private static void printSyntaxErrors(InterpreterState state, File file) {
        if (state.hasSyntaxError) {
            System.err.println("Syntax errors in file: " + file.getAbsolutePath());
        }
    }

    private static class InterpreterState {
        public InterpreterMode mode = InterpreterMode.NON_MAIN_FUNCTION;
        public boolean hasSyntaxError = false;
        public int numMainFunctions = 0;
        public boolean hasClosingCurlyBracket = false;
        public File file;

        public InterpreterState(File file) {
            this.file = file;
        }
    }

    private enum InterpreterMode {
        MAIN_FUNCTION,
        NON_MAIN_FUNCTION
    }
}