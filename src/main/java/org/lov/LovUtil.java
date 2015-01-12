package org.lov;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.helpers.BasicParserSettings;
import org.openrdf.sail.memory.MemoryStore;

import com.hp.hpl.jena.rdf.model.Resource;



public class  LovUtil {
	
 	  /**
 	   * @param s  the first String, must not be null
 	   * @param t  the second String, must not be null
 	   * @return result distance
 	   * @throws IllegalArgumentException if either String input <code>null</code>
 	   */
 	  public static int getLevenshteinDistance(String s, String t) {
 	      if (s == null || t == null) {
 	          throw new IllegalArgumentException("Strings must not be null");
 	      }

 	      /*
 	         The difference between this impl. and the previous is that, rather 
 	         than creating and retaining a matrix of size s.length()+1 by t.length()+1, 
 	         we maintain two single-dimensional arrays of length s.length()+1.  The first, d,
 	         is the 'current working' distance array that maintains the newest distance cost
 	         counts as we iterate through the characters of String s.  Each time we increment
 	         the index of String t we are comparing, d is copied to p, the second int[].  Doing so
 	         allows us to retain the previous cost counts as required by the algorithm (taking 
 	         the minimum of the cost count to the left, up one, and diagonally up and to the left
 	         of the current cost count being calculated).  (Note that the arrays aren't really 
 	         copied anymore, just switched...this is clearly much better than cloning an array 
 	         or doing a System.arraycopy() each time  through the outer loop.)

 	         Effectively, the difference between the two implementations is this one does not 
 	         cause an out of memory condition when calculating the LD over two very large strings.
 	       */

 	      int n = s.length(); // length of s
 	      int m = t.length(); // length of t

 	      if (n == 0) {
 	          return m;
 	      } else if (m == 0) {
 	          return n;
 	      }

 	      if (n > m) {
 	          // swap the input strings to consume less memory
 	          String tmp = s;
 	          s = t;
 	          t = tmp;
 	          n = m;
 	          m = t.length();
 	      }

 	      int p[] = new int[n+1]; //'previous' cost array, horizontally
 	      int d[] = new int[n+1]; // cost array, horizontally
 	      int _d[]; //placeholder to assist in swapping p and d

 	      // indexes into strings s and t
 	      int i; // iterates through s
 	      int j; // iterates through t

 	      char t_j; // jth character of t

 	      int cost; // cost

 	      for (i = 0; i<=n; i++) {
 	          p[i] = i;
 	      }

 	      for (j = 1; j<=m; j++) {
 	          t_j = t.charAt(j-1);
 	          d[0] = j;

 	          for (i=1; i<=n; i++) {
 	              cost = s.charAt(i-1)==t_j ? 0 : 1;
 	              // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
 	              d[i] = Math.min(Math.min(d[i-1]+1, p[i]+1),  p[i-1]+cost);
 	          }

 	          // copy current distance counts to 'previous row' distance counts
 	          _d = p;
 	          p = d;
 	          d = _d;
 	      }

 	      // our last action in the above loop was to switch d and p, so p now 
 	      // actually has the most recent cost counts
 	      return p[n];
 	  }
 	  
 	 public static Repository LoadRepositoryFromURL(URL url){
 		try {
 			Repository rdfRepository = new SailRepository(new MemoryStore());
 			rdfRepository.initialize();
 			RDFFormat format = parseRDF(url, null, rdfRepository, null);
 			if(format!=null && format.getName()!=null)System.out.println("Loading URL in repository: "+url+" ("+format.getName()+") .............  done ");
 			else System.out.println("Loading URL in repository: "+url+" .............  done ");
 			return rdfRepository;
 		} catch (Exception e) {
 			System.out.println("Exception: LoadRepositoryFromURL()"+url);
 			e.printStackTrace();
 			return null;
 		}
 	}
 	public static Repository LoadRepositoryFromURL(URL url,RDFFormat format){
 		try {
 			Repository rdfRepository = new SailRepository(new MemoryStore());
 			rdfRepository.initialize();
 			parseRDF(url, null, rdfRepository, format);
 			//System.out.println("Loading URL in repository: "+url+" .............  done ");
 			return rdfRepository;
 		} catch (Exception e) {
 			System.out.println("Exception: LoadRepositoryFromURL()"+url);
 			e.printStackTrace();
 			return null;
 		}
 	}
 	 
 	public static RDFFormat parseRDF(URL inputURL,String defaultNamespace,Repository rep,RDFFormat format)throws Exception {
		if(format != null) {
			RepositoryConnection connection = rep.getConnection();
			connection.getParserConfig().set(BasicParserSettings.VERIFY_DATATYPE_VALUES, Boolean.FALSE);
			connection.getParserConfig().set(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES, Boolean.FALSE);
			connection.getParserConfig().set(BasicParserSettings.FAIL_ON_UNKNOWN_LANGUAGES, Boolean.FALSE);
			try {
				connection.setAutoCommit(false);
				// a format is given, parse with it.
				connection.add(inputURL, defaultNamespace, format);
				connection.commit();
				return format;
			} catch (Exception e) {
				connection.rollback();
				throw new Exception(e);
			} finally{
				connection.close();
			}	
		} else {
			// no format, try everything we can
			List<RDFFormat> formats = Arrays.asList(new RDFFormat[] {
					RDFFormat.TURTLE,
					RDFFormat.N3,
					RDFFormat.NTRIPLES,
					RDFFormat.RDFXML,
					RDFFormat.TRIG,
					RDFFormat.TRIX
			});
			
			for (Iterator<RDFFormat> i = formats.iterator(); i.hasNext();) {
				RDFFormat f = (RDFFormat) i.next();
				try {
					return parseRDF(inputURL, defaultNamespace, rep, f);
					// as soon as one is OK, break out of the loop.
//					break;
				} catch (Exception e) {
					System.out.println("Cannot parse RDF input as '"+f.getName()+"'");
				}
			}
		}
		return format;
	}

 	public static int countVocabTerms(List<Resource> terms, String vocabNsp){
		int cpt=0;
		for (Iterator<Resource> iterator = terms.iterator(); iterator.hasNext();) {
			Resource resource = (Resource) iterator.next();
			if(resource.getNameSpace().equals(vocabNsp))cpt++;
		}
		return cpt;		
	}
}
