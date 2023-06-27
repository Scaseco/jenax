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
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;


/**
 * A stripped down copy of Vaadin's TreeData class.
 * Used to capture hierarchical projections of FacetPaths.
 */
public class TreeData<T> implements Serializable {
    private static final long serialVersionUID = 1L;


    private void putItem(T item, T parent) {
        HierarchyWrapper<T> wrappedItem = new HierarchyWrapper<>(parent);
        if (itemToWrapperMap.containsKey(parent)) {
            itemToWrapperMap.get(parent).addChild(item);
        }
        itemToWrapperMap.put(item, wrappedItem);
    }

    /** Return a new instance of this tree structure. Does not copy the elements of type T. */
    public TreeData<T> cloneTree() {
        TreeData<T> result = new TreeData<>();
        for (Entry<T, HierarchyWrapper<T>> entry : itemToWrapperMap.entrySet()) {
            HierarchyWrapper<T> original = entry.getValue();
            HierarchyWrapper<T> copy = new HierarchyWrapper<>(original.getParent(), new ArrayList<>(original.getChildren()));
            result.itemToWrapperMap.put(entry.getKey(), copy);
        }
        return result;
    }

    public TreeData<T> addItems(Collection<T> rootItems,
            Function<T, ? extends Collection<T>> childItemProvider) {
        rootItems.forEach(item -> {
            addItem(null, item);
            Collection<T> childItems = childItemProvider.apply(item);
            addItems(item, childItems);
            addItemsRecursively(childItems, childItemProvider);
        });
        return this;
    }

    private void addItemsRecursively(Collection<T> items,
            Function<T, ? extends Collection<T>> childItemProvider) {
        items.forEach(item -> {
            Collection<T> childItems = childItemProvider.apply(item);
            addItems(item, childItems);
            addItemsRecursively(childItems, childItemProvider);
        });
    }

    /** Add an item to this structure by recursively adding its ancestors first. */
    public void putItem(T item, Function<? super T, ? extends T> getParent) {
        Preconditions.checkNotNull(item);
        if (!contains(item)) {
            T parent = getParent.apply(item);
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


    private static class HierarchyWrapper<T> implements Serializable {
        private static final long serialVersionUID = 1L;
        private T parent;
        private List<T> children;

        public HierarchyWrapper(T parent) {
            this(parent, new ArrayList<>());
        }

        public HierarchyWrapper(T parent, List<T> children) {
            this.parent = parent;
            this.children = children;
        }

        public T getParent() {
            return parent;
        }

        public void setParent(T parent) {
            this.parent = parent;
        }

        public List<T> getChildren() {
            return children;
        }

        public void addChild(T child) {
            children.add(child);
        }

        public void removeChild(T child) {
            children.remove(child);
        }

        @Override
        public String toString() {
            return children.toString();
        }
    }

    private final Map<T, HierarchyWrapper<T>> itemToWrapperMap = new LinkedHashMap<>();

    public TreeData() {
        super();
        itemToWrapperMap.put(null, new HierarchyWrapper<>(null));
    }

    public TreeData<T> addRootItems(
            @SuppressWarnings("unchecked") T... items) {
        addItems(null, items);
        return this;
    }

    public TreeData<T> addRootItems(Collection<T> items) {
        addItems(null, items);
        return this;
    }

    public TreeData<T> addRootItems(Stream<T> items) {
        addItems(null, items);
        return this;
    }

    public TreeData<T> addItem(T parent, T item) {
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

    public TreeData<T> addItems(T parent,
            @SuppressWarnings("unchecked") T... items) {
        Arrays.stream(items).forEach(item -> addItem(parent, item));
        return this;
    }

    public TreeData<T> addItems(T parent, Collection<T> items) {
        items.forEach(item -> addItem(parent, item));
        return this;
    }

    public TreeData<T> addItems(T parent, Stream<T> items) {
        items.forEach(item -> addItem(parent, item));
        return this;
    }

    public TreeData<T> removeItem(T item) {
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

    public TreeData<T> clear() {
        removeItem(null);
        return this;
    }

    public List<T> getRootItems() {
        return getChildren(null);
    }

    public List<T> getChildren(T item) {
        if (!contains(item)) {
            throw new IllegalArgumentException(
                    "Item '" + item + "' not in the hierarchy");
        }
        return Collections
                .unmodifiableList(itemToWrapperMap.get(item).getChildren());
    }

    public T getParent(T item) {
        if (!contains(item)) {
            throw new IllegalArgumentException(
                    "Item '" + item + "' not in hierarchy");
        }
        return itemToWrapperMap.get(item).getParent();
    }

    public void setParent(T item, T parent) {
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

        T oldParent = itemToWrapperMap.get(item).getParent();

        if (!Objects.equals(oldParent, parent)) {
            // Remove item from old parent's children
            itemToWrapperMap.get(oldParent).removeChild(item);

            // Add item to parent's children
            itemToWrapperMap.get(parent).addChild(item);

            // Set item's new parent
            itemToWrapperMap.get(item).setParent(parent);
        }
    }

    public void moveAfterSibling(T item, T sibling) {
        if (!contains(item)) {
            throw new IllegalArgumentException(
                    "Item '" + item + "' not in the hierarchy");
        }

        if (sibling == null) {
            List<T> children = itemToWrapperMap.get(getParent(item))
                    .getChildren();

            // Move item to first position
            children.remove(item);
            children.add(0, item);
        } else {
            if (!contains(sibling)) {
                throw new IllegalArgumentException(
                        "Item '" + sibling + "' not in the hierarchy");
            }

            T parent = itemToWrapperMap.get(item).getParent();

            if (!Objects.equals(parent,
                    itemToWrapperMap.get(sibling).getParent())) {
                throw new IllegalArgumentException("Items '" + item + "' and '"
                        + sibling + "' don't have the same parent");
            }

            List<T> children = itemToWrapperMap.get(parent).getChildren();

            // Move item to the position after the sibling
            children.remove(item);
            children.add(children.indexOf(sibling) + 1, item);
        }
    }

    public boolean contains(T item) {
        return itemToWrapperMap.containsKey(item);
    }
}
