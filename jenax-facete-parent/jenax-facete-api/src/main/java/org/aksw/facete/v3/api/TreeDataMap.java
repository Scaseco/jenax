package org.aksw.facete.v3.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;


/**
 * A stripped down copy of Vaadin's TreeData class.
 * Used to capture hierarchical projections of FacetPaths.
 */
public class TreeDataMap<K, V> implements Serializable {
    private static final long serialVersionUID = 1L;

    public V get(Object key) {
        HierarchyWrapper<K, V> wrapper = itemToWrapperMap.get(key);
        V result = wrapper == null ? null : wrapper.getValue();
        return result;
    }

    public V put(K key, V value) {
        HierarchyWrapper<K, V> wrapper = itemToWrapperMap.get(key);
        if (wrapper == null) {
            throw new NoSuchElementException();
        }
        V result = wrapper.getValue();
        wrapper.setValue(value);
        return result;
    }


    private void putItem(K item, K parent) {
        HierarchyWrapper<K, V> wrappedItem = new HierarchyWrapper<>(parent, item);
        if (itemToWrapperMap.containsKey(parent)) {
            itemToWrapperMap.get(parent).addChild(item);
        }
        itemToWrapperMap.put(item, wrappedItem);
    }

    /** Return a new instance of this tree structure. Does not copy the elements of type T. */
    public TreeDataMap<K, V> cloneTree() {
        TreeDataMap<K, V> result = new TreeDataMap<>();
        for (Entry<K, HierarchyWrapper<K, V>> entry : itemToWrapperMap.entrySet()) {
            HierarchyWrapper<K, V> original = entry.getValue();
            HierarchyWrapper<K, V> copy = new HierarchyWrapper<>(original.getParent(), original.getKey(), new ArrayList<>(original.getChildren()));
            result.itemToWrapperMap.put(entry.getKey(), copy);
        }
        return result;
    }

    public TreeDataMap<K, V> addItems(Collection<K> rootItems,
            Function<K, ? extends Collection<K>> childItemProvider) {
        rootItems.forEach(item -> {
            addItem(null, item);
            Collection<K> childItems = childItemProvider.apply(item);
            addItems(item, childItems);
            addItemsRecursively(childItems, childItemProvider);
        });
        return this;
    }

    private void addItemsRecursively(Collection<K> items,
            Function<K, ? extends Collection<K>> childItemProvider) {
        items.forEach(item -> {
            Collection<K> childItems = childItemProvider.apply(item);
            addItems(item, childItems);
            addItemsRecursively(childItems, childItemProvider);
        });
    }

    /** Add an item to this structure by recursively adding its ancestors first. */
    public void putItem(K item, Function<? super K, ? extends K> getParent) {
        Preconditions.checkNotNull(item);
        if (!contains(item)) {
            K parent = getParent.apply(item);
            if (parent != null) {
                putItem(parent, getParent);
            }
            addItem(parent, item);
        }
    }

    @Override
    public String toString() {
        return this.itemToWrapperMap.toString();
    }


    private static class HierarchyWrapper<K, V> implements Entry<K, V>, Serializable {
        private static final long serialVersionUID = 1L;
        private K parent;
        private K key;
        private V value;
        private List<K> children;

        public HierarchyWrapper(K parent, K key) {
            this(parent, key, new ArrayList<>());
        }

        public HierarchyWrapper(K parent, K key, List<K> children) {
            this.parent = parent;
            this.key = key;
            this.children = children;
        }

        public K getParent() {
            return parent;
        }

        public void setParent(K parent) {
            this.parent = parent;
        }

        public List<K> getChildren() {
            return children;
        }

        public void addChild(K child) {
            children.add(child);
        }

        public void removeChild(K child) {
            children.remove(child);
        }

        @Override
        public String toString() {
            return children.toString();
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V result = this.value;
            this.value = value;
            return result;
        }
    }

    private final Map<K, HierarchyWrapper<K, V>> itemToWrapperMap = new LinkedHashMap<>();

    public TreeDataMap() {
        super();
        itemToWrapperMap.put(null, new HierarchyWrapper<>(null, null));
    }

    public TreeDataMap<K, V> addRootItems(
            @SuppressWarnings("unchecked") K... items) {
        addItems(null, items);
        return this;
    }

    public TreeDataMap<K, V> addRootItems(Collection<K> items) {
        addItems(null, items);
        return this;
    }

    public TreeDataMap<K, V> addRootItems(Stream<K> items) {
        addItems(null, items);
        return this;
    }

    public TreeDataMap<K, V> addItem(K parent, K item) {
        Objects.requireNonNull(item, "Item cannot be null");
        if (parent != null && !contains(parent)) {
            throw new IllegalArgumentException(
                    "Parent needs to be added before children. "
                            + "To add root items, call with parent as null");
        }
        if (contains(item)) {
            throw new IllegalArgumentException(
                    "Cannot add the same item multiple times: " + item);
        }
        putItem(item, parent);
        return this;
    }

    public TreeDataMap<K, V> addItems(K parent,
            @SuppressWarnings("unchecked") K... items) {
        Arrays.stream(items).forEach(item -> addItem(parent, item));
        return this;
    }

    public TreeDataMap<K, V> addItems(K parent, Collection<K> items) {
        items.forEach(item -> addItem(parent, item));
        return this;
    }

    public TreeDataMap<K, V> addItems(K parent, Stream<K> items) {
        items.forEach(item -> addItem(parent, item));
        return this;
    }

    public TreeDataMap<K, V> removeItem(K item) {
        if (!contains(item)) {
            throw new IllegalArgumentException(
                    "Item '" + item + "' not in the hierarchy");
        }
        new ArrayList<>(getChildren(item)).forEach(child -> removeItem(child));
        itemToWrapperMap.get(itemToWrapperMap.get(item).getParent())
                .removeChild(item);
        if (item != null) {
            // remove non root item from backing map
            itemToWrapperMap.remove(item);
        }
        return this;
    }

    public TreeDataMap<K, V> clear() {
        removeItem(null);
        return this;
    }

    public List<K> getRootItems() {
        return getChildren(null);
    }

    public List<K> getChildren(K item) {
        if (!contains(item)) {
            throw new IllegalArgumentException(
                    "Item '" + item + "' not in the hierarchy");
        }
        return Collections
                .unmodifiableList(itemToWrapperMap.get(item).getChildren());
    }

    public K getParent(K item) {
        if (!contains(item)) {
            throw new IllegalArgumentException(
                    "Item '" + item + "' not in hierarchy");
        }
        return itemToWrapperMap.get(item).getParent();
    }

    public void setParent(K item, K parent) {
        if (!contains(item)) {
            throw new IllegalArgumentException(
                    "Item '" + item + "' not in the hierarchy");
        }

        if (parent != null && !contains(parent)) {
            throw new IllegalArgumentException(
                    "Parent needs to be added before children. "
                            + "To set as root item, call with parent as null");
        }

        if (item.equals(parent)) {
            throw new IllegalArgumentException(
                    "Item cannot be the parent of itself");
        }

        K oldParent = itemToWrapperMap.get(item).getParent();

        if (!Objects.equals(oldParent, parent)) {
            // Remove item from old parent's children
            itemToWrapperMap.get(oldParent).removeChild(item);

            // Add item to parent's children
            itemToWrapperMap.get(parent).addChild(item);

            // Set item's new parent
            itemToWrapperMap.get(item).setParent(parent);
        }
    }

    public void moveAfterSibling(K item, K sibling) {
        if (!contains(item)) {
            throw new IllegalArgumentException(
                    "Item '" + item + "' not in the hierarchy");
        }

        if (sibling == null) {
            List<K> children = itemToWrapperMap.get(getParent(item))
                    .getChildren();

            // Move item to first position
            children.remove(item);
            children.add(0, item);
        } else {
            if (!contains(sibling)) {
                throw new IllegalArgumentException(
                        "Item '" + sibling + "' not in the hierarchy");
            }

            K parent = itemToWrapperMap.get(item).getParent();

            if (!Objects.equals(parent,
                    itemToWrapperMap.get(sibling).getParent())) {
                throw new IllegalArgumentException("Items '" + item + "' and '"
                        + sibling + "' don't have the same parent");
            }

            List<K> children = itemToWrapperMap.get(parent).getChildren();

            // Move item to the position after the sibling
            children.remove(item);
            children.add(children.indexOf(sibling) + 1, item);
        }
    }

    public boolean contains(K item) {
        return itemToWrapperMap.containsKey(item);
    }
}
