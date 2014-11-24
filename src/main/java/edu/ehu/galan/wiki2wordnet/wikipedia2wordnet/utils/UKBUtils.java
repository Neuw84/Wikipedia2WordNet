/*
 * Copyright (C) 2014 Angel Conde, neuw84 at gmail dot com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.ehu.galan.wiki2wordnet.wikipedia2wordnet.utils;

import edu.ehu.galan.wiki2wordnet.wikipedia2wordnet.utils.SystemCommandExecutor;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for processing UKB
 *
 * @author Angel Conde Manjon
 */

public class UKBUtils {

    private static final Logger logger = LoggerFactory.getLogger(UKBUtils.class);

    /**
     *
     * @param pFile
     * @return
     */
    public static HashMap<String,Integer> processUKB(String pFile) {
        List<String> command2 = new ArrayList<>();
        command2.add("/bin/sh");
        command2.add("-c");
        command2.add("/home/angel/lidom/ukb/bin/ukb_wsd --ppr -K "+System.getProperty("user.dir")+"/resources/wn3_0relBin.txt -D "+System.getProperty("user.dir")+"/resources/wn3_0.lex " + pFile);
        SystemCommandExecutor commande = new SystemCommandExecutor(command2);
        HashMap<String,Integer> synsets = new HashMap<>();
        try {
            commande.executeCommand();
            List<String> list = Arrays.asList(commande.getStandardOutputFromCommand().toString().split("\n"));
            list=list.subList(1, list.size());
            for (String line : list) {
                String[] s = line.split("\\s+");
                if (s.length > 0) {
                    int num = Integer.parseInt(s[2].trim().substring(0, s[2].trim().length() - 2));
                    synsets.put(s[4],num);
                }
            }
            if (commande.getStandardErrorFromCommand().toString().length() > 1) {
                logger.error("The Ukb command has encountered an error: " + commande.getStandardErrorFromCommand().toString());
            }
        } catch (IOException | InterruptedException ex) {
            logger.error("Error executing the Ukb command: ", ex);
        }
        return synsets;
    }

    /**
     * Given a HashMap containing the topics to disambiguate, and them a List of domain topics for
     * each topic that will be used in the disambiguation process
     *
     * For example:
     *
     * Planet (two synsets) --> Earth, Mars, Jupiter, Solar system....
     *
     * @param domainTopics
     * @param outDir 
     */
  
     public static void prepareInput(HashMap<String, List<String>> domainTopics, String outDir) {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        int j = 2;
        for (String col : domainTopics.keySet()) {
            j = 2;
            if (i < 10) {
                sb.append("ctx_0").append(i).append("\n");
            } else {
                sb.append("ctx_").append(i).append("\n");
            }
            i++;
            sb.append(col.replaceAll("\\s+", "_")).append("#n#w1#1");
            for (String s : domainTopics.get(col)) {
                sb.append(" ").append(s).append("#n#w").append(j).append("#0");
                j++;
            }
            sb.append("\n");
        }
        try {
            //TODO: not compatible with paralelization!
            Files.deleteIfExists(Paths.get(outDir));
            Files.write(Paths.get(outDir), sb.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);
        } catch (IOException ex) {
            logger.error("Error while deleting and creating the UKB context file", ex);
        }

    }
}
