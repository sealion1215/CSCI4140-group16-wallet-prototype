package com.reddcoin.stratumj;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.ListenableFuture;
import com.reddcoin.stratumj.messages.CallMessage;
import com.reddcoin.stratumj.messages.ResultMessage;

public abstract class ClientBase extends AbstractExecutionThreadService {

	public interface SubscribeResult {
        public void handle(CallMessage message);
    }
	public abstract boolean isConnected();
    public abstract void disconnect();
    public abstract ListenableFuture<ResultMessage> call(CallMessage message);
    public abstract ListenableFuture<ResultMessage> subscribe(CallMessage message, SubscribeResult handler);
}
