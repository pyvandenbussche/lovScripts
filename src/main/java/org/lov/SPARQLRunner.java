package org.lov;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.FileUtils;

/**
 * Convenience class for running SPARQL queries stored in files against
 * a Jena {@link Model} or {@link Dataset}.
 * 
 * @author Richard Cyganiak, Pierre-Yves Vandenbussche
 */
public class SPARQLRunner {
	private final Dataset dataset;
	private final String subfolder;
	
	public SPARQLRunner(Model model) {
		this(DatasetFactory.create(model),null);
	}
	
	public SPARQLRunner(Dataset dataset) {
		this(dataset,null);
	}
	public SPARQLRunner(Model model, String subfolder) {
		this(DatasetFactory.create(model),subfolder);
	}
	
	public SPARQLRunner(Dataset dataset, String subfolder) {
		this.dataset = dataset;
		this.subfolder = subfolder;
	}
	
	public List<Resource> getURIs(String queryFile, String paramVariable, RDFNode paramValue, String resultVariable) {
		Query query = getQuery(queryFile);
		QuerySolutionMap args = new QuerySolutionMap();
		if (paramVariable != null && paramValue != null) {
			args.add(paramVariable, paramValue);
		}
		ArrayList<Resource> result = new ArrayList<Resource>();
		ResultSet rs = QueryExecutionFactory.create(query, dataset, args).execSelect();
		while (rs.hasNext()) {
			RDFNode n = rs.next().get(resultVariable);
			if (n == null || !n.isURIResource()) continue;
			result.add(n.asResource());
		}
		Collections.sort(result, new Comparator<Resource>() {
			public int compare(Resource r1, Resource r2) {
				return r1.getURI().compareTo(r2.getURI());
			}
		});
		return result;
	}
	

	public List<Literal> getLiterals(String queryFile, String resultVariable, String paramVariable, RDFNode paramValue) {
		Query query = getQuery(queryFile);
		QuerySolutionMap args = new QuerySolutionMap();
		if (paramVariable != null && paramValue != null) {
			args.add(paramVariable, paramValue);
		}
		ResultSet rs = QueryExecutionFactory.create(query, dataset, args).execSelect();
		List<Literal> result= new ArrayList<Literal>();
		while(rs.hasNext()){
			RDFNode n = rs.next().get(resultVariable);
			if (n == null || !n.isLiteral()) return null;
			result.add(n.asLiteral());
		}
		Collections.sort(result, new Comparator<Literal>() {
			public int compare(Literal l1, Literal l2) {
				return l1.getLexicalForm().compareTo(l2.getLexicalForm());
			}
		});
		return result;
	}
	
	public String getLangString(String queryFile, Resource term, String resultVariable) {
		Query query = getQuery(queryFile);
		QuerySolutionMap args = new QuerySolutionMap();
		args.add("term", term);
		args.add("prefLang", ResourceFactory.createPlainLiteral("en"));
		ResultSet rs = QueryExecutionFactory.create(query, dataset, args).execSelect();
		if (!rs.hasNext()) return null;
		RDFNode n = rs.next().get(resultVariable);
		if (n == null || !n.isLiteral()) return null;
		return n.asLiteral().getLexicalForm();
	}
	
	public String getString(String queryFile, String resultVariable, String paramVariable, RDFNode paramValue) {
		Query query = getQuery(queryFile);
		QuerySolutionMap args = new QuerySolutionMap();
		if (paramVariable != null && paramValue != null) {
			args.add(paramVariable, paramValue);
		}
		ResultSet rs = QueryExecutionFactory.create(query, dataset, args).execSelect();
		if (!rs.hasNext()) return null;
		RDFNode n = rs.next().get(resultVariable);
		if (n == null || !n.isLiteral()) return null;
		return n.asLiteral().getLexicalForm();
	}
	
	public String getString(String queryFile, String paramVariable, Resource paramValue, String resultVariable) {
		Query query = getQuery(queryFile);
		QuerySolutionMap args = new QuerySolutionMap();
		if (paramVariable != null && paramValue != null) {
			args.add(paramVariable, paramValue);
		}
		ResultSet rs = QueryExecutionFactory.create(query, dataset, args).execSelect();
		if (!rs.hasNext()) return null;
		RDFNode n = rs.next().get(resultVariable);
		if (n == null || !n.isLiteral()) return null;
		return n.asLiteral().getLexicalForm();
	}
	
	public String getURI(String queryFile, String resultVariable, String paramVariable, RDFNode paramValue) {
		Query query = getQuery(queryFile);
		QuerySolutionMap args = new QuerySolutionMap();
		if (paramVariable != null && paramValue != null) {
			args.add(paramVariable, paramValue);
		}
		ResultSet rs = QueryExecutionFactory.create(query, dataset, args).execSelect();
		if (!rs.hasNext()) return null;
		RDFNode n = rs.next().get(resultVariable);
		if (n == null || !n.isResource()) return null;
		return n.asResource().toString();
	}
	
	public ResultSet getResultSet(String queryFile, String paramVariable, Resource paramValue) {
		Query query = getQuery(queryFile);
		QuerySolutionMap args = new QuerySolutionMap();
		if (paramVariable != null && paramValue != null) {
			args.add(paramVariable, paramValue);
		}
		args.add("prefLang", ResourceFactory.createPlainLiteral("en"));
		QueryExecution qe = QueryExecutionFactory.create(query, dataset, args);
		ResultSet rs = qe.execSelect();
		//qe.close();
		return rs;
	}

	public QuerySolution getOneSolution(String queryFile, String paramVariable, Resource paramValue) {
		Query query = getQuery(queryFile);
		QuerySolutionMap args = new QuerySolutionMap();
		if (paramVariable != null && paramValue != null) {
			args.add(paramVariable, paramValue);
		}
		args.add("prefLang", ResourceFactory.createPlainLiteral("en"));
		QueryExecution qe = QueryExecutionFactory.create(query, dataset, args);
		ResultSet rs = qe.execSelect();
		if (!rs.hasNext()) return null;
		QuerySolution result = rs.next();
		qe.close();
		return result;
	}
	
	public boolean askUri(String queryFile, String paramVariable, RDFNode paramValue) {
		Query query = getQuery(queryFile);
		QuerySolutionMap args = new QuerySolutionMap();
		if (paramVariable != null && paramValue != null) {
			args.add(paramVariable, paramValue);
		}
		args.add("prefLang", ResourceFactory.createPlainLiteral("en"));
		QueryExecution qe = QueryExecutionFactory.create(query, dataset, args);
		return qe.execAsk();
	}
	
	private Query getQuery(String filename) {
		if (!queryCache.containsKey(filename)) {
			try {
				return QueryFactory.create(FileUtils.readWholeFileAsUTF8(
						SPARQLRunner.class.getResourceAsStream("/queries/"+ (subfolder==null? "":subfolder+"/") + filename)));
			} catch (IOException ex) {
				System.out.println(filename);
				throw new RuntimeException(ex);
			}
		}
		return queryCache.get(filename);
	}
	private static final Map<String,Query> queryCache = new HashMap<String,Query>();
	
	public int getCount(String queryFile, Resource uri, String resultVariable, String paramVariable, RDFNode paramValue) {
		Query query = getQuery(queryFile);
		QuerySolutionMap args = new QuerySolutionMap();
		if(uri!=null)args.add("uri", uri);
		if (paramVariable != null && paramValue != null) {
			args.add(paramVariable, paramValue);
		}
		ResultSet rs = QueryExecutionFactory.create(query, dataset, args).execSelect();
		if (!rs.hasNext()) return 0;
		RDFNode n = rs.next().get(resultVariable);
		if (n == null || !n.isLiteral()) return -1;
		return Integer.parseInt(n.asLiteral().getLexicalForm());
	}
}
