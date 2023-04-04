import java.io.File;
import java.util.List;

public class Main {
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