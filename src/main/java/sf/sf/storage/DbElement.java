package sf.sf.storage;

/** 
 * Interface to represent objects is stored in MapDB.
 * 
 * @author ari
 * 
 * @see sf.sf.storage.DbObject and sf.sf.storage.DbPrefix
 * 
 *
 */
public interface DbElement {
	public abstract String getFullJson();
	public abstract String getPath();
}
