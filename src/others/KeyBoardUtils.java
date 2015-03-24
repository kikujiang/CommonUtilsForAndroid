package others;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * 打开或关闭软键盘
 *
 * @author powinandroid
 */
public class KeyBoardUtils {
    /**
     * 打卡软键盘
     *
     * @param mView    输入框
     * @param mContext 上下文
     */
    public static void openKeybord(View mView, Context mContext) {
        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mView, InputMethodManager.RESULT_SHOWN);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    /**
     * 关闭软键盘
     *
     * @param mView    输入框
     * @param mContext 上下文
     */
    public static void closeKeybord(View mView, Context mContext) {
        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        imm.hideSoftInputFromWindow(mView.getWindowToken(), 0);
    }
}
