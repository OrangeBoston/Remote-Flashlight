package com.orangeboston.remoteflashlight.base;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.blankj.utilcode.util.FlashlightUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.kongzue.dialog.interfaces.OnShowListener;
import com.kongzue.dialog.util.BaseDialog;
import com.kongzue.dialog.util.DialogSettings;
import com.kongzue.dialog.v3.FullScreenDialog;
import com.kongzue.dialog.v3.MessageDialog;
import com.kongzue.dialog.v3.TipDialog;
import com.orangeboston.remoteflashlight.MainActivity;
import com.orangeboston.remoteflashlight.R;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import cn.jpush.im.android.api.ContactManager;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoListCallback;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.event.ContactNotifyEvent;
import cn.jpush.im.android.api.event.LoginStateChangeEvent;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.event.OfflineMessageEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;

public abstract class BaseActivity extends AppCompatActivity {

    public AppCompatActivity mContext;

    protected abstract int getLayoutId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(getLayoutId());
        ButterKnife.bind(this);
        JMessageClient.registerEventReceiver(this);
        DialogSettings.init();
        DialogSettings.checkRenderscriptSupport(this);
        DialogSettings.isUseBlur = true;
        DialogSettings.style = DialogSettings.STYLE.STYLE_IOS;
        DialogSettings.theme = DialogSettings.THEME.LIGHT;
    }

    public void onEventMainThread(LoginStateChangeEvent event) {
        final LoginStateChangeEvent.Reason reason = event.getReason();
        UserInfo myInfo = event.getMyInfo();
        if (myInfo != null) {
            JMessageClient.logout();
        }
        switch (reason) {
            case user_logout:
                Intent intent_user_logout = new Intent(BaseActivity.this, MainActivity.class);
                startActivity(intent_user_logout);
                break;
            case user_password_change:
                Intent intent_user_password_change = new Intent(BaseActivity.this, MainActivity.class);
                startActivity(intent_user_password_change);
                break;
        }
    }

    public void onEvent(ContactNotifyEvent event) {
        String reason = event.getReason();
        String fromUsername = event.getFromUsername();
        String appkey = event.getfromUserAppKey();

        switch (event.getType()) {
            case invite_received:
                //收到好友邀请
                bindingDevices(fromUsername, reason);
                break;
            case invite_accepted:
                //对方接受了你的好友邀请
                MessageDialog.show(mContext, "提示",
                        "您已经与【" + fromUsername + "】成功绑定", "确定");
                break;
            case invite_declined:
                //对方拒绝了你的好友邀请
                MessageDialog.show(mContext, "提示",
                        "【" + fromUsername + "】拒绝了您的绑定请求", "确定");
                break;
            case contact_deleted:
                //对方将你从好友中删除
                MessageDialog.show(mContext, "提示",
                        "【" + fromUsername + "】已经与您解除绑定请求", "确定");
                break;
            default:
                break;
        }
    }


    public TextView tvUserName, tvUserDevice;
    public QMUIRoundButton btnAccept, btnRefuse;

    public void bindingDevices(String fromUsername, String reason) {
        FullScreenDialog
                .show(mContext, R.layout.layout_binding_devices, new FullScreenDialog.OnBindView() {
                    @Override
                    public void onBind(FullScreenDialog dialog, View rootView) {
                        tvUserName = rootView.findViewById(R.id.tv_userName);
                        tvUserDevice = rootView.findViewById(R.id.tv_userDevice);
                        btnAccept = rootView.findViewById(R.id.btn_accept);
                        btnRefuse = rootView.findViewById(R.id.btn_refuse);
                    }
                })
                .setOnShowListener(new OnShowListener() {
                    @Override
                    public void onShow(BaseDialog dialog) {
                        tvUserName.setText(fromUsername);
                        tvUserDevice.setText(reason);
                        btnAccept.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ContactManager.acceptInvitation(fromUsername, null, new BasicCallback() {
                                    @Override
                                    public void gotResult(int responseCode, String responseMessage) {
                                        if (0 == responseCode) {
                                            //接收好友请求成功
                                            TipDialog.show(mContext, "已接受设备绑定请求", TipDialog.TYPE.SUCCESS);
                                            dialog.doDismiss();
                                        } else {
                                            //接收好友请求失败
                                            TipDialog.show(mContext, responseMessage, TipDialog.TYPE.ERROR);
                                        }
                                    }
                                });
                            }
                        });
                        btnRefuse.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ContactManager.declineInvitation(fromUsername, null, "sorry~", new BasicCallback() {
                                    @Override
                                    public void gotResult(int responseCode, String responseMessage) {
                                        if (0 == responseCode) {
                                            //拒绝好友请求成功
                                            TipDialog.show(mContext, "已拒绝设备绑定请求", TipDialog.TYPE.SUCCESS);
                                            dialog.doDismiss();
                                        } else {
                                            //拒绝好友请求失败
                                            TipDialog.show(mContext, responseMessage, TipDialog.TYPE.ERROR);
                                        }
                                    }
                                });
                            }
                        });
                    }
                })
                .setTitle("设备绑定请求");
    }

    public void onEvent(MessageEvent event) {
        Message newMessage = event.getMessage();

        TextContent textContent = (TextContent) newMessage.getContent();
        String msg = textContent.getText();

        if (msg.contains("ON")) {
            FlashlightUtils.setFlashlightStatus(true);
        } else {
            FlashlightUtils.setFlashlightStatus(false);
        }

    }

    public void showTipsDialog(final QMUITipDialog dialog) {
        dialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        }, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.bind(this).unbind();
        JMessageClient.unRegisterEventReceiver(this);
    }
}
