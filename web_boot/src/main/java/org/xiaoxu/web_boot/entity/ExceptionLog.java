package org.xiaoxu.web_boot.entity;

import java.util.Date;

public class ExceptionLog {
    private Long id;

    private String requestId;

    private String module;

    private String description;

    private String level;

    private String exceptionClass;

    private String methodName;

    private String className;

    private String userId;

    private String userIp;

    private String requestUrl;

    private String httpMethod;

    private String userAgent;

    private Date occurTime;

    private Byte status;

    private Integer retryCount;

    private Date createTime;

    private String exceptionMessage;

    private String stackTrace;

    private String requestParams;

    public ExceptionLog(Long id, String requestId, String module, String description, String level, String exceptionClass, String methodName, String className, String userId, String userIp, String requestUrl, String httpMethod, String userAgent, Date occurTime, Byte status, Integer retryCount, Date createTime, String exceptionMessage, String stackTrace, String requestParams) {
        this.id = id;
        this.requestId = requestId;
        this.module = module;
        this.description = description;
        this.level = level;
        this.exceptionClass = exceptionClass;
        this.methodName = methodName;
        this.className = className;
        this.userId = userId;
        this.userIp = userIp;
        this.requestUrl = requestUrl;
        this.httpMethod = httpMethod;
        this.userAgent = userAgent;
        this.occurTime = occurTime;
        this.status = status;
        this.retryCount = retryCount;
        this.createTime = createTime;
        this.exceptionMessage = exceptionMessage;
        this.stackTrace = stackTrace;
        this.requestParams = requestParams;
    }

    public ExceptionLog() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId == null ? null : requestId.trim();
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module == null ? null : module.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level == null ? null : level.trim();
    }

    public String getExceptionClass() {
        return exceptionClass;
    }

    public void setExceptionClass(String exceptionClass) {
        this.exceptionClass = exceptionClass == null ? null : exceptionClass.trim();
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName == null ? null : methodName.trim();
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className == null ? null : className.trim();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId == null ? null : userId.trim();
    }

    public String getUserIp() {
        return userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp == null ? null : userIp.trim();
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl == null ? null : requestUrl.trim();
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod == null ? null : httpMethod.trim();
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent == null ? null : userAgent.trim();
    }

    public Date getOccurTime() {
        return occurTime;
    }

    public void setOccurTime(Date occurTime) {
        this.occurTime = occurTime;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage == null ? null : exceptionMessage.trim();
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace == null ? null : stackTrace.trim();
    }

    public String getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams == null ? null : requestParams.trim();
    }
}