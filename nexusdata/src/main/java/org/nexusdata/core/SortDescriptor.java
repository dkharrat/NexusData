package org.nexusdata.core;

public class SortDescriptor {

    private final String attributeName;
    private final boolean isAscending;

    public SortDescriptor(String attributeName, boolean isAscending) {
        this.attributeName = attributeName;
        this.isAscending = isAscending;
    }

    public String getAttributeName() {
        return attributeName;
    }

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