/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.commerceorgaddon.controllers.pages;

import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.breadcrumb.Breadcrumb;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.ThirdPartyConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractSearchPageController;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.b2b.model.B2BBudgetModel;
import de.hybris.platform.b2bcommercefacades.company.data.B2BBudgetData;
import de.hybris.platform.b2bcommercefacades.company.data.B2BUnitData;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.commerceorgaddon.controllers.ControllerConstants;
import de.hybris.platform.commerceorgaddon.forms.B2BBudgetForm;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;

import java.text.ParseException;
import java.util.List;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


/**
 * Controller defines routes to manage Budgets within My Company section.
 */
@Controller
@RequestMapping("/my-company/organization-management/manage-budgets")
public class BudgetManagementPageController extends MyCompanyPageController
{
	private static final Logger LOG = Logger.getLogger(BudgetManagementPageController.class);

	private static final String ERROR_NUMERIC_OUT_OF_RANGE = "numeric value out of range";

	@RequestMapping(method = RequestMethod.GET)
	@RequireHardLogIn
	public String manageBudgets(@RequestParam(value = "page", defaultValue = "0") final int page,
			@RequestParam(value = "show", defaultValue = "Page") final AbstractSearchPageController.ShowMode showMode,
			@RequestParam(value = "sort", defaultValue = B2BBudgetModel.CODE) final String sortCode, final Model model)
			throws CMSItemNotFoundException
	{
		final PageableData pageableData = createPageableData(page, getSearchPageSize(), sortCode, showMode);
		final SearchPageData<B2BBudgetData> searchPageData = budgetFacade.search(null, pageableData);
		populateModel(model, searchPageData, showMode);
		final ContentPageModel organizationManagementPage = getContentPageForLabelOrId(ORGANIZATION_MANAGEMENT_CMS_PAGE);
		storeCmsPageInModel(model, organizationManagementPage);
		setUpMetaDataForContentPage(model, organizationManagementPage);
		final List<Breadcrumb> breadcrumbs = myCompanyBreadcrumbBuilder.createManageBudgetsBreadCrumbs();
		model.addAttribute("breadcrumbs", breadcrumbs);
		model.addAttribute(ThirdPartyConstants.SeoRobots.META_ROBOTS, ThirdPartyConstants.SeoRobots.NOINDEX_NOFOLLOW);
		return ControllerConstants.Views.Pages.MyCompany.MyCompanyManageBudgetsPage;
	}

	@RequestMapping(value = "/view", method = RequestMethod.GET)
	@RequireHardLogIn
	public String viewBudgetsDetails(@RequestParam("budgetCode") final String budgetCode, final Model model)
			throws CMSItemNotFoundException
	{
		final B2BBudgetData budgetData = budgetFacade.getBudgetDataForCode(budgetCode);
		if (budgetData == null)
		{
			GlobalMessages.addErrorMessage(model, "error.bugetcode.notfound");
		}
		model.addAttribute("budget", budgetData);
		final ContentPageModel organizationManagementPage = getContentPageForLabelOrId(ORGANIZATION_MANAGEMENT_CMS_PAGE);
		storeCmsPageInModel(model, organizationManagementPage);
		setUpMetaDataForContentPage(model, organizationManagementPage);
		final List<Breadcrumb> breadcrumbs = myCompanyBreadcrumbBuilder.createManageBudgetsBreadCrumbs();
		model.addAttribute("breadcrumbs", breadcrumbs);
		myCompanyBreadcrumbBuilder.addViewBudgetBreadCrumbs(breadcrumbs, budgetCode);
		model.addAttribute(ThirdPartyConstants.SeoRobots.META_ROBOTS, ThirdPartyConstants.SeoRobots.NOINDEX_NOFOLLOW);
		return ControllerConstants.Views.Pages.MyCompany.MyCompanyManageBudgetsViewPage;
	}

	@RequestMapping(value = "/edit", method = RequestMethod.GET)
	@RequireHardLogIn
	public String editBudgetsDetails(@RequestParam("budgetCode") final String budgetCode, final Model model)
			throws CMSItemNotFoundException
	{
		if (!model.containsAttribute("b2BBudgetForm"))
		{
			final B2BBudgetData b2BBudgetData = budgetFacade.getBudgetDataForCode(budgetCode);
			final B2BBudgetForm b2BBudgetform = new B2BBudgetForm();
			b2BBudgetform.setOriginalCode(budgetCode);
			b2BBudgetform.setCode(b2BBudgetData.getCode());
			b2BBudgetform.setCurrency(b2BBudgetData.getCurrency().getIsocode());
			b2BBudgetform.setName(b2BBudgetData.getName());
			b2BBudgetform.setParentB2BUnit(b2BBudgetData.getUnit().getUid());
			b2BBudgetform.setStartDate(b2BBudgetData.getStartDate());
			b2BBudgetform.setEndDate(b2BBudgetData.getEndDate());
			b2BBudgetform.setBudget(formatFactory.createNumberFormat().format(b2BBudgetData.getBudget().doubleValue()));
			model.addAttribute(b2BBudgetform);
		}
		final ContentPageModel organizationManagementPage = getContentPageForLabelOrId(ORGANIZATION_MANAGEMENT_CMS_PAGE);
		storeCmsPageInModel(model, organizationManagementPage);
		setUpMetaDataForContentPage(model, organizationManagementPage);
		final List<Breadcrumb> breadcrumbs = myCompanyBreadcrumbBuilder.createManageBudgetsBreadCrumbs();
		myCompanyBreadcrumbBuilder.addViewBudgetBreadCrumbs(breadcrumbs, budgetCode);
		breadcrumbs.add(new Breadcrumb(String.format("/my-company/organization-management/manage-budgets/edit?budgetCode=%s",
				urlEncode(budgetCode)), getMessageSource().getMessage("text.company.budget.editPage", new Object[]
		{ budgetCode }, "Edit Budget: {0}", getI18nService().getCurrentLocale()), null));
		model.addAttribute("breadcrumbs", breadcrumbs);
		model.addAttribute(ThirdPartyConstants.SeoRobots.META_ROBOTS, ThirdPartyConstants.SeoRobots.NOINDEX_NOFOLLOW);
		return ControllerConstants.Views.Pages.MyCompany.MyCompanyManageBudgetsEditPage;
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@RequireHardLogIn
	public String updateBudgetsDetails(@RequestParam("budgetCode") final String budgetCode,
			@Valid final B2BBudgetForm b2BBudgetForm, final BindingResult bindingResult, final Model model,
			final RedirectAttributes redirectModel) throws CMSItemNotFoundException, ParseException
	{
		b2BBudgetFormValidator.validate(b2BBudgetForm, bindingResult);
		if (bindingResult.hasErrors())
		{
			model.addAttribute(b2BBudgetForm);
			return editBudgetsDetails(b2BBudgetForm.getOriginalCode(), model);
		}
		if (checkEndDateIsBeforeStartDateForBudget(b2BBudgetForm))
		{
			model.addAttribute(b2BBudgetForm);
			bindingResult.rejectValue("endDate", "text.company.budget.endDateLesser.error.title");
			GlobalMessages.addErrorMessage(model, "form.global.error");
			return editBudgetsDetails(b2BBudgetForm.getOriginalCode(), model);
		}
		final B2BBudgetData b2BBudgetData = populateB2BBudgetDataFromForm(b2BBudgetForm);
		try
		{
			budgetFacade.updateBudget(b2BBudgetData);
		}
		catch (final Exception e)
		{
			LOG.warn("Exception while saving the budget details " + e);
			model.addAttribute(b2BBudgetForm);
			GlobalMessages.addErrorMessage(model, "form.global.error");
			checkExceptionAndUpdateError(e, bindingResult);
			return editBudgetsDetails(b2BBudgetForm.getOriginalCode(), model);
		}
		final ContentPageModel organizationManagementPage = getContentPageForLabelOrId(ORGANIZATION_MANAGEMENT_CMS_PAGE);
		storeCmsPageInModel(model, organizationManagementPage);
		setUpMetaDataForContentPage(model, organizationManagementPage);
		final List<Breadcrumb> breadcrumbs = myCompanyBreadcrumbBuilder.createManageBudgetsBreadCrumbs();
		breadcrumbs.add(new Breadcrumb("/my-company/organization-management/manage-budgets/update", getMessageSource().getMessage(
				"text.company.budget.editPage", null, getI18nService().getCurrentLocale()), null));
		model.addAttribute("breadcrumbs", breadcrumbs);
		GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.CONF_MESSAGES_HOLDER, "text.confirmation.budget.updated");
		return String.format(REDIRECT_TO_BUDGET_DETAILS, urlEncode(b2BBudgetData.getCode()));
	}

	@RequestMapping(value = "/add", method = RequestMethod.GET)
	@RequireHardLogIn
	public String getAddBudgetPage(final Model model) throws CMSItemNotFoundException
	{
		if (!model.containsAttribute("b2BBudgetForm"))
		{
			final B2BBudgetForm b2BBudgetForm = new B2BBudgetForm();
			final B2BUnitData b2BUnitData = b2bUnitFacade.getParentUnit();
			b2BBudgetForm.setParentB2BUnit(b2BUnitData.getUid());
			model.addAttribute(b2BBudgetForm);
		}
		final ContentPageModel organizationManagementPage = getContentPageForLabelOrId(ORGANIZATION_MANAGEMENT_CMS_PAGE);
		storeCmsPageInModel(model, organizationManagementPage);
		setUpMetaDataForContentPage(model, organizationManagementPage);
		final List<Breadcrumb> breadcrumbs = myCompanyBreadcrumbBuilder.createManageBudgetsBreadCrumbs();
		breadcrumbs.add(new Breadcrumb("/my-company/organization-management/manage-budgets/update", getMessageSource().getMessage(
				"text.company.budget.addPage", null, getI18nService().getCurrentLocale()), null));
		model.addAttribute("breadcrumbs", breadcrumbs);
		model.addAttribute(ThirdPartyConstants.SeoRobots.META_ROBOTS, ThirdPartyConstants.SeoRobots.NOINDEX_NOFOLLOW);
		return ControllerConstants.Views.Pages.MyCompany.MyCompanyManageBudgetsAddPage;
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@RequireHardLogIn
	public String addNewBudget(@Valid final B2BBudgetForm b2BBudgetForm, final BindingResult bindingResult, final Model model,
			final RedirectAttributes redirectModel) throws CMSItemNotFoundException, ParseException
	{
		b2BBudgetFormValidator.validate(b2BBudgetForm, bindingResult);
		if (bindingResult.hasFieldErrors())
		{
			model.addAttribute(b2BBudgetForm);
			return getAddBudgetPage(model);
		}
		if (checkEndDateIsBeforeStartDateForBudget(b2BBudgetForm))
		{
			model.addAttribute(b2BBudgetForm);
			bindingResult.rejectValue("endDate", "text.company.budget.endDateLesser.error.title");
			GlobalMessages.addErrorMessage(model, "form.global.error");
			return getAddBudgetPage(model);
		}
		final B2BBudgetData b2BBudgetData = populateB2BBudgetDataFromForm(b2BBudgetForm);
		try
		{
			budgetFacade.addBudget(b2BBudgetData);
		}
		catch (final Exception e)
		{
			LOG.warn("Exception while saving the budget details " + e);
			model.addAttribute(b2BBudgetForm);
			checkExceptionAndUpdateError(e, bindingResult);
			GlobalMessages.addErrorMessage(model, "form.global.error");
			return getAddBudgetPage(model);
		}
		final ContentPageModel organizationManagementPage = getContentPageForLabelOrId(ORGANIZATION_MANAGEMENT_CMS_PAGE);
		storeCmsPageInModel(model, organizationManagementPage);
		setUpMetaDataForContentPage(model, organizationManagementPage);
		final List<Breadcrumb> breadcrumbs = myCompanyBreadcrumbBuilder.createManageBudgetsBreadCrumbs();
		breadcrumbs.add(new Breadcrumb("/my-company/organization-management/manage-budgets/update", getMessageSource().getMessage(
				"text.company.budget.editPage", null, getI18nService().getCurrentLocale()), null));
		model.addAttribute("breadcrumbs", breadcrumbs);
		GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.CONF_MESSAGES_HOLDER, "text.confirmation.budget.created");
		return String.format(REDIRECT_TO_BUDGET_DETAILS, urlEncode(b2BBudgetData.getCode()));
	}

	@RequestMapping(value = "/enable", method = RequestMethod.GET)
	@RequireHardLogIn
	public String enableBudget(@RequestParam("budgetCode") final String budgetCode, final Model model)
			throws CMSItemNotFoundException
	{

		final B2BBudgetData b2bBudgetData = budgetFacade.getBudgetDataForCode(budgetCode);

		if (b2bBudgetData.getUnit() != null && !b2bBudgetData.getUnit().isActive())
		{
			GlobalMessages.addErrorMessage(model, "error.budget.parentunit.disabled");
		}
		else
		{
			try
			{
				budgetFacade.enableDisableBudget(budgetCode, true);
			}
			catch (final Exception e)
			{
				LOG.error(e.getMessage(), e);
				GlobalMessages.addErrorMessage(model, "form.global.error");
			}

			if (model.containsAttribute(GlobalMessages.ERROR_MESSAGES_HOLDER))
			{
				return viewBudgetsDetails(budgetCode, model);
			}

		}

		return String.format(REDIRECT_TO_BUDGET_DETAILS, urlEncode(budgetCode));
	}

	@RequestMapping(value = "/disable", method = RequestMethod.GET)
	@RequireHardLogIn
	public String confirmDisableBudgets(@RequestParam("budgetCode") final String budgetCode, final Model model)
			throws CMSItemNotFoundException
	{
		final ContentPageModel organizationManagementPage = getContentPageForLabelOrId(ORGANIZATION_MANAGEMENT_CMS_PAGE);
		storeCmsPageInModel(model, organizationManagementPage);
		setUpMetaDataForContentPage(model, organizationManagementPage);
		final List<Breadcrumb> breadcrumbs = myCompanyBreadcrumbBuilder.createManageBudgetsBreadCrumbs();
		myCompanyBreadcrumbBuilder.addViewBudgetBreadCrumbs(breadcrumbs, budgetCode);
		model.addAttribute("breadcrumbs", breadcrumbs);
		model.addAttribute("budgetCode", budgetCode);
		model.addAttribute(ThirdPartyConstants.SeoRobots.META_ROBOTS, ThirdPartyConstants.SeoRobots.NOINDEX_NOFOLLOW);
		return ControllerConstants.Views.Pages.MyCompany.MyCompanyBudgetDisableConfirm;
	}

	@RequestMapping(value = "/disable", method = RequestMethod.POST)
	@RequireHardLogIn
	public String disableBudget(@RequestParam("budgetCode") final String budgetCode, final Model model)
			throws CMSItemNotFoundException
	{
		try
		{
			budgetFacade.enableDisableBudget(budgetCode, false);
		}
		catch (final Exception e)
		{
			LOG.error("Failed to disable budget: " + budgetCode, e);
			GlobalMessages.addErrorMessage(model, "form.global.error");
			return viewBudgetsDetails(budgetCode, model);
		}
		return String.format(REDIRECT_TO_BUDGET_DETAILS, urlEncode(budgetCode));
	}

	@RequestMapping(value = "/unitDetails", method = RequestMethod.GET)
	@RequireHardLogIn
	public String viewBudgetUnitDetails(@RequestParam("budgetCode") final String budgetCode,
			@RequestParam("unit") final String unit, final Model model) throws CMSItemNotFoundException
	{
		final String viewBudgetUniUrl = unitDetails(unit, model);

		final ContentPageModel organizationManagementPage = getContentPageForLabelOrId(ORGANIZATION_MANAGEMENT_CMS_PAGE);
		storeCmsPageInModel(model, organizationManagementPage);
		setUpMetaDataForContentPage(model, organizationManagementPage);
		final List<Breadcrumb> breadcrumbs = myCompanyBreadcrumbBuilder.createManageBudgetsBreadCrumbs();
		myCompanyBreadcrumbBuilder.addViewBudgetBreadCrumbs(breadcrumbs, budgetCode);
		breadcrumbs.add(new Breadcrumb(String.format(
				"/my-company/organization-management/manage-budgets/unitDetails?budgetCode=%s&unit=%s", urlEncode(budgetCode),
				urlEncode(unit)), getMessageSource().getMessage("text.company.manage.units.details", new Object[]
		{ unit }, "View {0} Business Unit ", getI18nService().getCurrentLocale()), null));
		model.addAttribute("breadcrumbs", breadcrumbs);
		model.addAttribute(ThirdPartyConstants.SeoRobots.META_ROBOTS, ThirdPartyConstants.SeoRobots.NOINDEX_NOFOLLOW);
		return viewBudgetUniUrl;
	}

	private void checkExceptionAndUpdateError(final Exception e, final BindingResult bindingResult)
	{
		final String msg = e.getMessage();
		// as only budget is number, so we can mark invalid here directly
		if (msg.contains(ERROR_NUMERIC_OUT_OF_RANGE))
		{
			bindingResult.rejectValue("budget", "text.company.manageBudgets.budget.invalid");
		}
		else
		{
			bindingResult.rejectValue("code", "text.company.budget.code.exists.error.title");
		}
	}
}
