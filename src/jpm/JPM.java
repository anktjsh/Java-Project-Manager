/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jpm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

/**
 *
 * @author Aniket
 */
public class JPM {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("No Arguments\nExiting JPM");
            return;
        }
        in = new Scanner(System.in);
        switch (args[0]) {
            case "start":
                runStart();
                return;
            case "build":
                runBuild();
                return;
//            case "run":
//                runRun();
//                return;
            case "init":
                runInit();
                return;
            case "deploy":
                runDeploy();
                return;
            case "install":
                runInstall();
                return;
        }
    }

    public static Scanner in;

    private static String jdkPath;

    public static String getJDK() {
        if (jdkPath == null) {
            try {
                File f = new File(new File(JPM.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile(), "jdk.txt");
                Scanner in = new Scanner(f);
                if (in.hasNextLine()) {
                    jdkPath = in.nextLine();
                }
            } catch (FileNotFoundException ex) {
            } catch (URISyntaxException ex) {
                Logger.getLogger(JPM.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jdkPath;
    }

    private static TreeMap<String, String> cache;

    public static TreeMap<String, String> getCache() {
        if (cache == null) {
            cache = new TreeMap<>();

            try {
                File f = new File(new File(JPM.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile(), "cache.txt");
                Scanner in = new Scanner(f);
                while (in.hasNextLine()) {
                    String[] spl = in.nextLine().trim().split("\\\\");
                    cache.put(spl[0].trim(), spl[1].trim());
                }
            } catch (FileNotFoundException | URISyntaxException ex) {
                Logger.getLogger(JPM.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return cache;
    }

    private static TreeMap<String, String> property;

    public static TreeMap<String, String> getConfig() {
        if (property == null) {
            property = new TreeMap<>();
            try {
                File f = new File(new File("").getAbsolutePath() + File.separator + "settings.config");
                Scanner in = new Scanner(f);
                while (in.hasNextLine()) {
                    String[] spl = in.nextLine().trim().split("=");
                    property.put(spl[0].trim(), spl[1].trim());
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(JPM.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return property;
    }

    private static void runStart() {
        String jdk = getJDK();
        if (jdk == null) {
            jdk = determineJDK();
            if (jdk == null) {
                System.out.println("Invalid JDK Version\nExiting JPM");
                return;
            }
        }
        saveJDK(jdk);
        System.out.println("Selected JDK : " + jdk);
    }

    public static void saveJDK(String s) {
        try {
            File f = new File(new File(JPM.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile(), "jdk.txt");
            Files.write(f.toPath(), Arrays.asList(s));
        } catch (FileNotFoundException ex) {
        } catch (IOException | URISyntaxException ex) {
            Logger.getLogger(JPM.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String getInput() {
        return in.nextLine();
    }

    private static String determineJDK() {
        List<String> options = getAvailableOptions();
        System.out.println("Select JDK Version, please enter the number as your selection");
        for (int x = 0; x < options.size(); x++) {
            System.out.println(x + "-" + options.get(x));
        }
        String in = getInput();
        int i = getInt(in);
        if (i >= 0) {
            if (i >= options.size()) {
                return null;
            } else {
                return options.get(i);
            }
        } else {
            return null;
        }
    }

    private static int getInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
        }
        return -1;
    }

    private static List<String> getAvailableOptions() {
        String property = System.getProperty("os.name").toLowerCase();
        if (property.contains("windows")) {
            return windowList();
        } else {
            return macList();
        }
    }

    private static List<String> windowList() {
        File f = new File("C:" + File.separator + "Program Files" + File.separator + "Java" + File.separator);
        ArrayList<String> al = new ArrayList<>();
        if (f.exists()) {
            for (File fl : f.listFiles()) {
                if (fl.getName().substring(0, 3).equals("jdk")) {
                    al.add(fl.getAbsolutePath() + File.separator + "bin");
                }
            }
        }
        f = new File("C:" + File.separator + "Program Files (x86)" + File.separator + "Java" + File.separator);
        if (f.exists()) {
            for (File fl : f.listFiles()) {
                if (fl.getName().substring(0, 3).equals("jdk")) {
                    al.add(fl.getAbsolutePath() + File.separator + "bin");
                }
            }
        }
        return al;
    }

    private static List<String> macList() {
        File f = new File("/Library/Java/JavaVirtualMachines/");
        ArrayList<String> al = new ArrayList<>();
        if (f.exists()) {
            for (File fl : f.listFiles()) {
                if (fl.getName().substring(0, 3).equals("jdk")) {
                    al.add(f.getAbsolutePath() + File.separator + fl.getName() + "/Contents/Home/bin");
                }
            }
        }
        return al;
    }

    private static void runRun() {
        Manager.run(new File(new File("").getAbsolutePath()), getConfig().get("mainClass"));
    }

    private static void runInit() {
        File current = new File("");
        System.out.println("Enter project name : ");
        String s = getInput();
        File project = new File(current, s);
        project.mkdirs();
        File src = new File(project, "src");
        File build = new File(project, "build");
        File dist = new File(project, "dist");
        File libs = new File(project, "libs");
        File config = new File(project, "settings.config");
        libs.mkdirs();
        dist.mkdirs();
        build.mkdirs();
        src.mkdirs();
        try {
            Files.createFile(config.toPath());
        } catch (IOException ex) {
            Logger.getLogger(JPM.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Enter Main Class Name? Enter (Y/N)");
        String in = getInput();
        if (in.equals("Y")) {
            System.out.println("Enter Main Class Name : ");
            in = getInput();
            try {
                Files.write(config.toPath(), Arrays.asList("mainClass=" + in));
            } catch (IOException ex) {
                Logger.getLogger(JPM.class.getName()).log(Level.SEVERE, null, ex);
            }
            String filePath = getFilePath(in);
            File main = new File(src, filePath);
            String name = main.getName();
            main.getParentFile().mkdirs();
            try {
                Path p;
                Files.createFile(p = new File(main.getParentFile(), name + ".java").toPath());
                ArrayList<String> l = new ArrayList<>();
                if (in.contains(".")) {
                    l.add("package " + in.substring(0, in.lastIndexOf(".")) + ";\n\n");
                }
                l.add("public class " + name + " {\n\n");
                l.add("\tpublic static void main(String []args) {\n\n");
                l.add("\t\tSystem.out.println(\"HelloWorld!\");\n\t}\n\n}");
                Files.write(p, l);
            } catch (IOException ex) {
                Logger.getLogger(JPM.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static String getFilePath(String className) {
        while (className.contains(".")) {
            String one = className.substring(0, className.indexOf('.'));
            String two = className.substring(className.indexOf('.') + 1);
            className = one + File.separator + two;
        }
        return File.separator + className;
    }

    public static String getURLPath(String className) {
        while (className.contains(".")) {
            String one = className.substring(0, className.indexOf('.'));
            String two = className.substring(className.indexOf('.') + 1);
            className = one + "/" + two;
        }
        return className;
    }

    //delete exiting lib
    //^^^ add feature that doent require that
    //if cache contrain, check if exist, if it does, copy, if not, download, place in cache, copy to lib
    //if not, download, copy to cache, copy to lib, add to cache
    private static void runInstall() {
        TreeMap<String, String> config = getConfig();
        if (config.containsKey("dependencies")) {
            String s = config.get("dependencies");
            String[] spl = s.split(",");
            ArrayList<String> dep = new ArrayList<>();
            for (String sa : spl) {
                String[] d = sa.split("//");
                System.out.println(Arrays.asList(d));
                String total = "";
                total += getURLPath(d[0]);
                for (int x = 1; x < d.length; x++) {
                    total += "/" + d[x];
                }
                total += "/" + d[1] + "-" + d[2] + ".jar";
                dep.add(total);
            }
            TreeMap<String, String> cache = getCache();
            for (String all : dep) {
                if (cache.containsKey("http://search.maven.org/remotecontent?filepath=" + all)) {
                    //HERE
                } else {
                    Download d = new Download("http://search.maven.org/remotecontent?filepath=" + all, new File("libs" + File.separator + all.substring(all.lastIndexOf("/"))));
                    Download pom = new Download("http://search.maven.org/remotecontent?filepath=" + all.replace("jar", "pom"), new File("libs" + File.separator + all.substring(all.lastIndexOf("/")).replace(".jar", ".pom")));
                    try {
                        d.call();
                        pom.call();
                        MavenXpp3Reader m = new MavenXpp3Reader();
                        Model read = m.read(new FileInputStream(pom.getLocalFile()));
                        downloadDependencies(read.getDependencies());
                    } catch (Exception ex) {
                        Logger.getLogger(JPM.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                System.out.println(all);

            }
        } else {

        }
    }

    private static void downloadDependencies(List<Dependency> dependencies) {
        if (!dependencies.isEmpty()) {
            for (Dependency d : dependencies) {
                if (d.getScope().equals("compile")) {
                    String full = getURLPath(d.getGroupId()) + "/"
                            + d.getArtifactId() + "/" + d.getVersion() + "/"
                            + d.getArtifactId() + "-" + d.getVersion() + ".jar";
                    System.out.println(full);
                    if (getCache().containsKey(full)) {
                      //  HERE
                    } else {
                        String p = full.replace(".jar", ".pom");
                        Download dow = new Download("http://search.maven.org/remotecontent?filepath=" + full, new File("libs" + File.separator + full.substring(full.lastIndexOf("/"))));
                        Download pom = new Download("http://search.maven.org/remotecontent?filepath=" + p, new File("libs" + File.separator + p.substring(p.lastIndexOf("/"))));
                        try {
                            dow.call();
                            pom.call();
                            MavenXpp3Reader m = new MavenXpp3Reader();
                            Model read = m.read(new FileInputStream(pom.getLocalFile()));
                            downloadDependencies(read.getDependencies());
                        } catch (Exception ex) {
                            Logger.getLogger(JPM.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }
    }

    private static void runBuild() {
        Manager.build(new File(new File("").getAbsolutePath()), getConfig().get("mainClass"));
    }

    private static void runDeploy() {

    }
}
