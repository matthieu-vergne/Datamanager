package fr.vergne.datamanager;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import fr.vergne.collection.MultiMap;
import fr.vergne.collection.impl.ListMultiMap;
import fr.vergne.logging.LoggerConfiguration;

/**
 * A {@link DataManager} is a generic structure aiming at providing data storage
 * facilities. Basically, it acts like a {@link Map} and maps some IDs to their
 * values. However, one can use
 * {@link #addDedicatedStorage(DedicatedStorage, boolean)} to use a specific
 * storing strategy or to provide additional data based on the stored one. As
 * long as no {@link DedicatedStorage} is provided, a default {@link Storage} is
 * used. When a {@link DedicatedStorage} is added, it gets the priority for all
 * the data it can store. If more than one {@link DedicatedStorage} is able to
 * store the same data, one of them is chosen as a responsible for the specific
 * ID and kept until some operations implies to change it (e.g. removing the
 * responsible {@link DedicatedStorage}).
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
// FIXME test
public class DataManager implements ReadWriteStorage {

	public final Logger logger = LoggerConfiguration.getSimpleLogger();
	private final ReadWriteStorage defaultStorage = new ReadWriteStorage() {

		private final Map<Object, Object> map = new HashMap<Object, Object>();

		@SuppressWarnings("unchecked")
		@Override
		public <ID, Data> Data get(ID id) {
			return (Data) map.get(id);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <ID, Data> Data remove(ID id) {
			return (Data) map.remove(id);
		}

		@Override
		public <ID, Data> void set(ID id, Data data) {
			map.put(id, data);
		}
	};
	private final Map<Object, Storage> responsibilities = new HashMap<Object, Storage>();
	private final Collection<DedicatedStorage> extraStorages = new HashSet<DedicatedStorage>();

	/**
	 * This method allows to provide special storage strategies to store data in
	 * a smarter way than just mapping an ID to a piece of data. For instance,
	 * by storing the data in a different form for performance purpose or by
	 * providing data which can be fully generated from the already stored data,
	 * without having the need to explicitly store it.<br/>
	 * <br/>
	 * As some relevant data could be already stored in the {@link DataManager},
	 * it is possible to reprocess the data to update the newly added
	 * {@link DedicatedStorage}. It could be costly on the method call, but more
	 * interesting on the long term. This compromise should be made depending on
	 * the {@link DedicatedStorage} added.
	 * 
	 * @param storage
	 *            the {@link DedicatedStorage} to add
	 * @param reprocessData
	 *            <code>true</code> if the data stored should be reprocessed
	 *            when needed, <code>false</code> otherwise
	 * 
	 */
	public void addDedicatedStorage(DedicatedStorage storage,
			boolean reprocessData) {
		boolean updated = extraStorages.add(storage);
		if (!updated || !reprocessData) {
			// no need to reprocess data
		} else {
			Map<Object, Object> dataToReprocess = new HashMap<Object, Object>();
			Iterator<Object> idIterator = responsibilities.keySet().iterator();
			while (idIterator.hasNext()) {
				Object id = idIterator.next();
				if (storage.isDependee(id)) {
					dataToReprocess.put(id, remove(id));
				} else {
					// no need to reprocess this data
				}
				if (storage.isStorable(id)) {
					idIterator.remove();
				} else {
					// no need to change the responsible
				}
			}
			for (Entry<Object, Object> entry : dataToReprocess.entrySet()) {
				Object id = entry.getKey();
				Object data = entry.getValue();
				set(id, data);
			}
		}
	}

	/**
	 * Remove a {@link DedicatedStorage} added via
	 * {@link #addDedicatedStorage(DedicatedStorage)}. Also in this case, it is
	 * possible to request the reprocess of the data. It could be costly on the
	 * method call, but critical if the {@link DedicatedStorage} stores data
	 * which should not be lost. This compromise should be made depending on the
	 * {@link DedicatedStorage} removed.
	 * 
	 * @param storage
	 *            the {@link DedicatedStorage} to remove
	 * @param reprocessData
	 *            <code>true</code> if the data stored in the removed
	 *            {@link DedicatedStorage} should be reprocessed,
	 *            <code>false</code> otherwise
	 */
	public void removeDedicatedStorage(DedicatedStorage storage,
			boolean reprocessData) {
		boolean updated = extraStorages.remove(storage);
		if (!updated || !reprocessData) {
			// no need to reprocess data
		} else {
			MultiMap<Object, Object> dataToReprocess = new ListMultiMap<Object, Object>();
			Iterator<Object> idIterator = responsibilities.keySet().iterator();
			while (idIterator.hasNext()) {
				Object id = idIterator.next();
				if (storage == getResponsibleFor(id)) {
					dataToReprocess.populate(id, remove(id));
					idIterator.remove();
				} else {
					// no need to reprocess this data
				}
			}
			for (Entry<Object, Object> entry : dataToReprocess) {
				Object id = entry.getKey();
				Object data = entry.getValue();
				set(id, data);
			}
		}
	}

	@Override
	public <ID, Data> Data get(ID id) {
		return getResponsibleFor(id).get(id);
	}

	@Override
	public <ID, Data> void set(ID id, Data data) {
		Storage responsible = getResponsibleFor(id);
		for (DedicatedStorage storage : extraStorages) {
			if (storage.isDependee(id)) {
				boolean isResponsible = storage == responsible;
				storage.set(id, data, isResponsible);
			} else {
				// irrelevant data for this storage
			}
		}
		if (responsible == null) {
			defaultStorage.set(id, data);
		} else {
			// already stored in an extra storage
		}
	}

	@Override
	public <ID, Data> Data remove(ID id) {
		Storage responsible = getResponsibleFor(id);
		Data removed = null;
		for (DedicatedStorage storage : extraStorages) {
			if (storage.isDependee(id)) {
				if (storage == responsible) {
					removed = storage.remove(id, true);
				} else {
					storage.remove(id, false);
				}
			} else {
				// irrelevant data for this storage
			}
		}
		if (responsible == defaultStorage) {
			removed = defaultStorage.remove(id);
		} else {
			// already provided by an extra storage
		}
		return removed;
	}

	private <ID> Storage getResponsibleFor(ID id) {
		Storage responsible = responsibilities.get(id);
		if (responsible == null) {
			Collection<DedicatedStorage> candidates = new LinkedList<DedicatedStorage>();
			for (DedicatedStorage storage : extraStorages) {
				if (storage.isStorable(id)) {
					candidates.add(storage);
				} else {
					// irrelevant storage
				}
			}
			if (candidates.isEmpty()) {
				responsible = defaultStorage;
			} else if (candidates.size() == 1) {
				responsible = candidates.iterator().next();
			} else {
				logger.warning("Several responsible candidates for " + id
						+ ": " + candidates);
				responsible = candidates.iterator().next();
				logger.warning("Take one of them without any specific criteria: "
						+ responsible);
			}
			responsibilities.put(id, responsible);
		} else {
			// responsible already known
		}
		return responsible;
	}

}
