/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jpm;

/**
 *
 * @author Aniket
 */
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 *
 * @author Aniket
 */
public class Manager {

    private static final String OS = System.getProperty("os.name").toLowerCase();

    public Manager() {
    }

    public static void compile(File dir) {
        ProcessBuilder pb = getCompileString(dir);
        pb.redirectErrorStream(true);
        pb.directory(dir);
        try {
            Process start = pb.start();
            System.out.println("Compile Files for Project " + new File(dir.getAbsolutePath()).getName());
            Thread a, b;
            a = new Thread(new OutputReader(start.getInputStream()));
            a.start();
            int waitFor = start.waitFor();
            System.out.println(waitFor);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static ProcessBuilder getCompileString(File dir) {
        String one;
        if (OS.contains("win")) {
            one = "\"" + JPM.getJDK() + File.separator + "javac\""
                    + getFileList(dir) + "//-d//"
                    + new File(dir.getAbsolutePath(), "build").getAbsolutePath()
                    + (new File(dir.getAbsolutePath(), "libs").listFiles().length == 0 ? "" : ("//-classpath" + getLibsList(dir)));
        } else {
            one = JPM.getJDK() + File.separator + "javac"
                    + getFileList(dir)
                    + "//-d//"
                    + new File(dir.getAbsolutePath(), "build").getAbsolutePath()
                    + (new File(dir.getAbsolutePath(), "libs").listFiles().length == 0 ? "" : ("//-classpath" + getLibsList(dir)));
        }
        System.out.print(one);
        return new ProcessBuilder(one.split("//"));
    }

    private static String getFileList(File dir) {
        StringBuilder sb = new StringBuilder();
        File src = new File(dir.getAbsolutePath(), "src");
        for (File f : src.listFiles()) {
            addToFileList(f, sb);
        }
        return sb.toString();
    }

    private static void addToFileList(File f, StringBuilder sb) {
        if (f.isDirectory()) {
            for (File fe : f.listFiles()) {
                addToFileList(fe, sb);
            }
        } else {
            sb.append("//").append(f.getAbsolutePath());
        }
    }

    private static String getLibsList(File dir) {
        StringBuilder sb = new StringBuilder();
        File src = new File(dir.getAbsolutePath(), "libs");
        for (File f : src.listFiles()) {
            if (f.getName().endsWith(".jar")) {
                sb.append("//").append(f.getAbsolutePath());
            }
        }
        return sb.toString();
    }

    public static void build(File f, String main) {
        compile(f);
        ProcessBuilder pb;
        if (OS.contains("win")) {
            pb = getWindowsBuildString(f, main);
        } else {
            pb = getMacBuildString(f, main);
        }
        pb.directory(new File(f.getAbsolutePath()));
        pb.redirectErrorStream(true);
        try {
            Process start = pb.start();
            System.out.print("Build Jar File for Project " + f.getName());
            (new Thread(new OutputReader(start.getInputStream()))).start();
            int waitFor = start.waitFor();
            System.out.println(waitFor);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static ProcessBuilder getWindowsBuildString(File dir, String main) {
        File f = new File(JPM.getJDK() + File.separator + "javapackager" + (OS.contains("win") ? ".exe" : ""));
        String one;
        if (f.exists()) {
            one = "\"" + JPM.getJDK() + File.separator + "javapackager\"" + "//-createjar//-appclass//" + main
                    + "//-srcdir//" + new File(dir.getAbsolutePath(), "build").getAbsolutePath() + "//-outdir//"
                    + new File(dir.getAbsolutePath(), "dist").getAbsolutePath() + "//-outfile//" + new File(dir.getAbsolutePath()).getName() + ".jar"
                    + (new File(dir.getAbsolutePath(), "libs").listFiles().length == 0 ? "" : ("//-classpath" + getLibsList(dir)));
        } else {
            one = "\"" + JPM.getJDK() + File.separator + "javafxpackager\"" + "//-createjar//-appclass//" + main
                    + "//-srcdir//" + new File(dir.getAbsolutePath(), "build").getAbsolutePath() + "//-outdir//"
                    + new File(dir.getAbsolutePath(), "dist").getAbsolutePath() + "//-outfile//" + new File(dir.getAbsolutePath()).getName() + ".jar"
                    + (new File(dir.getAbsolutePath(), "libs").listFiles().length == 0 ? "" : ("//-classpath" + getLibsList(dir)));
        }
        System.out.println(one);
        return new ProcessBuilder(one.split("//"));
    }

    private static ProcessBuilder getMacBuildString(File dir, String main) {
        File f = new File(JPM.getJDK() + File.separator + "javapackager" + (OS.contains("win") ? ".exe" : ""));
        String one;
        if (f.exists()) {
            one = JPM.getJDK() + File.separator + "javapackager" + "//-createjar//-appclass//" + main
                    + "//-srcdir//" + new File(dir.getAbsolutePath(), "build").getAbsolutePath() + "//-outdir//"
                    + new File(dir.getAbsolutePath(), "dist").getAbsolutePath() + "//-outfile//" + new File(dir.getAbsolutePath()).getName() + ".jar"
                    + (new File(dir.getAbsolutePath(), "libs").listFiles().length == 0 ? "" : ("//-classpath" + getLibsList(dir)));
        } else {
            one = JPM.getJDK() + File.separator + "javafxpackager" + "//-createjar//-appclass " + main
                    + "//-srcdir//" + new File(dir.getAbsolutePath(), "build").getAbsolutePath() + "//-outdir//"
                    + new File(dir.getAbsolutePath(), "dist").getAbsolutePath() + "//-outfile//" + new File(dir.getAbsolutePath()).getName() + ".jar"
                    + (new File(dir.getAbsolutePath(), "libs").listFiles().length == 0 ? "" : ("//-classpath" + getLibsList(dir)));
        }
        System.out.println(one);
        return new ProcessBuilder(one.split("//"));
    }

    public static void run(File f, String name) {
        build(f, name);
        ProcessBuilder pb = getRunString(f);
        pb.directory(f);
        pb.redirectErrorStream(true);
        try {
            Process start = pb.start();
            System.out.println("Launching Jar File for Project " + f.getName());
            (new Thread(new OutputReader(start.getInputStream()))).start();
            int waitFor = start.waitFor();
            System.out.println(waitFor);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static ProcessBuilder getRunString(File dir) {
        String one;
        if (OS.contains("win")) {
            one = "\"" + JPM.getJDK()
                    + File.separator + "java\"" + "//-jar//" + new File(dir, "dist").getAbsolutePath() 
                    + File.separator + dir.getName() + ".jar"
                    + (new File(dir, "libs").listFiles().length == 0 ? "" : ("//-classpath" + getLibsList(dir)));
        } else {
            one = JPM.getJDK()
                    + File.separator + "java" + "//-jar " + new File(dir, "dist").getAbsolutePath() + "//"
                    + File.separator + dir.getName() + ".jar"
                    + (new File(dir, "libs").listFiles().length == 0 ? "" : ("//-classpath" + getLibsList(dir)));
        }
        System.out.println(one);        
        System.out.println(Arrays.asList(one.split("//")));
        return new ProcessBuilder(one.split("//"));
    }

    /*
    
//    public void nativeExecutable(File f) {
//        build(f);
//            ProcessBuilder pb;
//            if (OS.contains("win")) {
//                pb = getWindowsExecutableString();
//            } else {
//                pb = getMacExecutableString();
//            }
//            pb.directory(f);
//            try {
//                Process start = pb.start();
//                System.out.println("Compile Native for Project " + f.getName());
//                (new Thread(new OutputReader(start.getInputStream()))).start();
//                (new Thread(new ErrorReader(start.getErrorStream()))).start();
//                int waitFor = start.waitFor();
//            } catch (IOException | InterruptedException ex) {
//            }
//    }
//
//    private ProcessBuilder getWindowsExecutableString() {
//        String ico = getIconPath();
//        File f = new File(JPM.getJDK() + File.separator + "javapackager" + (OS.contains("win") ? ".exe" : ""));
//        String a;
//        if (f.exists()) {
//            a = "\"" + JPM.getJDK() + File.separator + "javapackager\"" + " -deploy -native exe " + (ico == null ? "" : " -Bicon=" + ico) + " -outdir " + getProject().getDist().toAbsolutePath().toString()
//                    + " -outfile " + getProject().getProjectName() + " -srcdir " + getProject().getDist().toAbsolutePath().toString() + " -srcFiles " + getProject().getProjectName()
//                    + ".jar " + " -appclass " + getProject().getMainClassName() + " -name " + getProject().getProjectName() + " -title " + getProject().getProjectName() + " -v";
//        } else {
//            a = "\"" + JPM.getJDK() + File.separator + "javafxpackager\"" + " -deploy -native exe " + (ico == null ? "" : " -Bicon=" + ico) + " -outdir " + getProject().getDist().toAbsolutePath().toString()
//                    + " -outfile " + getProject().getProjectName() + " -srcdir " + getProject().getDist().toAbsolutePath().toString() + " -srcFiles " + getProject().getProjectName()
//                    + ".jar " + " -appclass " + getProject().getMainClassName() + " -name " + getProject().getProjectName() + " -title " + getProject().getProjectName() + " -v";
//        }
//        return new ProcessBuilder(a.split(" "));
//    }
//
//    private ProcessBuilder getMacExecutableString() {
//        String ico = getIconPath();
//        File f = new File(JPM.getJDK() + File.separator + "javapackager" + (OS.contains("win") ? ".exe" : ""));
//        String a;
//        if (f.exists()) {
//            a = JPM.getJDK() + File.separator + "javapackager -deploy -native dmg" + (ico == null ? "" : " -Bicon=" + ico) + " -outdir " + getProject().getDist().toAbsolutePath().toString()
//                    + " -outfile " + getProject().getProjectName() + " -srcdir " + getProject().getDist().toAbsolutePath().toString() + " -srcFiles " + getProject().getProjectName()
//                    + ".jar " + "-appclass " + getProject().getMainClassName() + " -name " + getProject().getProjectName() + " -title " + getProject().getProjectName() + " mac.CFBundleName=" + getProject().getProjectName() + " -v";
//        } else {
//            a = JPM.getJDK() + File.separator + "javafxpackager -deploy -native dmg" + (ico == null ? "" : " -Bicon=" + ico) + " -outdir " + getProject().getDist().toAbsolutePath().toString()
//                    + " -outfile " + getProject().getProjectName() + " -srcdir " + getProject().getDist().toAbsolutePath().toString() + " -srcFiles " + getProject().getProjectName()
//                    + ".jar " + "-appclass " + getProject().getMainClassName() + " -name " + getProject().getProjectName() + " -title " + getProject().getProjectName() + " mac.CFBundleName=" + getProject().getProjectName() + " -v";
//        }
//        return new ProcessBuilder(Arrays.asList(a.split(" ")));
//    }
//
//    private String getIconPath() {
//        if (getProject().getFileIconPath().isEmpty()) {
//            return null;
//        }
//        if (OS.contains("win")) {
//            if (getProject().getFileIconPath().endsWith(".ico")) {
//                return getProject().getFileIconPath();
//            } else {
//                File f = new File(getProject().getFileIconPath());
//                if (f.exists()) {
//                    File to = new File(getProject().getDist().toAbsolutePath().toString() + File.separator + getFilename(f) + ".ico");
//                    if (!to.exists()) {
//                        try {
//                            Image im = new Image(f.toURI().toString(), 256, 256, true, true);
//                            ICOEncoder.write(SwingFXUtils.fromFXImage(im, null), to);
//                            if (to.exists()) {
//                                return to.getAbsolutePath();
//                            }
//                        } catch (IOException ex) {
//                        }
//                    } else {
//                        return to.getAbsolutePath();
//                    }
//                }
//            }
//        } else if (getProject().getFileIconPath().endsWith(".icns")) {
//            return getProject().getFileIconPath();
//        } else {
//            File f = new File(getProject().getFileIconPath());
//            if (f.exists()) {
//                File to = new File(getProject().getDist().toAbsolutePath().toString() + File.separator + getFilename(f) + ".icns");
//                if (!to.exists()) {
//                    try {
//                        Image im = new Image(f.toURI().toString());
//                        Imaging.writeImage(SwingFXUtils.fromFXImage(im, null), f, null, null);
//                        return to.getAbsolutePath();
//                    } catch (IOException | ImageWriteException ex) {
//                    }
//                } else {
//                    return to.getAbsolutePath();
//                }
//            }
//        }
//        return null;
//    }

    private String getFilename(File f) {
        String s = f.getName();
        if (s.contains(".")) {
            s = s.substring(0, s.indexOf("."));
        }
        return s;
    }

    private void buildFatJar(File f) throws IOException {

        Path fat = Paths.get(f.getAbsolutePath() + File.separator + "deploy" + File.separator + getProject().getProjectName() + ".jar");
        if (!Files.exists(fat.getParent())) {
            try {
                Files.createDirectories(fat.getParent());
            } catch (IOException ex) {
            }
        }
        almostDeepDelete(new File(fat.getParent().toAbsolutePath().toString()));
        String input = getProject().getDist().toAbsolutePath().toString() + File.separator + getProject().getProjectName() + ".jar";
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(input))) {
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                String filePath = fat.getParent().toAbsolutePath().toString() + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    extractToFile(zipIn, filePath);
                } else {
                    Path dir = Paths.get(filePath);
                    Files.createDirectory(dir);
                }
                entry = zipIn.getNextEntry();
            }
        }

        for (JavaLibrary lib : getProject().getAllLibs()) {
            ZipInputStream zipIn = lib.getBinaryZipInputStream();
            if (zipIn != null) {
                ZipEntry entry = zipIn.getNextEntry();
                while (entry != null) {
                    String filePath = fat.getParent().toAbsolutePath().toString() + File.separator + entry.getName();
                    if (!entry.isDirectory()) {
                        extractToFile(zipIn, filePath);
                    } else {
                        Path dir = Paths.get(filePath);
                        Files.createDirectory(dir);
                    }
                    entry = zipIn.getNextEntry();
                }
            }
        }
        Path mani = Paths.get(getProject().getRootDirectory().toAbsolutePath().toString() + File.separator + "bundle" + File.separator + "META-INF" + File.separator
                + "MANIFEST.MF");
        if (Files.exists(mani)) {
            Files.delete(mani);
        }
        buildFat(item);
        deepDelete(fat.getParent());
    }

    
    public void fatBuild(ProcessItem item) {
        build(item);
        try {
            buildFatJar(item);
        } catch (IOException ex) {
        }
    }

    private void extractToFile(ZipInputStream sipIn, String filePath) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] bytesIn = new byte[4096];
            int read;
            while ((read = sipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }

    private void almostDeepDelete(File p) {
        if (p.isDirectory()) {
            for (File f : p.listFiles()) {
                deepDelete(f.toPath());
            }
        }
    }

    private void deepDelete(Path fe) {
        if (!Files.exists(fe)) {
            return;
        }
        if (Files.isDirectory(fe)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(fe)) {
                for (Path run : stream) {
                    deepDelete(run);
                }
            } catch (IOException | DirectoryIteratorException x) {
            }
            try {
                Files.delete(fe);
            } catch (IOException ex) {
            }
        } else {
            try {
                Files.delete(fe);
            } catch (IOException ex) {
            }
        }
    }

    private void buildFat(ProcessItem pro) {
        if (!pro.isCancelled()) {
            ProcessBuilder pb;
            if (OS.contains("win")) {
                pb = getWindowsFatJarString();
            } else {
                pb = getMacFatJarString();
            }
            pb.directory(getProject().getRootDirectory().toFile());
            try {
                Process start = pb.start();
                pro.setName("Combining All Existing Jars for Project " + getProject().getProjectName());
                pro.setProcess(start);
                ProcessPool.getPool().addItem(pro);
                (new Thread(new OutputReader(start.getInputStream()))).start();
                (new Thread(new ErrorReader(start.getErrorStream()))).start();
                int waitFor = start.waitFor();
                System.out.println(waitFor);
            } catch (IOException | InterruptedException e) {
            }
        }
    }

    private ProcessBuilder getWindowsFatJarString() {
        File f = new File(JPM.getJDK() + File.separator + "javapackager" + (OS.contains("win") ? ".exe" : ""));
        if (f.exists()) {
            return new ProcessBuilder("\"" + JPM.getJDK() + File.separator + "javapackager\"",
                    "-createjar",
                    "-appClass", getProject().getMainClassName(),
                    "-srcdir", "bundle",
                    "-outdir", "dist",
                    "-outfile", "bundle.jar", "-v");
        } else {
            return new ProcessBuilder("\"" + JPM.getJDK() + File.separator + "javafxpackager\"",
                    "-createjar",
                    "-appClass", getProject().getMainClassName(),
                    "-srcdir", "bundle",
                    "-outdir", "dist",
                    "-outfile", "bundle.jar", "-v");
        }
    }

    private ProcessBuilder getMacFatJarString() {
        File f = new File(JPM.getJDK()+ File.separator + "javapackager" + (OS.contains("win") ? ".exe" : ""));
        if (f.exists()) {
            return new ProcessBuilder(JPM.getJDK() + File.separator + "javapackager",
                    "-createjar",
                    "-appClass", getProject().getMainClassName(),
                    "-srcdir", "bundle",
                    "-outdir", "dist",
                    "-outfile", "bundle.jar", "-v");
        } else {
            return new ProcessBuilder(JPM.getJDK() + File.separator + "javafxpackager",
                    "-createjar",
                    "-appClass", getProject().getMainClassName(),
                    "-srcdir", "bundle",
                    "-outdir", "dist",
                    "-outfile", "bundle.jar", "-v");
        }
    }
     */
    private static class OutputReader implements Runnable {

        private final Scanner in;

        public OutputReader(InputStream is) {
            in = new Scanner(is);
        }

        public void run() {
            while (in.hasNextLine()) {
                System.out.println(in.nextLine());
            }
        }
    }
}
