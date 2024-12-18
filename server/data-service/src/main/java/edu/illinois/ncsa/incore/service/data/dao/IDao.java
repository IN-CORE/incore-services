/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.service.data.dao;

/**
 * Created by ywkim on 9/29/2017.
 */

import java.io.Serializable;
import java.util.List;

/**
 * DAO interface for generic operations supported by all DAOs
 */
public interface IDao<T, ID extends Serializable> {
    T save(T entity);

    void delete(T entity);

    T findOne(ID id);

    List<T> findAll();

    List<T> findAll(int page, int size);

    boolean exists(ID id);

    long count(boolean deleted);
}
