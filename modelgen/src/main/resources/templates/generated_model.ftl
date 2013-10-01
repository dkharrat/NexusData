// THIS IS AN AUTO-GENERATED CLASS FILE. DO NOT EDIT DIRECTLY.

package ${packageName};

import java.util.Date;
import java.util.Set;
import org.nexusdata.core.ManagedObject;

class _${entity.name} extends ManagedObject {

    public interface Property {
<#list entity.attributes as attribute>
        final static String ${attribute.getNameAsConstant()} = "${attribute.name}";
</#list>
<#list entity.relationships as relationship>
        final static String ${relationship.getNameAsConstant()} = "${relationship.name}";
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
    public ${attribute.getJavaType()} ${attribute.getMethodNameForGetter()}() {
        return (${attribute.getJavaType()})getValue(Property.${attribute.getNameAsConstant()});
    }

</#if>
<#if attribute.hasSetter>
    public void ${attribute.getMethodNameForSetter()}(${attribute.getJavaType()} ${attribute.name}) {
        setValue(Property.${attribute.getNameAsConstant()}, ${attribute.name});
    }

</#if>
</#list>

<#list entity.relationships as relationship>
<#if relationship.hasGetter>
    public ${relationship.getJavaType()} ${relationship.getMethodNameForGetter()}() {
        return (${relationship.getJavaType()})getValue(Property.${relationship.getNameAsConstant()});
    }

</#if>
<#if relationship.hasSetter>
    public void ${relationship.getMethodNameForSetter()}(${relationship.getJavaType()} ${relationship.name}) {
        setValue(Property.${relationship.getNameAsConstant()}, ${relationship.name});
    }

<#if relationship.toMany>
    public void ${relationship.getMethodNameForAddingToCollection()}(${relationship.destinationEntity} ${relationship.getSingularName()}) {
        ${relationship.getMethodNameForGetter()}().add(${relationship.getSingularName()});
    }
</#if>
</#if>
</#list>
}
