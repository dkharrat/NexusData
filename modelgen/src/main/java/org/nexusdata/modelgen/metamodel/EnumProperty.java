package com.github.dkharrat.nexusdata.modelgen.metamodel;

import com.google.common.base.CaseFormat;

import java.util.List;

public class EnumProperty extends Property {

    private List<String> values;

    String getJavaType() {
        return name;
    }

    public List<String> getValues() {
        return values;
    }
}
