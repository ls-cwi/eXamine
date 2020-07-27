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
import org.cwi.examine.internal.Option;

public class DataRead {


	public static double[][] coordinates;
	public static int[] PosAtom;
	public static int atomNo;
	public static int atomPl = 1;
	public static AtomContainer mol = new AtomContainer();
	public static String col = ""; 
	
	public  DataRead() {
	
		String file = Option.getPath();
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
		List<String> categories = new ArrayList<String>();
		List<String> collision = new ArrayList<String>();
		
		/*
		List<String> resid = new ArrayList<String>();
		List<String> cgnr = new ArrayList<String>();
		List<String> charge = new ArrayList<String>();
		List<String> total_charge = new ArrayList<String>();*/
 			  
   		ConvertToAtom cta = new ConvertToAtom();
 		
 		try (Writer annotations_w = new BufferedWriter(new OutputStreamWriter(
           		new FileOutputStream("data/partitions.annotations"), "utf-8"))) {
           			  		
       		try (Writer partitions_w = new BufferedWriter(new OutputStreamWriter(
        		new FileOutputStream("data/partitions.links"), "utf-8"))) {  
        		
        	try (Writer modules_w = new BufferedWriter(new OutputStreamWriter(
             		new FileOutputStream("data/modules.links"), "utf-8"))) {
             		
        	try (Writer atoms_w = new BufferedWriter(new OutputStreamWriter(
             		new FileOutputStream("data/atoms.nodes"), "utf-8"))) {
             		
       		try (Writer bonds_w = new BufferedWriter(new OutputStreamWriter(
             		new FileOutputStream("data/bonds.links"), "utf-8"))) {
             		
            		annotations_w.write(annotations_header); 
            		
   					atoms_w.write(atom_header);
   					
   					
   			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
		
    		 String line;
    		 int i = 0;
    		 int j = 0;
    		 int cat = 0;
    		 boolean once = true;	
    		 boolean isRead = false;
    		 boolean isReadBond = false;
    		 boolean isAtomRead = false;
    		 
   			 while ((line = br.readLine()) != null) {
   			 
   			 	if( line!=null ){
   			 		
   			 		
   			 			
   			 			
   			 		parts = line.split("\\s+");	
   			 		
   			 		
   			 		
   			 		if( line.contains(pairs)){ 
     					isBond = false;
     				}
   			 	
   			 		
   			 		
   			 		// collect categorie items
   			 		if(	parts[0].contains(comment)&&isAtom==true && isRead ==false){
   			 			cat = parts.length-1;
   			 			for(int a = 2;a<cat+1;a++){	
   			 				categories.add(parts[a]);
   						
   			 			}
   			 			
   			 		isRead = true;
   			 		System.out.println(categories);	
   			 		}
   			 		
   			 		if( !line.contains(pairs) && isBond == true && !parts[0].contains(comment) && !line.contains(bonds) && isReadBond == true){
   			 		    bonds_w.write("\n");}
   			 		
   			 		
   			 		
   			 		if( !parts[0].contains(comment) && isBond == true){
     					mol.addBond( new Bond( mol.getAtom(Integer.parseInt(parts[1])-1), mol.getAtom(Integer.parseInt(parts[2])-1) ));
     					bonds_w.write(parts[1]+ "	"+ parts[2] );
     					isReadBond = true;
     					}
     					
   			 
   
   			 		if( line.contains(bonds)){ 
     					isAtom = false;
						isBond = true;
     				}
     				
     				if( !line.contains(bonds) && isAtom == true && isAtomRead == true && !parts[0].contains(comment)){
   			 		    modules_w.write("\n");}
     				
     				// Read Atom and Atom_C
   			 		if(!parts[0].contains(comment) && isAtom == true){
   			 		
   			 			mol.addAtom(new Atom(cta.convert_to_atom(parts[5])) ); 
   			 			
   			 			atoms_w.write("\n" +parts[1] +"	"+ "0" +"	"+ parts[5] + "	" +url ); //cta.convert_to_atom(parts[5])
   			 			
   			 			isAtomRead = true;
   			 			
   			 			int k = 0;
   			 			partitions_w.write("\n");	
   			 			for( int a = 2;a<parts.length;a++){	
   			 				//if( collision.contains(categories[a] + "-" + parts[a]) )
   			 				if(!parts[a].contains(comment) && (!Option.getScel() || !parts[5].contains("H")) ){ //CHANGE MONDAY
   			 					if(!collision.contains( categories.get(k) + "_" + "(" + parts[a] + ")" ) ){
   			 					
   			 					    collision.add(categories.get(k) + "_" + "(" + parts[a] + ")" );
     								annotations_w.write("\n" + categories.get(k) + "_" + "(" + parts[a] + ")"  +	"	" + categories.get(k) 
     												+ "	0	"  + parts[a]  + "	about:blank" );	
   			 					}
   			 					if(a == 2)
   			 					partitions_w.write(parts[a-1] );
   			 					
   			 					if(a!=2 && !parts[a].contains(comment)){
   			 					partitions_w.write("	"  + categories.get(k) + "_(" + parts[a] + ")");	}	
   			 						// HERE	
   			 					k++;
   			 					//categories.add(parts[a]);
   			 					}
   			 					
   			 			}
   			 			ReCon.put(parts[1], parts[5]);
   			 			modules_w.write("small	" + parts[1]);
   			 			
   			 			 
   			 			 /*
   			 			 for(Object element : categories)
								{
							System.out.println(element);
							} */
							
							
   			 			/*
     					ReCon.put(parts[1], parts[5]);
     					modules_w.write("small	" + parts[1] + "\n"); */
     					
     
   			 		} //end first 'if'
   			 		
   			 		//switch atom_read:
   			 		if(	line.contains(atom_signal)) isAtom = true;
   			 		
   			 		
   			 		
   			 		
   			 		
   			 		/*
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
     				if(	line.contains(atom_signal)) isAtom = true;  */	     					
     						 			
   			 	}
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
       		
		}
		catch (IOException e) {
       		System.err.println("Error: " + e);
		} 
 			
 				
     		int i = 0;
     		PosAtom = new int[900];  // TODO! 
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
 			
 			System.out.println("ano: " + atomNo);
 			
 			
 			coordinates = new double[2][atomNo];
 			for( i = 0;i<layedOutMol.getAtomCount();i++){
 				coordinates[0][i] = layedOutMol.getAtom(i).getPoint2d().x;
 				coordinates[1][i] = layedOutMol.getAtom(i).getPoint2d().y;	
 			}	
 		} 
		catch(CDKException ex){
			System.err.println("Error: " + ex);
		}		
	}    	
}



/*
public  class test{
	public static void main (String[] args){
	int n = Integer.parseInt(args[0]);
	double y = 0.5;  //Node_0  x
	double x = 0.5;	 //Node_0  y
	double r = 2;
	double g = Math.PI - (2*Math.PI/n);
	double a = 2*Math.PI-g;
	double[][] P = new double[2][n];
	double b = 0;	

	2D:[(-1.0199999999999996, -0.17817352384164786)]

	2D:[(0.47999999999999954, -0.17817352384165264)]

	2D:[(1.2300000000000026, 1.1208645818350038)],

	2D:[(1.4904722665004, 2.598076211353315)]

	2D:[(-0.17953893117885888, 1.6338947968235102)]

	2D:[(1.229999999999996, -1.4772116295183122)],

	2D:[(2.730000000000003, 1.1208645818350027)]

	2D:[(4.030000000000004, 1.8708645818350016)],

	2D:[(4.030000000000003, 0.3708645818350018)],

	StdDraw.setPenRadius(0.01);
	StdDraw.setPenColor(StdDraw.BLACK);
			
	double y = 0.2;
	double x = 0; 
	
	
	StdDraw.text(2.05/20+x, 5.798917918952455/20+y, "1");
	StdDraw.text(0.75/20+x, 5.038917918952455/20+y, "2");
	StdDraw.text(0.75/20+x, 3.538917918952455/20+y, "3");
	StdDraw.text(2.05/20+x, 2.798917918952455/20+y, "4");
	StdDraw.text(3.34/20+x, 3.538917918952455/20+y, "5");
	StdDraw.text(3.34/20+x, 5.038917918952455/20+y, "6");
	StdDraw.text(4.640000000000001/20+x, 5.798917918952455/20+y, "7");
	StdDraw.text(5.9399999999999995/20+x, 5.038917918952455/20+y, "8");
	StdDraw.text(5.9399999999999995/20+x, 3.538917918952455/20+y, "9");
	StdDraw.text(4.640000000000001/20+x, 2.798917918952455/20+y, "10");
	StdDraw.text(5.9399999999999995/20+x, 2.048917918952455/20+y, "11");
	StdDraw.text(6.690208131003807/20+x, 0.7500000000000004/20+y, "12");
	StdDraw.text(10.788284342357125/20+x, 5.049218485895393/20+y, "13");
	StdDraw.text(10.788284342357123/20+x, 3.5492184858953926/20+y, "14");
	StdDraw.text(9.489246236680463/20+x, 2.799218485895395/20+y, "15");
	StdDraw.text(8.190208131003807/20+x, 3.5492184858953983/20+y, "16");
	StdDraw.text(12.087322448033781/20+x, 2.7992184858953912/20+y, "17");
	StdDraw.text(13.58815547295355/20+x, 2.79873753873372/20+y, "18");
	StdDraw.text(12.83815547295355/20+x, 1.499699433057062/20+y, "19");

}  }        

	for(int i=0;i<n;i++){
	b = (i*2*Math.PI/n);
	a = a + g;	
	P[0][i] = x + Math.cos(b);
	P[1][i] = y + Math.sin(b);
			
	StdDraw.setPenRadius(0.001);
	StdDraw.setPenColor(StdDraw.BLACK);
				
	StdDraw.line(x/10+0.5,y/10+0.5,(P[0][i]/10)+0.5,(P[1][i]/10)+0.5);
				
	StdDraw.setPenRadius(0.01);	
	if(i>1)StdDraw.setPenColor(StdDraw.RED);
	else if (i==1)StdDraw.setPenColor(StdDraw.GREEN);
	else StdDraw.setPenColor(StdDraw.BLUE);
	StdDraw.point((P[0][i]/10)+0.5, (P[1][i]/10)+0.5);
	x = P[0][i];
	y = P[1][i];
}
				 	
	for(int i=0;i<4;i++){
	//if (i>-1){a = a + g;}
	P[0][i] = x + Math.cos(Math.pow(-1,i)*120);
	P[1][i] = y + Math.sin(Math.pow(-1,i)*120);
	StdDraw.setPenRadius(0.001);
	StdDraw.setPenColor(StdDraw.BLACK);
				
	StdDraw.line(x/10+0.5,y/10+0.5,(P[0][i]/10)+0.5,(P[1][i]/10)+0.5);
			
	StdDraw.point((P[1][i]/10)+0.5, (P[0][i]/10)+0.5);
	x = P[0][i];
	y = P[1][i];
	}	
	}
}
         
    / Vertex positions start at (0,0), or at position of previous layout.
    P = new double[2][vN];
    for(int i = 0; i < nodes.length; i++) {
    PVector pos = oldLayout == null ? PVector.v() : oldLayout.position(richNodes[i]);
    P[0][i] = pos.x;   // Koordinatenvergabe?
    P[1][i] = pos.y;
    }
            
    // Gradient descent. 			// Minum
    G = new double[vN][vN];
    for(int i = 0; i < vN; i++)
    	for(int j = i; j < vN; j++)
    		G[i][j] = G[j][i] =
            extRichGraph.containsEdge(richNodes[i], richNodes[j]) ||
            network.graph.containsEdge(richNodes[i].element, richNodes[j].element) ? 1 : 2;
            descent = new Descent(P, D, null);
            
            // Apply initialIterations without user constraints or non-overlap constraints.
            descent.run(INITIAL_ITERATIONS);
            
            // Initialize vertex and contour bound respecting projection.
            // TODO: convert to rich graph form.
            descent.project = new BoundProjection(radii, mD).projectFunctions();
            
            // Allow not immediately connected (by direction) nodes to relax apart (p-stress).
            descent.G = G;
            descent.run(PHASE_ITERATIONS);
            
            converged = false;
        }
        // Improve layout.
        else {
            converged = descent.run(PHASE_ITERATIONS);
        }
        
        // Measure span and shift nodes top left to (0,0).
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        for(int i = 0; i < vN; i++) {
            minX = Math.min(minX, P[0][i]);
            minY = Math.min(minY, P[1][i]);
            maxX = Math.max(maxX, P[0][i]);
            maxY = Math.max(maxY, P[1][i]);
        }
        this.dimensions = PVector.v(maxX - minX, maxY - minY);
        
        for(int i = 0; i < vN; i++) {
            P[0][i] -= minX;
            P[1][i] -= minY;
        }
        
        return converged;
    }
    
*/
