package com.matrix.wechat.logic;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeoutException;

import android.app.Activity;
import android.app.Dialog;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.matrix.wechat.activity.AddNewFriendActivity;
import com.matrix.wechat.activity.FriendRequestActivity;
import com.matrix.wechat.model.FriendRequest;
import com.matrix.wechat.model.User;
import com.matrix.wechat.utils.CacheUtil;
import com.matrix.wechat.utils.DialogUtil;
import com.matrix.wechat.web.Request;
import com.matrix.wechat.web.service.factory.ContactsServiceFactory;

import static com.matrix.wechat.global.Constants.*;

public class ContactsWorker {
	public static List<User> getContacts(Activity activity) {
		List<User> contacts = null;
		try {
			contacts = ContactsServiceFactory.getInstance().GetContactList(
					CacheUtil.getUser(activity).getUserid());
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(CacheUtil.context, "network anomaly",
					Toast.LENGTH_LONG).show();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Toast.makeText(CacheUtil.context, "network anomaly",
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
			return null;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			Toast.makeText(CacheUtil.context, "network anomaly",
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
			return null;
		}

		if (contacts == null)
			contacts = new ArrayList<User>();

		return contacts;
	}

	@SuppressWarnings("unchecked")
	public static void notifyContacts(HashMap<String, Object> resultMap) {
		List<User> contacts = (List<User>) resultMap.get(API_CONTACTS);

		if (contacts == null)
			return;

		ContactsViewWorker.contacts.clear();
		ContactsViewWorker.contacts.addAll(contacts);
		ContactsViewWorker.adapter.notifyDataSetChanged();

	}

	public static List<User> getNewFriends(String username) {

		Log.i("info", "username --> " + username);

		List<User> friends = new ArrayList<User>();
		User friend = null;
		try {
			friend = ContactsServiceFactory.getInstance().GetUserByUsername(username);
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			Toast.makeText(CacheUtil.context, "network anomaly",
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Toast.makeText(CacheUtil.context, "network anomaly",
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
			return null;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			Toast.makeText(CacheUtil.context, "network anomaly",
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
			return null;
		}

		if (friend != null)
			friends.add(friend);

		return friends;
	}

	@SuppressWarnings("unchecked")
	public static void notifyNewFriends(HashMap<String, Object> resultMap) {
		List<User> friends = (List<User>) resultMap.get(API_FIND_FRIEND);

		if (friends == null)
			return;

		AddNewFriendActivity.friendList.clear();
		AddNewFriendActivity.friendList.addAll(friends);
		AddNewFriendActivity.adapter.notifyDataSetChanged();
	}

	public static boolean postNewFriendRequest(long from, long to) {
		boolean isSuccess = false;
		boolean result = false;
		try {
			result = ContactsServiceFactory.getInstance().PostRequest(from, to);
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (result)
			isSuccess = true;
		return isSuccess;
	}

	public static void notifyNewFriendRequest(HashMap<String, Object> resultMap) {
		boolean result = (Boolean) resultMap.get(API_ADD_FRIEND_REQUEST);

		Toast toast;
		if (result) {
			toast = Toast.makeText(CacheUtil.context, "Send request success",
					Toast.LENGTH_LONG);
		} else {
			toast = Toast.makeText(CacheUtil.context, "Send request fail",
					Toast.LENGTH_LONG);
		}

		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	public static List<FriendRequest> getRequestList(long userid) {
		List<FriendRequest> friendRequests = null;
		try {
			friendRequests = ContactsServiceFactory.getInstance().GetRequestList(userid);
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (friendRequests == null)
			friendRequests = new ArrayList<FriendRequest>();

		return friendRequests;
	}

	public static void notifyRequestList(HashMap<String, Object> resultMap) {
		@SuppressWarnings("unchecked")
		List<FriendRequest> friendRequests = (List<FriendRequest>) resultMap
				.get(API_GET_REQUEST_LIST);

		FriendRequestActivity.friendRequests.clear();
		FriendRequestActivity.friendRequests.addAll(friendRequests);
		FriendRequestActivity.adapter.notifyDataSetChanged();
	}

	public static User gerUserByUserid(long userid) {
		User user = null;
		try {
			user = ContactsServiceFactory.getInstance().GetUserByID(userid);
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		user.setUserid(userid);

		return user;
	}

	public static void notifyGetUserById(Activity activity,
			HashMap<String, Object> resultMap) {
		User user = (User) resultMap.get(API_GET_USER_BY_USERID);
		if (user == null)
			return;

		if (activity instanceof AddNewFriendActivity) {
			Log.i("info", user.toString());
			Dialogs.showFriendInfoDialog(activity, user, CacheUtil.getUser(activity).getUserid(),
					user.getUserid());
		} else if (activity instanceof FriendRequestActivity) {
			FriendRequest request = (FriendRequest) resultMap
					.get(ACTIVITY_REQUEST);
			Log.i("info", request.toString());
			Dialogs.showRequestInfoDialog(activity, user, request);
		}
	}

	public static boolean responseRequest(long requestID, String status) {
		boolean result = false;
		try {
			result = ContactsServiceFactory.getInstance().ResponseRequest(requestID, status);
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public static void notifyResponseRequests(HashMap<String, Object> resultMap) {
		boolean result = (Boolean) resultMap.get(API_RESPONSE_REQUEST);
		final Dialog dialog = (Dialog) resultMap.get(ACTIVITY_DIALOG);
		Toast toast;

		if (result) {
			toast = Toast.makeText(CacheUtil.context, "Dealing success",
					Toast.LENGTH_LONG);
		} else {
			toast = Toast.makeText(CacheUtil.context, "Dealing fail",
					Toast.LENGTH_LONG);
		}

		new Request(FriendRequestActivity.instance, API_GET_REQUEST_LIST, true)
				.execute(CacheUtil.getUser(FriendRequestActivity.instance).getUserid());
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
		dialog.dismiss();
	}
}
