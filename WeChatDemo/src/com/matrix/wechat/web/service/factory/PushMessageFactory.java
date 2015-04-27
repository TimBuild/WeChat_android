package com.matrix.wechat.web.service.factory;

import retrofit.RestAdapter;

import com.matrix.wechat.web.service.PushMessageService;

/**
 * push message
 * 
 */
public class PushMessageFactory {
	private static PushMessageService pushMessageSrevice = null;

	public static PushMessageService getInstance(String postUrl) {
		if (pushMessageSrevice == null) {
			RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(
					postUrl).build();
			restAdapter.setLogLevel(RestAdapter.LogLevel.FULL);
			pushMessageSrevice = restAdapter.create(PushMessageService.class);
		}
		return pushMessageSrevice;
	}
}
