package convex.core.lang;

import convex.core.data.ACell;
import convex.core.data.AList;
import convex.core.data.Address;
import convex.core.data.List;
import convex.core.data.Symbol;
import convex.core.init.Init;

/**
 * Static utilities and functions for CVM code generation
 * 
 * In general, these are helper functions which:
 * - Abstract away from complexity and specific details of code generation
 * - Are more efficient than most alternative approaches e.g. going via the Reader
 * 
 */
public class Code {

	/**
	 * Create code for a CNS update call
	 * @param name Symbol to update in CNS e.g. 'app.cool.project'
	 * @param addr Address to associate with CNS record e.g. #123
	 * @return Code for CNS call
	 */
	public static AList<ACell> cnsUpdate(Symbol name, Address addr) {
		AList<ACell> cmd=List.of(Symbols.CNS_UPDATE, Code.quote(name), addr);
		return List.of(Symbols.CALL, Init.REGISTRY_ADDRESS, cmd );
	}

	public static AList<Symbol> quote(Symbol name) {
		if (name==null) throw new NullPointerException("null Symbol for Code.quote");
		return List.of(Symbols.QUOTE, name);
	}
}
