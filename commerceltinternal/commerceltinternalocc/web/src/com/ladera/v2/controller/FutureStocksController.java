/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.ladera.v2.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import de.hybris.platform.commercefacades.futurestock.FutureStockFacade;
import de.hybris.platform.commercefacades.product.data.FutureStockData;
import de.hybris.platform.commercewebservicescommons.dto.product.ProductFutureStocksListWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.product.ProductFutureStocksWsDTO;
import de.hybris.platform.webservicescommons.cache.CacheControl;
import de.hybris.platform.webservicescommons.cache.CacheControlDirective;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.ladera.product.data.ProductFutureStocksData;
import com.ladera.product.data.ProductFutureStocksDataList;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.tags.Tag;


@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/futureStocks")
@CacheControl(directive = CacheControlDirective.NO_CACHE)
//@Api(tags = "Future Stocks")
@Tag(name = "Future Stocks")
public class FutureStocksController extends BaseCommerceController
{
	@Resource
	private FutureStockFacade futureStockFacade;

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@GetMapping(produces = APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@ApiOperation(nickname = "getFutureStocksList", value = "Gets the future product availability for the list of specified products.", notes = "Returns a list of product codes with a list of future product availability.")
	@ApiBaseSiteIdAndUserIdParam
	public ProductFutureStocksListWsDTO getFutureStocksList(
			@ApiParam(value = "Products identifiers.", example = "3318057,72399000_55,72399000_56", required = true) @RequestParam final String productCodes,
			@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		final ProductFutureStocksDataList productFutureStocksDataList = new ProductFutureStocksDataList();
		final List<String> productCodesList = Arrays.asList(productCodes.split(","));
		final Map<String, List<FutureStockData>> futureStockList = futureStockFacade.getFutureAvailability(productCodesList);
		final List<ProductFutureStocksData> result = MapUtils.emptyIfNull(futureStockList).entrySet().stream()
				.map(entry -> createProductFutureStocksData(entry.getKey(), entry.getValue())).collect(Collectors.toList());
		productFutureStocksDataList.setProductFutureStocks(result);
		return getDataMapper().map(productFutureStocksDataList, ProductFutureStocksListWsDTO.class, fields);
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
	@GetMapping(value = "/{productCode}", produces = APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@ApiOperation(nickname = "getFutureStocks", value = "Gets the future product availability for the specified product.", notes = "Returns a list of future product availability of the specified product.")
	@ApiBaseSiteIdAndUserIdParam
	public ProductFutureStocksWsDTO getFutureStocks(
			@ApiParam(value = "Product identifier", required = true) @PathVariable final String productCode,
			@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields)
	{
		final List<FutureStockData> futureStockList = futureStockFacade.getFutureAvailability(productCode);
		final ProductFutureStocksData productFutureStocksData = createProductFutureStocksData(productCode, ListUtils.emptyIfNull(futureStockList));
		return getDataMapper().map(productFutureStocksData, ProductFutureStocksWsDTO.class, fields);
	}

	protected ProductFutureStocksData createProductFutureStocksData(final String productCode,
			final List<FutureStockData> futureStockList)
	{
		final ProductFutureStocksData productFutureStocksData = new ProductFutureStocksData();
		productFutureStocksData.setProductCode(productCode);
		productFutureStocksData.setFutureStocks(futureStockList);
		return productFutureStocksData;
	}
}
