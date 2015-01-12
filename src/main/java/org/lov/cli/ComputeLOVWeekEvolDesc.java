package org.lov.cli;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.lov.LovUtil;
import org.lov.SPARQLRunner;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.n3.N3Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import arq.cmdline.CmdGeneral;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.NotFoundException;

/**
 * A command line tool that output inline the number of vocabularies per week based on the archives analysis.
 * 
 * @author Pierre-Yves Vandenbussche
 */
public class ComputeLOVWeekEvolDesc extends CmdGeneral {
	private final static Logger log = LoggerFactory.getLogger(ComputeLOVWeekEvolDesc.class);
	
	public static void main(String... args) {
		new ComputeLOVWeekEvolDesc(args).mainRun();
	}
	private String archiveFolderPath;
	
	public ComputeLOVWeekEvolDesc(String[] args) {
		super(args);
		getUsage().startCategory("Arguments");
		getUsage().addUsage("archiveFolderPath", "Path to the archive folder");
	}
	
	@Override
    protected String getCommandName() {
		return "computelovevol";
	}
	
	@Override
	protected String getSummary() {
		return getCommandName() + " archiveFolderPath";
	}

	@Override
	protected void processModulesAndArgs() {
		if (getPositional().size() < 1) {
			doHelp();
		}
		archiveFolderPath = getPositionalArg(0);
	}

	@Override
	protected void exec() {
		try {
			//sendPost(namespace);
			log.info("Computing lov weekly evolution using the archive folder: " + archiveFolderPath);
			File archiveFolder= new File(archiveFolderPath);
			
			//set starting date
			Calendar dateIndex = Calendar.getInstance();
			dateIndex.set(2011, Calendar.MARCH, 4);
			SimpleDateFormat formatArchive = new SimpleDateFormat("yyyy_MM_dd");
			SimpleDateFormat formatLatex = new SimpleDateFormat("yyyy-MM-dd");
			byte[] buffer = new byte[1024];
			StringBuilder sb = new StringBuilder();
						
			
			//iterate until today
			while(dateIndex.before(Calendar.getInstance())){
				boolean isFound=false;
				Calendar dateTemp = Calendar.getInstance();
				dateTemp.setTime(dateIndex.getTime());
				for (int i = 0; i < 7; i++) {
					if(i>0)dateTemp.add(Calendar.DAY_OF_YEAR, 1);
					for (final File fileEntry : archiveFolder.listFiles()) {
				       if(fileEntry.getName().startsWith("LOV_"+formatArchive.format(dateTemp.getTime()))){
				    	   
				    	   
				    	   //unzip
					    	 //get the zip file content
					       	ZipInputStream zis = new ZipInputStream(new FileInputStream(fileEntry.getAbsoluteFile()));
					       	//get the zipped file list entry
					       	ZipEntry ze = zis.getNextEntry();
					    
					       	while(ze!=null){
					       	   String fileName = ze.getName();
					              File newFile = new File(archiveFolder + File.separator + fileName);
					               //create all non exists folders
					               //else you will hit FileNotFoundException for compressed folder
					               new File(newFile.getParent()).mkdirs();
					    
					               FileOutputStream fos = new FileOutputStream(newFile);             
					    
					               int len;
					               while ((len = zis.read(buffer)) > 0) {
					          		fos.write(buffer, 0, len);
					               }
					               fos.close();   
					               ze = zis.getNextEntry();
					       	}
					        zis.closeEntry();
					       	zis.close();
					       	
					       	File lovFile = new File(archiveFolder + File.separator + "lov.rdf");
					       	if(lovFile.exists()){
					       		
					       		Repository rep = LovUtil.LoadRepositoryFromURL(new URL(lovFile.toURI().toString()),RDFFormat.RDFXML);
								ByteArrayOutputStream out = new ByteArrayOutputStream();
								RDFWriter writer=new N3Writer(out);
								RepositoryConnection connec = rep.getConnection();
								connec.export(writer);
								connec.close();
								rep.shutDown();
								ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
								Model model = ModelFactory.createDefaultModel();//transform openrdf repository to jena model
								RDFDataMgr.read(model, in, Lang.N3);
								out.close();
								in.close();
								SPARQLRunner sparqlRunner = new SPARQLRunner(model);
								int nbVocabs = sparqlRunner.getCount("count-lov-vocabularies.sparql", null, "nbVocabs", null, null);
								sb.append(formatLatex.format(dateIndex.getTime())+"\t"+ nbVocabs);
								sb.append(System.getProperty("line.separator"));
					       		lovFile.delete();
					       	}
				    	   
				    	   isFound=true;
				    	   break;
				       }
				    }
					if(isFound)break;
				}
				dateIndex.add(Calendar.WEEK_OF_YEAR, 1);
			}
			
			log.info("####### <Summary> #######");
			log.info(sb.toString());
			log.info("####### </Summary> #######");
			
			log.info("Done!");
		} catch (NotFoundException ex) {
			cmdError("Not found: " + ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
}
