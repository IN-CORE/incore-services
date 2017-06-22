package edu.illinois.ncsa.incore.repo;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

/**
 * Created by ywkim on 6/8/2017.
 */
public class RepoUtils {
    public static void deleteTmpDir(File metadataFile, String fileExt) {
        String fileName = metadataFile.getAbsolutePath();
        String filePath = fileName.substring(0, fileName.lastIndexOf(metadataFile.separator));
        int extLoc = metadataFile.getName().indexOf(".");
        String extName = metadataFile.getName().substring(extLoc);
        String fileNameWithNoExt = FilenameUtils.removeExtension(fileName);

        String delFileName = fileNameWithNoExt + "." + fileExt;
        File delFile = new File(delFileName);
        deleteFiles(delFile, delFileName);

        File delDir = new File(filePath);
        deleteFiles(delDir, filePath);
    }

    public static void deleteTmpDir(File shapefile, String[] fileExts) {
        String fileName = shapefile.getAbsolutePath();
        String filePath = fileName.substring(0, fileName.lastIndexOf(shapefile.separator));
        int extLoc = shapefile.getName().indexOf(".");
        String extName = shapefile.getName().substring(extLoc);
        String fileNameWithNoExt = FilenameUtils.removeExtension(fileName);

        for (String extension : fileExts) {
            String delFileName = fileNameWithNoExt + "." + extension;
            File delFile = new File(delFileName);
            deleteFiles(delFile, delFileName);
        }
        File delDir = new File(filePath);
        deleteFiles(delDir, filePath);
    }

    public static void deleteFiles(File delFile, String delFileName){
        try {
            if (delFile.delete()) {
//                System.out.println("file or directory deleted: " + delFileName);
            } else {
                System.out.println("file or directory did not deleted: " + delFileName);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
