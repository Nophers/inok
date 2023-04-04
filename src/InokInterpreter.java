import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InokInterpreter {
    private static final Pattern MAIN_FUNCTION_PATTERN = Regex.MAIN_FUNCTION_PATTERN;
    private static final Pattern DISPLAY_STATEMENT_PATTERN = Regex.DISPLAY_STATEMENT_PATTERN;

    public static void interpret(File file) throws IOException, Exception {
        List<String> lines = Files.readAllLines(file.toPath());
        InterpreterState state = new InterpreterState(file);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            switch (state.mode) {
                case MAIN_FUNCTION:
                    interpretMainFunctionLine(line, i, state);
                    break;
                case NON_MAIN_FUNCTION:
                    interpretNonMainFunctionLine(line, i, state);
                    break;
                default:
                    throw new IllegalStateException("Unknown interpreter mode: " + state.mode);
            }
        }
        checkForMissingClosingCurlyBracket(state, file);
        printSyntaxErrors(state, file);
    }

    private static void interpretMainFunctionLine(String line, int lineNumber, InterpreterState state) {
        Matcher displayMatcher = DISPLAY_STATEMENT_PATTERN.matcher(line);
        if (displayMatcher.matches()) {
            interpretDisplayStatement(displayMatcher.group(1), lineNumber, state);
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

    private static void interpretNonMainFunctionLine(String line, int lineNumber, InterpreterState state)
            throws Exception {
        Matcher mainFunctionMatcher = MAIN_FUNCTION_PATTERN.matcher(line);
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

    private static void interpretDisplayStatement(String message, int lineNumber, InterpreterState state) {
        if (message.trim().isEmpty()) {
            handleEmptyDisplayStatement(lineNumber, state);
        } else {
            System.out.println(message);
        }
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