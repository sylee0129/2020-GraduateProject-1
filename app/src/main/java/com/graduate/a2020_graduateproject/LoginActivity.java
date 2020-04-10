package com.graduate.a2020_graduateproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kakao.network.ErrorResult;
import com.kakao.auth.*;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.usermgmt.response.model.Profile;
import com.kakao.usermgmt.response.model.UserAccount;
import com.kakao.util.OptionalBoolean;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private SessionCallback callback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //카카오 로그인 콜백받기
        callback = new SessionCallback();
        Session.getCurrentSession().addCallback(callback);
        Session.getCurrentSession().checkAndImplicitOpen();

        //키값 알아내기(알아냈으면 등록하고 지워도 상관없다)
        //getAppKeyHash();



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(callback);
    }



    protected void redirectMainActivity() {
        //final Intent intent = new Intent(this, SuccessLoginActivity.class);
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    protected void redirectLoginActivity(){
        final Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }




    private class SessionCallback implements ISessionCallback {

        @Override
        public void onSessionOpened() {

            Log.e("KakaoLogin ::", "로그인 성공");
            requestMe();

        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            Log.e("KakaoLogin ::", "로그인 실패");
            if(exception != null) {
                Logger.e(exception);
            }
            redirectLoginActivity();
        }
    }

    public ISessionConfig getSessionConfig() {
        return new ISessionConfig() {
            @Override
            public AuthType[] getAuthTypes() {
                return new AuthType[] {AuthType.KAKAO_TALK};
            }

            @Override
            public boolean isUsingWebviewTimer() {
                return false;
            }

            @Override
            public boolean isSecureMode() {
                return false;
            }

            @Override
            public ApprovalType getApprovalType() {
                return ApprovalType.INDIVIDUAL;
            }

            @Override
            public boolean isSaveFormData() {
                return true;
            }
        };
    }

    /* 사용자 정보 수집*/
    public void requestMe(){
        List<String> keys = new ArrayList<>();
        keys.add("properties.nickname");
        keys.add("properties.profile_image");
        keys.add("kakao_account.email");

        // 사용자정보 요청 결과에 대한 Callback
        UserManagement.getInstance().me(keys, new MeV2ResponseCallback() {

            // 세션 오픈 실패. 세션이 삭제된 경우
            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                Log.e("KakaoSessionCallback :: ", "onSessionClosed : " + errorResult.getErrorMessage());
            }

            @Override
            public void onFailure(ErrorResult errorResult){
                Log.e("SessionCallback :: ", "onFailure : " + errorResult.getErrorMessage());
            }


            @Override
            public void onSuccess(MeV2Response response) {

                Long id;
                String name = null;
                String email = null;
                String thumbnail = null;

                Log.e("SessionCallback :: ", "onSuccess");
                Log.e("kakaoLogin ::  ", "카카오 로그인 정보 가져오기");
                Log.e("KakaoLogin","user id : " + response.getId());
                /*사용자 아이디(ID)의 경우 앱 연결 과정에서 발급하는 앱별 사용자의 고유 아이디입니다. 해당 아이디를 통해 사용자를 앱에서 식별 가능하며, 앱 연결 해제를 하더라도 같은 값으로 계속 유지됩니다.*/

                id = response.getId();

                UserAccount kakaoAccount = response.getKakaoAccount();

                if (kakaoAccount != null) {

                    email = kakaoAccount.getEmail();

                    if (email != null) {
                        Log.e("KakaoLogin","email : " + email);
                    } else if (kakaoAccount.emailNeedsAgreement() == OptionalBoolean.TRUE) {
                        // 동의 요청 후 이메일 획득 가능
                        // 단, 선택 동의로 설정되어 있다면 서비스 이용 시나리오 상에서 반드시 필요한 경우에만 요청해야 합니다.
                        ///// 이메일 따로 획득해야한다.
                    } else {
                        // 이메일 획득 불가
                    }

                    Profile profile = kakaoAccount.getProfile();

                    if (profile != null) {
                        Log.e("KakaoLogin","nickname : " + profile.getNickname());
                        Log.e("KakaoLogin","profile image : " + profile.getProfileImageUrl());
                        Log.e("KakaoLogin","thumbnail image : " + profile.getThumbnailImageUrl());

                        name = profile.getNickname();
                        thumbnail = profile.getThumbnailImageUrl();

                    } else if (kakaoAccount.profileNeedsAgreement() == OptionalBoolean.TRUE) {
                        // 동의 요청 후 프로필 정보 획득 가능
                        Log.e("KakaoLogin","동의 요청 후 프로필 정보 획득 가능");

                    } else {
                        // 프로필 획득 불가
                        Log.e("KakaoLogin","프로필 획득 불가");
                    }
                }

                writeNewUser(id, name, email, thumbnail); // 파이어베이스에 저장
                redirectMainActivity();
            }

        });
    }

    public void writeNewUser(Long id, String name, String email, String thumbnail){

        DatabaseReference mPostReference = FirebaseDatabase.getInstance().getReference("sharing_tirps");
        Map<String, Object> childUpdates = new HashMap<>();
        Map<String, Object> postValues = null;

        User post = new User(id, name, email, thumbnail);
        postValues = post.toMap();

        childUpdates.put("/user_list/" + id, postValues);
        mPostReference.updateChildren(childUpdates);
    }

    /* 해쉬키 구하는 함수 (구했으면 지워도됨)*/
    private void getAppKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                Log.e("Hash key", something);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e("name not found", e.toString());
        }
    }
}
