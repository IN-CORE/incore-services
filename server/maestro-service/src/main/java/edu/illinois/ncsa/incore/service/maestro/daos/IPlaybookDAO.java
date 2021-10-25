/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Indira Gutierrez (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.maestro.daos;

import edu.illinois.ncsa.incore.service.maestro.models.Playbook;

import java.util.List;

public interface IPlaybookDAO {
    void initialize();

    List<Playbook> getAllPlaybooks();

    Playbook getPlaybookById(String playbookId);

    Playbook addPlaybook(Playbook playbook);

    Playbook removePlaybook(String playbookId);
    

}
