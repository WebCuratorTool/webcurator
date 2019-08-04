package org.webcurator.test;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class WCTTestUtils {
    protected static Log log = LogFactory.getLog(WCTTestUtils.class);

    /**
     * Returns the given resourcePath as a File. This is to accommodate situations where the resource may exist in
     * the file system or in a jar, depending on when the test was run.
     * <p>
     * This solution is not ideal, but it works within the current test classes with minimal modification.
     *
     * @param resourcePath
     * @return
     */
    public static File getResourceAsFile(String resourcePath) {
        File resourceFile = null;
        try {
            URL fileUrl = WCTTestUtils.class.getResource(resourcePath);
            Path resourceAsPath = null;
            try {
                resourceAsPath = Paths.get(fileUrl.toURI());
            } catch (FileSystemNotFoundException fsnfe) {
                Map<String, String> env = new HashMap<>();
                env.put("create", "true");
                // Create the file system by using the syntax defined in java.net.JarURLConnection
                FileSystem fileSystem = FileSystems.newFileSystem(fileUrl.toURI(), env);
                resourceAsPath = fileSystem.getPath(resourcePath);
            }
            if (resourceAsPath != null) {
                try {
                    resourceFile = resourceAsPath.toFile();
                } catch (UnsupportedOperationException use) {
                    resourceFile = convertToTemporaryFile(resourceAsPath);
                }
            }
        } catch (URISyntaxException | IOException e) {
            log.error("Unable to convert resourcePath=" + resourcePath + " to a File, exception=" + e, e);
        }
        return resourceFile;
    }

    public static File convertToTemporaryFile(Path resourceAsPath) throws IOException {
        File resourceFile = null;
        if (Files.isRegularFile(resourceAsPath)) {
            resourceFile = basicConvertToTemporaryFile(resourceAsPath);
        } else if (Files.isDirectory(resourceAsPath)) {
            Path targetPath = basicConvertToTemporaryFolder(resourceAsPath);
            DirectoryCopyVisitor directoryCopyVisitor = new DirectoryCopyVisitor(resourceAsPath, targetPath);
            Files.walkFileTree(resourceAsPath, directoryCopyVisitor);

            resourceFile = targetPath.toFile();
        }
        return resourceFile;
    }

    public static File basicConvertToTemporaryFile(Path resourceAsPath) throws IOException {
        String filename = resourceAsPath.getFileName().toString();
        final File resourceFile = File.createTempFile(FilenameUtils.getBaseName(filename) + "_",
                "." + FilenameUtils.getExtension(filename));
        resourceFile.deleteOnExit();
        copyUsingStreams(resourceAsPath, resourceFile.toPath());
        return resourceFile;
    }

    public static File copyUsingStreams(Path resourceAsPath, Path targetPath) throws IOException {
        InputStream resourceInputStream = Files.newInputStream(resourceAsPath);
        try (FileOutputStream fileOutputStream = new FileOutputStream(targetPath.toFile())) {
            IOUtils.copy(resourceInputStream, fileOutputStream);
        }
        return targetPath.toFile();
    }

    public static Path basicConvertToTemporaryFolder(Path resourceAsPath) throws IOException {
        String directoryName = resourceAsPath.getFileName().toString();
        final Path resourceDirectory = Files.createTempDirectory(directoryName + "_");

        return resourceDirectory;
    }

    static class DirectoryCopyVisitor extends SimpleFileVisitor<Path> {
        final Path source;
        final Path target;

        public DirectoryCopyVisitor(Path source, Path target) {
            this.source = source;
            this.target = target;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            // If the filesystems are different (i.e. source is zip filesystem and target is unix or windows
            // then we need to break the connection with the source filesystem, which we do by converting to string.
            Path relativePath = Paths.get(source.relativize(dir).toString());
            Path newDirectory = target.resolve(relativePath);
            try {
                Files.createDirectories(newDirectory);
            } catch (FileAlreadyExistsException ioException) {
                log.warn("Directory=" + dir + " already exists, skipping subtree but continuing.");
                return FileVisitResult.SKIP_SUBTREE; // skip processing
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            // If the filesystems are different (i.e. source is zip filesystem and target is unix or windows
            // then we need to break the connection with the source filesystem, which we do by converting to string.
            Path relativePath = Paths.get(source.relativize(file).toString());
            Path newFile = target.resolve(relativePath);
            copyUsingStreams(file, newFile);
            newFile.toFile().deleteOnExit();

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }
    }
}
