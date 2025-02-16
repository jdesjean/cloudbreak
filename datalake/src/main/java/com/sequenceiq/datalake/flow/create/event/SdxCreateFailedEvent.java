package com.sequenceiq.datalake.flow.create.event;

import com.sequenceiq.datalake.flow.SdxEvent;

public class SdxCreateFailedEvent extends SdxEvent {

    private Exception exception;

    public SdxCreateFailedEvent(Long sdxId, String userId, String requestId, String sdxCrn, Exception exception) {
        super(sdxId, userId, requestId, sdxCrn);
        this.exception = exception;
    }

    public static SdxCreateFailedEvent from(SdxEvent event, Exception exception) {
        return new SdxCreateFailedEvent(event.getResourceId(), event.getUserId(), event.getRequestId(), event.getSdxCrn(), exception);
    }

    @Override
    public String selector() {
        return "SdxCreateFailedEvent";
    }

    public Exception getException() {
        return exception;
    }
}
