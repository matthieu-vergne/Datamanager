package fr.vergne.datamanager;

/**
 * A {@link Storage} is an entity aiming at storing data for future access. As
 * such, it provides different ways to retrieve it. Notice that no
 * adding/removal method is defined in this interface, because no assumption is
 * taken regarding how the underlying data is stored: having for instance a
 * method <code>{@code public <ID, Data> void set(ID id, Data data)}</code>
 * would imply to give a modification access to the same entities having the
 * reading access. While the reading access is mandatory (it is the minimum
 * which should be available to make sense of using a {@link Storage} instance),
 * the writing access can be managed from a different perspective.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
public interface Storage {

	/**
	 * 
	 * @param id
	 *            the {@link ID} of the {@link Data} to retrieve
	 * @return the {@link Data} retrieved
	 */
	public <ID, Data> Data get(ID id);
}
