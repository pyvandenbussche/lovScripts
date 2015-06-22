package org.lov.cli;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.lov.objects.LogSearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import arq.cmdline.CmdGeneral;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hp.hpl.jena.shared.NotFoundException;

/**
 * A command line tool that output inline the number of vocabularies per week based on the archives analysis.
 * 
 * @author Pierre-Yves Vandenbussche
 */
public class ComputeLOVSearchStats extends CmdGeneral {
	private final static Logger log = LoggerFactory.getLogger(ComputeLOVSearchStats.class);
	private final static SimpleDateFormat formatLatex = new SimpleDateFormat("yyyy-MM-dd");
	
	private String logsearchesFilePath;
	
	
	public static void main(String... args) {
		new ComputeLOVSearchStats(args).mainRun();
	}	
	
	public ComputeLOVSearchStats(String[] args) {
		super(args);
		getUsage().startCategory("Arguments");
		getUsage().addUsage("logsearchesFilePath", "absolute path for the logsearches.json file  (e.g. /home/...)");
	}
	
	@Override
    protected String getCommandName() {
		return "computelovsearchstats";
	}
	
	@Override
	protected String getSummary() {
		return getCommandName() + "logsearchesFilePath";
	}

	@Override
	protected void processModulesAndArgs() {
		if (getPositional().size() < 1) {
			doHelp();
		}
		logsearchesFilePath = getPositionalArg(0);
	}

	@Override
	protected void exec() {
		Long minDate=null;
		Long maxDate=null;
		
		
		float cat_term=0;
		float cat_vocab=0;
		float cat_agent=0;
		
		Map<String, Integer> topTerms = new HashMap<String, Integer>();
		float nb_word_term=0;
		Map<String, Integer> topAgents = new HashMap<String, Integer>();
		float nb_word_agent=0;
		Map<String, Integer> topVocabs = new HashMap<String, Integer>();
		float nb_word_vocab=0;
		
		Map<String, Float[]> apiVsUIOverMonth = new LinkedHashMap<String, Float[]>();
		
		try {
			Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
            .create();
			
			LogSearch log;
			SimpleDateFormat yearMonth = new SimpleDateFormat("MMM-yyyy");
			String currentMonth=null;
			float nbUI=0;
			float nbAPI=0;
			
			BufferedReader br = Files.newBufferedReader(Paths.get(logsearchesFilePath), StandardCharsets.UTF_8);
		    for (String line = null; (line = br.readLine()) != null;) {
		    	log = gson.fromJson(line, LogSearch.class);
		    	if(minDate==null || minDate> log.getDate())minDate = log.getDate();
		    	if(maxDate==null || maxDate< log.getDate())maxDate = log.getDate();
		    	
		    	Date d = new Date(log.getDate());
		    	if(currentMonth==null) currentMonth=yearMonth.format(d);
		    	if(!currentMonth.equals(yearMonth.format(d))){
	    			apiVsUIOverMonth.put(currentMonth, new Float[]{nbAPI,nbUI});
	    			currentMonth=yearMonth.format(d);
	    			nbAPI=0;
	    			nbUI=0;
		    	}
		    	if(log.getMethod().equals(LogSearch.METH_UI))nbUI++;
		    	else if(log.getMethod().equals(LogSearch.METH_API))nbAPI++;
		    	
		    	if(log.getCategory().equals(LogSearch.CAT_AGENT)){
		    		cat_agent++;
		    		//case we have a search word
		    		if(log.getSearchWords()!=null){
		    			String word = log.getSearchWords().toLowerCase();
		    			nb_word_agent++;
		    			if(topAgents.containsKey(word)) topAgents.put(word, topAgents.get(word)+1);
		    			else topAgents.put(word, 1);
		    		}
		    	}
		    	else if(log.getCategory().equals(LogSearch.CAT_TERM)){
		    		cat_term++;
		    		//case we have a search word
		    		if(log.getSearchWords()!=null){
		    			String word = log.getSearchWords().toLowerCase();
		    			nb_word_term++;
		    			if(topTerms.containsKey(word)) topTerms.put(word, topTerms.get(word)+1);
		    			else topTerms.put(word, 1);
		    		}
		    		
		    	}
		    	else if(log.getCategory().equals(LogSearch.CAT_VOCAB)){
		    		cat_vocab++;
		    		//case we have a search word
		    		if(log.getSearchWords()!=null){
		    			String word = log.getSearchWords().toLowerCase();
		    			nb_word_vocab++;
		    			if(topVocabs.containsKey(word)) topVocabs.put(word, topVocabs.get(word)+1);
		    			else topVocabs.put(word, 1);
		    		}
		    	}
		    }
		    apiVsUIOverMonth.put(currentMonth, new Float[]{nbAPI,nbUI});
			
		    printLogDateRange(minDate, maxDate);
		    printUIVsAPI(apiVsUIOverMonth);
		    printTypeOfElementSearched(cat_term, cat_vocab, cat_agent);
		    printTypeOfElementSearchedWithWords(nb_word_term, nb_word_vocab, nb_word_agent);
		    printTopSearchedElements(topTerms, nb_word_term,topAgents,nb_word_agent,topVocabs,nb_word_vocab);
			
		} catch (NotFoundException ex) {
			cmdError("Not found: " + ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void printLogDateRange(Long dateMin, Long dateMax){
		Date dMin = new Date(dateMin);
		Date dMax = new Date(dateMax);
		
		log.info("Search logs from {} to {}",formatLatex.format(dMin), formatLatex.format(dMax));
		log.info("");
		
	}
	
	private void printUIVsAPI(Map<String, Float[]> map){
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		
        log.info("month\tnbAPI\tnbUI");
        for (Map.Entry<String,Float[]> entry:map.entrySet()) {
            log.info(entry.getKey()+"\t{}\t{}", entry.getValue()[0], entry.getValue()[1] );
         }
        log.info("");
	}
	
	private void printTypeOfElementSearched(float cat_term, float cat_vocab, float cat_agent){
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		log.info("###### type of elements searched (agentSearch, vocabularySearch, termSearch) ######");
		log.info("\\textbf{Element Type searched} & \\textbf{Nb searches} & \\textbf{\\% searches} \\\\ \\hline");
		log.info("Agent & {} & {}\\% \\\\",cat_agent, df.format(cat_agent/(cat_term+cat_vocab+cat_agent)*100) );
		log.info("Term & {} & {}\\% \\\\",cat_term, df.format(cat_term/(cat_term+cat_vocab+cat_agent)*100) );
		log.info("Vocabulary & {} & {}\\% \\\\",cat_vocab, df.format(cat_vocab/(cat_term+cat_vocab+cat_agent)*100) );
		log.info("");
	}
	
	private void printTypeOfElementSearchedWithWords(float cat_term, float cat_vocab, float cat_agent){
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		log.info("###### type of elements searched with words (agentSearch, vocabularySearch, termSearch) ######");
		log.info("\\textbf{Element Type searched with keywords} & \\textbf{Nb searches} & \\textbf{\\% searches} \\\\ \\hline");
		log.info("Agent & {} & {}\\% \\\\",cat_agent, df.format(cat_agent/(cat_term+cat_vocab+cat_agent)*100) );
		log.info("Term & {} & {}\\% \\\\",cat_term, df.format(cat_term/(cat_term+cat_vocab+cat_agent)*100) );
		log.info("Vocabulary & {} & {}\\% \\\\",cat_vocab, df.format(cat_vocab/(cat_term+cat_vocab+cat_agent)*100) );
		log.info("");
	}
	
	private void printTopSearchedElements(
			Map<String, Integer> topTerms, float nb_word_term, 
			Map<String, Integer> topAgents, float nb_word_agent,
			Map<String, Integer> topVocabs, float nb_word_vocab){
		int topk = 20;
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		
		// top Terms
		ValueComparator bvc =  new ValueComparator(topTerms);
        TreeMap<String,Integer> sorted_mapTerms = new TreeMap<String,Integer>(bvc);
        sorted_mapTerms.putAll(topTerms);
        int cpt=0;
        log.info("\\textbf{Vocabulary} & \\textbf{Nb searches} & \\textbf{\\% searches} \\\\ \\hline");
        for (Map.Entry<String,Integer> entry:sorted_mapTerms.entrySet()) {
            if (cpt >= topk) break;
            log.info(entry.getKey()+" & {} & {}\\% \\\\", entry.getValue(), df.format(((float)entry.getValue())/nb_word_term*100) );
            cpt++;
         }
        log.info("");
        
        // top Agents
		ValueComparator bvcAgent =  new ValueComparator(topAgents);
	     TreeMap<String,Integer> sorted_mapAgents = new TreeMap<String,Integer>(bvcAgent);
	     sorted_mapAgents.putAll(topAgents);
	     cpt=0;
	     log.info("\\textbf{Vocabulary} & \\textbf{Nb searches} & \\textbf{\\% searches} \\\\ \\hline");
	     for (Map.Entry<String,Integer> entry:sorted_mapAgents.entrySet()) {
	         if (cpt >= topk) break;
	         log.info(entry.getKey()+" & {} & {}\\% \\\\", entry.getValue(), df.format(((float)entry.getValue())/nb_word_agent*100) );
	         cpt++;
	      }
	     log.info("");
	     
	     // top Agents
		ValueComparator bvcVocab =  new ValueComparator(topVocabs);
	     TreeMap<String,Integer> sorted_mapVocabs = new TreeMap<String,Integer>(bvcVocab);
	     sorted_mapVocabs.putAll(topVocabs);
	     cpt=0;
	     log.info("\\textbf{Vocabulary} & \\textbf{Nb searches} & \\textbf{\\% searches} \\\\ \\hline");
	     for (Map.Entry<String,Integer> entry:sorted_mapVocabs.entrySet()) {
	         if (cpt >= topk) break;
	         log.info(entry.getKey()+" & {} & {}\\% \\\\", entry.getValue(), df.format(((float)entry.getValue())/nb_word_vocab*100) );
	         cpt++;
	      }
	     log.info("");
		
	}
	
	class ValueComparator implements Comparator<String> {
	    Map<String, Integer> base;
	    public ValueComparator(Map<String, Integer> base) {
	        this.base = base;
	    }
	    // Note: this comparator imposes orderings that are inconsistent with equals.    
	    public int compare(String a, String b) {
	        if (base.get(a) >= base.get(b)) {
	            return -1;
	        } else {
	            return 1;
	        } // returning 0 would merge keys
	    }
	}
	
}
