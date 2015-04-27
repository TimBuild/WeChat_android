package com.matrix.wechat.activity;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.matrix.wechat.R;
import com.matrix.wechat.adapter.ChatMsgViewAdapter;
import com.matrix.wechat.global.Constants;
import com.matrix.wechat.listener.TouchListener;
import com.matrix.wechat.logic.ShowAllContactsHistory;
import com.matrix.wechat.model.ChatHistoryMessage;
import com.matrix.wechat.model.ChatMsgEntity;
import com.matrix.wechat.model.GroupHistoryMessage;
import com.matrix.wechat.model.GroupMember;
import com.matrix.wechat.utils.CacheUtil;
import com.matrix.wechat.utils.FormatDate;
import com.matrix.wechat.utils.NetworkUtil;
import com.matrix.wechat.utils.ReadProperties;
import com.matrix.wechat.utils.voice.PlayVoice;
import com.matrix.wechat.utils.voice.RecordVoice;
import com.matrix.wechat.utils.voice.SendVoice;
import com.matrix.wechat.web.service.ChatHistoryContactService;
import com.matrix.wechat.web.service.ChatMessageSrevice;
import com.matrix.wechat.web.service.GroupService;
import com.matrix.wechat.web.service.PushMessageService;
import com.matrix.wechat.web.service.factory.ChatHistoryContactFactory;
import com.matrix.wechat.web.service.factory.ChatMessageFactory;
import com.matrix.wechat.web.service.factory.GroupServiceFactory;
import com.matrix.wechat.web.service.factory.PushMessageFactory;

@SuppressLint("SimpleDateFormat")
public class ChatActivity extends Activity implements OnClickListener {
	/** Called when the activity is first created. */
	private String postUrl = "http://192.168.1.80/api1/upload.php"; // 处理POST请求的页面
	public static final String IMAGE_PATH = Environment
			.getExternalStorageDirectory().getPath() + "/imgs";
	public static int PIC_REQUEST_CODE = 2;
	public static LinearLayout zoomView = null;
	private String url = "";
	private TextView chat_with_name,chat_with_name_group;
	private Button mBtnSend;
	private Button mBtnBack;
	private Button mBtnAddGroup;
	private ImageButton mBtnSendVoice;
	private EditText mEditTextContent;
	public static ListView mListView;
	public static List<ChatMsgEntity> mDataArrays = new ArrayList<ChatMsgEntity>();
	public static ChatMsgViewAdapter mAdapter;
	public static Activity instance = null;
	public static ImageView imageView = null;

	private Integer contact_userid = null;
	private String contact_name = "", contact_userName = "",contact_name_group="";
	private List<ChatHistoryMessage> chatHistoryMessages = null;

	private static final String mUriImage = MediaStore.Images.Media.DATA;
	private ArrayList<Bitmap> mImageArr = new ArrayList<Bitmap>();

	private ImageView picIMG;
	private ImageView expressBtn;
	private Dialog builder;
	private int[] imageIds = new int[50];

	private ImageButton voiceBT;
	static RecordVoice recordVoice = new RecordVoice();
	static PlayVoice pv = new PlayVoice();
	
	private boolean isGroupChat = false;
	private String isGroup = "false";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		/**
		 * 打开手机相册
		 * */
		picIMG = (ImageView) findViewById(R.id.img_bt_pic);
		voiceBT = (ImageButton) findViewById(R.id.voiceBT);
		voiceBT.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getActionMasked()) {
				case MotionEvent.ACTION_DOWN:
					voiceBT.setBackgroundResource(R.drawable.microphone_press);
					System.out.println("开始录音");
					recordVoice.startRecording();
					break;
				case MotionEvent.ACTION_UP:
					voiceBT.setBackgroundResource(R.drawable.microphone);
					System.out.println("结束录音");
					boolean result = recordVoice.stopRecording();
					if(!result){
						Toast.makeText(ChatActivity.this,
								  "record time is to short", Toast.LENGTH_LONG).show();
						return true;
					}
					try {
						if (NetworkUtil.isNetworkConnected(ChatActivity.this)) {
							String voicePath = SendVoice.uploadFile(Environment
									.getExternalStorageDirectory()
									.getAbsolutePath()
									+ "/test.3gp", postUrl);
							
							ChatMsgEntity entity = new ChatMsgEntity();
							entity.setDate(getDate());
							entity.setMsgType(false);

							entity.setText("[Voice][" + voicePath);

							mDataArrays.add(entity);
							mAdapter.notifyDataSetChanged();

							mEditTextContent.setText("");

							mListView.setSelection(mListView.getCount() - 1);
							new SendMessageAsync().execute(entity,isGroupChat);

						} else {
							/*
							 * Toast.makeText(ChatActivity.this,
							 * "network anomaly", Toast.LENGTH_LONG).show();
							 */
						}

					} catch (Exception e) {
						e.printStackTrace();
					}

					break;
				default:
					break;
				}

				return true;
			}
		});

		picIMG.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_PICK);
				intent.setType("image/*");
				// Intent intent = new Intent(ChatActivity.this,
				// PhotosActivity.class);
				startActivityForResult(intent, PIC_REQUEST_CODE);
			}
		});

		instance = this;

		// 启动activity时不自动弹出软键盘
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		Constants.CURRENT_VIEW = "chatting";

		initView();

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		String userId = (String) bundle.get("contact_userid");
		contact_userid = Integer.parseInt(userId);
	    isGroup = bundle.getString("isGroup");
		contact_name = bundle.getString("contact_name");
		System.out.println("chatactivity --->isGroup:"+isGroup);
		System.out.println("chatactivity --->contact_name:"+contact_name);
		if(isGroup.equals("true")){
			//要从数据库中取群名称
			contact_name_group = bundle.getString("contact_groupName");
			chat_with_name.setVisibility(View.INVISIBLE);
			chat_with_name_group.setVisibility(View.VISIBLE);
			chat_with_name_group.setText(contact_name_group);
		}
		else{
			chat_with_name.setVisibility(View.VISIBLE);
			chat_with_name_group.setVisibility(View.INVISIBLE);
			chat_with_name.setText(contact_name);
		}
		
		zoomView = (LinearLayout) findViewById(R.id.zoomView);
		imageView = (ImageView) zoomView.findViewById(R.id.imageView);
		imageView.setOnTouchListener(new TouchListener(imageView));

		contact_userName = bundle.getString("contact_userName");
		Constants.CURRENT_CHAT_WITH = contact_userName;

		url = ReadProperties.read("url", "url") + "message";

		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
		if (NetworkUtil.isNetworkConnected(this))
			if(pattern.matcher(contact_userName).matches()){
				//群聊
				new GetMessageAsync().execute("group",contact_userName);
			}
			else{
				new GetMessageAsync().execute(url);
			}
		else {
			Toast.makeText(AddNewFriendActivity.instance, "network anomaly",
					Toast.LENGTH_LONG).show();
		}
	}

	public void initView() {
		chat_with_name = (TextView) findViewById(R.id.chat_with_name);
		chat_with_name_group = (TextView) findViewById(R.id.chat_with_name_group);
		mListView = (ListView) findViewById(R.id.listview);
		mBtnSend = (Button) findViewById(R.id.btn_send);
		mBtnSend.setOnClickListener(this);
		mBtnSendVoice = (ImageButton) findViewById(R.id.btn_send_voice);
		mBtnSendVoice.setOnClickListener(this);
		mBtnBack = (Button) findViewById(R.id.btn_back);
		mBtnBack.setOnClickListener(this);
		mBtnAddGroup = (Button) findViewById(R.id.btn_add_group);
		mBtnAddGroup.setOnClickListener(this);
		expressBtn = (ImageView) findViewById(R.id.team_singlechat_id_expression);
		expressBtn.setOnClickListener(this);
		mEditTextContent = (EditText) findViewById(R.id.et_sendmessage);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_send:
			send();
			break;
		case R.id.btn_send_voice:
			sendVoice();
			break;
		case R.id.btn_back:
			finish();
			break;
		case R.id.team_singlechat_id_expression:
			createExpressionDialog();

			break;
		case R.id.btn_add_group:
			Intent intent = new Intent(ChatActivity.this,
					GroupActivity.class);
			startActivity(intent);
			break;
		}
	}

	/**
	 * 创建一个表情选择对话框
	 */
	private void createExpressionDialog() {
		builder = new Dialog(ChatActivity.this);
		GridView gridView = createGridView();
		builder.setContentView(gridView);
		builder.setTitle("default expression");
		builder.show();

		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Bitmap bitmap = null;
				bitmap = BitmapFactory.decodeResource(getResources(),
						imageIds[position % imageIds.length]);
				ImageSpan imageSpan = new ImageSpan(ChatActivity.this, bitmap);
				String str = null;
				if (position < 10) {
					str = "f00" + position;
				} else if (position < 100) {
					str = "f0" + position;
				}

				SpannableString spanableString = new SpannableString(str);
				spanableString.setSpan(imageSpan, 0, 4,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				mEditTextContent.append(spanableString);
				builder.dismiss();

			}
		});
	}

	/**
	 * 生成一个表情对话框中的gridview
	 * 
	 * @return
	 */
	private GridView createGridView() {
		final GridView view = new GridView(this);
		List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
		// 生成20个表情的id，封装

		for (int i = 0; i < 50; i++) {
			try {
				if (i < 10) {
					Field field = R.drawable.class.getDeclaredField("f00" + i);
					int resourceId = Integer.parseInt(field.get(null)
							.toString());
					imageIds[i] = resourceId;
				} else if (i < 100) {
					Field field = R.drawable.class.getDeclaredField("f0" + i);
					int resourceId = Integer.parseInt(field.get(null)
							.toString());
					imageIds[i] = resourceId;
				}

			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Map<String, Object> listItem = new HashMap<String, Object>();
			listItem.put("image", imageIds[i]);
			listItems.add(listItem);
		}

		SimpleAdapter simpleAdapter = new SimpleAdapter(this, listItems,
				R.layout.team_layput_single_expression_cell,
				new String[] { "image" }, new int[] { R.id.image });
		view.setAdapter(simpleAdapter);
		view.setNumColumns(5);
		view.setBackgroundColor(Color.rgb(214, 211, 214));
		view.setHorizontalSpacing(1);
		view.setVerticalSpacing(1);
		view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		view.setGravity(Gravity.CENTER);

		return view;

	}

	private void send() {
//		System.out.println("当前信息：---------------->"+Constants.CURRENT_CHAT_WITH);
		
//		String pattern = "^[-\\+]?[\\d]*$";
		
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
		
		if(pattern.matcher(Constants.CURRENT_CHAT_WITH).matches()){
			System.out.println("数字"+Constants.CURRENT_CHAT_WITH);
			isGroupChat = true;
			//群聊
			if (mEditTextContent.getText().toString().equals("")) {
				mBtnSend.setVisibility(View.INVISIBLE);
				mBtnSendVoice.setVisibility(View.VISIBLE);
				findViewById(R.id.rl_bottom_media).setVisibility(View.VISIBLE);
			}
			if (NetworkUtil.isNetworkConnected(ChatActivity.this)) {
				String contString = mEditTextContent.getText().toString();
				
				if (contString.length() > 0) {
					ChatMsgEntity entity = new ChatMsgEntity();
					entity.setDate(getDate());
					entity.setMsgType(false);
					entity.setGroup(true);
					entity.setText(contString);
					
					mDataArrays.add(entity);
					mAdapter.notifyDataSetChanged();
					
					mEditTextContent.setText("");
					
					mListView.setSelection(mListView.getCount() - 1);
					new SendMessageAsync().execute(entity,isGroupChat);
				}
			} else {
				Toast.makeText(ChatActivity.this, "network anomaly",
						Toast.LENGTH_LONG).show();
			}
		}
		else{
			System.out.println("name"+Constants.CURRENT_CHAT_WITH);
			isGroupChat = false;
			//单个聊天
			if (mEditTextContent.getText().toString().equals("")) {
				mBtnSend.setVisibility(View.INVISIBLE);
				mBtnSendVoice.setVisibility(View.VISIBLE);
				findViewById(R.id.rl_bottom_media).setVisibility(View.VISIBLE);
			}
			if (NetworkUtil.isNetworkConnected(ChatActivity.this)) {
				String contString = mEditTextContent.getText().toString();
				
				if (contString.length() > 0) {
					ChatMsgEntity entity = new ChatMsgEntity();
					entity.setDate(getDate());
					entity.setMsgType(false);
					entity.setGroup(false);
					entity.setText(contString);
					
					mDataArrays.add(entity);
					mAdapter.notifyDataSetChanged();
					
					mEditTextContent.setText("");
					
					mListView.setSelection(mListView.getCount() - 1);
					new SendMessageAsync().execute(entity,isGroupChat);
				}
			} else {
				Toast.makeText(ChatActivity.this, "network anomaly",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	private String getDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateStr = sdf.format(new Date());
		return dateStr;
	}

	public void head_xiaohei(View v) { // 标题栏 返回按钮
		Intent intent = new Intent(ChatActivity.this, ChatContactInfoActivity.class);
		startActivity(intent);
	}

	private class GetMessageAsync extends AsyncTask<String, Void, Void> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			if (chatHistoryMessages == null) {
				chatHistoryMessages = new ArrayList<ChatHistoryMessage>();
			}
		}

		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			String getMsgUrl = params[0];
			
			if(getMsgUrl.equals("group")){
				//群聊
				int group_id = Integer.parseInt(params[1]);
				
				List<GroupHistoryMessage> groupHistoryMessages = GroupServiceFactory.getInstance().getGroupMessage(group_id);
				chatHistoryMessages = new ArrayList<ChatHistoryMessage>();
				if(groupHistoryMessages == null){
					groupHistoryMessages = new ArrayList<GroupHistoryMessage>();
				}
				for(GroupHistoryMessage message:groupHistoryMessages){
					ChatHistoryMessage chm = new ChatHistoryMessage();
					chm.setUserIdFrom(message.getSenduser());
					chm.setUserIdTo(group_id);
					chm.setDate(message.getSendtime());
					chm.setContent(message.getMessage());
					chm.setPicture(message.getPicture());
					chm.setMessageId(message.getMessageid());
					chm.setStatus("1");
					//2015-4-16
					chm.setOwnerName(message.getNickname());
					chatHistoryMessages.add(chm);
					
				}
				
			}
			else{
				ChatHistoryContactService chatHistoryContactService = ChatHistoryContactFactory
						.getInstance(getMsgUrl);
				chatHistoryMessages = chatHistoryContactService
						.getChatHistoryMessages(Constants.USER_ID, contact_userid);
				if (chatHistoryMessages == null) {
					chatHistoryMessages = new ArrayList<ChatHistoryMessage>();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			for (ChatHistoryMessage chatHistoryMessage : chatHistoryMessages) {
				ChatMsgEntity entity = new ChatMsgEntity();
				if (chatHistoryMessage.getUserIdFrom() == Constants.USER_ID) {
					entity.setMsgType(false);
				} else {
					entity.setName(contact_name);
					entity.setMsgType(true);
				}
				
				if(chatHistoryMessage.getOwnerName() != null){
					entity.setSendNameBy(chatHistoryMessage.getOwnerName());
					entity.setPic(chatHistoryMessage.getPicture());
				}
				
				entity.setGroup(isGroup.equals("true"));
				System.out.println("GetMessageAsync----------->"+isGroupChat);
				entity.setText(chatHistoryMessage.getContent());
				entity.setDate(FormatDate.TimeStamp2Date(
						chatHistoryMessage.getDate(), "yyyy-MM-dd HH:mm:ss"));
				mDataArrays.add(entity);
			}

			mAdapter = new ChatMsgViewAdapter(ChatActivity.this, mDataArrays);
			mListView.setAdapter(mAdapter);
		}
	}

	private class SendMessageAsync extends
			AsyncTask<Object, Void, String> {
		ChatMsgEntity chatMsgEntity = null;
		ChatMessageSrevice chatMessageSrevice = null;
		PushMessageService pushMessageService = null;
		GroupService groupService = null;
		Date date = null;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Object... params) {
			// TODO Auto-generated method stub
			if ((Boolean) params[1]) {
				// 群聊
				chatMsgEntity = (ChatMsgEntity) params[0];
//				chatMessageSrevice = ChatMessageFactory.getInstance(url);
				groupService = GroupServiceFactory.getInstance();
				String pushPath = url + "/jpush/examples";
				pushMessageService = PushMessageFactory.getInstance(pushPath);
				
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");

				try {
					date = sdf.parse(chatMsgEntity.getDate());
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				boolean flag = groupService.postMessageGroup(
						Integer.parseInt(Constants.CURRENT_CHAT_WITH),
						chatMsgEntity.getText(),
						(int) CacheUtil.getUser(ChatActivity.this).getUserid(),
						FormatDate.toUnixTime(date));
				/*boolean flag = chatMessageSrevice.postMesage(Constants.USER_ID,
						contact_userid, chatMsgEntity.getText(),
						FormatDate.toUnixTime(date));*/
				//修改
				String contentText = "{\"send_userId\":\""
						+ CacheUtil.getUser(ChatActivity.this).getUserid()
						+ "\",\"send_userName\":\""
						+ Constants.CURRENT_CHAT_WITH
						+ "\",\"isGroup\":\"true"
						+ "\",\"groupName\":\""
						+ contact_name_group
						+ "\",\"send_userNickName\":\""
						+ CacheUtil.getUser(ChatActivity.this).getNickname()
						+ "\",\"msg_content\":\"" + chatMsgEntity.getText()
						+ "\"}";
				System.out.println("context::::::"+contentText);
				StringBuffer contact = new StringBuffer("[");
				List<GroupMember> groupMembers = groupService.getGroupMember(Integer.parseInt(Constants.CURRENT_CHAT_WITH));
				
				for(GroupMember groupMember : groupMembers){
					if(groupMember.getUserid() == (int) CacheUtil.getUser(ChatActivity.this).getUserid()){
						continue;
					}
					System.out.println("SendMessageAsync------------------>"+groupMember.getUsername());
					contact.append("\""+groupMember.getUsername()+"\",");
				}
				contact.deleteCharAt(contact.length() - 1);
				contact.append("]");
				System.out.println("----------------------------------->"+contact.toString());
				pushMessageService.pushMessageGroup(contact.toString(), contentText,
						new Callback<String>() {

							@Override
							public void success(String arg0, Response arg1) {
								// TODO Auto-generated method stub

							}

							@Override
							public void failure(RetrofitError arg0) {

							}
						});
				
				return flag+"";
			} else {
				chatMsgEntity = (ChatMsgEntity) params[0];
				chatMessageSrevice = ChatMessageFactory.getInstance(url);
				

				String pushPath = url + "/jpush/examples";
				pushMessageService = PushMessageFactory.getInstance(pushPath);

				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");

				try {
					date = sdf.parse(chatMsgEntity.getDate());
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				boolean flag = chatMessageSrevice.postMesage(Constants.USER_ID,
						contact_userid, chatMsgEntity.getText(),
						FormatDate.toUnixTime(date));
				String contentText = "{\"send_userId\":\""
						+ CacheUtil.getUser(ChatActivity.this).getUserid()
						+ "\",\"send_userName\":\""
						+ CacheUtil.getUser(ChatActivity.this).getUsername()
						+ "\",\"isGroup\":\"false"
						+ "\",\"send_userNickName\":\""
						+ CacheUtil.getUser(ChatActivity.this).getNickname()
						+ "\",\"msg_content\":\"" + chatMsgEntity.getText()
						+ "\"}";
				// Log.i("ChatActivity", "---------------->" + contentText);
				// push message
				pushMessageService.pushMessage(contact_userName, contentText,
						new Callback<String>() {

							@Override
							public void success(String arg0, Response arg1) {
								// TODO Auto-generated method stub

							}

							@Override
							public void failure(RetrofitError arg0) {

							}
						});

				return flag + "";
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			System.out.println(result);
			ShowAllContactsHistory.updateLocalChatHistoryContacts(
					contact_userName, FormatDate.toUnixTime(date),
					chatMsgEntity.getText());

			if ("false".equals(result)) {
				chatMsgEntity.setName("send failed");
			}

			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Constants.CURRENT_VIEW = "";
		Constants.CURRENT_CHAT_WITH = "";
	}

	/**
	 * 点击图片后的回调函数
	 * */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PIC_REQUEST_CODE && resultCode == RESULT_OK) {

			Uri uri = data.getData();

			Log.i("info", uri + "");
			System.out.println(uri.getPath());
			ContentResolver cr = this.getContentResolver();
			try {
				BitmapFactory.Options opt = new BitmapFactory.Options();
				opt.inJustDecodeBounds = true;
				Bitmap bmp = BitmapFactory.decodeStream(
						cr.openInputStream(uri), null, opt);

				int picWidth = opt.outWidth;
				int picHeight = opt.outHeight;

				WindowManager windowManager = getWindowManager();
				Display display = windowManager.getDefaultDisplay();

				int width = display.getWidth();
				int height = display.getHeight();

				opt.inSampleSize = 1;
				// 根据屏的大小和图片大小计算出缩放比例
				if (picWidth > picHeight) {
					if (picWidth > width)
						opt.inSampleSize = picWidth / width;
				}

				else {
					if (picHeight > height)
						opt.inSampleSize = picHeight / height;
				}

				// 这次再真正地生成一个有像素的，经过缩放了的bitmap
				opt.inJustDecodeBounds = false;

				bmp = BitmapFactory.decodeStream(cr.openInputStream(uri), null,
						opt);
				;

				File folder = new File(IMAGE_PATH);
				if (!folder.exists()) {
					folder.mkdirs();
				}
				String path = IMAGE_PATH + "/" + "1.png";
				copyImageToCard(bmp, "1.png");
				uploadFile(path);

				deleteFile(path);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	/* 上传文件至Server的方法 */
	@SuppressLint("NewApi")
	private void uploadFile(String uploadFile) {
		String end = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		try {
			URL url = new URL(postUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			/*
			 * Output to the connection. Default is false, set to true because
			 * post method must write something to the connection
			 */
			con.setDoOutput(true);
			/* Read from the connection. Default is true. */
			con.setDoInput(true);
			/* Post cannot use caches */
			con.setUseCaches(false);
			/* Set the post method. Default is GET */
			con.setRequestMethod("POST");
			/* 设置请求属性 */
			con.setRequestProperty("Connection", "Keep-Alive");
			con.setRequestProperty("Charset", "UTF-8");
			con.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);
			/* 设置StrictMode 否则HTTPURLConnection连接失败，因为这是在主进程中进行网络连接 */
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
					.detectDiskReads().detectDiskWrites().detectNetwork()
					.penaltyLog().build());
			/* 设置DataOutputStream，getOutputStream中默认调用connect() */
			DataOutputStream ds = new DataOutputStream(con.getOutputStream()); // output
																				// to
																				// the
																				// connection
			ds.writeBytes(twoHyphens + boundary + end);
			ds.writeBytes("Content-Disposition: form-data; "
					+ "name=\"file\";filename=\"" + "111.jpg" + "\"" + end);
			ds.writeBytes(end);
			/* 取得文件的FileInputStream */
			FileInputStream fStream = new FileInputStream(uploadFile);
			/* 设置每次写入8192bytes */
			int bufferSize = 8192;
			byte[] buffer = new byte[bufferSize]; // 8k
			int length = -1;
			/* 从文件读取数据至缓冲区 */
			while ((length = fStream.read(buffer)) != -1) {
				/* 将资料写入DataOutputStream中 */
				ds.write(buffer, 0, length);
			}
			ds.writeBytes(end);
			ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
			/* 关闭流，写入的东西自动生成Http正文 */
			fStream.close();
			/* 关闭DataOutputStream */
			ds.close();
			/* 从返回的输入流读取响应信息 */
			InputStream is = con.getInputStream(); // input from the connection
													// 正式建立HTTP连接
			int ch;
			StringBuffer b = new StringBuffer();
			while ((ch = is.read()) != -1) {
				b.append((char) ch);
			}
			/* 显示网页响应内容 */
			sendPicture(b.toString());
			// Toast.makeText(this, b.toString().trim(),
			// Toast.LENGTH_LONG).show();//Post成功
			// android.util.Log.i("TAG", b.toString());

		} catch (Exception e) {
			/* 显示异常信息 */
			Toast.makeText(this, "Fail:" + e, Toast.LENGTH_LONG).show();// Post失败
			android.util.Log.i("TAG", e + "");

		}
	}

	public void sendPicture(String imgUrl) {
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
		if(pattern.matcher(Constants.CURRENT_CHAT_WITH).matches()){
			isGroupChat = true;
			if (NetworkUtil.isNetworkConnected(ChatActivity.this)) {
				if (imgUrl.length() > 0) {
					ChatMsgEntity entity = new ChatMsgEntity();
					entity.setDate(getDate());
					entity.setMsgType(false);
					entity.setGroup(true);
					entity.setText("[Image][" + imgUrl);
	
					mDataArrays.add(entity);
					mAdapter.notifyDataSetChanged();
	
					mEditTextContent.setText("");
	
					mListView.setSelection(mListView.getCount() - 1);
					new SendMessageAsync().execute(entity,isGroupChat);
				}
			} else {
				Toast.makeText(ChatActivity.this, "network anomaly",
						Toast.LENGTH_LONG).show();
			}
		}
		else{
			if (NetworkUtil.isNetworkConnected(ChatActivity.this)) {
				if (imgUrl.length() > 0) {
					ChatMsgEntity entity = new ChatMsgEntity();
					entity.setDate(getDate());
					entity.setMsgType(false);
					entity.setText("[Image][" + imgUrl);
					
					mDataArrays.add(entity);
					mAdapter.notifyDataSetChanged();
					
					mEditTextContent.setText("");
					
					mListView.setSelection(mListView.getCount() - 1);
					new SendMessageAsync().execute(entity,isGroupChat);
				}
			} else {
				Toast.makeText(ChatActivity.this, "network anomaly",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * copy image to sdCard
	 * 
	 * @param bitmap
	 * @param name
	 */
	private static String copyImageToCard(Bitmap bitmap, String name) {

		String path = IMAGE_PATH;
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File file = new File(path, name);
		try {
			FileOutputStream out = new FileOutputStream(file);
			if (name.endsWith(".png")) {
				bitmap.compress(CompressFormat.PNG, 100, out);
			} else if (name.endsWith(".jpg") || name.endsWith(".JPG")
					|| name.endsWith(".jpeg")) {
				bitmap.compress(CompressFormat.JPEG, 100, out);
			}

			out.flush();
			out.close();
			return path + "/" + name;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public boolean deleteFile(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return false;
		}
		file.delete();
		return true;
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (zoomView.getVisibility() != View.VISIBLE)
			super.onBackPressed();
		else {
			zoomView.setVisibility(View.GONE);
			imageView.setScaleType(ScaleType.CENTER);
		}
	}

	
	public void sendVoice() {
		mBtnSend.setVisibility(View.VISIBLE);
		mBtnSendVoice.setVisibility(View.INVISIBLE);
		findViewById(R.id.rl_bottom_media).setVisibility(View.GONE);
	}

}