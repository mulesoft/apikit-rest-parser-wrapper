/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.apikit.implv2.v10.model;

import org.mule.apikit.model.QueryString;
import org.mule.apikit.model.parameter.Parameter;
import org.raml.v2.api.model.common.ValidationResult;
import org.raml.v2.api.model.v10.datamodel.ArrayTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.ObjectTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.UnionTypeDeclaration;
import org.raml.v2.internal.impl.v10.type.TypeId;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singletonList;
import static org.raml.v2.internal.impl.v10.type.TypeId.ARRAY;
import static org.raml.v2.internal.impl.v10.type.TypeId.OBJECT;

public class QueryStringImpl implements QueryString {

  private TypeDeclaration typeDeclaration;
  private Collection<String> scalarTypes;

  public QueryStringImpl(TypeDeclaration typeDeclaration) {
    this.typeDeclaration = typeDeclaration;

    Set<TypeId> typeIds = newHashSet(TypeId.values());
    typeIds.remove(OBJECT);
    typeIds.remove(ARRAY);

    scalarTypes = transform(typeIds, TypeId::getType);
  }

  @Override
  public String getDefaultValue() {
    return typeDeclaration.defaultValue();
  }

  @Override
  public boolean isArray() {
    return typeDeclaration instanceof ArrayTypeDeclaration;
  }

  @Override
  public boolean validate(String value) {
    List<ValidationResult> results = typeDeclaration.validate(value);
    return results.isEmpty();
  }

  @Override
  public boolean isScalar() {
    return scalarTypes.contains(typeDeclaration.type());
  }

  @Override
  public boolean isFacetArray(String facet) {
    if (typeDeclaration instanceof ObjectTypeDeclaration) {
      for (TypeDeclaration type : ((ObjectTypeDeclaration) typeDeclaration).properties()) {
        if (type.name().equals(facet)) {
          return type instanceof ArrayTypeDeclaration;
        }
      }
    }
    return false;
  }

  @Override
  public Map<String, Parameter> facets() {
    Map<String, Parameter> result = new HashMap<>();
    for (TypeDeclaration type : getTypeDeclarations()) {
      if (type instanceof ObjectTypeDeclaration) {
        for (TypeDeclaration prop : ((ObjectTypeDeclaration) type).properties()) {
          result.put(prop.name(), new ParameterImpl(prop));
        }
      }
    }
    return result;
  }

  private List<TypeDeclaration> getTypeDeclarations() {
    if (typeDeclaration instanceof UnionTypeDeclaration) {
      return ((UnionTypeDeclaration) typeDeclaration).of();
    } else if (typeDeclaration instanceof ArrayTypeDeclaration) {
      return singletonList(((ArrayTypeDeclaration) typeDeclaration).items());
    }
    return singletonList(typeDeclaration);
  }
}
