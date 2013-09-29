// THIS IS AN AUTO-GENERATED CLASS FILE. DO NOT EDIT DIRECTLY.

package ${packageName};

import java.util.Date;
import java.util.Set;
import com.nexusdata.core.ManagedObject;

class _${entity.name} extends ManagedObject {

    public interface Property {
<#list entity.attributes as attribute>
        final static String ${attribute.getAllCapsName()} = "${attribute.name}";
</#list>
<#list entity.relationships as relationship>
        final static String ${relationship.getAllCapsName()} = "${relationship.name}";
</#list>
    }

<#list entity.enums as enum>
    public enum ${enum.name} {
    <#list enum.values as enumValue>
        ${enumValue},
    </#list>
    }
</#list>

<#list entity.attributes as attribute>
<#if attribute.hasGetter>
    public ${attribute.getJavaType()} get${attribute.getCapitalizedName()}() {
        return (${attribute.getJavaType()})getValue(Property.${attribute.getAllCapsName()});
    }

</#if>
<#if attribute.hasSetter>
    public void set${attribute.getCapitalizedName()}(${attribute.getJavaType()} ${attribute.name}) {
        setValue(Property.${attribute.getAllCapsName()}, ${attribute.name});
    }

</#if>
</#list>

<#list entity.relationships as relationship>
<#if relationship.hasGetter>
    public ${relationship.getJavaType()} get${relationship.getCapitalizedName()}() {
        return (${relationship.getJavaType()})getValue(Property.${relationship.getAllCapsName()});
    }

</#if>
<#if relationship.hasSetter>
    public void set${relationship.getCapitalizedName()}(${relationship.getJavaType()} ${relationship.name}) {
        setValue(Property.${relationship.getAllCapsName()}, ${relationship.name});
    }

</#if>
</#list>
}
