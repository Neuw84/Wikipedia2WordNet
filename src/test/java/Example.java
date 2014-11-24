/*
 * Copyright (C) 2014 Angel Conde Manjon neuw84 at gmail.com
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

import edu.ehu.galan.wiki2wordnet.wikipedia2wordnet.Mapper;
import edu.ehu.galan.wiki2wordnet.wikipedia2wordnet.Wiki2WordnetMapping;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Example  {
    public static void main(String[] args) {
        IDictionary wordnet= new Dictionary(new File("resources/dict/"));
        try {
            wordnet.open();
        } catch (IOException ex) {
            Logger.getLogger(Example.class.getName()).log(Level.SEVERE, null, ex);
        }
        HashMap<String,List<Wiki2WordnetMapping>> babel=Mapper.loadBabelnetMappings("resources/babelnetMappings.txt.bz2");
        HashMap<String,Wiki2WordnetMapping> fernan=Mapper.loadFernandoMappings("resources/fernandoMappings.txt.bz2");        
        List<String> wikiTitles = new ArrayList<>(Arrays.asList("Saturn","Moon","Cosmologist"));
        List<String> desambiguationContext= Arrays.asList("Mars", "Solar System");
        HashMap<String,Integer> maps=Mapper.babelnetFernandoToWordnet(babel,fernan , wikiTitles, "/tmp/ukb", wordnet,desambiguationContext,"/home/ukb/ukbdir/bin");
        System.out.println(maps);
    }
}
