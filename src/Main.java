import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final Pattern MAIN_FUNCTION_PATTERN = Regex.MAIN_FUNCTION_PATTERN;
    private static final Pattern DISPLAY_STATEMENT_PATTERN = Regex.DISPLAY_STATEMENT_PATTERN;

    @SuppressWarnings("SpellCheckingInspection")
    public static void main(String[] args) {
        try {
            List<File> inokFiles = InokFiles.getInokFiles(new File("."));

            for (File file : inokFiles) {
                System.out.println("Running file: " + file.getAbsolutePath());
                try {
                    InokInterpreter.interpret(file);
                } catch (Exception e) {
                    System.err.println("Running file: " + file.getAbsolutePath());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

}