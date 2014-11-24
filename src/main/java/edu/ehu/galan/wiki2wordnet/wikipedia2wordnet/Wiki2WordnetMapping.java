package edu.ehu.galan.wiki2wordnet.wikipedia2wordnet;
/*
 * Copyright (C) 2014 Angel Conde Manjon    
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

/**
 * Helper class for mapping Wikipedia articles to Wordnet synsets usings Babelnet:
 * http://babelnet.org/ and from Fernando's work: Mapping WordNet synsets to Wikipedia articles
 * as the fernando's mappings have confidence we will use this whether the two systems disagree in the mapping
 * process. 
 * 
 * @author Angel Conde Manjon
 */

public class Wiki2WordnetMapping {

    private String wikiTitle;
    private String wordnetTitle;
    private int wordNetSynset;
    private int wikiId;
    private float confidence;
    private WordnetType wordnetType;
    private String type;

    /**
     * @return the wordnetType
     */
    public WordnetType getWordnetType() {
        return wordnetType;
    }

    /**
     * @param wordnetType the wordnetType to set
     */
    public void setWordnetType(WordnetType wordnetType) {
        this.wordnetType = wordnetType;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Holder for knowing the type of the word mapped to Wordnet (noun,verb,etc.)
     */
    public enum WordnetType {

        noun, verb;

    };

    public Wiki2WordnetMapping(String pWikiTitle, String pWordnetTitle, int pWordnetSynset, int pWikiId, float pConfidence, WordnetType pWordnetType) {
        wikiTitle = pWikiTitle;
        wordnetType = pWordnetType;
        wordnetTitle = pWordnetTitle;
        wordNetSynset = pWordnetSynset;
        wikiId = pWikiId;
        confidence = pConfidence;
        type = "";
    }

    public Wiki2WordnetMapping(String pWikiTitle, int pWordnetSynset, String pWordnetTitle, String pType, WordnetType pWordnetType) {
        wikiTitle = pWikiTitle;
        wordnetType = WordnetType.noun;
        wordnetTitle = pWordnetTitle;
        type = pType;
        wordNetSynset = pWordnetSynset;
        wikiId = -1;
        confidence = -1f;

    }

    /**
     * @return the wikiTitle
     */
    public String getWikiTitle() {
        return wikiTitle;
    }

    /**
     * @param wikiTitle the wikiTitle to set
     */
    public void setWikiTitle(String wikiTitle) {
        this.wikiTitle = wikiTitle;
    }

    /**
     * @return the wordnetTitle
     */
    public String getWordnetTitle() {
        return wordnetTitle;
    }

    /**
     * @param wordnetTitle the wordnetTitle to set
     */
    public void setWordnetTitle(String wordnetTitle) {
        this.wordnetTitle = wordnetTitle;
    }

    /**
     * @return the wordNetSynset
     */
    public int getWordNetSynset() {
        return wordNetSynset;
    }

    /**
     * @param wordNetSynset the wordNetSynset to set
     */
    public void setWordNetSynset(int wordNetSynset) {
        this.wordNetSynset = wordNetSynset;
    }

    /**
     * @return the wikiId
     */
    public int getWikiId() {
        return wikiId;
    }

    /**
     * @param wikiId the wikiId to set
     */
    public void setWikiId(int wikiId) {
        this.wikiId = wikiId;
    }

    /**
     * @return the confidence
     */
    public float getConfidence() {
        return confidence;
    }

    /**
     * @param confidence the confidence to set
     */
    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

}
