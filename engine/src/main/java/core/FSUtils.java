package core;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FSUtils {

    static void zip(String fullZipPath, String fullFilePath){
        try {
            byte[] buffer = new byte[1024];
            FileOutputStream fos = new FileOutputStream(fullZipPath);
            ZipOutputStream zos = new ZipOutputStream(fos);
            File srcFile = new File(fullFilePath);
            FileInputStream fis = new FileInputStream(srcFile);
            zos.putNextEntry(new ZipEntry(srcFile.getName()));
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();
            fis.close();
            zos.close();
        }
        catch (IOException ioe) {
            System.out.println("Error creating zip file" + ioe);
        }

    }

    static void unzip(String zipFilePath, String destDir, String newFileName) {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if(!dir.exists()) dir.mkdirs();
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();

            // LETS TRY WITH ONLY ONE ENTRY FOR NOW

            File newFile = new File(destDir + File.separator + newFileName);
            //create directories for sub directories in zip
            new File(newFile.getParent()).mkdirs();
            FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            //close this ZipEntry
            zis.closeEntry();

            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getZippedPath(String directory, String fileName){
        return new File(directory , fileName+".zip").getAbsolutePath();
    }

    public static List<String> getZippedContent(String directory, String sha1){
        FSUtils.unzip(getZippedPath(directory, sha1), directory , sha1 + ".txt");
        List<String > content = FSUtils.getFileLines(new File(directory, sha1+ ".txt").getAbsolutePath());
        FSUtils.deleteFile(directory + sha1 + ".txt");
        return content;
    }

    static String getTxtPath(String directory, String fileName){
        return new File(directory, fileName+".txt").getAbsolutePath();
    }

    static void createNewFile(String fileName, String content){
        File file = new File(fileName);
        try {

            if (!file.getParentFile().exists())
                file.getParentFile().mkdirs();

            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static boolean deleteFile(String filePath){
        try{
            return Files.deleteIfExists(Paths.get(filePath));
        }
        catch (IOException e){
            System.out.println("Could not delete file " + filePath);
            e.printStackTrace();
        }
        return false;
    }

    static void writeFile(String fullPath, String fileContent, boolean append){
        try{
            FileWriter fileWriter = new FileWriter(fullPath, append);
            fileWriter.write(fileContent);
            fileWriter.close();
        }
        catch (IOException e){e.printStackTrace(); }
    }

    public static List<String> getFileLines(String filePath){
        try{
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            List<String> lines = br.lines().collect(Collectors.toList());
            br.close();
            return lines;
        }
        catch (IOException e){e.printStackTrace();}
        return null;
    }

    static void clearWC(String path){
        File directory = new File(path);
        File[] listOfItems = directory.listFiles();
        for(File item: listOfItems){
            if(item.isDirectory()){
                if(!item.getName().equals(Settings.gitFolder)){
                    deleteSubFilesRec(item);
                }
            }
            else{
                FSUtils.deleteFile(item.getPath());
            }
        }
    }

    private static void deleteSubFilesRec(File folder) {
        File directory = new File(folder.getPath());
        File[] listOfItems = directory.listFiles();
        for (File item : listOfItems) {
            if (item.isDirectory()) {
                deleteSubFilesRec(item);
            }
            else{
                FSUtils.deleteFile(item.getPath());
            }
        }
        FSUtils.deleteFile(folder.getPath());
    }

    static void copyWC(String srcPath, String destPath){
        File destDir = new File(destPath);

        File srcDir = new File(srcPath);
        File[] listOfItems = srcDir.listFiles();
        for(File item: listOfItems){
            try{
                if(item.isDirectory()){
                    if(item.getName().equals(Settings.gitFolder))
                        continue;

                    FileUtils.copyDirectory(item, new File(destDir.getAbsolutePath(), item.getName()));
                }
                else{
                    FileUtils.copyFile(item, new File(destDir.getAbsolutePath(), item.getName()));
                }
            }
            catch (IOException e){
                System.out.println(String.format("Cannot copy %s to %s", item.getAbsolutePath(), destDir.getAbsolutePath()));
            }
        }
    }

}
