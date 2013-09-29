package org.nexusdata.core;

public class SortDescriptor {

    private final String m_attributeName;
    private final boolean m_isAscending;

    public SortDescriptor(String attributeName, boolean isAscending) {
        m_attributeName = attributeName;
        m_isAscending = isAscending;
    }

    public String getAttributeName() {
        return m_attributeName;
    }

    public boolean isAscending() {
        return m_isAscending;
    }
}