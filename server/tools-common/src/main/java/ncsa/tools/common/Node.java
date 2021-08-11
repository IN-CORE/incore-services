/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common;

public interface Node {
    String TAG_SELF = "node";

    Node[] getChildren();

    void setChildren(Node[] children);

    Node[] getDependencies();

    void setDependencies(Node[] dependencies);

    String getName();

    void setName(String name);

    void setChildNames(String[] names);

    void setDependencyNames(String[] names);

    String[] getChildNames();

    String[] getDependencyNames();

    void addChild(Node child) throws IllegalArgumentException;

    void addDependency(Node dependency) throws IllegalArgumentException;

    void addChildName(String name);

    void addDependencyName(String name);

    void removeChild(Node child);

    void removeDependency(Node dependency);

    void removeChildName(String name);

    void removeDependencyName(String name);
}
