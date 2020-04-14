package com.orangeboston.remoteflashlight.flashlight;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.kongzue.dialog.v3.TipDialog;
import com.orangeboston.remoteflashlight.R;
import com.orangeboston.remoteflashlight.base.BaseActivity;
import com.orangeboston.remoteflashlight.fragment.LeftFragment;
import com.orangeboston.remoteflashlight.fragment.RightFragment;
import com.orangeboston.remoteflashlight.utils.qqnaviview.QQNaviView;
import com.qmuiteam.qmui.layout.QMUIFrameLayout;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;

import butterknife.BindView;
import butterknife.OnClick;

public class FlashLightActivity extends BaseActivity {

    @BindView(R.id.fragment)
    QMUIFrameLayout fragment;
    @BindView(R.id.qq_view_bubble)
    QQNaviView qqViewBubble;
    @BindView(R.id.qq_view_person)
    QQNaviView qqViewPerson;
    @BindView(R.id.topbar)
    QMUITopBarLayout mTopBar;

    private Fragment currentFragment = new Fragment();
    private LeftFragment leftFragment = new LeftFragment();
    private RightFragment rightFragment = new RightFragment();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_flash_light;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTopBar();
        qqViewBubble.performClick();
    }

    @OnClick({R.id.qq_view_bubble, R.id.qq_view_person})
    public void onViewClicked(View view) {
        switch (view.getId()) {

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

    private void initTopBar() {
        mTopBar.setTitle("远程手电筒");
        mTopBar.addRightImageButton(R.drawable.ic_more, R.id.topbar_right_image_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TipDialog.show(mContext, "别着急，正在开发~", TipDialog.TYPE.WARNING);
            }
        });
    }

    private void resetIcon() {
        qqViewBubble.setBigIcon(R.drawable.pre_bubble_big);
        qqViewBubble.setSmallIcon(R.drawable.pre_bubble_small);
        qqViewPerson.setBigIcon(R.drawable.pre_person_big);
        qqViewPerson.setSmallIcon(R.drawable.pre_person_small);
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

}
