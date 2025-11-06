/******************************************************************************* * 
 * Copyright (c) 2025 Denis Melnik.
 * Copyright (c) 2025 Ruslan Sabirov.
 * Copyright (c) 2025 Andrei Motorin.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package ru.dip.core.manager;

import java.util.Arrays;
import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import ru.dip.core.model.DipProject;

public class DipNatureManager {

	public static final String NATURE_ID = "ru.dip.requirement.nature";

	public static void addNature(IProject project) {
		try {
			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();
			String[] newNatures = Arrays.copyOf(natures, natures.length + 1);
			newNatures[natures.length] = NATURE_ID;
			description.setNatureIds(newNatures);
			project.setDescription(description, new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public static void removeNatrue(IProject project) {
		try {
			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();
			String[] newNatures = Stream.of(natures).filter((it) -> !it.equals(NATURE_ID)).toArray(String[]::new);
			description.setNatureIds(newNatures);
			project.setDescription(description, new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public static boolean hasNature(IResource resource) {
		IProject project = resource.getProject();
		try {
			return project.hasNature(NATURE_ID);
		} catch (CoreException e) {
			return false;
		}
	}

	public static void checkNature(DipProject project) {
		if (!hasNature(project.resource())) {
			addNature(project.resource());
		}
	}
}
