// Generated code from Butter Knife. Do not modify!
package susankyatech.com.hisabkitab;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class GettingStartedFragment_ViewBinding implements Unbinder {
  private GettingStartedFragment target;

  @UiThread
  public GettingStartedFragment_ViewBinding(GettingStartedFragment target, View source) {
    this.target = target;

    target.groupCode = Utils.findRequiredViewAsType(source, R.id.group_code, "field 'groupCode'", EditText.class);
    target.joinGroup = Utils.findRequiredViewAsType(source, R.id.join_group_btn, "field 'joinGroup'", Button.class);
    target.createGroup = Utils.findRequiredViewAsType(source, R.id.create_group_tv, "field 'createGroup'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    GettingStartedFragment target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.groupCode = null;
    target.joinGroup = null;
    target.createGroup = null;
  }
}
