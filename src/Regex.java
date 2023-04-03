import java.util.regex.Pattern;

public class Regex {
    public static final Pattern MAIN_FUNCTION_PATTERN = Pattern.compile("^fu\\s+main\\(\\)\\s*\\{\\s*$");
    public static final Pattern DISPLAY_STATEMENT_PATTERN = Pattern.compile("^\\s*display\\(\\s*\"(.*)\"\\s*\\)\\s*;\\s*$");
}