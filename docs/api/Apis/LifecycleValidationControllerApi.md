# LifecycleValidationControllerApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**validate**](LifecycleValidationControllerApi.md#validate) | **POST** /{notificationType}/$validate |  |
| [**validate1**](LifecycleValidationControllerApi.md#validate1) | **POST** /$validate |  |


<a name="validate"></a>
# **validate**
> String validate(Content-Type, notificationType, body, x-sender)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **Content-Type** | [**MediaType**](../Models/.md)|  | [default to null] |
| **notificationType** | **String**|  | [default to null] |
| **body** | **String**|  | |
| **x-sender** | **String**|  | [optional] [default to null] |

### Return type

**String**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/fhir+json, application/json, application/json+fhir, application/xml
- **Accept**: */*

<a name="validate1"></a>
# **validate1**
> String validate1(Content-Type, body, x-sender)



### Parameters

|Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **Content-Type** | [**MediaType**](../Models/.md)|  | [default to null] |
| **body** | **String**|  | |
| **x-sender** | **String**|  | [optional] [default to null] |

### Return type

**String**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/fhir+json, application/json, application/json+fhir, application/xml
- **Accept**: */*

