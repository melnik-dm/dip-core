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
package ru.dip.core.utilities.git;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

public class CommitCache {
	
	/**
	 * кэш для описания (git describe) коммитов
	 * Ключ - хэш коммита для репозитория
	 * Значение - git describe для этого коммита
	 */
	public static class DescribeCommitCash {
		
		private Map<String, String> fDescribeCommitCaсhe = new HashMap<>(); 
	}

	/**
	 * кэш для коммитов с последними изменениями файла
	 * Ключ основной маппы - коммит проекта (репозитория)
	 * В значении мапа: ресурс - коммит с последними изменениями этого ресурса
	 *
	 */
	public static class CommitChangedCash {
		
		private Map<String, Map<IResource, String>> fCommitChangedHash = new HashMap<String, Map<IResource,String>>();
		
		public String getChangedCommit(IResource resource, String currentProjectCommit) {
			if (currentProjectCommit != null) {
				Map<IResource, String> map = fCommitChangedHash.get(currentProjectCommit);
				if (map != null) {
					return map.get(resource);
				} 
			} 
			return null;
		}
		
		public void putChangedCommit(IResource resource, String currentProjectCommit, String changedResourceCommit) {
			if (currentProjectCommit != null) {
				Map<IResource, String> map = fCommitChangedHash.get(currentProjectCommit);
				if (map == null) {
					map = new HashMap<IResource, String>();
					fCommitChangedHash.put(currentProjectCommit, map);
				}
				map.put(resource, changedResourceCommit);
			}
		}
	}
	
	public static final CommitCache instance = new CommitCache();
	
	// кэш разложен по проектам
	private Map<IProject, CommitChangedCash> fCommitChangedCashByProject = new HashMap<>();
	private Map<IProject, DescribeCommitCash> fDescribeCashByProject = new HashMap<>();
	
	//==================
	// describe
	
	public String getDescribe(IProject project, String commitName) {
		DescribeCommitCash cash = fDescribeCashByProject.get(project);
		if (cash != null) {
			return cash.fDescribeCommitCaсhe.get(commitName);
		}
		return null;
	}
	
	public void putDescribe(IProject project, String commitName, String describe) {
		DescribeCommitCash cash = fDescribeCashByProject.get(project);
		if (cash == null) {
			cash = new DescribeCommitCash();
			fDescribeCashByProject.put(project, cash);
		}
		cash.fDescribeCommitCaсhe.put(commitName, describe);
	}
	
	//=====================
	// коммит с последними изменениями ресурса
	
	public String getCommitChanged(IResource resource, String currentProjectCommit) {
		CommitChangedCash cash = fCommitChangedCashByProject.get(resource.getProject());
		if (cash != null) {
			return cash.getChangedCommit(resource, currentProjectCommit);
		}
		return null;
	}
	
	public void putChangedCommit(IResource resource, String currentProjectCommit, String changedResourceCommit) {
		CommitChangedCash cash = fCommitChangedCashByProject.get(resource.getProject());
		if (cash == null) {
			cash = new CommitChangedCash();
			fCommitChangedCashByProject.put(resource.getProject(), cash);
		}
		cash.putChangedCommit(resource, currentProjectCommit, changedResourceCommit);
	}

}
