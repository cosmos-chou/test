package extend.engine;

import android.content.Context;

import cn.rongcloud.im.server.SealAction;
import cn.rongcloud.im.server.network.async.AsyncTaskManager;
import cn.rongcloud.im.server.network.async.OnDataListener;
import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.response.JoinGroupResponse;
import io.rong.common.RLog;

/**
 * Created by cosmos on 17-9-11.
 */

public class JoinGroupEngine implements OnDataListener {



    private static final String TAG = "JoinGroupEngine";

    private Context context;

    private JoinGroupEngine.IGroupMembersCallback groupMembersCallback;

    private static final int REQUEST_GROUP_MEMBER = 5000;

    public JoinGroupEngine(Context context) {
        this.context = context;
    }


    public void startEngine(String groupId, JoinGroupEngine.IGroupMembersCallback callback) {
        this.groupMembersCallback = callback;
        AsyncTaskManager.getInstance(context).request(groupId, REQUEST_GROUP_MEMBER, this);
    }

    @Override
    public Object doInBackground(int requestCode, String groupId) throws HttpException {
        return new SealAction(context).JoinGroup(groupId);
    }

    @Override
    public void onSuccess(int requestCode, Object result) {
        if (result != null) {
            JoinGroupResponse response = (JoinGroupResponse) result;
            if (response.getCode() == 200) {
                //TODO
            }
            if (groupMembersCallback != null) {
                groupMembersCallback.onResult();
            }
        }
    }

    @Override
    public void onFailure(int requestCode, int state, Object result) {
        RLog.d(TAG, "onFailure state = " + state);
    }

    public interface IGroupMembersCallback {
        void onResult();
    }
}
