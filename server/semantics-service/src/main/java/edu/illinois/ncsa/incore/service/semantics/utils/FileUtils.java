package edu.illinois.ncsa.incore.service.semantics.utils;

import com.opencsv.CSVWriter;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtils {

    public static final Logger logger = Logger.getLogger(FileUtils.class);

    /**
     * create a zip file from given file list
     *
     * @param fileList
     * @param tempDir
     * @param baseName
     * @return
     * @throws IOException
     */
    public static File createZipFile(List<String> fileList, String tempDir, String baseName) throws IOException {
        String zipfile = tempDir + File.separator + baseName + ".zip";

        // create zip file
        byte[] buffer = new byte[1024];
        FileOutputStream fileOS = new FileOutputStream(zipfile);
        ZipOutputStream zipOS = new ZipOutputStream(fileOS);
        for (int i = 0; i < fileList.size(); i++) {
            ZipEntry zEntry = new ZipEntry(fileList.get(i));
            zipOS.putNextEntry(zEntry);

            FileInputStream in = new FileInputStream(tempDir + File.separator + fileList.get(i));
            int index;
            while ((index = in.read(buffer)) > 0) {
                zipOS.write(buffer, 0, index);
            }
            in.close();
        }

        zipOS.closeEntry();
        zipOS.close();

        return new File(zipfile);
    }

    /**
     * write headers to a csv file
     *
     * @param outCsv
     * @return void
     * @throws IOException
     */
    public static void writeHeadersToCsvFile(File outCsv, String[] headers) throws IOException {
        try (FileWriter fileWriter = new FileWriter(outCsv);
            CSVWriter csvWriter = new CSVWriter(fileWriter)) {
            csvWriter.writeNext(headers);
        } catch (IOException e) {
            logger.error(e);
        }
    }

    /**
     * delete temporary directory
     *
     * @param inFile
     */
    public static void deleteTmpDir(File inFile) {
        //remove temp dir
        String tempDir = inFile.getParent();
        File dirFile = new File(tempDir);
        ArrayList<File> files = new ArrayList<File>(Arrays.asList(dirFile.listFiles()));
        deleteTmpDir(files);
    }

    /**
     * delete temporary directory created for temporary file processing
     *
     * @param delFiles
     */
    public static void deleteTmpDir(List<File> delFiles) {
        File delDir = null;
        String filePath = null;
        for (File delFile : delFiles) {
            deleteFiles(delFile);
            delDir = new File(delFile.getParent());
            filePath = delFile.getParent();
        }
        deleteFiles(delDir, filePath);
    }

    /**
     * delete temporary files created for temporary file processing
     *
     * @param delFile
     * @param delFileName
     */
    public static void deleteFiles(File delFile, String delFileName) {
        try {
            if (delFile.delete()) {
                logger.debug("file or directory deleted: " + delFileName);
            } else {
                logger.error("file or directory did not deleted: " + delFileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * delete temporary file created for temporary file processing
     *
     * @param delFile
     */
    public static void deleteFiles(File delFile) {
        String delFileName = delFile.getName();
        deleteFiles(delFile, delFileName);
    }

}
