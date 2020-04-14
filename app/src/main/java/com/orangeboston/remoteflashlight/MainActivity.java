package com.orangeboston.remoteflashlight;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kongzue.dialog.interfaces.OnDialogButtonClickListener;
import com.kongzue.dialog.interfaces.OnDismissListener;
import com.kongzue.dialog.interfaces.OnShowListener;
import com.kongzue.dialog.util.BaseDialog;
import com.kongzue.dialog.v3.FullScreenDialog;
import com.kongzue.dialog.v3.TipDialog;
import com.kongzue.dialog.v3.WaitDialog;
import com.orangeboston.remoteflashlight.base.BaseActivity;
import com.orangeboston.remoteflashlight.flashlight.FlashLightActivity;
import com.orangeboston.remoteflashlight.fragment.LeftFragment;
import com.orangeboston.remoteflashlight.fragment.RightFragment;
import com.orangeboston.remoteflashlight.utils.HandleResponseCode;
import com.orangeboston.remoteflashlight.utils.easypermission.EasyPermission;
import com.orangeboston.remoteflashlight.utils.easypermission.GrantResult;
import com.orangeboston.remoteflashlight.utils.easypermission.NextAction;
import com.orangeboston.remoteflashlight.utils.easypermission.Permission;
import com.orangeboston.remoteflashlight.utils.easypermission.PermissionRequestListener;
import com.orangeboston.remoteflashlight.utils.easypermission.RequestPermissionRationalListener;
import com.orangeboston.remoteflashlight.utils.qqnaviview.QQNaviView;
import com.qmuiteam.qmui.layout.QMUIFrameLayout;
import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.popup.QMUIPopups;
import com.qmuiteam.qmui.widget.popup.QMUIQuickAction;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import cn.jpush.im.android.api.ContactManager;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoListCallback;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;

public class MainActivity extends BaseActivity {

    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;
    @BindView(R.id.tv_username)
    TextView tvUsername;
    @BindView(R.id.rbtn_copy)
    QMUIRoundButton rbtnCopy;
    @BindView(R.id.floatingActionButton)
    FloatingActionButton floatingActionButton;

    @BindView(R.id.tv_device_test)
    TextView tvDeviceTest;
    @BindView(R.id.rbtn_turn_on_test)
    QMUIRoundButton rbtnTurnOnTest;
    @BindView(R.id.rbtn_turn_off_test)
    QMUIRoundButton rbtnTurnOffTest;
    @BindView(R.id.rbtn_refresh_test)
    QMUIRoundButton rbtnRefreshTest;

    @BindView(R.id.qq_view_bubble)
    QQNaviView qqViewBubble;
    @BindView(R.id.qq_view_person)
    QQNaviView qqViewPerson;
    @BindView(R.id.fragment)
    QMUIFrameLayout fragment;


    private long mBackClickLastTime = System.currentTimeMillis();
    private String userId = "rf" + DeviceUtils.getAndroidID();
    private String password = "remoteflashlight";

    private Fragment currentFragment = new Fragment();
    private LeftFragment leftFragment = new LeftFragment();
    private RightFragment rightFragment = new RightFragment();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTopBar();
        myInfo();

        qqViewBubble.setBigIcon(R.drawable.bubble_big);
        qqViewBubble.setSmallIcon(R.drawable.bubble_small);
        qqViewBubble.check();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                qqViewPerson.lookLeft();
            }
        }, 50);

        switchFragment(leftFragment).commit();

//        easypermission();
    }

    @OnClick({R.id.rbtn_copy, R.id.floatingActionButton, R.id.tv_username,
            R.id.qq_view_bubble, R.id.qq_view_person})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_username:
                QMUIPopups.quickAction(mContext,
                        QMUIDisplayHelper.dp2px(mContext, 56),
                        QMUIDisplayHelper.dp2px(mContext, 56))
                        .shadow(true)
                        .skinManager(QMUISkinManager.defaultInstance(mContext))
                        .edgeProtection(QMUIDisplayHelper.dp2px(mContext, 20))
                        .addAction(new QMUIQuickAction.Action().icon(R.drawable.icon_quick_action_copy).text("复制").onClick(
                                new QMUIQuickAction.OnClickListener() {
                                    @Override
                                    public void onClick(QMUIQuickAction quickAction, QMUIQuickAction.Action action, int position) {
                                        quickAction.dismiss();
                                        copyTextToClipboard(mContext, tvUsername.getText().toString());
                                        ToastUtils.showShort("复制成功");
                                    }
                                }
                        ))
                        .show(view);
                break;
            case R.id.rbtn_copy:
                shareDeviceID(mContext, "设备ID:" + tvUsername.getText().toString() + "，绑定设备后即可远程控制手电筒开关。\n@远程手电筒");
                break;
            case R.id.floatingActionButton:
//                JMessageClient.logout();
                addDevice();
                break;
            case R.id.qq_view_bubble:
                resetIcon();
                qqViewBubble.setBigIcon(R.drawable.bubble_big);
                qqViewBubble.setSmallIcon(R.drawable.bubble_small);
                qqViewPerson.lookLeft();
                switchFragment(leftFragment).commit();
                break;
            case R.id.qq_view_person:
                resetIcon();
                qqViewPerson.setBigIcon(R.drawable.person_big);
                qqViewPerson.setSmallIcon(R.drawable.person_small);
                qqViewBubble.lookRight();
                switchFragment(rightFragment).commit();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        myInfo();
    }

    private void initTopBar() {
        mTopBar.setTitle("远程手电筒");
        mTopBar.addRightImageButton(R.drawable.ic_more, R.id.topbar_right_image_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                TipDialog.show(mContext, "别着急，正在开发~", TipDialog.TYPE.WARNING);
                ActivityUtils.startActivity(FlashLightActivity.class);
            }
        });
    }

    private void myInfo() {
        UserInfo myInfo = JMessageClient.getMyInfo();
        if (myInfo != null) {
            String username = myInfo.getUserName();
            String appKey = myInfo.getAppKey();
            tvUsername.setText(username);

            getFriendList();//测试

        } else {
            Login();
        }
    }


    private void Login() {
        WaitDialog.show(mContext, "初始化...");
        JMessageClient.login(userId, password, new BasicCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage) {
                WaitDialog.dismiss();
                if (responseCode == 0) {
                    myInfo();
                    TipDialog.show(mContext, "登录成功", TipDialog.TYPE.SUCCESS);
                } else if (responseCode == 801003) {
                    register();
                } else {
                    ToastUtils.showShort(responseMessage);
                }

            }
        });
    }

    private void register() {
        WaitDialog.show(mContext, "正在注册...");
        JMessageClient.register(userId, password, new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                if (i == 0) {
                    Login();
                } else {
                    HandleResponseCode.onHandle(mContext, i, false);
                }
            }
        });
    }

    private void resetIcon() {
        qqViewBubble.setBigIcon(R.drawable.pre_bubble_big);
        qqViewBubble.setSmallIcon(R.drawable.pre_bubble_small);
        qqViewPerson.setBigIcon(R.drawable.pre_person_big);
        qqViewPerson.setSmallIcon(R.drawable.pre_person_small);
    }

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        long timeGap = currentTime - mBackClickLastTime;
        if (timeGap >= 2000) {
            ToastUtils.showShort("再按一次退出程序");
            mBackClickLastTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();

        }
    }

    private EditText editUserName;

    private void addDevice() {
        FullScreenDialog
                .show(mContext, R.layout.layout_add_devices, new FullScreenDialog.OnBindView() {
                    @Override
                    public void onBind(FullScreenDialog dialog, View rootView) {
                        editUserName = rootView.findViewById(R.id.edit_userName);
                    }
                })
                .setOnShowListener(new OnShowListener() {
                    @Override
                    public void onShow(BaseDialog dialog) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        KeyboardUtils.showSoftInput(editUserName);
                                    }
                                });
                            }
                        }, 500);
                    }
                })
                .setOkButton("发送请求", new OnDialogButtonClickListener() {
                    @Override
                    public boolean onClick(BaseDialog baseDialog, View v) {
                        String userName = editUserName.getText().toString();
                        if (StringUtils.isEmpty(userName)) {
                            ToastUtils.showShort("设备ID不能为空");
                        } else {
                            sendInvitationRequest(userName);
                        }
                        return false;
                    }
                })
                .setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        KeyboardUtils.hideSoftInput(editUserName);
                    }
                })
                .setCancelButton("取消")
                .setTitle("添加设备");
    }

    private void sendInvitationRequest(String userName) {
        WaitDialog.show(mContext, "正在发送请求...");
        String deviceInfo = DeviceUtils.getManufacturer() + " " + DeviceUtils.getModel();
        ContactManager.sendInvitationRequest(userName, null, deviceInfo, new BasicCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage) {
                WaitDialog.dismiss();
                if (responseCode == 0) {
                    showTipsDialog(new QMUITipDialog.Builder(mContext)
                            .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                            .setTipWord("发送请求成功")
                            .create());
                    myInfo();
                } else if (responseCode == 871317) {
                    showTipsDialog(new QMUITipDialog.Builder(mContext)
                            .setIconType(QMUITipDialog.Builder.ICON_TYPE_INFO)
                            .setTipWord("自己不能绑定自己")
                            .create());
                } else {
                    showTipsDialog(new QMUITipDialog.Builder(mContext)
                            .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                            .setTipWord(responseMessage)
                            .create());
                }
            }
        });
    }

    private void getFriendList() {
        ContactManager.getFriendList(new GetUserInfoListCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<UserInfo> userInfoList) {
                if (0 == responseCode) {
                    //获取好友列表成功
                    if (userInfoList.size() > 0) {
                        tvDeviceTest.setText("已绑定：" + userInfoList.get(0).getUserName());
                        Conversation.createSingleConversation(userInfoList.get(0).getUserName(), null);
                        rbtnTurnOnTest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Message message = JMessageClient.createSingleTextMessage(userInfoList.get(0).getUserName(), null, "开关测试：ON");
                                message.setOnSendCompleteCallback(new BasicCallback() {
                                    @Override
                                    public void gotResult(int responseCode, String responseDesc) {
                                        if (responseCode == 0) {
                                            //消息发送成功
                                            TipDialog.show(mContext, "请求发送成功", TipDialog.TYPE.SUCCESS);
                                        } else {
                                            //消息发送失败
                                            TipDialog.show(mContext, "请求发送失败" + responseDesc, TipDialog.TYPE.ERROR);
                                        }
                                    }
                                });
                                JMessageClient.sendMessage(message);
                            }
                        });
                        rbtnTurnOffTest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Message message = JMessageClient.createSingleTextMessage(userInfoList.get(0).getUserName(), null, "开关测试：OFF");
                                message.setOnSendCompleteCallback(new BasicCallback() {
                                    @Override
                                    public void gotResult(int responseCode, String responseDesc) {
                                        if (responseCode == 0) {
                                            //消息发送成功
                                            TipDialog.show(mContext, "请求发送成功", TipDialog.TYPE.SUCCESS);
                                        } else {
                                            //消息发送失败
                                            TipDialog.show(mContext, "请求发送失败" + responseDesc, TipDialog.TYPE.ERROR);
                                        }
                                    }
                                });
                                JMessageClient.sendMessage(message);
                            }
                        });
                        rbtnRefreshTest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                myInfo();
                            }
                        });
                    } else {
                        //获取好友列表失败
                    }
                }
            }
        });
    }

    public static void shareDeviceID(Context context, String extraText) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "设备ID：");
        intent.putExtra(Intent.EXTRA_TEXT, extraText);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(
                Intent.createChooser(intent, "远程手电筒"));
    }

    public static void copyTextToClipboard(Context context, String string) {
        if (TextUtils.isEmpty(string)) {
            return;
        } else {
            ClipboardManager clip = (ClipboardManager) context
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            clip.setText(string);
        }
    }


    private FragmentTransaction switchFragment(Fragment targetFragment) {

        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        if (!targetFragment.isAdded()) {
            //第一次使用switchFragment()时currentFragment为null，所以要判断一下
            if (currentFragment != null) {
                transaction.hide(currentFragment);
            }
            transaction.add(R.id.fragment, targetFragment, targetFragment.getClass().getName());

        } else {
            transaction
                    .hide(currentFragment)
                    .show(targetFragment);
        }
        currentFragment = targetFragment;
        return transaction;
    }

    private void easypermission() {
        EasyPermission.with(this)
                .addRequestPermissionRationaleHandler(Permission.ACCESS_FINE_LOCATION, new RequestPermissionRationalListener() {
                    @Override
                    public void onRequestPermissionRational(String permission, boolean requestPermissionRationaleResult, final NextAction nextAction) {
                        //这里处理具体逻辑，如弹窗提示用户等,但是在处理完自定义逻辑后必须调用nextAction的next方法
                    }
                })
                .addRequestPermissionRationaleHandler(Permission.CALL_PHONE, new RequestPermissionRationalListener() {
                    @Override
                    public void onRequestPermissionRational(String permission, boolean requestPermissionRationaleResult, final NextAction nextAction) {
                        //这里处理具体逻辑，如弹窗提示用户等,但是在处理完自定义逻辑后必须调用nextAction的next方法
                    }
                })
                .request(new PermissionRequestListener() {
                    @Override
                    public void onGrant(Map<String, GrantResult> result) {
                        //权限申请返回
                    }

                    @Override
                    public void onCancel(String stopPermission) {
                        //在addRequestPermissionRationaleHandler的处理函数里面调用了NextAction.next(NextActionType.STOP,就会中断申请过程，直接回调到这里来
                    }
                });
    }

}