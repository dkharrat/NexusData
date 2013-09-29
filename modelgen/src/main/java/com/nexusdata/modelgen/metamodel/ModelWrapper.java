package com.nexusdata.modelgen.metamodel;

import com.google.gson.JsonElement;

public class ModelWrapper {
    public Integer metaVersion;
    public JsonElement model;       // parsed separately so that metaVersion can be used
}
