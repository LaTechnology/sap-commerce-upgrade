/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.ladera.core.jalo;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import com.ladera.core.constants.CommerceltinternalCoreConstants;
import com.ladera.core.setup.CoreSystemSetup;


/**
 * Do not use, please use {@link CoreSystemSetup} instead.
 * 
 */
public class CommerceltinternalCoreManager extends GeneratedCommerceltinternalCoreManager
{
	public static final CommerceltinternalCoreManager getInstance()
	{
		final ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (CommerceltinternalCoreManager) em.getExtension(CommerceltinternalCoreConstants.EXTENSIONNAME);
	}
}
