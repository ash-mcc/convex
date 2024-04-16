package convex.core.data;

import java.lang.reflect.Array;
import java.util.function.Consumer;

import convex.core.store.AStore;
import convex.core.store.Stores;

/**
 * Static utility class for dealing with cells
 */
public class Cells {

	/**
	 * An empty Java array of cells
	 */
	public static final ACell[] EMPTY_ARRAY = new ACell[0];

	/**
	 * Equality method allowing for nulls
	 *
	 * @param a First value
	 * @param b Second value
	 * @return true if arguments are equal, false otherwise
	 */
	public static boolean equals(ACell a, ACell b) {
		if (a == b) return true;
		if (a == null) return false; // b can't be null because of above line
		return a.equals(b); // fall back to ACell equality
	}

	/**
	 * Converts any array to an ACell[] array. Elements must be Cells.
	 *
	 * @param anyArray Array to convert
	 * @return ACell[] array
	 */
	public static ACell[] toCellArray(Object anyArray) {
		int n = Array.getLength(anyArray);
		ACell[] result = new ACell[n];
		for (int i = 0; i < n; i++) {
			result[i] = (ACell) Array.get(anyArray, i);
		}
		return result;
	}

	/**
	 * Gets the number of Refs directly contained in a Cell (will be zero if the
	 * Cell is not a Ref container)
	 *
	 * @param a Cell to check (may be null)
	 * @return Number of Refs in the object.
	 */
	public static int refCount(ACell a) {
		if (a==null) return 0;
		return a.getRefCount();
	}

	/**
	 * Gets a Ref from a Cell by index
	 * @param <R>
	 * @param cell Cell to read Ref from
	 * @param index Numerical index of Ref
	 * @throws IndexOutOfBoundsException if the index is out of range for the Cell
	 * @return
	 */
	public static <R extends ACell> Ref<R> getRef(ACell cell, int index) {
		if (cell ==null) throw new IndexOutOfBoundsException("Bad ref index called on null");
		return cell.getRef(index);
	}

	/**
	 * Checks if a Cell is a valid CVM value
	 * @param a Cell to check
	 * @return True if CVM VAlue, false otherwise
	 */
	public static boolean isCVM(ACell a) {
		if (a==null) return true;
		return a.isCVMValue();
	}
	
	/**
	 * Checks if a Cell is a first class value
	 * @param a Cell to check
	 * @return True if CVM VAlue, false otherwise
	 */
	public static boolean isValue(ACell a) {
		if (a==null) return true;
		return a.isDataValue();
	}
	
	/**
	 * Persist a cell in the current store
	 * @param a Cell to persist
	 * @return Cell after persisting (may be the same Cell if no change in cell hierarchy)
	 * @throws MissingDataException if the cell cannot be fully persisted due to missing data
	 */
	public static <T extends ACell> T persist(T a) {
		return persist(a,Stores.current());
	}

	/**
	 * Persist a cell in the given store
	 * @param a Cell to persist
	 * @param store Store instance to persist in
	 * @return Cell after persisting (may be the same Cell if no change in cell hierarchy)
	 * @throws MissingDataException if the cell cannot be fully persisted due to missing data
	 */
	public static <T extends ACell> T persist(T a, AStore store) {
		Ref<T> ref=Ref.get(a);
		Ref<T> sref=store.storeTopRef(ref, Ref.PERSISTED, null);
		return sref.getValue();
	}

	/**
	 * Announces a Cell, reporting as novelty any cells that have not been previously announced
	 * @param a Cell to announce
	 * @param noveltyHandler Handler for novelty values
	 * @return Cell after announcing (may be the same Cell if no change in cell hierarchy)
	 * @throws MissingDataException if the cell cannot be fully persisted due to missing data
	 */
	public static <T extends ACell> T announce(T a, Consumer<Ref<ACell>> noveltyHandler) {
		if (a==null) {
			return null; // null is never "novelty"
		};
		Ref<T> ref=Stores.current().storeTopRef(a.getRef(), Ref.ANNOUNCED, noveltyHandler);
		return ref.getValue();
	}

	/**
	 * Gets the Hash (Value ID of a cell)
	 * @param a Cell to get hash from
	 * @return Hash value
	 */
	public static Hash getHash(ACell a) {
		if (a==null) return Hash.NULL_HASH;
		return a.getHash();
	}

}