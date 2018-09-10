// Generated code from Butter Knife. Do not modify!
package susankyatech.com.hisabkitab;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import de.hdodenhof.circleimageview.CircleImageView;
import java.lang.IllegalStateException;
import java.lang.Override;

public class CreateGroupFragment_ViewBinding implements Unbinder {
  private CreateGroupFragment target;

  @UiThread
  public CreateGroupFragment_ViewBinding(CreateGroupFragment target, View source) {
    this.target = target;

    target.groupImage = Utils.findRequiredViewAsType(source, R.id.new_group_image, "field 'groupImage'", CircleImageView.class);
    target.groupName = Utils.findRequiredViewAsType(source, R.id.group_name, "field 'groupName'", EditText.class);
    target.maxMember = Utils.findRequiredViewAsType(source, R.id.max_no_of_member, "field 'maxMember'", EditText.class);
    target.autoJoin = Utils.findRequiredViewAsType(source, R.id.join_automatically, "field 'autoJoin'", CheckBox.class);
    target.createGroup = Utils.findRequiredViewAsType(source, R.id.create_group_btn, "field 'createGroup'", Button.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    CreateGroupFragment target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.groupImage = null;
    target.groupName = null;
    target.maxMember = null;
    target.autoJoin = null;
    target.createGroup = null;
  }
}
