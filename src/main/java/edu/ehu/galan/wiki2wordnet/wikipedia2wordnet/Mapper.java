/*
 * Copyright (C) 2014 angel
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package edu.ehu.galan.wiki2wordnet.wikipedia2wordnet;

import edu.ehu.galan.wiki2wordnet.wikipedia2wordnet.utils.UKBUtils;
import edu.ehu.galan.wiki2wordnet.wikipedia2wordnet.utils.FileUtils;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.SynsetID;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.compress.compressors.CompressorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.util.stream.Collectors.toList;

/**
 * Class that groups the Wikipedia to Wordnet utils
 *
 * @author Angel Conde Manjon
 */
public class Mapper {

    private static final Logger logger = LoggerFactory.getLogger(Mapper.class);

    /**
     * Loads the mappings file from Wordnet 3.0 Fernando P.h.D thessis BZ2 format
     * http://staffwww.dcs.shef.ac.uk/people/S.Fernando/mappings.shtml
     *
     * @param pFileMappings - The bz2 file containing the mappings
     * @return HashMap<String,Wiki2WordnetMapping> - containing the mappings
     * (wikiTitle,Wiki2WordnetMapping)
     *
     */
    public static HashMap<String, Wiki2WordnetMapping> loadFernandoMappings(String pFileMappings) {
        try {
            BufferedReader reader = null;
            String inputLine;
            reader = FileUtils.getBufferedReaderForBZ2File(pFileMappings);
            HashMap<String, Wiki2WordnetMapping> mappings = new HashMap<>();
            String wikiTitle;
            String wordnetSynset;
            while ((inputLine = reader.readLine()) != null) {
                if (!inputLine.isEmpty()) {
                    String[] line = inputLine.split("\t");
                    wordnetSynset = line[0];
                    String[] data = line[2].split("#");
                    String wikiId = data[0];
                    wikiTitle = data[1].replaceAll("_", " ");
                    String wordnetName = line[1].replaceAll("_", " ");
                    float confidence = Float.parseFloat(data[2]);
                    mappings.put(wikiTitle, new Wiki2WordnetMapping(wikiTitle, wordnetName, Integer.parseInt(wordnetSynset), Integer.parseInt(wikiId), confidence, Wiki2WordnetMapping.WordnetType.noun));
                }
            }
            return mappings;
        } catch (FileNotFoundException ex) {
            logger.error("File with babelnet mappings not found ", ex);
        } catch (CompressorException | IOException ex) {
            logger.error("Error with the bz2 format of IO exception: ", ex);
        }
        return null;
    }

    /**
     * Loads the mappings file extracted from Babelnet 2.0.1 to Wordnet 3.0 in BZ2 format
     *
     * @param pFileMappings - The bz2 file containing the mappings
     * @return HashMap<String,Wiki2WordnetMapping> - containing the mappings
     * (wikiTitle,Wiki2WordnetMapping)
     *
     */
    public static HashMap<String, List<Wiki2WordnetMapping>> loadBabelnetMappings(String pFileMappings) {
        try {
            BufferedReader reader = null;
            String inputLine;
            reader = FileUtils.getBufferedReaderForBZ2File(pFileMappings);
            HashMap<String, List<Wiki2WordnetMapping>> mappings = new HashMap<>();
            String wikiTitle;
            String wordnetSynset;
            String wordnetType;
            Wiki2WordnetMapping.WordnetType type;
            while ((inputLine = reader.readLine()) != null) {
                if (!inputLine.isEmpty()) {
                    if (inputLine.contains("WIKI:EN")) {
                        String[] line = inputLine.split("WIKI:EN")[1].split("\t");
                        String linsp[] = inputLine.split("\t");
                        String typ = linsp[2];
                        wordnetSynset = inputLine.split("\t")[1];
                        String wordnetTittle = linsp[3].split("#")[0].replaceAll("_", " ");
                        wordnetType = wordnetSynset.substring(wordnetSynset.length() - 2, wordnetSynset.length() - 1);
                        wordnetSynset = wordnetSynset.substring(1, wordnetSynset.length() - 2);
                        if (wordnetSynset.contains(",")) {
                            wordnetSynset = wordnetSynset.split(",")[0];
                            wordnetType = wordnetSynset.substring(wordnetSynset.length() - 1, wordnetSynset.length());
                            wordnetSynset = wordnetSynset.substring(0, wordnetSynset.length() - 1);
                        } else {
                            String wordnetS = wordnetSynset;
                            wordnetType = wordnetSynset.substring(wordnetSynset.length() - 1, wordnetSynset.length());
                            wordnetSynset = wordnetS;
                        }
                        switch (wordnetType) {
                            case "n":
                                type = Wiki2WordnetMapping.WordnetType.noun;
                                break;
                            case "v":
                                type = Wiki2WordnetMapping.WordnetType.verb;
                                break;
                            default:
                                type = Wiki2WordnetMapping.WordnetType.noun;
                        }
                        wikiTitle = line[0].substring(1);
                        if (mappings.containsKey(wikiTitle)) {
                            mappings.get(wikiTitle).add(new Wiki2WordnetMapping(wikiTitle, Integer.parseInt(wordnetSynset), wordnetTittle, typ, type));
                        } else {
                            List<Wiki2WordnetMapping> list = new ArrayList<>();
                            list.add(new Wiki2WordnetMapping(wikiTitle, Integer.parseInt(wordnetSynset), wordnetTittle, typ, type));
                            mappings.put(wikiTitle, list);
                        }
                    }
                }
            }
            return mappings;
        } catch (FileNotFoundException ex) {
            logger.error("File with babelnet mappings not found ", ex);
        } catch (CompressorException | IOException ex) {
            logger.error("Error with the bz2 format of IO exception: ", ex);
        }
        return null;
    }

    /**
     * Maps LiteWi loaded topics (LionTopics) that have a Wikipedia Mapping to WordNet using
     * Babelnet and Fernando's work.
     *
     * @param pBabelnetMappings - Babelnet Mappings
     * @param pFernandoMappings - Samuel Fernando Mappings
     * @param pWikiTitles - The List of Wipedia articles that will be mapped to WordNet
     * @param pDesambiguationContext - The reference topics that will be used as context, MUST HAVE
     * an unique wordnet mapping (synset) in babelnet/fernando mappings
     * @param pFile - The file where the ubk will be processed in case its needed
     * @param pWordnet - A Wordnet dict using edu.mit.jwi library with WordNet 3.0
     * @param pUkbBinDir - The dir where the ukb binaries are
     * @return - a hashmap with the wikititles / synsets pairs
     */
    public static HashMap<String, Integer> babelnetFernandoToWordnet(HashMap<String, List<Wiki2WordnetMapping>> pBabelnetMappings, HashMap<String, Wiki2WordnetMapping> pFernandoMappings, List<String> pWikiTitles, String pFile, IDictionary pWordnet, List<String> pDesambiguationContext,String pUkbBinDir) {
        HashMap<String, Integer> mappings = new HashMap<>(pWikiTitles.size());
        HashMap<String, List<Wiki2WordnetMapping>> ukbList = new HashMap<>();
        if (pBabelnetMappings != null && pFernandoMappings != null) {
            pWikiTitles.addAll(pDesambiguationContext);
            for (String title : pWikiTitles) {
                List<Wiki2WordnetMapping> babelMapping = null;
                Wiki2WordnetMapping fernandoMapping = null;
                if (pBabelnetMappings.containsKey(title)) {
                    babelMapping = pBabelnetMappings.get(title);
                }
                if (pFernandoMappings.containsKey(title)) {
                    fernandoMapping = pFernandoMappings.get(title);
                }
                if (babelMapping != null && fernandoMapping == null) {
                    if (babelMapping.size() == 1) {
                        mappings.put(title, babelMapping.get(0).getWordNetSynset());
                    } else if (babelMapping.size() > 1) {
                        ukbList.put(title ,babelMapping);
                        int l = title.split("\\s").length;
                        for (Wiki2WordnetMapping babelMapping1 : babelMapping) {
                            int l1 = babelMapping1.getWordnetTitle().split("\\s+").length;
                            if (l == l1) {
                                mappings.put(title,babelMapping1.getWordNetSynset());
                                break;
                            }
                        }
                        if (!mappings.containsKey(title)) {
                            mappings.put(title,babelMapping.get(0).getWordNetSynset());
                        }
                    }
                } else if (babelMapping == null && fernandoMapping != null) {
                    mappings.put(title,fernandoMapping.getWordNetSynset());
                } else if (babelMapping != null && fernandoMapping != null) {
                    if (babelMapping.size() == 1) {
                        if (babelMapping.get(0).getWordNetSynset() == fernandoMapping.getWordNetSynset()) {
                            mappings.put(title, +fernandoMapping.getWordNetSynset());
                        } else {
                            List<Wiki2WordnetMapping> maps = new ArrayList<>();
                            maps.add(fernandoMapping);
                            maps.add(babelMapping.get(0));
                            ukbList.put(title, maps);
                            mappings.put(title, fernandoMapping.getWordNetSynset());
                        }
                    } else {
                        List<Wiki2WordnetMapping> maps = new ArrayList<>();
                        maps.add(fernandoMapping);
                        maps.addAll(babelMapping);
                        ukbList.put(title, maps);
                        int l = title.split("\\s+").length;
                        for (Wiki2WordnetMapping babelMapping1 : babelMapping) {
                            int l1 = babelMapping1.getWordnetTitle().split("\\s+").length;
                            if (l == l1) {
                                mappings.put(title, babelMapping1.getWordNetSynset());
                                break;
                            }
                        }
                        if (mappings.get(title) == -1) {
                            mappings.put(title, fernandoMapping.getWordNetSynset());
                        }
                    }
                }
            }
        } else {
            logger.error("No mappings provided");
        }
        if (!ukbList.isEmpty()) {
            disambiguateUKB(ukbList, pDesambiguationContext, pFile, pWordnet, mappings,pUkbBinDir);
        }
        for (String context : pDesambiguationContext) {
            mappings.remove(context);
        }
        return mappings;
    }

    private static void disambiguateUKB(HashMap<String, List<Wiki2WordnetMapping>> pUkbList, List<String> pDesambContext, String pFile, IDictionary pWordnet, HashMap<String, Integer> pMappings,String pUkbBinDir) {
        logger.debug("Desambiguating Wornet synsets via UKB method");
        HashMap<String, List<String>> toDisam = new HashMap<>();
        logger.debug("Finding gold topics for disambiguation...");
        Function<String,String> contextSynsets = (String t) -> pWordnet.getSynset(new SynsetID(pMappings.get(t), POS.NOUN)).getWords().get(0).getLemma();
        List<String> context = pDesambContext.stream().map(contextSynsets).collect(toList());
        for (String topics21 : pUkbList.keySet()) {
            toDisam.put(pWordnet.getSynset(new SynsetID(pMappings.get(topics21), POS.NOUN)).getWords().get(0).getLemma().toLowerCase(), context);
        }
        UKBUtils.prepareInput(toDisam, pFile);
        HashMap<String, Integer> synsets = UKBUtils.processUKB(pUkbBinDir,pFile);
        if (toDisam.size() == synsets.size()) {
            for (String topics21 : pUkbList.keySet()) {
                Integer inte = synsets.get(pWordnet.getSynset(new SynsetID(pMappings.get(topics21), POS.NOUN)).getWords().get(0).getLemma().toLowerCase());
                pMappings.put(topics21, inte);
            }
            logger.debug("UKB disambiguation using WordNet finished");
        } else {
            logger.error("The number of topics to disambiguate via ukb must be the same than the ouput size");
        }

    }
}
