/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ehu.galan.wiki2wordnet.wikipedia2wordnet.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

/**
 *
 * @author angel
 */
public class FileUtils {
     /**
     * Given a String containing a path to a bz2 compressed file returns a bufferedReader with the
     * contents of the file. The first line will contain the file info, Be warned about this!
     *
     * @param fileIn - the file to be processed
     * @return BufferedReader with the contents of the file
     * @throws FileNotFoundException
     * @throws CompressorException
     */
    public static BufferedReader getBufferedReaderForBZ2File(String fileIn) throws FileNotFoundException, CompressorException {
        FileInputStream fin = new FileInputStream(fileIn);
        BufferedInputStream bis = new BufferedInputStream(fin);
        CompressorInputStream input = new CompressorStreamFactory().createCompressorInputStream(bis);
        BufferedReader br2 = new BufferedReader(new InputStreamReader(input));
        return br2;
    }
}
