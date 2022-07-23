package com.tosan.client.http.starter.impl.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tosan.client.http.starter.impl.feign.exception.TosanWebServiceException;
import com.tosan.client.http.starter.impl.feign.exception.TosanWebServiceRuntimeException;

import java.util.*;

/**
 * @author Ali Alimohammadi
 * @since 5/10/2021
 */
public class CustomErrorDecoderConfig {
    private ExceptionExtractType exceptionExtractType;
    private Map<String, Class<? extends Exception>> exceptionMap = new HashMap<>();
    private Class<? extends TosanWebServiceException> checkedExceptionClass;
    private Class<? extends TosanWebServiceRuntimeException> uncheckedExceptionClass;
    private List<String> scanPackageList = new ArrayList<>();
    private ObjectMapper objectMapper;

    public ExceptionExtractType getExceptionExtractType() {
        return exceptionExtractType;
    }

    public void setExceptionExtractType(ExceptionExtractType exceptionExtractType) {
        this.exceptionExtractType = exceptionExtractType;
    }

    public Map<String, Class<? extends Exception>> getExceptionMap() {
        return exceptionMap;
    }

    public void setExceptionMap(Map<String, Class<? extends Exception>> exceptionMap) {
        this.exceptionMap = exceptionMap;
    }

    public Class<? extends TosanWebServiceException> getCheckedExceptionClass() {
        return checkedExceptionClass;
    }

    public void setCheckedExceptionClass(Class<? extends TosanWebServiceException> checkedExceptionClass) {
        this.checkedExceptionClass = checkedExceptionClass;
    }

    public Class<? extends TosanWebServiceRuntimeException> getUncheckedExceptionClass() {
        return uncheckedExceptionClass;
    }

    public void setUncheckedExceptionClass(Class<? extends TosanWebServiceRuntimeException> uncheckedExceptionClass) {
        this.uncheckedExceptionClass = uncheckedExceptionClass;
    }

    public List<String> getScanPackageList() {
        return scanPackageList;
    }

    public CustomErrorDecoderConfig addPackage(String packageName) {
        if (this.scanPackageList == null) {
            this.scanPackageList = new ArrayList<>();
        }
        this.scanPackageList.add(packageName);
        return this;
    }

    public CustomErrorDecoderConfig addPackages(String... packageNames) {
        if (this.scanPackageList == null) {
            this.scanPackageList = new ArrayList<>();
        }
        this.scanPackageList.addAll(Arrays.asList(packageNames));
        return this;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
