package com.matrix.wechat.web.service.factory;

import retrofit.RestAdapter;

import com.matrix.wechat.web.service.ChatHistoryContactService;

/**
 * provide a static method to return a object of ChatHistoryContactService
 */
public class ChatHistoryContactFactory {
	private static ChatHistoryContactService chatHistoryContactService = null;

	public static ChatHistoryContactService getInstance(String getUrl) {
		if (chatHistoryContactService == null) {
			RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(
					getUrl).build();
			restAdapter.setLogLevel(RestAdapter.LogLevel.FULL);
			chatHistoryContactService = restAdapter
					.create(ChatHistoryContactService.class);
		}
		return chatHistoryContactService;
	}
}
