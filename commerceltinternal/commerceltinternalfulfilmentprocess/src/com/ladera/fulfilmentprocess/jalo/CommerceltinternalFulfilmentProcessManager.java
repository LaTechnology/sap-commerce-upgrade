/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.ladera.fulfilmentprocess.jalo;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import com.ladera.fulfilmentprocess.constants.CommerceltinternalFulfilmentProcessConstants;

public class CommerceltinternalFulfilmentProcessManager extends GeneratedCommerceltinternalFulfilmentProcessManager
{
	public static final CommerceltinternalFulfilmentProcessManager getInstance()
	{
		ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (CommerceltinternalFulfilmentProcessManager) em.getExtension(CommerceltinternalFulfilmentProcessConstants.EXTENSIONNAME);
	}
	
}
