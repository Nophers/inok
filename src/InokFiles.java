import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("SpellCheckingInspection")
public class InokFiles {
    public static List<File> getInokFiles(File directory) throws Exception {
        List<File> inokFiles = new ArrayList<>();
        FileVisitor visitor = new InokFileVisitor(inokFiles);
        visitFiles(directory, visitor);
        return inokFiles;
    }

    private static void visitFiles(File file, FileVisitor visitor) {
        if (file.isDirectory()) {
            visitor.visitDirectory(file, () -> visitChildren(file, visitor));
        } else {
            visitor.visitFile(file);
        }
    }

    private static void visitChildren(File directory, FileVisitor visitor) {
        for (File child : Objects.requireNonNull(directory.listFiles())) {
            visitFiles(child, visitor);
        }
    }
}

interface FileVisitor {
    void visitDirectory(File directory, Runnable visitChildren);
    void visitFile(File file);
}

@SuppressWarnings("SpellCheckingInspection")
class InokFileVisitor implements FileVisitor {
    private final List<File> inokFiles;

    public InokFileVisitor(List<File> inokFiles) {
        this.inokFiles = inokFiles;
    }

    @Override
    public void visitDirectory(File directory, Runnable visitChildren) {
        if (!directory.getName().equals("out")) {
            visitChildren.run();
        }
    }

    @Override
    public void visitFile(File file) {
        if (file.getName().endsWith(".inok")) {
            inokFiles.add(file);
        }
    }
}