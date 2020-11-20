package net.peacefulcraft.secretsanta.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class IOHandler {
    public static List<File> getAllFiles(String directoryName) {
        File directory = new File(directoryName);
        List<File> resultList = new ArrayList<>();
        File[] fList = directory.listFiles();
        for(File file : fList) {
            if(file.isFile() && (file.getName().endsWith(".yml") || file.getName().endsWith(".txt"))) {
                resultList.add(file);
            } else if(file.isDirectory()) {
                resultList.addAll(getAllFiles(file.getAbsolutePath()));
            }
        }
        return resultList;
    }

    public static String getList(String s, File[] list) {
        for(File f : list) {
            s = s + f.getName() + ", ";
        }
        return s;
    }

    public static <T extends org.bukkit.plugin.java.JavaPlugin> List<IOLoader<T>> getSaveLoad(T plugin, List<File> itemFiles, String s) {
        List<IOLoader<T>> list = new ArrayList<>();
        for(File f : itemFiles) {
            list.add(new IOLoader<>(plugin, f, s));
        }
        return list;
    }
}
