package fr.vergne.datamanager;

/**
 * A {@link DedicatedStorage} aims at storing only specific data. Such selection
 * is managed differently depending on the direction (storage or retrieval).
 * When storing data (identified with a specific ID), a {@link DedicatedStorage}
 * should tell whether or not it depends on it. Indeed, even if it does not
 * store it, it could store dependent data which need to be updated
 * consequently. On the retrieval task, the {@link DedicatedStorage} should tell
 * whether or not it can store the data.<br/>
 * <br/>
 * While two {@link DedicatedStorage} can depend on the same data, only one
 * should store it to avoid redundancy. Yet, no {@link DedicatedStorage} is
 * assumed to have global information about which {@link DedicatedStorage}
 * should store which data. The only assumption is to know which data it is
 * <b>able</b> to store. Thus, the {@link DedicatedStorage} actually responsible
 * of storing a specific piece of data is decided externally, depending on
 * higher level strategies. A {@link DedicatedStorage} is informed about its
 * responsibility to store a piece of data by a {@link Boolean} value in
 * argument of the methods which can change the stored content (add/remove). If
 * the {@link DedicatedStorage} is not responsible, it should only update its
 * own dependencies, otherwise it should also update the data provided (i.e. add
 * or remove it).
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
public interface DedicatedStorage extends Storage {

	/**
	 * 
	 * @param id
	 *            the {@link ID} of the data
	 * @return <code>true</code> if the {@link DedicatedStorage} needs to be
	 *         informed when this data is updated (add/remove),
	 *         <code>false</code> otherwise
	 */
	public <ID> boolean isDependee(ID id);

	/**
	 * 
	 * @param id
	 *            the {@link ID} of the data
	 * @return <code>true</code> if the {@link DedicatedStorage} is able to
	 *         store the corresponding data, <code>false</code> otherwise
	 * 
	 */
	public <ID> boolean isStorable(ID id);

	/**
	 * 
	 * @param id
	 *            the {@link ID} of the {@link Data} to store
	 * @param data
	 *            the {@link Data} to store
	 * @param isStorer
	 *            <code>true</code> if the {@link DedicatedStorage} is
	 *            considered as responsible for storing this {@link Data},
	 *            <code>false</code> if it should only update its own
	 *            dependencies
	 */
	public <ID, Data> void set(ID id, Data data, boolean isStorer);

	/**
	 * 
	 * @param id
	 *            the {@link ID} of the {@link Data} to remove
	 * @param isStorer
	 *            <code>true</code> if the {@link DedicatedStorage} is
	 *            considered as responsible for storing this {@link Data},
	 *            <code>false</code> if it should only update its own
	 *            dependencies
	 * @return the {@link Data} removed if the {@link DedicatedStorage} is
	 *         responsible, <code>null</code> otherwise
	 */
	public <ID, Data> Data remove(ID id, boolean isStorer);

}
