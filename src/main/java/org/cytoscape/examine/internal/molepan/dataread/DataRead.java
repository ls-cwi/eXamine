package org.cwi.examine.internal.molepan.dataread;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.*;
import org.openscience.cdk.layout.*;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.exception.*;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.Atom.*;


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
	
	public  DataRead() {
	
	String file = "data/molecule/1787_united_atom.itp";
	
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
	AtomContainer mol = new AtomContainer();
	
	
	
	
	
	 		/******************************************************************/
			/******************		Read Atoms and Bonds **********************/
			/******************************************************************/
			/******************************************************************/	 
			
			/*
       		Path path_adom_nodes = FileSystems.getDefault().getPath("data" , "atoms.nodes");
			try {
			Files.deleteIfExists(path_adom_nodes);
			} catch (IOException | SecurityException e) {
            System.err.println(e);
        	}*/
        	/*
        	try { //atom.nodes writer
        	PrintWriter writer_atom_nodes = new PrintWriter("data/atoms.nodes", "UTF-8");
        	writer_atom_nodes.println(atom_header);
			writer_atom_nodes.close();
			} catch (IOException | SecurityException e) {
            System.err.println(e);
        	} // end atom.nodes writer
        	*/
        	
        	Map<String, String> ReCon = new HashMap<>();
        	
        	
        	/******************		Read Categories 	 **********************/	
 		
 		/******************************************************************/	
 		/******************			 			 	 **********************/
 		/******************************************************************/
 		/******************************************************************/
 				
 			  List<String> type = new ArrayList<String>();
 			  List<String> resid = new ArrayList<String>();
 			  List<String> cgnr = new ArrayList<String>();
 			  List<String> charge = new ArrayList<String>();
 			  List<String> total_charge = new ArrayList<String>();
 			  
 		
 			try (Writer annotations = new BufferedWriter(new OutputStreamWriter(
             new FileOutputStream("data/partitions.annotations"), "utf-8"))) {	
             		annotations.write(annotations_header);
       		 try (Writer partitions = new BufferedWriter(new OutputStreamWriter(
             new FileOutputStream("data/partitions.links"), "utf-8"))) {  
        	
        	try (Writer writer3 = new BufferedWriter(new OutputStreamWriter(
             new FileOutputStream("data/modules.links"), "utf-8"))) {
        	try (Writer writer = new BufferedWriter(new OutputStreamWriter(
             new FileOutputStream("data/atoms.nodes"), "utf-8"))) {
        	try (Writer writer2 = new BufferedWriter(new OutputStreamWriter(
             new FileOutputStream("data/bonds.links"), "utf-8"))) {
   				writer.write(atom_header + "\n");
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
    		 String line;
    		 int i = 0;
    		 int j = 0;
    		 boolean once = true;	
   			 while ((line = br.readLine()) != null) {
   			 		if( line!=null ){
     					parts = line.split("\\s+");
     					if(	line.contains(pairs)){ 
     						isBond = false;
     					}
     					if(	!parts[0].contains(comment) && isBond == true){
     						//System.out.println(+ " " +parts[2]);
     						mol.addBond( new Bond( mol.getAtom(Integer.parseInt(parts[1])-1), mol.getAtom(Integer.parseInt(parts[2])-1) ));
     						writer2.write(parts[1]+ "	"+ parts[2]+ "\n");
     						}
     					if(	line.contains(bonds)){ 
     						isAtom = false;
     						isBond = true;
     					}
     					if(	!parts[0].contains(comment) && isAtom == true){
     						mol.addAtom(new Atom(convert_to_atom(parts[5])) );
     						//System.out.println(parts[0]+parts[0]+" "+parts[1] +" "
     						//+parts[5]+ "   " + convert_to_atom(parts[5]));
     						writer.write(parts[1] +"	"+ "0" +"	"+ convert_to_atom(parts[5]) + "	" +url + "\n");
     						//writer.write(parts[1] +"	"+ "0" +"	"+ parts[5] + "	" +url + "\n");
     						ReCon.put(parts[1], parts[5]);
     						writer3.write("small	" + parts[1] + "\n");
     						
     						//Partitions (Links)
     						/*
     						partitions.write(parts[1] + "	" + parts[2]+ "	" + parts[4]);
     						partitions.write("\n"); */
     						
     						partitions.write(parts[1] + "	" + parts[2] + "	" + parts[4] + "	" 
     							+ "#" + parts[6]+ "	"  + parts[7]);
     							
     						if( line.contains(comment) )partitions.write("	"  + "tc(" +parts[10] + ")");
     						partitions.write("\n"); 
     						
     						//Partitions (Annotations)
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
     						
     						
     						//PrintWriter writer_atom_nodes = new PrintWriter("data/atoms.nodes", "UTF-8");
     						//writer_atom_nodes.println(parts[5]);
							//writer_atom_nodes.close();		
     					if(	line.contains(atom_signal)) isAtom = true;	     					
     				} //END if_1  			 		
     				/*	if( once ==true ){once = false;} mol.addAtom(new Atom(convert_to_atom
     						(parts[5])) ); System.out.print(parts[1] +" ");System.out.println
     						(convert_to_atom(parts[5]) +" ");}if (line.contains(atom_signal))
     						ignore = false;*/	
    		  } //END While 
			}catch (IOException e) {
       			System.err.println("Error: " + e);
     		}
     		}//end writer
				catch (IOException e) {
       			System.err.println("Error: " + e);
			}//catch writer
			}//end writer
				catch (IOException e) {
       			System.err.println("Error: " + e);
			}//catch writer			
			}//end writer
				catch (IOException e) {
       			System.err.println("Error: " + e);
			}//catch writer 
			//Map<String, int> X = new HashMap<>();
 			//Map<String, int> Y = new HashMap<>();
 			 		}//end writer
				catch (IOException e) {
       			System.err.println("Error: " + e);
			}//catch writer 
			}//end writer
				catch (IOException e) {
       			System.err.println("Error: " + e);
			}//catch writer 
 		

 		
 		/******************************************************************/	
 		/******************		SDG 			 	 **********************/
 		/******************************************************************/
 		/******************************************************************/
 		
 		
 				
     		int i = 0;
     		PosAtom = new int[113];
     		String a;
     		/*try (Writer fwriter = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream("data/temp"), "utf-8"))) {*/
     		 Set set = ReCon.entrySet();
     		 Iterator iterator = set.iterator();
     		 while(iterator.hasNext()) {
       			Map.Entry mentry = (Map.Entry)iterator.next();/*
         		System.out.print("key is: "+ mentry.hashCode() + " & Value is: ");
         		System.out.println(mentry.getValue() ); */
         		
         		a = (String)mentry.getKey();
         		//fwriter.write((String)mentry.getKey()+ "\n");
         		
         		
         		PosAtom [i]=Integer.parseInt(a); //(Integer)a;  //System.out.println(Integer) ;
				System.out.println(PosAtom [i]);
         		i++;
     		 }
    		
     		 /* }//end writer
				catch (IOException e) {
       			System.err.println("Error: " + e);
			}//catch writer*/
			
     		/*
     		for ( String ra: ReCon.keySet() ){
     		System.out.print(ReCon.getValue() + " ");
       		 System.out.println(ReCon.get(ra));
       		 }
       		 */
     		
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
 					
 					//Map<String, int> X = new HashMap<>();
 					//Map<String, int> Y = new HashMap<>();
 					
 				}
 			
 				
 			}
 			catch(CDKException ex){System.err.println("Error: " + ex);}	
	}
	
	public  static String convert_to_atom(String readAtom){
	 String carbon = "C";
	 String oxigen = "O";
	 String hydrogen = "H";
	 String nitrogen = "N";
	 String Atom = "";
	 	if( readAtom.contains(carbon) )
			Atom = "C";
     	else if(readAtom.contains(hydrogen))
     		Atom = "H";
     	else if(readAtom.contains(oxigen))
     		Atom = "O";
     	else if(readAtom.contains(nitrogen))
     		Atom = "N";
     			 
     			 
     return Atom;
     }
     

 		
 		/******************************************************************/	
 		/******************		Testing 			 	 **********************/
 		/******************************************************************/
 		/******************************************************************/




	  /*mol.addAtom(new Atom("C"));
		mol.addAtom(new Atom("C"));
		mol.addAtom(new Atom("C"));
		mol.addAtom(new Atom("C"));
		mol.addAtom(new Atom("C"));
		mol.addAtom(new Atom("C"));
		mol.addAtom(new Atom("C"));
		mol.addAtom(new Atom("C"));
		mol.addAtom(new Atom("C"));
		mol.addAtom(new Atom("C"));
		mol.addAtom(new Atom("C"));
		mol.addAtom(new Atom("C"));
		mol.addAtom(new Atom("C"));
		mol.addAtom(new Atom("C"));
		mol.addAtom(new Atom("C"));
		mol.addAtom(new Atom("C"));
		mol.addAtom(new Atom("C"));
		mol.addAtom(new Atom("C"));
		mol.addAtom(new Atom("C"));													
		mol.addBond( new Bond( mol.getAtom(0), mol.getAtom(1) ));  //12
		mol.addBond( new Bond( mol.getAtom(1), mol.getAtom(2) ));
		mol.addBond( new Bond( mol.getAtom(2), mol.getAtom(3) ));
		mol.addBond( new Bond( mol.getAtom(3), mol.getAtom(4) ));
		mol.addBond( new Bond( mol.getAtom(4), mol.getAtom(5) ));
		mol.addBond( new Bond( mol.getAtom(0), mol.getAtom(5) ));
		mol.addBond( new Bond( mol.getAtom(5), mol.getAtom(6) ));
		mol.addBond( new Bond( mol.getAtom(6), mol.getAtom(7) ));
		mol.addBond( new Bond( mol.getAtom(7), mol.getAtom(8) ));
		mol.addBond( new Bond( mol.getAtom(8), mol.getAtom(9) ));
		mol.addBond( new Bond( mol.getAtom(4), mol.getAtom(9) ));
		mol.addBond( new Bond( mol.getAtom(8), mol.getAtom(10) ));
		mol.addBond( new Bond( mol.getAtom(9), mol.getAtom(10) ));
		mol.addBond( new Bond( mol.getAtom(10), mol.getAtom(11) )); //11_12
		mol.addBond( new Bond( mol.getAtom(11), mol.getAtom(12) ));	
		mol.addBond( new Bond( mol.getAtom(12), mol.getAtom(13) )); //13_14
		mol.addBond( new Bond( mol.getAtom(13), mol.getAtom(14) )); //14_15
		mol.addBond( new Bond( mol.getAtom(14), mol.getAtom(15) ));
		mol.addBond( new Bond( mol.getAtom(13), mol.getAtom(16) ));
		mol.addBond( new Bond( mol.getAtom(16), mol.getAtom(17) ));							
		mol.addBond( new Bond( mol.getAtom(17), mol.getAtom(18) ));
		mol.addBond( new Bond( mol.getAtom(16), mol.getAtom(18) ));				*/
	
}
