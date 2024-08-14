package convex.core.lang.exception;

import java.util.ArrayList;
import java.util.List;

import convex.core.data.ACell;
import convex.core.data.AString;
import convex.core.data.Address;
import convex.core.data.Strings;

/**
 * Class representing an Error value produced by the CVM.
 * 
 * See "Error Handling" CAD.
 * 
 * Contains:
 * <ul>
 * <li>An immutable Error Code</li>
 * <li>An immutable Error Message</li>
 * <li>A mutable error trace (for information purposes outside the CVM)</li>
 * <li>Address where the error occurred</li>
 * </ul>
 * 
 * "Computers are useless. They can only give you answers."
 * - Pablo Picasso
 * 
 */
public class ErrorValue extends AThrowable {

	private final ACell message;
	private final ArrayList<AString> trace=new ArrayList<>();
	private ACell log;
	private Address address=null;

	private ErrorValue(ACell code, ACell message) {
		super (code);
		this.message=message;
	}

	public static ErrorValue create(ACell code) {
		return new ErrorValue(code,null);
	}
	
	/**
	 * Creates an ErrorValue with the specified type and message. Message may be null.
	 * @param code Type of error
	 * @param message Off-chain message as CVM String
	 * @return New ErrorValue instance
	 */
	public static ErrorValue create(ACell code, AString message) {
		return new ErrorValue(code,message);
	}
	
	/**
	 * Creates an ErrorValue with the specified type and message. Message may be null.
	 * @param code Type of error
	 * @param message Off-chain message, must be valid CVM Value
	 * @return New ErrorValue instance
	 */
	public static ErrorValue createRaw(ACell code, ACell message) {
		return new ErrorValue(code,message);
	}
	
	/**
	 * Creates an ErrorValue with the specified type and message. Message may be null.
	 * @param code Code of error
	 * @param message Off-chain message as Java String
	 * @return New ErrorValue instance
	 */
	public static ErrorValue create(ACell code, String message) {
		return new ErrorValue(code,Strings.create(message));
	}


	
	public void addTrace(String traceMessage) {
		trace.add(Strings.create(traceMessage));
	}
	
	/**
	 * Stores the CVM local log at the point of the error
	 * @param log Sets the CVM log value for this error
	 */
	public void addLog(ACell log) {
		this.log=log;
	}
	
	/**
	 * Sets the address which is the source of this error
	 * @param a Address of error cause
	 */
	public void setAddress(Address a) {
		this.address=a;
	}
	
	/**
	 * Gets the address which is the source of this error
	 * @return Address of account where error occurred
	 */
	public Address getAddress() {
		return address;
	}
	
	@Override
	public ACell getMessage() {
		return message;
	}

	@Override 
	public String toString() {
		StringBuilder sb=new StringBuilder();
		sb.append("ErrorValue["+code+"]"+((message==null)?"":" : "+message));
		if (trace!=null) {
			for (Object o:trace) {
				sb.append("\n");
				sb.append(o.toString());
			}
		}
			
		return sb.toString();
	}

	/**
	 * Gets the trace for this Error.
	 * 
	 * The trace List is mutable, and may be used to implement accumulation of additional trace entries.
	 * 
	 * @return List of trace entries.
	 */
	public List<AString> getTrace() {
		return trace;
	}
	
	/**
	 * Gets the CVM local log at the time of the Error.
	 * 
	 * @return List of trace entries.
	 */
	public ACell getLog() {
		return log;
	}



}
