import java.util.regex.Pattern;

public class Regex {

        /*
         * Patterns for the main Interpreter
         */
        public static final Pattern MAIN_FUNCTION_PATTERN = Pattern.compile("^fu\\s+main\\(\\)\\s*\\{\\s*$");
        public static final Pattern DISPLAY_STATEMENT_PATTERN = Pattern
                        .compile("^\\s*display\\(\\s*\"(.*)\"\\s*\\)\\s*;\\s*$");
        public static final Pattern VARIABLE_DECLARATION_PATTERN = Pattern
                        .compile("^\\s*let\\s+(\\w+)\\s*=\\s*(.*)\\s*;\\s*$");
        public static final Pattern VARIABLE_REFERENCE_PATTERN = Pattern.compile("\\$\\{(\\w+|\\d+)}");
        public static final Pattern TOKEN_PATTERN = Pattern
                        .compile("\\s*(let|fu|main|display|\\{|\\$\\{|\\(|\\)|;|\\w+|\"[^\"]*\")");

        /*
         * Pattern for the Lexer
         */
        public static final Pattern LEXER_VARIABLE_REFERENCE_PATTERN = Pattern.compile("\\$\\{\\w+}");

        public static final Pattern LEXER_IDENTIFIER_PATTERN = Pattern.compile("\\w+");

        public static final Pattern LEXER_STRING_LITERAL_PATTERN = Pattern.compile("\"[^\"]*\"");
}