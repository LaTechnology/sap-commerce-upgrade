/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.b2bacceleratoraddon.checkout.steps.validation;

import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.validation.AbstractCheckoutStepValidator;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.validation.ValidationResults;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.b2bacceleratoraddon.security.B2BUserGroupProvider;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


/**
 * Abstract checkout step validator for the B2B accelerator.
 */
public abstract class AbstractB2BCheckoutStepValidator extends AbstractCheckoutStepValidator
{
	private B2BUserGroupProvider b2bUserGroupProvider;

	private static final Logger LOG = Logger.getLogger(AbstractB2BCheckoutStepValidator.class);


	@Override
	public ValidationResults validateOnEnter(final RedirectAttributes redirectAttributes)
	{
		if (!getB2bUserGroupProvider().isCurrentUserAuthorizedToCheckOut())
		{
			GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.ERROR_MESSAGES_HOLDER,
					"checkout.error.invalid.accountType");
			return ValidationResults.FAILED;
		}

		if (!getCheckoutFlowFacade().hasValidCart())
		{
			LOG.info("Missing, empty or unsupported cart");
			return ValidationResults.REDIRECT_TO_CART;
		}

		return doValidateOnEnter(redirectAttributes);
	}

	/**
	 * Performs implementation specific validation on entering a checkout step after the common validation has been
	 * performed in the abstract implementation.
	 *
	 * @param redirectAttributes
	 * @return {@link ValidationResults}
	 */
	protected abstract ValidationResults doValidateOnEnter(final RedirectAttributes redirectAttributes);

	protected B2BUserGroupProvider getB2bUserGroupProvider()
	{
		return b2bUserGroupProvider;
	}

	@Required
	public void setB2bUserGroupProvider(final B2BUserGroupProvider b2bUserGroupProvider)
	{
		this.b2bUserGroupProvider = b2bUserGroupProvider;
	}

}
