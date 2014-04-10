package com.github.dkharrat.nexusdata.core;

/**
 * This class describes how elements within a collection are sorted. Sorting is defined by the attribute name to sort on
 * and the direction of the sort (ascending/descending)
 */
public class SortDescriptor {

    private final String attributeName;
    private final boolean isAscending;

    /**
     * Creates a new SortDescriptor
     *
     * @param attributeName the attribute name to sort on
     * @param isAscending   the direction to sort on. If true, elements will be in ascending order, or otherwise
     *                      in descending order.
     */
    public SortDescriptor(String attributeName, boolean isAscending) {
        this.attributeName = attributeName;
        this.isAscending = isAscending;
    }

    /**
     * Returns the attribute name that is sorted on
     *
     * @return Returns the attribute name that is sorted on
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * Returns the direction of the sort
     *
     * @return if true, sorting is done in ascending order. Otherwise, it's in descending order.
     */
    public boolean isAscending() {
        return isAscending;
    }

    @Override
    public String toString() {
        return "SortDescriptor{" +
                "attributeName='" + attributeName + '\'' +
                ", isAscending=" + isAscending +
                '}';
    }
}