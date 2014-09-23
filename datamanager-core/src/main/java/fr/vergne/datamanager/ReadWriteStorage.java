package fr.vergne.datamanager;

/**
 * A {@link ReadWriteStorage} is a {@link Storage} which provides writing access
 * (add/remove) to complete the reading access (get) provided by the
 * {@link Storage} interface.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
public interface ReadWriteStorage extends Storage {

	/**
	 * 
	 * @param id
	 *            the {@link ID} of the {@link Data} to store
	 * @param data
	 *            the {@link Data} to store
	 */
	public <ID, Data> void set(ID id, Data data);

	/**
	 * 
	 * @param id
	 *            the {@link ID} of the {@link Data} to remove
	 * @return the {@link Data} removed
	 */
	public <ID, Data> Data remove(ID id);
}
