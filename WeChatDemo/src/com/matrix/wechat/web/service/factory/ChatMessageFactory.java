package com.matrix.wechat.web.service.factory;

import retrofit.RestAdapter;

import com.matrix.wechat.web.service.ChatMessageSrevice;

/**
 * provide a static method to return a object of ChatMessageSrevice
 * 
 */
public class ChatMessageFactory {
	private static ChatMessageSrevice chatMessageSrevice = null;

	public static ChatMessageSrevice getInstance(String postUrl) {
		if (chatMessageSrevice == null) {
			RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(
					postUrl).build();
			restAdapter.setLogLevel(RestAdapter.LogLevel.FULL);
			chatMessageSrevice = restAdapter.create(ChatMessageSrevice.class);
		}
		return chatMessageSrevice;
	}
}
