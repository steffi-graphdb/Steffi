package com.imgraph.tests.titan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map.Entry;


import org.glassfish.pfl.basic.tools.file.FileWrapper;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;



/**
 * Hello world!
 *
 */
public class App 
{
	
	private static class NullRepresenter extends Representer {
        public NullRepresenter() {
            super();
            // null representer is exceptional and it is stored as an instance
            // variable.
            this.nullRepresenter = new RepresentNull();
        }
        
     

        private class RepresentNull implements Represent {
            public Node representData(Object data) {
                // possible values are here http://yaml.org/type/null.html
                return representScalar(Tag.NULL, "");
            }
        }
    }
	
	
	@SuppressWarnings("unchecked")
	public static void genCassandraYaml(String tempDirectory) throws IOException{
		InputStream input = new String().getClass().getResourceAsStream("/cassandra.yaml");
        Yaml inYaml = new Yaml();
        LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) inYaml.load(input);
        Yaml outYaml =  new Yaml(new NullRepresenter());
        Writer writer = new FileWriter(tempDirectory +  "cassandra.yaml");
        outYaml.dump(data, writer);
        writer.close();
	}
	
	
    public static void main( String[] args ) throws Exception
    {
    
    	
    	GraphTestCase graphTestCase = new GraphTestCase(args[0]);
    	
    	String [] addresses = graphTestCase.getCassandraClusterIps().split(",");
    	
    	for (int i=0; i<addresses.length; i++) {
    		TestTools.genCassandraYaml(new CassandraStartMsg(graphTestCase.getWorkDirectory(), 
        			graphTestCase.getStorageDirectory(), addresses.length, i, addresses[0].trim(), 
        			addresses[i].trim(), false));
    		
    	}
    	
    	
    }
}
