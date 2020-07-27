package org.cwi.examine.internal.molepan.dataread;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.*;
import org.openscience.cdk.layout.*;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.exception.*;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.Atom.*;
import org.cwi.examine.internal.molepan.*;
import org.openscience.cdk.ChemObject.*;
import org.openscience.cdk.Element.*;
import org.openscience.cdk.Isotope.*;
import org.openscience.cdk.AtomType.*;
import java.awt.Color;
import java.io.*;
import java.util.*;
import java.lang.Object;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.FileSystems;

public class DataRead {


	public static double[][] coordinates;
	public static int[] PosAtom;
	public static int atomNo;
	public static AtomContainer mol = new AtomContainer();
	
	public  DataRead() {
	
		String file = "data/molecule/test.itp";
		String node_file = "data/atoms.nodes";
		String atom_header = "Identifier	Score	Symbol	URL";
		String annotations_header = "Identifier	Category	Score	Symbol	URL";
		String url = "about:blank";
		String comment = ";";
		String atom_signal = "[ atoms ]";
		String bonds = "[ bonds ]";
		String pairs = "[ pairs ]";
		String[] parts;
	
		boolean isAtom = false; 
		boolean isBond = false; 
	
    		Map<String, String> ReCon = new HashMap<>();
		List<String> type = new ArrayList<String>();
		List<String> resid = new ArrayList<String>();
		List<String> cgnr = new ArrayList<String>();
		List<String> charge = new ArrayList<String>();
		List<String> total_charge = new ArrayList<String>();
 			  
   		ConvertToAtom cta = new ConvertToAtom();
 		
 		try (Writer annotations = new BufferedWriter(new OutputStreamWriter(
           		new FileOutputStream("data/partitions.annotations"), "utf-8"))) {	  		
       		try (Writer partitions = new BufferedWriter(new OutputStreamWriter(
        		new FileOutputStream("data/partitions.links"), "utf-8"))) {  
        	try (Writer writer3 = new BufferedWriter(new OutputStreamWriter(
             		new FileOutputStream("data/modules.links"), "utf-8"))) {
        	try (Writer writer = new BufferedWriter(new OutputStreamWriter(
             		new FileOutputStream("data/atoms.nodes"), "utf-8"))) {
       		try (Writer writer2 = new BufferedWriter(new OutputStreamWriter(
             		new FileOutputStream("data/bonds.links"), "utf-8"))) {
            		annotations.write(annotations_header); 
   			writer.write(atom_header + "\n");
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
    		 String line;
    		 int i = 0;
    		 int j = 0;
    		 boolean once = true;	
   			 while ((line = br.readLine()) != null) {
   			 	if( line!=null ){
   			 		
     				parts = line.split("\\s+");
     				if( line.contains(pairs)){ 
     					isBond = false;
     				}
     				if( !parts[0].contains(comment) && isBond == true){
     					mol.addBond( new Bond( mol.getAtom(Integer.parseInt(parts[1])-1), mol.getAtom(Integer.parseInt(parts[2])-1) ));
     					writer2.write(parts[1]+ "	"+ parts[2]+ "\n");
     				}
     				if( line.contains(bonds)){ 
     					isAtom = false;
						isBond = true;
     				}
     				if( !parts[0].contains(comment) && isAtom == true){
     					mol.addAtom(new Atom(cta.convert_to_atom(parts[5])) );
     					writer.write(parts[1] +"	"+ "0" +"	"+ cta.convert_to_atom(parts[5]) + "	" +url + "\n");
     					ReCon.put(parts[1], parts[5]);
     					writer3.write("small	" + parts[1] + "\n");
     					partitions.write(parts[1] + "	" + parts[2] + "	" + parts[4] + "	" 
     					+ "#" + parts[6]+ "	"  + parts[7]);		
     				if( line.contains(comment) )partitions.write("	"  + "tc(" +parts[10] + ")");
     					partitions.write("\n"); 	
     				if( !type.contains(parts[2]) ){
     					type.add(parts[2]);
     					annotations.write("\n" + parts[2] +	"	Type	0	" + parts[2] + "	about:blank" );
     				}
     				if( !resid.contains(parts[4]) ){
     					resid.add(parts[4]);
     					annotations.write("\n" +  parts[4] +	"	resid	0	" + parts[4] + "	about:blank" );
     				}	
     				if( !cgnr.contains(parts[6]) ){
     					cgnr.add(parts[6]);
     					annotations.write("\n" +  "#" + parts[6] +	"	cgnr	0	" + "#" +  parts[6] + "	about:blank" );
     				} 			
     				if( !charge.contains(parts[7]) ){
     					charge.add(parts[7]);
     					annotations.write("\n" +   parts[7] +	"	charge	0	" + parts[7] + "	about:blank" );
     				}	
     				if( line.contains(comment) ){
     					if( !total_charge.contains(parts[10]) ){
     						total_charge.add(parts[10]);
     						annotations.write("\n" +   "tc(" +parts[10] + ")" +	"	total_charge	0	" + "tc(" +parts[10] + ")" + "	about:blank" );
     					}
     				}
     				}	
     				if(	line.contains(atom_signal)) isAtom = true;	     					
     			} 			 			
    		}
		}catch (IOException e) {
       		System.err.println("Error: " + e);
     		}
     		}
		catch (IOException e) {
       		System.err.println("Error: " + e);
		}
		}
		catch (IOException e) {
       		System.err.println("Error: " + e);
		}		
		}
		catch (IOException e) {
       		System.err.println("Error: " + e);
		}
 		}
		catch (IOException e) {
       		System.err.println("Error: " + e);
		} 
		}
		catch (IOException e) {
       		System.err.println("Error: " + e);
		} 
 			
 				
     		int i = 0;
     		PosAtom = new int[113];  // TODO! 
     		String a;
     		Set set = ReCon.entrySet();
     		Iterator iterator = set.iterator();
     		while(iterator.hasNext()) {
       			Map.Entry mentry = (Map.Entry)iterator.next();
         		a = (String)mentry.getKey();
         		PosAtom [i]=Integer.parseInt(a);
			System.out.println(PosAtom [i]);  
			i++;
     		}
    		//SDG sdg = new SDG();
    		//sdg.sdg();	
  		try{ 
			StructureDiagramGenerator SDG = new StructureDiagramGenerator();
 			SDG.setMolecule(mol);
 			SDG.generateCoordinates();
 			IAtomContainer layedOutMol = SDG.getMolecule();	
 			atomNo = layedOutMol.getAtomCount();
 			//System.out.println(SDG.getMolecule());
 			coordinates = new double[2][layedOutMol.getAtomCount()];
 			for( i = 0;i<layedOutMol.getAtomCount();i++){
 				coordinates[0][i] = layedOutMol.getAtom(i).getPoint2d().x;
 				coordinates[1][i] = layedOutMol.getAtom(i).getPoint2d().y;	
 			}	
 		} 
		catch(CDKException ex){System.err.println("Error: " + ex);
		}		
	}    	
}
