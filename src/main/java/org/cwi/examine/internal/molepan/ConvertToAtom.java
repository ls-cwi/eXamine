package org.cwi.examine.internal.molepan;

public class ConvertToAtom {


	public  static String convert_to_atom(String readAtom){
	 	String carbon = "C";
	 	String oxigen = "O";
	 	String hydrogen = "H";
		String nitrogen = "N";
		String chlorine = "CL";
		String bromine = "Br";
		String sulfur = "S";
		String phosphorus = "P";
		String fluorine = "F";
	 	String Atom = "";
	 	if( readAtom.contains(carbon) ) Atom = "C";
     		else if(readAtom.contains(hydrogen)) Atom = "H";
     		else if(readAtom.contains(oxigen)) Atom = "O";
     		else if(readAtom.contains(nitrogen)) Atom = "N";
		else if(readAtom.contains(chlorine)) Atom = "CL";
		else if(readAtom.contains(bromine)) Atom = "Br";
		else if(readAtom.contains(sulfur)) Atom = "S";
		else if(readAtom.contains(phosphorus)) Atom = "P";
		else if(readAtom.contains(fluorine)) Atom = "F";
    	return Atom;
	}	
}
